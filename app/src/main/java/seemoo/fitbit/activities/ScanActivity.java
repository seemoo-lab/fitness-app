package seemoo.fitbit.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import seemoo.fitbit.R;
import seemoo.fitbit.commands.Commands;
import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.InternalStorage;

/**
 * The scan menu.
 */
public class ScanActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private ListView mListView;
    private Activity activity = this;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ArrayList<BluetoothDevice> devices; // list of all found devices
    private ArrayList<BluetoothDevice> correctDevices; // list of devices that are the correct ones to show on screen
    private ArrayList<String> correctDevicesString;
    private Button rescanButton;
    private ArrayList<BluetoothGattService> services;
    private Handler handler;
    private Runnable runnable;
    private int flags;
    private TextView textView;
    private HashMap<String, Commands> commandsList = new HashMap<>();
    private ProgressBar progressBar;
    private boolean deviceFound;
    private boolean progressBarStopp = false;

    private Toast toast;

    // Stops scanning after SCAN_TIMER milliseconds (only for flags 1 and 2).
    private static final long SCAN_TIMER = 60000;

    /**
     * {@inheritDoc}
     * Initializes several objects and starts to scan for devices.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        initialize();
        scan();

        if (flags == ConstantValues.FLAG_SCAN) {
            ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, correctDevicesString);
            mListView = (ListView) findViewById(R.id.bluetoothLeDevicesList);
            mListView.setAdapter(mArrayAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switchActivity(correctDevices.get(position));
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     * Tries to reconnect, depending on the mode selected in MainActivity.
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        if (flags != ConstantValues.FLAG_SCAN) {
            initialize();
            scan();
        }
    }

    /**
     * {@inheritDoc}
     * Closes / finishes all no longer need procedures.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (flags != ConstantValues.FLAG_SCAN) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
        if (commandsList.size() > 0) {
            for (String macAddress : commandsList.keySet()) {
                commandsList.get(macAddress).close();
            }
        }
    }

    /**
     * {@inheritDoc}
     * Closes / finishes all no longer need procedures.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothLeScanner.stopScan(mScanCallback);
        progressBarStopp = true;
        rescanButton.setVisibility(View.VISIBLE);
        if (commandsList.size() > 0) {
            for (String macAddress : commandsList.keySet()) {
                commandsList.get(macAddress).close();
            }
        }
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        toast.cancel();
    }

    /**
     * Initializes several objects.
     */
    private void initialize() {
        flags = getIntent().getFlags();
        deviceFound = false;
        devices = new ArrayList<>();
        correctDevices = new ArrayList<>();
        correctDevicesString = new ArrayList<>();
        services = new ArrayList<>();
        handler = new Handler();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        rescanButton = (Button) findViewById(R.id.button_rescan);
        rescanButton.setVisibility(View.GONE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.scanTextView);
        textView.setVisibility(View.GONE);
        toast = Toast.makeText(activity, "", Toast.LENGTH_SHORT);
    }

    /**
     * Scans for Bluetooth LE devices.
     */
    private void scan() {
        mBluetoothLeScanner.startScan(mScanCallback);
        Log.e(TAG, "Started scanning for Bluetooth LE devices.");
        toast.setText("Scanning...");
        toast.show();
        if (flags == ConstantValues.FLAG_SCAN) {
            progressBar();
            // Timer, to stop scanning after SCAN_TIMER milliseconds
            runnable = new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    Log.e(TAG, "Stopped scanning for Bluetooth LE devices.");
                    if (correctDevices.size() == 0) {
                        toast.setText("No device found.");
                        Log.e(TAG, "No device found.");
                    } else if (correctDevices.size() == 1) {
                        toast.setText("1 device found.");
                        Log.e(TAG, "1 device found.");
                    } else {
                        toast.setText(correctDevices.size() + " devices found.");
                        Log.e(TAG, correctDevices.size() + " devices found.");
                    }
                    toast.show();
                    rescanButton.setVisibility(View.VISIBLE);
                }
            };
            handler.postDelayed(runnable, SCAN_TIMER);
        } else {
            textView.setText("Fitbit " + getIntent().getExtras().getString("name"));
            textView.setVisibility(View.VISIBLE);
            alternatingProgressBar();
        }
    }

    /**
     * Called when the user taps the 'scan' button. Restarts the scan procedure.
     *
     * @param view View object that was clicked by the user.
     */
    public void rescan(View view) {
        rescanButton.setVisibility(View.GONE);
        scan();
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        /**
         * {@inheritDoc}
         *
         * Adds detected device to device list, if it is not already part of it, and generates a new commands instance for the device
         */
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice detectedDevice = result.getDevice();
            String macAddress = "";
            if (!devices.contains(detectedDevice)) {
                devices.add(detectedDevice);
                Log.e(TAG, "device found: " + detectedDevice.getAddress());
                if (flags == ConstantValues.FLAG_SCAN) {
                    if (commandsList.get(detectedDevice.getAddress()) != null) {
                        commandsList.get(detectedDevice.getAddress()).close();
                    }
                    commandsList.put(detectedDevice.getAddress(), new Commands(detectedDevice.connectGatt(getBaseContext(), false, mBluetoothGattCallback)));
                } else {
                    macAddress = getIntent().getExtras().getString("macAddress");
                }
                if (detectedDevice.getAddress().equals(macAddress)) {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    toast.setText("Device found.");
                    toast.show();
                    commandsList.put(detectedDevice.getAddress(), new Commands(detectedDevice.connectGatt(getBaseContext(), false, mBluetoothGattCallback)));
                }
            }
        }
    };

    /**
     * Defines scanning progress bar, which just goes from zero to max.
     */
    private void progressBar() {
        progressBarStopp = false;
        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            public void run() {
                int progressStatus = 0;
                progressBar.setMax(1000);
                while (!progressBarStopp && progressStatus < 1000) {
                    progressStatus += 1;
                    progressBar.setProgress(progressStatus);
                    try {
                        Thread.sleep(SCAN_TIMER / 1000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    /**
     * Defines scanning progress bar, which alternates between zero and max.
     */
    private void alternatingProgressBar() {
        progressBarStopp = false;
        progressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            public void run() {
                int progressStatus = 0;
                progressBar.setMax(1000);
                boolean up = true;
                while (!progressBarStopp && !deviceFound) {
                    if (up) {
                        progressStatus += 1;
                        if (progressStatus == 1000) {
                            up = false;
                        }
                        progressBar.setProgress(progressStatus);
                    } else {
                        progressStatus -= 1;
                        if (progressStatus == 0) {
                            up = true;
                        }
                        progressBar.setProgress(progressStatus);
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }).start();
    }

    /**
     * Switches to WorkActivity.
     *
     * @param selectedDevice The device to take to WorkActivity.
     */
    private void switchActivity(BluetoothDevice selectedDevice) {
        progressBarStopp = true;
        Intent intent = new Intent(getApplicationContext(), WorkActivity.class);
        InternalStorage.saveLastDevice(selectedDevice.getName() + ": " + selectedDevice.getAddress(), activity);
        InternalStorage.saveCurrentDevice(selectedDevice.getName() + ": " + selectedDevice.getAddress(), activity);
        intent.putExtra("device", selectedDevice);
        startActivity(intent);
        mBluetoothLeScanner.stopScan(mScanCallback);
        toast.cancel();
    }

    private final BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        /**
         * {@inheritDoc}
         *
         * Outputs the current connection state and tries the reconnect, depending on the mode selected in MainActivity.
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String connectionState = "";
            switch (newState) {
                case BluetoothProfile.STATE_DISCONNECTED:
                    connectionState = getString(R.string.connection_state0);
                    commandsList.get(gatt.getDevice().getAddress()).close();
                    if (flags != ConstantValues.FLAG_SCAN) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toast.setText("Disconnected. Rescanning...");
                                toast.show();
                                progressBarStopp = true;
                                devices.clear();
                                scan();
                            }
                        });
                    }
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    connectionState = getString(R.string.connection_state1);
                    break;
                case BluetoothProfile.STATE_CONNECTED:
                    connectionState = getString(R.string.connection_state2);
                    commandsList.get(gatt.getDevice().getAddress()).comDiscoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    connectionState = getString(R.string.connection_state3);
                    break;
            }
            Log.e(TAG, "onConnectionStateChange: " + connectionState);
        }

        /**
         * {@inheritDoc}
         *
         * Depending on the mode selected in MainActivity: Checks if the detected device is a general fitbit device or the wanted device.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.e(TAG, "onServicesDiscovered");
            services.addAll(gatt.getServices());
            if (flags == ConstantValues.FLAG_SCAN) {
                for (int i = 0; i < services.size(); i++) {
                    if (services.get(i).getCharacteristic(UUID.fromString(ConstantValues.CHARACTERISTIC_1_1)) != null) {
                        correctDevices.add(gatt.getDevice());
                        correctDevicesString.add(gatt.getDevice().getName() + ": " + gatt.getDevice().getAddress());
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mListView.invalidateViews();
                            }
                        });
                        break;
                    }
                }
                commandsList.get(gatt.getDevice().getAddress()).commandFinished();
            } else {
                services.addAll(gatt.getServices());
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        toast.setText(R.string.connection_established);
                        toast.show();
                    }
                });
                commandsList.get(gatt.getDevice().getAddress()).commandFinished();
                deviceFound = true;
                switchActivity(gatt.getDevice());
            }
        }
    };
}
