package seemoo.fitbit.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.security.*;

import seemoo.fitbit.R;
import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.InternalStorage;
import seemoo.fitbit.miscellaneous.Messenger;

/**
 * The main menu.
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_APP_SETTINGS = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private Activity activity;
    private Button scanButton;
    private TextView textView;
    private ListView lastDevices;
    ArrayList<String> lastDevicesString = new ArrayList<>();
    private FloatingActionButton clearLastDevicesButton;


    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Initializes objects and requests needed permissions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
        requestPermissionsLocation();
        enableBluetooth();
        checkLastDeviceIsSet();
    }

    /**
     * {@inheritDoc}
     * Reinitializes several objects.
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        //App needs to check on each restart if the needed permissions are granted
        requestPermissionsLocation();
        initialize();

    }

    /**
     * Initializes several objects and shows the last device list, if is is not empty.
     */
    private void initialize() {
        activity = this;
        scanButton = (Button) findViewById(R.id.button_scanFitbit);
        scanButton.setVisibility(View.VISIBLE);
        clearLastDevicesButton = (FloatingActionButton) findViewById(R.id.clear_lastDevices);
        // Initializes Bluetooth adapter.
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        textView = (TextView) findViewById(R.id.textLastDevices);
        if (InternalStorage.loadLastDevices(activity) != null) {
            lastDevicesString.clear();
            lastDevicesString.addAll(InternalStorage.loadLastDevices(activity));
        }
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lastDevicesString);
        lastDevices = (ListView) findViewById(R.id.lastDevices);
        lastDevices.setAdapter(mArrayAdapter);
        if (lastDevicesString == null || lastDevicesString.size() <= 0) {
            textView.setVisibility(View.GONE);
            lastDevices.setVisibility(View.GONE);
            clearLastDevicesButton.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            lastDevices.setVisibility(View.VISIBLE);
            clearLastDevicesButton.setVisibility(View.VISIBLE);
        }
        lastDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                autoScan(position);
            }
        });
    }

    /**
     * Asks user for permissions: access fine location
     */
    protected void requestPermissionsLocation() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
            } else {
                showDialogOnMissingPermission();
            }
        //If the location-permission was already granted, we want to check the External-Storage-Permission as well.
        } else {
            requestPermissionsExternalStorage();
        }
    }
    /**
     * Asks user for permissions: write to external storage
     */
    protected void requestPermissionsExternalStorage() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            } else{
                showDialogOnMissingPermission();
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks if the user granted permission to access fine location.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION: {
                //location permission granted:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanButton.setVisibility(View.VISIBLE);
                    // Check External-Storage-Permission next
                    requestPermissionsExternalStorage();
                }
                //No location permission granted:
                else {
                    scanButton.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    lastDevices.setVisibility(View.GONE);
                    clearLastDevicesButton.setVisibility(View.GONE);
                    Toast.makeText(activity, getString(R.string.no_location_access), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, getString(R.string.no_location_access));
                    // Request Location-Permission again because it is needed for app-functionality
                    requestPermissionsLocation();
                }
                break;
            }
            case REQUEST_EXTERNAL_STORAGE: {
                //location permission granted:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanButton.setVisibility(View.VISIBLE);
                }
                //No location permission granted:
                else {
                    scanButton.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    lastDevices.setVisibility(View.GONE);
                    clearLastDevicesButton.setVisibility(View.GONE);
                    Toast.makeText(activity, getString(R.string.no_external_storage_access), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, getString(R.string.no_external_storage_access));
                    // Request Location-Permission again because it is needed for app-functionality
                    requestPermissionsExternalStorage();
                }
                break;

            }
        }
    }

    /**
     * Called when the user taps the 'Scan for Fitbit devices' button. Scans for the Fitbit devices.
     *
     * @param view View object that was clicked by the user.
     */
    public void fitbitScan(View view) {
        if (enableBluetooth()) {
            Intent intent = new Intent(this, ScanActivity.class);
            intent.addFlags(ConstantValues.FLAG_SCAN);
            startActivity(intent);
        } else {
            Log.e(TAG, "Error: MainActivity.fitbitScan, Bluetooth not enabled");
        }
    }

    /**
     * Scans for the device with position 'position' in 'last devices list'.
     *
     * @param position The position of the device to scan.
     */
    public void autoScan(int position) {
        if (enableBluetooth()) {
            Intent intent = new Intent(this, ScanActivity.class);
            intent.addFlags(position);
            int index = lastDevicesString.get(position).lastIndexOf(": ");
            String macAddress = lastDevicesString.get(position).substring(index + 2);
            intent.putExtra("macAddress", macAddress);
            String name = lastDevicesString.get(position).substring(0, index);
            intent.putExtra("name", name);
            startActivity(intent);
        } else {
            Log.e(TAG, "Error: MainActivity.autoScan, Bluetooth not enabled");
        }
    }

    /**
     * Ensures Bluetooth (LE) is available on the device and it is enabled.
     * If not, displays a dialog requesting user permission to enable Bluetooth.
     *
     * @return False, if Bluetooth (LE) is not supported by the device, else true.
     */
    private boolean enableBluetooth() {
        //Ensures Bluetooth is available on the device.
        //If not, a message is shown to the user.
        if (mBluetoothAdapter == null) {
            Messenger.message(activity, android.R.drawable.ic_dialog_alert, R.string.error,
                    R.string.b_not_supported, R.string.cancel);
            Log.e(TAG, getString(R.string.b_not_supported));
            return false;
        }
        //Ensures Bluetooth LE is available on the device.
        //If not, a message is shown to the user.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Messenger.message(activity, android.R.drawable.ic_dialog_alert, R.string.error,
                    R.string.ble_not_supported, R.string.cancel);
            Log.e(TAG, getString(R.string.ble_not_supported));
            return false;
        }
        //Ensures Bluetooth is enabled on the device.
        //If not, displays a dialog requesting user permission to enable Bluetooth.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, REQUEST_ENABLE_BLUETOOTH);
        }
        return true;
    }

    /**
     * Called when the user tapps the 'clear last devices' button. Clears the last devices list.
     *
     * @param view View object that was clicked by the user.
     */
    public void clearLastDevices(View view) {
        InternalStorage.clearLastDevices(activity);
        Toast.makeText(activity, "Last devices cleared.", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Last devices cleared.");
        lastDevicesString.clear();
        textView.setVisibility(View.GONE);
        lastDevices.setVisibility(View.GONE);
        clearLastDevicesButton.setVisibility(View.GONE);
        lastDevices.invalidateViews();
    }

    public void decrypttest(View view) throws UnsupportedEncodingException{

        //Crypto.decrypttest_fw_update(activity);
    }


    private void checkLastDeviceIsSet(){
        String lastDevice = InternalStorage.loadString(ConstantValues.LAST_DEVICE, activity);

        if(!"".equals(lastDevice) && lastDevice != null){
            scanForLastDevice();
        }
    }

    private void scanForLastDevice(){
        if (enableBluetooth()) {
            String currentDevice = InternalStorage.loadString(ConstantValues.LAST_DEVICE, activity);

            Intent intent = new Intent(this, ScanActivity.class);
            intent.addFlags(9999);
            int index = currentDevice.lastIndexOf(": ");
            String macAddress = currentDevice.substring(index + 2);
            intent.putExtra("macAddress", macAddress);
            String name = currentDevice.substring(0, index);
            intent.putExtra("name", name);
            startActivity(intent);
        } else {
            Log.e(TAG, "Error: MainActivity.fitbitScan, Bluetooth not enabled");
        }
    }

    /**
     * Show a dialog which explains shortly to the user that the app needs access to location and external storage and offer him to bring
     * him directly to the app-preferences.
     * If the user denies the request, the app will hide all buttons.
     */
    private void showDialogOnMissingPermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(R.string.permission_Dialog_explanation)
                .setTitle(R.string.permission_Dialog_title);


        builder.setPositiveButton(R.string.permission_Dialog_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                goToSettings();
            }
        });
        builder.setNegativeButton(R.string.permission_Dialog_negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                finishAndRemoveTask();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * Bring the user directly to the app-settings to grant the permissions needed for the functionality
     */
    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(myAppSettings, REQUEST_APP_SETTINGS);
    }
}