package seemoo.fitbit.activities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import seemoo.fitbit.R;
import seemoo.fitbit.commands.Commands;
import seemoo.fitbit.miscellaneous.AuthValues;
import seemoo.fitbit.https.HttpsClient;
import seemoo.fitbit.information.Alarm;
import seemoo.fitbit.information.Information;
import seemoo.fitbit.information.InformationList;
import seemoo.fitbit.interactions.Interactions;
import seemoo.fitbit.miscellaneous.ButtonHandler;
import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.Crypto;
import seemoo.fitbit.miscellaneous.Firmware;
import seemoo.fitbit.miscellaneous.ExternalStorage;
import seemoo.fitbit.miscellaneous.InternalStorage;
import seemoo.fitbit.miscellaneous.Utilities;
import seemoo.fitbit.tasks.Tasks;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;

import org.spongycastle.pqc.math.ntru.util.Util;

/**
 * The working menu.
 */
public class WorkActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private WorkActivity activity = this;
    private BluetoothDevice device;
    private ArrayList<BluetoothGattService> services = new ArrayList<>();
    private ButtonHandler buttonHandler;
    private Commands commands;
    private Interactions interactions;
    private Tasks tasks;
    private InformationList informationToDisplay = new InformationList("");
    private ListView mListView;
    private WebView mWebView;
    private FloatingActionButton clearAlarmsButton;
    private FloatingActionButton saveButton;
    private EditText editText;
    private TextView textView;
    private HttpsClient client;

    private Object interactionData;
    private Toast toast_short;
    private Toast toast_long;
    private int alarmIndex = -1;
    private String currentInformationList;
    private int customLength = -1;
    private String fileName;
    private boolean firstPress = true;

    private SparseBooleanArray settings = new SparseBooleanArray();
    private HashMap<String, InformationList> information = new HashMap<>();

    private GraphView graph;
    private BarGraphSeries<DataPoint> graphDataSeries;
    private int graphCounter = 0;

    /**
     * {@inheritDoc}
     * Initializes several objects and connects to the deivec.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work);
        setFinishOnTouchOutside(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        initialize();
        collectBasicInformation();
        connect();

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
            }
        });
    }

    /**
     * {@inheritDoc}
     * Closes bluetooth gatt and clears history.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (commands != null) {
            commands.close();
        }
        mWebView.clearHistory();
        toast_short.cancel();
        toast_long.cancel();
        AuthValues.clearCache();
        tasks.clearList();
    }

    /**
     * {@inheritDoc}
     * Closes bluetooth gatt.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (commands != null) {
            commands.close();
        }
    }

    /**
     * {@inheritDoc}
     * Loads the saved settings from internal storage.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_workactivity, menu);
        for (int i = 0; i < menu.size() - 1; i++) { //load settings
            MenuItem item = menu.getItem(i);
            SharedPreferences settings = getSharedPreferences("" + item.getTitle(), MODE_PRIVATE);
            item.setChecked(settings.getBoolean((String) item.getTitle(), false));
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * Lets the user choose the external directory and stores settings internally.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(true);
        }
        if (item.getItemId() == R.id.settings_workactivity_4) {
            if (firstPress) {
                tasks.taskStartup(interactions, this);
                firstPress = false;
            }
            buttonHandler.setAllGone();
            mListView.setVisibility(View.GONE);
            editText.setText(ExternalStorage.DIRECTORY);
            textView.setVisibility(View.VISIBLE);
            textView.setText(ConstantValues.ASK_DIRECTORY);
            editText.setVisibility(View.VISIBLE);
            buttonHandler.setVisible(R.id.button_WorkActivity_9);
        }
        settings.put(item.getItemId(), item.isChecked());
        if (item.getItemId() != R.id.settings_workactivity_4) { //stores settings
            SharedPreferences settings = getSharedPreferences("" + item.getTitle(), MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("" + item.getTitle(), true);
            editor.apply();
        }
        return true;
    }

    /**
     * Initializes several objects.
     */
    private void initialize() {
        device = (BluetoothDevice) getIntent().getExtras().get("device");
        buttonHandler = new ButtonHandler(activity);
        buttonHandler.addButton(R.id.button_WorkActivity_1);
        buttonHandler.addButton(R.id.button_WorkActivity_2);
        buttonHandler.addButton(R.id.button_WorkActivity_3);
        buttonHandler.addButton(R.id.button_WorkActivity_4);
        buttonHandler.addButton(R.id.button_WorkActivity_5);
        buttonHandler.addButton(R.id.button_WorkActivity_6);
        clearAlarmsButton = (FloatingActionButton) findViewById(R.id.button_WorkActivity_7);
        clearAlarmsButton.setVisibility(View.GONE);
        buttonHandler.addSpecialButton(R.id.button_WorkActivity_8);
        buttonHandler.addSpecialButton(R.id.button_WorkActivity_9);
        saveButton = (FloatingActionButton) findViewById(R.id.button_WorkActivity_10);
        saveButton.setVisibility(View.GONE);
        ArrayAdapter<Information> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, informationToDisplay.getList());
        mListView = (ListView) findViewById(R.id.WorkActivityList);
        mListView.setAdapter(arrayAdapter);
        toast_short = Toast.makeText(activity, "", Toast.LENGTH_SHORT);
        toast_long = Toast.makeText(activity, "", Toast.LENGTH_LONG);
        settings.put(R.id.settings_workactivity_1, false);
        settings.put(R.id.settings_workactivity_2, false);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setVisibility(View.GONE);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.equals("https://www.fitbit.com/oauth") || url.equals("https://www.fitbit.com/oauth/oauth_login_allow")) {
                    toast_long.setText("Please copy the PIN.");
                    toast_long.show();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            activity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    buttonHandler.setVisible(R.id.button_WorkActivity_8);
                                }
                            });
                        }
                    }, 3000);
                }
            }
        });
        editText = (EditText) findViewById(R.id.editText);
        editText.setVisibility(View.GONE);
        textView = (TextView) findViewById(R.id.textView);
        textView.setText(ConstantValues.ASK_AUTH_PIN);
        textView.setVisibility(View.GONE);
        client = new HttpsClient(toast_short, this);

        //Accel-Live: initialisation of graph
        graph = (GraphView) findViewById(R.id.graph);
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
        staticLabelsFormatter.setHorizontalLabels(new String[] {" ", "x", "y", "z", " "});
        graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
    }

    /**
     * Connects the app with the device.
     */
    private void connect() {
        BluetoothGatt mBluetoothGatt = device.connectGatt(getBaseContext(), false, mBluetoothGattCallback);
        commands = new Commands(mBluetoothGatt);
        interactions = new Interactions(activity, toast_short, commands, buttonHandler);
        tasks = new Tasks(interactions, activity, buttonHandler);
    }

    /**
     * Collects basic information about the selected device, stores them in 'information' and displays them to the user.
     */
    public void collectBasicInformation() {
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


        InternalStorage.loadAuthFiles(activity);

        if (AuthValues.AUTHENTICATION_KEY == null) {
            list.add(new Information("Authentication credentials unavailable, user login with previously associated tracker required. Association is only supported by the official Fitbit app."));
        } else {
            list.add(new Information("Authentication Key & Nonce: " + AuthValues.AUTHENTICATION_KEY + ", " + AuthValues.NONCE));
        }

        if (AuthValues.ENCRYPTION_KEY == null) {
            list.add(new Information("Encryption key unavailable, requires authenticated memory readout on vulnerable tracker models."));
        } else {
            list.add(new Information("Encryption Key: " + AuthValues.ENCRYPTION_KEY));
        }


        information.put("basic", list);
        informationToDisplay.override(information.get("basic"), mListView);
    }


    /**
     * Local BLE-only interactions.
     * Authenticates with the device if needed.
     *
     * @param view The current view.
     */
    public void button_Local(View view) {
        if (firstPress) {
            tasks.taskStartup(interactions, this);
            firstPress = false;
        }
        clearAlarmsButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        final String[] items = new String[]{"Live Mode", "Alarms", "Set Date", "Firmware Modifications", "Activity and Data Dumps"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Choose a local interaction:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //button_liveMode(view);
                        break;
                    case 1:

                    case 2:
                        interactions.intSetDate();
                        break;
                    case 3:
                    case 4:
                    default:
                        break; //TODO
                }
            }
        });
        builder.show();
    }


    /**
     * Remote actions on server: retrieve authentication credentials, upload dumps.
     *
     * @param view The current view.
     */
    public void button_Server(View view) {
        if (firstPress) {
            tasks.taskStartup(interactions, this);
            firstPress = false;
        }
        clearAlarmsButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        final String[] items = new String[]{"User Login", "Upload Activity Dumps"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Choose a server interaction:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    default:
                    case 0:
                        startAuthentication();
                        break;
                    case 1:
                        break; //TODO
                }
            }
        });
        builder.show();
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
     *
     * @param view The current view.
     */
    public void button_Dump(View view) {
        if (firstPress) {
            tasks.taskStartup(interactions, this);
            firstPress = false;
        }
        clearAlarmsButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        final String[] items = new String[]{"Microdump", "Megadump", "Key", "Flash: start", "Flash: BSL", "Flash: APP", "EEPROM", "SRAM", "Console Printf"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Choose a dump type:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        interactions.intMicrodump();
                        break;
                    case 1:
                        interactions.intMegadump();
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                    case 2:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        interactions.intReadOutMemory(ConstantValues.MEMORY_FLEX_KEY, ConstantValues.MEMORY_FLEX_KEY_END, "KEY");
                        break;
                    case 3:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        interactions.intReadOutMemory(ConstantValues.MEMORY_FLEX_START, ConstantValues.MEMORY_FLEX_BSL, "START");
                        break;
                    case 4:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        interactions.intReadOutMemory(ConstantValues.MEMORY_FLEX_BSL, ConstantValues.MEMORY_FLEX_APP, "BSL");
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                    case 5:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        interactions.intReadOutMemory(ConstantValues.MEMORY_FLEX_APP, ConstantValues.MEMORY_FLEX_APP_END, "APP");
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                    case 6:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        interactions.intReadOutMemory(ConstantValues.MEMORY_FLEX_EEPROM, ConstantValues.MEMORY_FLEX_EEPROM_END, "EEPROM");
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                    case 7:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        interactions.intReadOutMemory(ConstantValues.MEMORY_FLEX_SRAM, ConstantValues.MEMORY_FLEX_SRAM_END, "SRAM");
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                    case 8:
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        interactions.intReadOutMemory(ConstantValues.MEMORY_FLEX_CONSOLE, ConstantValues.MEMORY_FLEX_CONSOLE_END, "CONSOLE");
                        toast_long.setText(getString(R.string.time));
                        toast_long.show();
                        break;
                }
            }
        });
        builder.show();
    }

    /**
     * Gets called, when 'set date' button is pressed.
     * Lets the user set the date of the device.
     *
     * @param view The current view.
     */
    public void button_SetDate(View view) {
        if (firstPress) {
            tasks.taskStartup(interactions, this);
            firstPress = false;
        }
        interactions.intSetDate();
    }


    /**
     * Gets called, when the 'live mode' button is pressed.
     * Depending on the current state, it switches to live mode or back to normal mode.
     *
     * @param view The current view.
     */
    public void button_liveMode(View view) {

        if (firstPress) {

            tasks.taskStartup(interactions, this);
            firstPress = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Switching to live mode.");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    clearAlarmsButton.setVisibility(View.GONE);
                    saveButton.setVisibility(View.GONE);
                    graph.setVisibility(View.GONE);

                    if (!interactions.liveModeActive()) {
                        if (!interactions.getAuthenticated()) {
                            interactions.intAuthentication();
                        }
                        interactions.intLiveModeEnable(buttonHandler, R.id.button_WorkActivity_3);
                    } else {
                        interactions.intLiveModeDisable(buttonHandler, R.id.button_WorkActivity_3);
                        graph.setVisibility(View.GONE);
                    }
                }
            });
            builder.show();
        } else {
            clearAlarmsButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            if (!interactions.liveModeActive()) {
                if (!interactions.getAuthenticated()) {
                    interactions.intAuthentication();
                }
                interactions.intLiveModeEnable(buttonHandler, R.id.button_WorkActivity_3);
            } else {
                interactions.intLiveModeDisable(buttonHandler, R.id.button_WorkActivity_3);
                graph.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Gets called, when 'alarms' button is pressed.
     * Does an authentication, if necessary, collects the alarms from the device and shows them to the user.
     *
     * @param view The current view.
     */
    public void button_Alarms(View view) {
        if (firstPress) {
            tasks.taskStartup(interactions, this);
            firstPress = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
        } else {
            if (!interactions.getAuthenticated()) {
                interactions.intAuthentication();
            }
            interactions.intGetAlarm();
        }
    }

    /**
     * Gets called, when 'online' button is pressed.
     * Shows a list to the user, which lets her/him choose between:
     * - Authentication via a web interface.
     * - a local authentication, if there already was an authentication for this device in the past.
     * - the upload of a microdump.
     * - the upload of a megadump.
     * - the upload of a firmware.
     *
     * @param view The current view.
     */
    public void button_Online(View view) {
        if (firstPress) {
            tasks.taskStartup(interactions, this);
            firstPress = false;
        }
        clearAlarmsButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        final String[] items = new String[]{"Authenticate", "Local Authenticate", "Upload Microdump", "Upload Megadump", "Upload&Encrypt from Firmware FLASH Binary", "Upload&Encrypt Frame", "Set Encryption Key", "Set Authentication Credentials", "Switch Live Mode Output"};//, "Clear Data on Tracker", "Boot to BSL", "Boot to APP"};
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Choose an option:");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        startAuthentication();
                        break;
                    case 1:
                        interactions.intAuthentication();
                        break;
                    case 2:
                        tasks.taskUploadDump(client, device, ConstantValues.INFORMATION_MICRODUMP);
                        break;
                    case 3:
                        tasks.taskUploadDump(client, device, ConstantValues.INFORMATION_MEGADUMP);
                        break;
                    case 4:
                        buttonHandler.setAllGone();
                        mListView.setVisibility(View.GONE);
                        editText.setText("");
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(ConstantValues.ASK_FIRMWARE_FLASH_FILE);
                        editText.setVisibility(View.VISIBLE);
                        buttonHandler.setVisible(R.id.button_WorkActivity_9);
                        break;
                    case 5:
                        buttonHandler.setAllGone();
                        mListView.setVisibility(View.GONE);
                        editText.setText("");
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(ConstantValues.ASK_FIRMWARE_FRAME_FILE);
                        editText.setVisibility(View.VISIBLE);
                        buttonHandler.setVisible(R.id.button_WorkActivity_9);
                        break;
                    case 6:
                        buttonHandler.setAllGone();
                        mListView.setVisibility(View.GONE);
                        editText.setText("");
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(ConstantValues.ASK_ENC_KEY);
                        editText.setVisibility(View.VISIBLE);
                        buttonHandler.setVisible(R.id.button_WorkActivity_9);
                        break;
                    case 7:
                        buttonHandler.setAllGone();
                        mListView.setVisibility(View.GONE);
                        editText.setText("");
                        textView.setVisibility(View.VISIBLE);
                        textView.setText(ConstantValues.ASK_AUTH_KEY);
                        editText.setVisibility(View.VISIBLE);
                        buttonHandler.setVisible(R.id.button_WorkActivity_9);
                        break;
                        //TODO implement server responses that delete data on trackers...
                    case 8:
                        toast_long.setText("Switch Live Mode output");
                        toast_long.show();
                        interactions.intAccelReadout(buttonHandler, R.id.button_WorkActivity_3);
                        interactions.setAccelReadoutActive(!interactions.accelReadoutActive());
                        break;
                    case 9:
                        //interactions.intUploadMegadumpInteraction(Utilities.hexToBase64(Crypto.encryptDump(Utilities.hexStringToByteArray("2602000000000000000000000000d602b904e82c52091d1700000000000000ff4800202020202020202020204c4f5645205941202020474f20202020202020205543414e444f49542020290000000030000000000000000000000000000000000400b4bfd6570000000072040000fcffffff00000000ffffffff0000000000000300000005b4bfd6570233bfd65704b2bfd65701000000019f860180d60000000afff03f03f03f03f0381c000000007192000000000000a50000"), activity)));
                        //260200000100000000000000000050835988d7540ac156da5a3453f38ab178d5dc5d0515894c707c511de5bbfda945604254ad792cc9ca009ae7d88293ae5a1900661c167219f956b65200ddd7d0d0564b7f44f00f17295978e4fc199eb8c6ef707a8f00da40cd73e483bbd81ec4c773edf88c997aba41461ef33b6382f6d75b5f17844b0dab0ec1f94fd1c215c02c5687316c69ecbdc8066a3b1c438af655f7b4be5ccb4935c7e75669ce4c14bb691833ffd469aefde7000000
                        //interactions.intUploadMegadumpInteraction(Utilities.hexToBase64(                                                     "260200000100000000000000000050835988d7540ac156da5a3453f38ab178d5dc5d0515894c707c511de5bbfda945604254ad792cc9ca009ae7d88293ae5a1900661c167219f956b65200ddd7d0d0564b7f44f00f17295978e4fc199eb8c6ef707a8f00da40cd73e483bbd81ec4c773edf88c997aba41461ef33b6382f6d75b5f17844b0dab0ec1f94fd1c215c02c5687316c69ecbdc8066a3b1c438af655f7b4be5ccb4935c7e75669ce4c14bb691833ffd469aefde7a50000"));
                        //break;
                    case 10:
                        bootToBSL(); //TODO implement as normal task
                        break;
                    case 11:
                        bootToApp();
                        break;

                }
            }
        });
        builder.show();
    }

    /**
     * Gets called, when the 'information' button is pressed.
     * Collects basic information form the device.
     *
     * @param view The current view.
     */
    public void button_collectBasicInformation(View view) {
        if (firstPress) {
            tasks.taskStartup(interactions, this);
            firstPress = false;
        }
        clearAlarmsButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        collectBasicInformation();
    }

    /**
     * Closes the web interface regularly and shows the PIN input panel.
     *
     * @param view The current view.
     */
    public void button_finishWebView(View view) {
        mWebView.loadUrl(ConstantValues.EMPTY_URL);
        editText.setText("");
        buttonHandler.setGone(R.id.button_WorkActivity_8);
        textView.setVisibility(View.VISIBLE);
        editText.setVisibility(View.VISIBLE);
        buttonHandler.setVisible(R.id.button_WorkActivity_9);
        mWebView.setVisibility(View.GONE);
    }

    /**
     * Closes the web interface, when the app is unable to connect to the server.
     */
    public void reverseWebView() {
        mListView.setVisibility(View.VISIBLE);
        mWebView.setVisibility(View.GONE);
        buttonHandler.setAllVisible();
        toast_short.setText("Error: Unable to connect to Server!");
        toast_short.show();
    }

    /**
     * Reads in text from the user.
     * Depending on the current situation, the text can be:
     * - the external directory to store / load files.
     * - the name of a firmware to upload.
     * - the length of a firmware to upload.
     * - the PIN of an online authentication.
     *
     * @param view The current view.
     */
    public void readInText(View view) {
        if (textView.getText().equals(ConstantValues.ASK_DIRECTORY)) { // asks for directory
            textView.setText(ConstantValues.ASK_AUTH_PIN);
            ExternalStorage.setDirectory(editText.getText().toString(), this);
            Log.e(TAG, "New external directory = " + editText.getText().toString());
            editText.setText("");
            mListView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            buttonHandler.setGone(R.id.button_WorkActivity_9);
            buttonHandler.setAllVisible();

        }
        //Ask for encryption key / auth credentials
        else if (textView.getText().equals(ConstantValues.ASK_ENC_KEY)) { // asks for encryption key
            mListView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            buttonHandler.setGone(R.id.button_WorkActivity_9);
            AuthValues.setEncryptionKey(editText.getText().toString());
            InternalStorage.saveString(AuthValues.ENCRYPTION_KEY, ConstantValues.FILE_ENC_KEY, activity);
            editText.setText("");
        }
        else if (textView.getText().equals(ConstantValues.ASK_AUTH_KEY)) { // asks for authentication key and then for nonce
            textView.setText(ConstantValues.ASK_AUTH_NONCE);
            AuthValues.setAuthenticationKey(editText.getText().toString());
            InternalStorage.saveString(AuthValues.AUTHENTICATION_KEY, ConstantValues.FILE_AUTH_KEY, activity);
            editText.setText("");
        }
        else if (textView.getText().equals(ConstantValues.ASK_AUTH_NONCE)) { // asks for nonce
            mListView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            buttonHandler.setGone(R.id.button_WorkActivity_9);
            AuthValues.setNonce(editText.getText().toString());
            InternalStorage.saveString(AuthValues.NONCE, ConstantValues.FILE_NONCE, activity);
            editText.setText("");
            //TODO calculate hex to int format:
            // System.out.println("long: " + ((Long.parseLong("c17c9d26", 16))-Math.pow(2,32)) ); or
            //System.out.println("long: " + ((Long.parseLong("269d7cc1", 16)) ));  (with hex reverse byte order, and then back to int etc...)
            //should also be possible with more performant code (is this ones complement?)
        }
        //Firmware update via FLASH.bin file
        else if (textView.getText().equals(ConstantValues.ASK_FIRMWARE_FLASH_FILE)) { // asks for firmware name
            textView.setText(ConstantValues.ASK_FIRMWARE_FLASH_APP);
            fileName = editText.getText().toString();
            editText.setText("");
        }
        else if (textView.getText().equals(ConstantValues.ASK_FIRMWARE_FLASH_APP)) { // asks for firmware name
            textView.setText(ConstantValues.ASK_AUTH_PIN);
            mListView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            buttonHandler.setGone(R.id.button_WorkActivity_9);
            //FIXME actually authentication is not required for FW update, but otherwise encryption key variable is empty
            if (!interactions.getAuthenticated()) {
                interactions.intAuthentication();
            }


            String plain = "";
            //flash APP
            if (editText.getText().toString().equals("app")) {
                plain = Firmware.generateFirmwareFrame(fileName, 0xa000, 0xa000+0x26020, 0x800a000, false, activity);
            }
            //flash BSL
            else {
                plain = Firmware.generateFirmwareFrame(fileName, 0x0200, 0x0200+0x09e00, 0x8000200, true, activity);
            }



            ExternalStorage.saveString(plain, "fwplain", activity); //just for debugging...

            String fw = "";
            try {
                fw = Crypto.encryptDump(Utilities.hexStringToByteArray(plain), activity);
            }catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Encrypting dump failed.");
            }


            interactions.intUploadFirmwareInteraction(fw, fw.length());
        }
            //Firmware update via APP/BSL part from firmware.json, but custom encryption
        else if (textView.getText().equals(ConstantValues.ASK_FIRMWARE_FRAME_FILE)) { // asks for firmware name
            textView.setText(ConstantValues.ASK_AUTH_PIN);
            mListView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            buttonHandler.setGone(R.id.button_WorkActivity_9);
            if (!interactions.getAuthenticated()) {
                interactions.intAuthentication();
            }

            String fw = "";
            try {
                //fw = Crypto.decrypttest_fw_update(activity);
                fw = Crypto.encryptDumpFile(fileName, activity);
            }catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Encrypting dump failed.");
            }

            interactions.intUploadFirmwareInteraction(fw, fw.length());

            //interactions.intUploadFirmwareInteraction(ExternalStorage.loadString(fileName, activity), customLength);
        } else if(textView.getText().equals(ConstantValues.ASK_AUTH_PIN)){ // asks for authentication PIN
            mListView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.GONE);
            buttonHandler.setGone(R.id.button_WorkActivity_9);
            client.getUserName(editText.getText().toString(), interactions);
        } else{
            Log.e(TAG, "Error: Wrong text in textView!");
        }
    }

    /**
     * Gets called, when clear alarms button is pressed.
     *
     * @param view The current view.
     */
    public void clearAlarmsButton(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
        ExternalStorage.saveInformationList(information.get(currentInformationList), currentInformationList, activity);
        toast_short.setText("Information saved.");
        toast_short.show();
    }

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        /**
         * {@inheritDoc}
         * Logs aconnection state change and tries to reconnect, if connection is lost.
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String connectionState = "Unknown";
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = getString(R.string.connection_state0);
                services.clear();
                commands.close();
                buttonHandler.setAllGone();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mWebView.setVisibility(View.GONE);
                        toast_short.setText("Connection lost. Trying to reconnect...");
                        toast_short.show();
                        connect();
                    }
                });
                Log.e(TAG, "Connection lost. Trying to reconnect.");
            } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                connectionState = getString(R.string.connection_state1);
            } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = getString(R.string.connection_state2);
                commands.comDiscoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                connectionState = getString(R.string.connection_state3);
            }
            Log.e(TAG, "onConnectionStateChange: " + connectionState);
        }

        /**
         * {@inheritDoc}
         * Logs a service discovery and finishes the corresponding command.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.e(TAG, "onServicesDiscovered");
            services.addAll(gatt.getServices());
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toast_short.setText(R.string.connection_established);
                    toast_short.show();
                }
            });
            buttonHandler.setAllVisible();
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

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if(interactions.accelReadoutActive()) {
                            graph.setVisibility(View.VISIBLE);
                            graph.removeAllSeries();

                            graph.addSeries(graphDataSeries);


                        }else {
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

            if(Utilities.byteArrayToHexString(characteristic.getValue()) == "c01301000") {
                //Command
                Log.e(TAG, "Error: " + Utilities.getError(Utilities.byteArrayToHexString(characteristic.getValue())));
            }

            if (Utilities.byteArrayToHexString(characteristic.getValue()).length() >= 4 && Utilities.byteArrayToHexString(characteristic.getValue()).substring(0, 4).equals(ConstantValues.NEG_ACKNOWLEDGEMENT)) {
                Log.e(TAG, "Error: " + Utilities.getError(Utilities.byteArrayToHexString(characteristic.getValue())));
                services.clear();
                commands.close();
                buttonHandler.setAllGone();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        mWebView.setVisibility(View.GONE);
                        toast_short.setText("Disconnected. Trying to reconnect...");
                        toast_short.show();
                        connect();
                    }
                });
                Log.e(TAG, "Disconnected. Trying to reconnect...");
            } else {
                interactionData = interactions.interact(characteristic.getValue());
                if (interactions.isFinished()) {
                    interactionData = interactions.interactionFinished();
                }
                if (interactionData != null) {
                    currentInformationList = ((InformationList) interactionData).getName();
                    information.put(currentInformationList, (InformationList) interactionData);


                    graphDataSeries = Utilities.updateGraph(characteristic.getValue());

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            if(Utilities.checkLiveModeReadout(characteristic.getValue()) == false) {
                                graph.setVisibility(View.GONE);
                            }

                            if((graphCounter % 30) == 0) {
                                graph.removeAllSeries();
                                graph.addSeries(graphDataSeries);
                            }

                            graphCounter++;


                            InformationList temp = new InformationList("");
                            temp.addAll(information.get(((InformationList) interactionData).getName()));
                            if (settings.get(R.id.settings_workactivity_3)) {
                                ExternalStorage.saveInformationList(information.get(currentInformationList), currentInformationList, activity);
                            }
                            if (currentInformationList.equals("Memory_KEY")) {
                                AuthValues.setEncryptionKey(information.get(currentInformationList).getBeautyData().trim());
                                Log.e(TAG, "Encryption Key: " + AuthValues.ENCRYPTION_KEY);
                                InternalStorage.saveString(AuthValues.ENCRYPTION_KEY, ConstantValues.FILE_ENC_KEY, activity);
                            }
                            final int positionRawOutput = temp.getPosition(new Information(ConstantValues.RAW_OUTPUT));
                            if (!settings.get(R.id.settings_workactivity_1) && positionRawOutput > 0) {
                                temp.remove(positionRawOutput - 1, temp.size());
                            }
                            final int positionAdditionalInfo = temp.getPosition(new Information(ConstantValues.ADDITIONAL_INFO));
                            if (!settings.get(R.id.settings_workactivity_2) && positionAdditionalInfo > 0) {
                                temp.remove(positionAdditionalInfo - 1, positionRawOutput - 1);
                            }
                            informationToDisplay.override(temp, mListView);
                            if (mListView.getVisibility() == View.VISIBLE) {
                                saveButton.setVisibility(View.VISIBLE);
                            }
                            if (informationToDisplay.size() > 1 && informationToDisplay.get(1) instanceof Alarm) {
                                clearAlarmsButton.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
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
     * Starts the authentication via the web interface.
     */
    public void startAuthentication() {
        clearAlarmsButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        mListView.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        buttonHandler.setAllGone();
        client.getVerifier(mWebView);
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
     *
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
        command = Firmware.rebootToBSL(activity);


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

}

