package seemoo.fitbit.fragments;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;

import seemoo.fitbit.R;
import seemoo.fitbit.activities.MainActivity;
import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.commands.Commands;
import seemoo.fitbit.dialogs.TransferProgressDialog;
import seemoo.fitbit.events.TransferProgressEvent;
import seemoo.fitbit.information.Alarm;
import seemoo.fitbit.information.Information;
import seemoo.fitbit.information.InformationList;
import seemoo.fitbit.interactions.Interactions;
import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.Crypto;
import seemoo.fitbit.miscellaneous.ExternalStorage;
import seemoo.fitbit.miscellaneous.Firmware;
import seemoo.fitbit.miscellaneous.FitbitDevice;
import seemoo.fitbit.miscellaneous.InfoArrayAdapter;
import seemoo.fitbit.miscellaneous.InternalStorage;
import seemoo.fitbit.miscellaneous.Utilities;
import seemoo.fitbit.tasks.Tasks;

import static android.content.Context.MODE_PRIVATE;

public class MainFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();

    private BluetoothDevice device;
    private ArrayList<BluetoothGattService> services = new ArrayList<>();

    private Commands commands;
    private Interactions interactions;
    private Tasks tasks;
    private InformationList informationToDisplay = new InformationList("");
    private ListView mListView;
    private FloatingActionButton clearAlarmsButton;
    private FloatingActionButton saveButton;

    private Object interactionData;
    private Toast toast_short;
    private Toast toast_long;
    private int alarmIndex = -1;
    private String currentInformationList;
    private int customLength = -1;
    private String fileName;
    private boolean firstPress = true;
    private AlertDialog connectionLostDialog = null;

    public enum BluetoothConnectionState {DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING, UNKNOWN}
    private BluetoothConnectionState bluetoothConnectionState = BluetoothConnectionState.UNKNOWN;

    private HashMap<String, InformationList> information = new HashMap<>();

    private GraphView graph;
    private BarGraphSeries<DataPoint> graphDataSeries;
    private int graphCounter = 0;

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        /**
         * {@inheritDoc}
         * Logs aconnection state change and tries to reconnect, if connection is lost.
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bluetoothConnectionState = BluetoothConnectionState.DISCONNECTED;
                services.clear();
                commands.close();
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showConnectionLostDialog();
                    }
                });
                Log.e(TAG, "Connection lost. Trying to reconnect.");
            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                bluetoothConnectionState = BluetoothConnectionState.CONNECTING;
            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothConnectionState = BluetoothConnectionState.CONNECTED;
                destroyConnectionLostDialog();

                TransferProgressEvent event = new TransferProgressEvent(TransferProgressEvent.EVENT_TYPE_FW);
                event.setTransferState(TransferProgressEvent.STATE_REBOOT_FIN);
                EventBus.getDefault().post(event);

                commands.comDiscoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                bluetoothConnectionState = BluetoothConnectionState.DISCONNECTING;
            }
            Log.e(TAG, "onConnectionStateChange: " + bluetoothConnectionState);
        }

        /**
         * {@inheritDoc}
         * Logs a service discovery and finishes the corresponding command.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.e(TAG, "onServicesDiscovered");
            services.addAll(gatt.getServices());
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toast_short.setText(R.string.connection_established);
                    toast_short.show();
                }
            });
            commands.commandFinished();
        }

        /**
         * {@inheritDoc}
         * Logs a characteristic read and finishes the corresponding command.
         * If the device is in live mode, the data is stored in 'information' and shown to the user.
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "onCharacteristicRead(): " + characteristic.getUuid() + ", " + Utilities.byteArrayToHexString(characteristic.getValue()));
            if (interactions.liveModeActive()) {
                interactions.setAccelReadoutActive(Utilities.checkLiveModeReadout(characteristic.getValue()));
                information.put(interactions.getCurrentInteraction(), Utilities.translate(characteristic.getValue()));
                graphDataSeries = Utilities.updateGraph(characteristic.getValue());
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (interactions.accelReadoutActive() && interactions.liveModeActive()) {
                            graph.setVisibility(View.VISIBLE);
                            graph.removeAllSeries();
                            graph.addSeries(graphDataSeries);
                        } else {
                            graph.setVisibility(View.GONE);
                        }
                        informationToDisplay.override(information.get(interactions.getCurrentInteraction()), mListView);
                        saveButton.setVisibility(View.VISIBLE);
                        currentInformationList = "LiveMode";
                    }
                });
            }
            commands.commandFinished();
        }

        /**
         * {@inheritDoc}
         * Logs a characteristic write and finishes the corresponding command.
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "onCharacteristicWrite(): " + characteristic.getUuid() + ", " + Utilities.byteArrayToHexString(characteristic.getValue()));
            commands.commandFinished();
        }

        /**
         * {@inheritDoc}
         * Logs a characteristic change and finishes the corresponding command.
         * If the new value is a negative acknowledgement it reconnects to the device to avoid subsequent errors.
         * If there is any relevant data in the new value it is stored in 'information' and shown to the user, if necessary.
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

            Log.e(TAG, "onCharacteristicChanged(): " + characteristic.getUuid() + ", " + Utilities.byteArrayToHexString(characteristic.getValue()));

            if (Utilities.byteArrayToHexString(characteristic.getValue()) == "c01301000") {
                //Command
                Log.e(TAG, "Error: " + Utilities.getError(Utilities.byteArrayToHexString(characteristic.getValue())));
            }

            if (Utilities.byteArrayToHexString(characteristic.getValue()).length() >= 4 && Utilities.byteArrayToHexString(characteristic.getValue()).substring(0, 4).equals(ConstantValues.NEG_ACKNOWLEDGEMENT)) {
                Log.e(TAG, "Error: " + Utilities.getError(Utilities.byteArrayToHexString(characteristic.getValue())));
                services.clear();
                commands.close();
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        showConnectionLostDialog();
                    }
                });
                Log.e(TAG, "Disconnected. Trying to reconnect...");
            } else {
                Log.e(TAG, "Getting interaction response.");
                byte[] value = characteristic.getValue();
                //Log.e(TAG, "Response value: " + Utilities.byteArrayToHexString(value));
                interactionData = interactions.interact(value);
                //Log.e(TAG, "Interaction called.");
                if (interactions.isFinished()) {
                    interactionData = interactions.interactionFinished();
                }
                if (interactionData != null) {

                    String keyAdditionalRawOutput = getResources().getString(R.string.settings_workactivity_1);
                    String keyAdditionalAlarmInformation = getResources().getString(R.string.settings_workactivity_2);
                    String keySaveDumpFiles = getResources().getString(R.string.settings_workactivity_3);
                    final SharedPreferences spAdditionalRawOutput = getActivity().getSharedPreferences(keyAdditionalRawOutput, MODE_PRIVATE);
                    final SharedPreferences spAdditionalAlarmInformation = getActivity().getSharedPreferences(keyAdditionalAlarmInformation, MODE_PRIVATE);
                    final SharedPreferences spSaveDumpFiles = getActivity().getSharedPreferences(keySaveDumpFiles, MODE_PRIVATE);
                    final Boolean additionalRawOutputBoolean = spAdditionalRawOutput.getBoolean(keyAdditionalRawOutput, false);
                    final Boolean additionalAlarmInformationBoolean = spAdditionalAlarmInformation.getBoolean(keyAdditionalAlarmInformation, false);
                    final Boolean saveDumpFilesBoolean = spSaveDumpFiles.getBoolean(keySaveDumpFiles, false);

                    currentInformationList = ((InformationList) interactionData).getName();
                    information.put(currentInformationList, (InformationList) interactionData);
                    graphDataSeries = Utilities.updateGraph(characteristic.getValue());
                    getActivity().runOnUiThread(informationListRunnable(currentInformationList, information, interactionData,
                            additionalRawOutputBoolean, additionalAlarmInformationBoolean,
                            saveDumpFilesBoolean, informationToDisplay, mListView, saveButton,
                            clearAlarmsButton, characteristic.getValue()));
                }
            }
        }

        private Runnable informationListRunnable(final String currentInformationListRun, final HashMap<String, InformationList> informationRun,
                                                 final Object interactionDataRun, final Boolean additionalRawOutputBooleanRun, final Boolean additionalAlarmInformationBooleanRun,
                                                 final Boolean saveDumpFilesBooleanRun, final InformationList informationToDisplayRun,
                                                 final ListView mListViewRun, final FloatingActionButton saveButtonRun,
                                                 final FloatingActionButton clearAlarmsButtonRun, final byte[] characteristicValue) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (Utilities.checkLiveModeReadout(characteristicValue) == false) {
                        graph.setVisibility(View.GONE);
                    }
                    if ((graphCounter % 30) == 0) {
                        graph.removeAllSeries();
                        graph.addSeries(graphDataSeries);
                    }
                    graphCounter++;
                    InformationList temp = new InformationList("");
                    temp.addAll(informationRun.get(((InformationList) interactionDataRun).getName()));
                    if (saveDumpFilesBooleanRun) {
                        ExternalStorage.saveInformationList(informationRun.get(currentInformationListRun), currentInformationListRun, getActivity());
                    }
                    if (currentInformationListRun.equals("Memory_KEY")) {
                        FitbitDevice.setEncryptionKey(informationRun.get(currentInformationListRun).getBeautyData().trim());
                        Log.e(TAG, "Encryption Key: " + FitbitDevice.ENCRYPTION_KEY);
                        InternalStorage.saveString(FitbitDevice.ENCRYPTION_KEY, ConstantValues.FILE_ENC_KEY, getActivity());
                    }
                    final int positionRawOutput = temp.getPosition(new Information(ConstantValues.RAW_OUTPUT));
                    if (!additionalRawOutputBooleanRun && positionRawOutput > 0) {
                        temp.remove(positionRawOutput - 1, temp.size());
                    }
                    final int positionAdditionalInfo = temp.getPosition(new Information(ConstantValues.ADDITIONAL_INFO));
                    if (!additionalAlarmInformationBooleanRun && positionAdditionalInfo > 0) {
                        temp.remove(positionAdditionalInfo - 1, positionRawOutput - 1);
                    }
                    informationToDisplayRun.override(temp, mListViewRun);
                    if (mListViewRun.getVisibility() == View.VISIBLE) {
                        saveButtonRun.setVisibility(View.VISIBLE);
                    }
                    if (informationToDisplayRun.size() > 1 && informationToDisplayRun.get(1) instanceof Alarm) {
                        clearAlarmsButtonRun.setVisibility(View.VISIBLE);
                    }
                }
            };

            return runnable;
        }

        /**
         * {@inheritDoc}
         * Logs a descriptor read and finishes the corresponding command.
         */
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e(TAG, "onDescriptorRead(): " + descriptor.getCharacteristic().getUuid() + ", " + descriptor.getUuid() + ", " + Utilities.byteArrayToHexString(descriptor.getValue()));
            commands.commandFinished();
        }

        /**
         * {@inheritDoc}
         * Logs a descriptor write and finishes the corresponding command.
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e(TAG, "onDescriptorWrite(): " + descriptor.getCharacteristic().getUuid() + ", " + descriptor.getUuid() + ", " + Utilities.byteArrayToHexString(descriptor.getValue()));
            commands.commandFinished();
        }

    };

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootFragmentView = inflater.inflate(R.layout.fragment_main, container, false);
        device = (BluetoothDevice) getActivity().getIntent().getExtras().get(WorkActivity.ARG_EXTRA_DEVICE);
        initialize(rootFragmentView);

        collectBasicInformation();
        connect();

        if (getActivity().getIntent().getExtras().getBoolean(WorkActivity.ARG_SHOULD_BLINK, false)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    letDeviceBlink();
                    toast_short.setText("Connection to new tracker. Will blink");
                    toast_short.show();
                }
            }, 3000);
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            /**
             * {@inheritDoc}
             *  Lets the user change an alarm, with the current view shows the alarms.
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (information.get(ConstantValues.INFORMATION_ALARM) != null && services.size() != 0 && position > 0 && position < 9) {
                    InformationList temp = new InformationList("");
                    temp.addAll(information.get(ConstantValues.INFORMATION_ALARM));
                    for (int i = temp.size() - 1; i >= 0; i--) {
                        if (!(temp.get(i) instanceof Alarm)) {
                            temp.remove(i);
                        }
                    }
                    interactions.intSetAlarm(position - 1, temp);
                }
                if (parent.getItemAtPosition(position) instanceof Information) {
                    String cellContent = ((Information) parent.getItemAtPosition(position)).getData();
                    if (cellContent.equals(getString(R.string.no_enc_key))) {
                        readOutEncKey();
                    } else if (cellContent.equals(getString(R.string.no_auth_cred))) {
                        ((WorkActivity) getActivity()).startFitbitAuthentication();
                    }
                }

            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                String cellContent = ((Information) parent.getItemAtPosition(pos)).getData();
                ClipboardManager clipboardManager = (ClipboardManager)
                        getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("text", cellContent));
                toast_short.setText("Content copied to clipboard");
                toast_short.show();

                return false;
            }
        });

        return rootFragmentView;
    }

    /**
     *
     */
    public void showConnectionLostDialog(){
        if(getActivity() != null && bluetoothConnectionState != BluetoothConnectionState.CONNECTED &&
                bluetoothConnectionState != BluetoothConnectionState.CONNECTING) {
            if (null == connectionLostDialog) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getString(R.string.connectionLostDialogDescription))
                        .setTitle(getString(R.string.connectionLostDialogTitle));
                builder.setCancelable(false);
                builder.setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode,
                                         KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            InternalStorage.clearLastDevice(getActivity());
                            if (commands != null) {
                                commands.close();
                            }
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            startActivity(intent);
                        }
                        return true;
                    }
                });

                connectionLostDialog = builder.create();
                connectionLostDialog.show();
            }
            connect();
        }
    }

    /**
     * If there is a connectionLostDialog shown, dismiss it to show the user the tracker is connected again.
     */
    public void destroyConnectionLostDialog() {
        if (null != connectionLostDialog) {
            connectionLostDialog.dismiss();
            connectionLostDialog = null;
        }
    }

    /**
     * Initializes several objects.
     */
    private void initialize(View rootView) {
        clearAlarmsButton = (FloatingActionButton) rootView.findViewById(R.id.fragment_main_clear_alarms_button);
        clearAlarmsButton.setVisibility(View.GONE);
        clearAlarmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAlarmsButton(v);
            }
        });

        saveButton = (FloatingActionButton) rootView.findViewById(R.id.fragment_main_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton(v);
            }
        });
        saveButton.setVisibility(View.GONE);
        InfoArrayAdapter arrayAdapter = new InfoArrayAdapter(getActivity(), informationToDisplay.getList());
        mListView = (ListView) rootView.findViewById(R.id.WorkActivityList);
        mListView.setAdapter(arrayAdapter);
        toast_short = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        toast_long = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG);

        //Accel-Live: initialisation of graph
        graph = (GraphView) rootView.findViewById(R.id.graph);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(2);
        graph.getViewport().setMinY(-8500);
        graph.getViewport().setMaxY(8500);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.setTitle("Gravitational force on accelerometer");
        graph.setVisibility(View.GONE);
        graph.getGridLabelRenderer().setVerticalAxisTitle("Value");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Axis");
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(new String[]{" ", "x", "y", "z", " "});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
    }


    /**
     * Connects the app with the device.
     */
    public void connect() {
        FitbitDevice.setMacAddress(device.getAddress());
        BluetoothGatt mBluetoothGatt = device.connectGatt(getActivity().getBaseContext(), false, mBluetoothGattCallback);
        commands = new Commands(mBluetoothGatt);
        interactions = new Interactions(this, toast_short, commands);
        tasks = new Tasks(interactions, this);
    }

    /**
     * Collects basic information about the selected device, stores them in 'information' and displays them to the user.
     */
    public void collectBasicInformation() {
        if(isAdded()){
            if (!firstPress) {
                saveButton.setVisibility(View.VISIBLE);
            }
            InformationList list = new InformationList("basic");
            currentInformationList = "basic";
            list.add(new Information("MAC Address: " + device.getAddress()));
            list.add(new Information("Name: " + device.getName()));

            int type = device.getType();
            if (type == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
                list.add(new Information(getString(R.string.device_type0)));
            } else if (type == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                list.add(new Information(getString(R.string.device_type1)));
            } else if (type == BluetoothDevice.DEVICE_TYPE_LE) {
                list.add(new Information(getString(R.string.device_type2)));
            } else if (type == BluetoothDevice.DEVICE_TYPE_DUAL) {
                list.add(new Information(getString(R.string.device_type3)));
            }

            int bondState = device.getBondState();
            if (bondState == BluetoothDevice.BOND_NONE) {
                list.add(new Information(getString(R.string.bond_state0)));
            } else if (bondState == BluetoothDevice.BOND_BONDING) {
                list.add(new Information(getString(R.string.bond_state1)));
            } else if (bondState == BluetoothDevice.BOND_BONDED) {
                list.add(new Information(getString(R.string.bond_state2)));
            }


            InternalStorage.loadAuthFiles(getActivity());

            if (FitbitDevice.AUTHENTICATION_KEY == null || FitbitDevice.AUTHENTICATION_KEY.equals("")) {
                list.add(new Information(getString(R.string.no_auth_cred)));
            } else {
                list.add(new Information("Authentication Key & Nonce: " + FitbitDevice.AUTHENTICATION_KEY + ", " + FitbitDevice.NONCE));
            }

            if (FitbitDevice.ENCRYPTION_KEY == null || FitbitDevice.ENCRYPTION_KEY.equals("")) {
                list.add(new Information(getString(R.string.no_enc_key)));
            } else {
                list.add(new Information("Encryption Key: " + FitbitDevice.ENCRYPTION_KEY));
            }


            information.put("basic", list);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    informationToDisplay.override(information.get("basic"), mListView);

                }
            }, 300);
        }
    }

    public void checkFirstButtonPress() {
        if (firstPress) {
            tasks.taskStartup(interactions, (WorkActivity) getActivity());
            firstPress = false;
        }
    }

    /**
     * Gets called, when the 'dump' button is pressed.
     * Lets the user device, which dump to get from the device:
     * - microdump.
     * - megadump.
     * - flash memory: start.
     * - flash memory: BSL.
     * - flash memory: APP.
     * - EEPROM.
     * - SRAM.
     * Authenticates with the device if needed.
     */
    public void buttonDump() {
        checkFirstButtonPress();
        setAlarmAndSaveButtonGone();
        final String[] items = new String[]{"Microdump", "Megadump", "Key", "Flash: start", "Flash: BSL", "Flash: APP", "Flash: all", "EEPROM", "SRAM", "Console Printf"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose a dump type:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((WorkActivity) getActivity()).setDumpMenuButtonActive();
                switch (which) {
                    case 0:
                        new TransferProgressDialog(getContext(), "Microdump", TransferProgressDialog.TRANSFER_TRACKER_TO_APP).show();
                        interactions.intMicrodump();
                        break;
                    case 1:
                        new TransferProgressDialog(getContext(), "Megadump", TransferProgressDialog.TRANSFER_TRACKER_TO_APP).show();
                        interactions.intMegadump();
                        break;
                    case 2:
                        readOutEncKey();
                        break;
                    case 3:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        new TransferProgressDialog(getActivity(), "Flash: start", TransferProgressDialog.TRANSFER_TRACKER_TO_APP).show();
                        interactions.intReadOutMemory(FitbitDevice.MEMORY_START, FitbitDevice.MEMORY_BSL, "START");
                        break;
                    case 4:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        new TransferProgressDialog(getActivity(), "Flash: BSL", TransferProgressDialog.TRANSFER_TRACKER_TO_APP).show();
                        interactions.intReadOutMemory(FitbitDevice.MEMORY_BSL, FitbitDevice.MEMORY_APP, "BSL");
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                    case 5:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        new TransferProgressDialog(getActivity(), "Flash: APP", TransferProgressDialog.TRANSFER_TRACKER_TO_APP).show();
                        interactions.intReadOutMemory(FitbitDevice.MEMORY_APP, FitbitDevice.MEMORY_APP_END, "APP");
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                    case 6:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        new TransferProgressDialog(getActivity(), "Flash: all", TransferProgressDialog.TRANSFER_TRACKER_TO_APP).show();
                        interactions.intReadOutMemory(FitbitDevice.MEMORY_START, FitbitDevice.MEMORY_APP_END, "APP");
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                    case 7:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        new TransferProgressDialog(getActivity(), "EEPROM", TransferProgressDialog.TRANSFER_TRACKER_TO_APP).show();
                        interactions.intReadOutMemory(FitbitDevice.MEMORY_EEPROM, FitbitDevice.MEMORY_EEPROM_END, "EEPROM");
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                    case 8:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        new TransferProgressDialog(getActivity(), "SRAM", TransferProgressDialog.TRANSFER_TRACKER_TO_APP).show();
                        interactions.intReadOutMemory(FitbitDevice.MEMORY_SRAM, FitbitDevice.MEMORY_SRAM_END, "SRAM");
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                    case 9:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        new TransferProgressDialog(getActivity(), "Console Printf", TransferProgressDialog.TRANSFER_TRACKER_TO_APP).show();
                        interactions.intReadOutMemory(FitbitDevice.MEMORY_CONSOLE, FitbitDevice.MEMORY_CONSOLE_END, "CONSOLE");
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                }
            }
        });
        builder.show();
    }

    private void readOutEncKey() {
        if (!interactions.getAuthenticated()) {
            interactions.intAuthentication();
        }
        interactions.intReadOutMemory(FitbitDevice.MEMORY_KEY, FitbitDevice.MEMORY_KEY_END, "KEY");
    }

    public void setAlarmAndSaveButtonGone() {
        if (clearAlarmsButton != null) {
            clearAlarmsButton.setVisibility(View.GONE);
        }
        if (saveButton != null) {
            saveButton.setVisibility(View.GONE);
        }
    }

    /**
     * Gets called, when 'set date' button is pressed.
     * Lets the user set the date of the device.
     */
    public void buttonSetDate() {
        checkFirstButtonPress();
        interactions.intSetDate();
    }

    /**
     * Gets called, when the 'live mode' button is pressed.
     * Depending on the current state, it switches to live mode or back to normal mode.
     */
    public void buttonLiveMode() {
        setAlarmAndSaveButtonGone();
        graph.setVisibility(View.GONE);
        if (!interactions.liveModeActive()) {
            if (!interactions.getAuthenticated()) {
                interactions.intAuthentication();
            }
            interactions.intLiveModeEnable();
            graph.setVisibility(View.GONE);
        }
    }

    public boolean isLiveModeActive() {
        return interactions.liveModeActive();
    }

    public void endLiveMode() {
        graph.setVisibility(View.GONE);
        interactions.intLiveModeDisable();
    }

    /**
     * Gets called, when 'alarms' button is pressed.
     * Does an authentication, if necessary, collects the alarms from the device and shows them to the user.
     */
    public void buttonAlarms() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Getting alarms from device. Alarms are editable by tapping on them.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!interactions.getAuthenticated()) {
                    interactions.intAuthentication();
                }
                interactions.intGetAlarm();
            }
        });
        builder.show();

    }

    /**
     * Gets called, when the 'information' button is pressed.
     * Collects basic information form the device.
     */
    public void buttonCollectBasicInformation() {
        checkFirstButtonPress();
        setAlarmAndSaveButtonGone();
        collectBasicInformation();
    }

    public void buttonDevices() {
        InternalStorage.clearLastDevice(getActivity());
        if (commands != null) {
            commands.close();
        }
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);

    }

    public void flashFirmware(String fileName, boolean isAppFirmware) {
        //TODO GUI freezes before showing this toast (which should inform user...). Freeze should not happen anyway
        toast_short.setText("prepare flashing ...");
        toast_short.show();

        //FIXME actually authentication is not required for FW update, but otherwise encryption key variable is empty
        //if (!interactions.getAuthenticated()) {
        //    interactions.intAuthentication();
        //}

        String type = "";
        String plain = "";

        //flashing positions
        //int flashbase = Utilities.hexStringToInt(FitbitDevice.MEMORY_START);
        //int bslpos = Utilities.hexStringToInt(FitbitDevice.MEMORY_BSL);
        //int apppos = Utilities.hexStringToInt(FitbitDevice.MEMORY_APP);

        //flash APP
        if (isAppFirmware) {
            if (FitbitDevice.DEVICE_TYPE == 0x07) {
                plain = Firmware.generateFirmwareFrame(fileName, 0xa000, 0xa000 + 0x026020, Utilities.hexStringToInt(FitbitDevice.MEMORY_APP), false, getActivity());
            }
            else {
                plain = Firmware.generateFirmwareFrame(fileName, 0x9c00, 0x9c00 + 0x048c50, Utilities.hexStringToInt(FitbitDevice.MEMORY_APP), false, getActivity());// charge hr
            }
            //TODO make this non-hw specific
            type = "app";
        }
        //flash BSL
        else {

            if (FitbitDevice.DEVICE_TYPE == 0x07) {
                plain = Firmware.generateFirmwareFrame(fileName, 0x0200, 0x0200 + 0x009e00, Utilities.hexStringToInt(FitbitDevice.MEMORY_BSL), true, getActivity());
            }
            else {
                plain = Firmware.generateFirmwareFrame(fileName, 0x0200, 0x0200 + 0x009800, Utilities.hexStringToInt(FitbitDevice.MEMORY_BSL), true, getActivity()); //charge hr
                //plain = Firmware.generateFirmwareFrame(fileName, (bslpos-flashbase), (bslpos-flashbase) + (apppos-flashbase), bslpos , true, getActivity()); //RF_ERR_RX_PACKET_NOT_HANDLED
            }
            type = "bsl";
        }

        ExternalStorage.saveString(plain, "fwplain", getActivity()); //just for debugging...

        String fw = plain;
        if (FitbitDevice.ENCRYPTED)
        {
            try {
                fw = Crypto.encryptDump(Utilities.hexStringToByteArray(plain), getActivity());
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Encrypting dump failed.");
            }
        }

        interactions.intUploadFirmwareInteraction(fw, fw.length());
    }

    /**
     * Gets called, when clear alarms button is pressed.
     *
     * @param view The current view.
     */
    public void clearAlarmsButton(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Erasing all alarms.");
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (!interactions.getAuthenticated()) {
                    interactions.intAuthentication();
                }
                interactions.intClearAlarms();
                interactions.intGetAlarm();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    /**
     * Gets called, when save button is pressed.
     *
     * @param view The current view.
     */
    public void saveButton(View view) {
        ExternalStorage.saveInformationList(information.get(currentInformationList), currentInformationList, getActivity());
        toast_short.setText("Information saved.");
        toast_short.show();
    }

    /**
     * Returns alarmIndex and increments it value by one afterwards
     *
     * @return The alarm index.
     */
    public int getAlarmIndexAndIncrement() {
        return alarmIndex++;
    }

    /**
     * Returns the value of the alarm index.
     *
     * @param value The index of the alarm.
     */
    public void setAlarmIndex(int value) {
        alarmIndex = value;
    }

    /**
     * Returns the content of an information list in 'information' as a string.
     *
     * @param name The name of the information list.
     * @return The content as a string.
     */
    public String getDataFromInformation(String name) {
        if (information.get(name) != null) {
            return information.get(name).getData();
        } else {
            return null;
        }
    }

    /**
     * Return the tasks object.
     *
     * @return The tasks object.
     */
    public Tasks getTasks() {
        return tasks;
    }

    /**
     * Checks, if an information list in 'information' was already uploaded to the fitbit server in the past.
     *
     * @param name The name of the information list to check.
     * @return True, if the information list was not uploaded in the past.
     */
    public boolean wasInformationListAlreadyUploaded(String name) {
        return information.get(name).getAlreadyUploaded();
    }

    /**
     * Sets an information list in 'information' as already uploaded.
     *
     * @param name The name of the information list.
     */
    public void setInformationListAsAlreadyUploaded(String name) {
        information.get(name).setAlreadyUploaded(true);
    }

    /**
     * Reads in text from the user.
     * Depending on the current situation, the text can be:
     * - the external directory to store / load files.
     * - the name of a firmware to upload.
     * - the length of a firmware to upload.
     * - the PIN of an online authentication.
     */
    public void updatewithbsl() {
        if (!interactions.getAuthenticated()) {
            interactions.intAuthentication();
        }

        String fw = "";

        /*try {
            //fw = Crypto.decrypttest_reboot_bsl_standalone(activity);
            fw = Crypto.encryptedFwUpdate(activity);

        }catch (UnsupportedEncodingException e) {

        }*/

        //interactions.intUploadFirmwareInteraction(fw, fw.length());

        //interactions.intUploadFirmwareInteraction(ExternalStorage.loadString(fileName, activity), customLength);
    }

    public void bootToBSL() {
        if (!interactions.getAuthenticated()) {
            interactions.intAuthentication();
        }

        String command = "";
        command = Firmware.rebootToBSL(getActivity());


        interactions.intUploadFirmwareInteraction(command, command.length());

        //interactions.intUploadFirmwareInteraction(ExternalStorage.loadString(fileName, activity), customLength);*/
    }

    public void bootToApp() {
        if (!interactions.getAuthenticated()) {
            interactions.intAuthentication();
        }

        String command = "";
        /*try {
            //fw = Crypto.decrypttest_reboot_bsl_standalone(activity);
            fw = Crypto.decrypttest_reboot_app_standalone(activity);

        }catch (UnsupportedEncodingException e) {

        }

        interactions.intUploadFirmwareInteraction(fw, fw.length());

        //interactions.intUploadFirmwareInteraction(ExternalStorage.loadString(fileName, activity), customLength);*/
    }


    /**
     * {@inheritDoc}
     * Closes bluetooth gatt and clears history.
     */
    @Override
    public void onPause() {
        super.onPause();
        WorkActivity workActivity = (WorkActivity) getActivity();
        if(workActivity != null && workActivity.isBluetoothDisconnectOnPause()){
            tasks.clearList();
            interactions.disconnectBluetooth();
        }
        toast_short.cancel();
        toast_long.cancel();
    }


    public void buttonLocalAuthenticate() {
        interactions.intAuthentication();
    }

    public void fitbitApiKeyEntered(String input) {
        ((WorkActivity) getActivity()).getHttpsClient().getUserName(input, interactions);
    }

    public void letDeviceBlink() {
        checkFirstButtonPress();
        interactions.letDeviceBlink();
    }

    public void buttonSwitchLiveMode() {
        toast_long.setText("Switch Live Mode output");
        toast_long.show();
        interactions.intAccelReadout();
        interactions.setAccelReadoutActive(!interactions.accelReadoutActive());
    }

    public void setBluetoothConnectionState(BluetoothConnectionState newState) {
        bluetoothConnectionState = newState;
    }
}
