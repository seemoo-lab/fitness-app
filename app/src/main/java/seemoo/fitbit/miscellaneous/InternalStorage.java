package seemoo.fitbit.miscellaneous;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Offers load and save methods for the internal storage.
 */
public class InternalStorage {

    private final static String TAG = InternalStorage.class.getSimpleName();

    /**
     * Saves a string in the internal storage of the device.
     *
     * @param string   The string to be saved.
     * @param name     The name of the string to be saved. (The real file name will be: name_serialNumber)
     * @param activity The current activity.
     */
    public static void saveString(String string, String name, Activity activity) {
        save(string, name + "_" + AuthValues.SERIAL_NUMBER, activity);
    }

    /**
     * Saves a string in the internal storage.
     * @param string The data of the file.
     * @param name The name of the file.
     * @param activity The current activity.
     */
    private static void save(String string, String name, Activity activity) {
        try {
            FileOutputStream outputStream = activity.openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
            Log.e(TAG, "saved file on internal storage: " + name);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * Loads a file from internal storage.
     * @param name The name of the file.
     * @param activity The current activity.
     * @return The loaded file.
     */
    public static String loadString(String name, Activity activity) {
        return load(name + "_" + AuthValues.SERIAL_NUMBER, activity);
    }

    /**
     * Loads a file from internal storage
     * @param name The name of the file.
     * @param activity The current activity.
     * @return The loaded file.
     */
    private static String load(String name, Activity activity) {
        String result = "";
        try {
            FileInputStream inputStream = activity.openFileInput(name);
            byte[] input = new byte[inputStream.available()];
            while (inputStream.read(input) != -1) {
                result += new String(input);
            }
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "No old files to load.");
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }
        Log.e(TAG, "loaded file from external storage: " + name);
        return result;
    }

    /**
     * Loads the 'last device' file and outputs it.
     * @param activity The current activity.
     * @return The loaded file.
     */
    public static ArrayList<String> loadLastDevices(Activity activity) {
        ArrayList<String> result = new ArrayList<>();
        String lastBLEDevices = load(ConstantValues.LAST_DEVICES, activity);
        for (int i = 0; i < 10; i++) {
            int index = -1;
            if (lastBLEDevices != null && !lastBLEDevices.equals("")) {
                index = lastBLEDevices.indexOf("\n");
                if (index == -1) {
                    result.add(lastBLEDevices);
                    break;
                } else {
                    result.add(lastBLEDevices.substring(0, index));
                    lastBLEDevices = lastBLEDevices.substring(index + 1);
                }
            }
        }
        return result;
    }

    /**
     * Saves the last connected device in the corresponding file.
     * @param name The name + mac address of the device to store.
     * @param activity The current activity.
     */
    public static void saveLastDevice(String name, Activity activity) {
        ArrayList<String> lastDevices = new ArrayList<>();
        lastDevices.addAll(loadLastDevices(activity));
        while (lastDevices.size() > 10) {
            lastDevices.remove(lastDevices.size() - 1);
        }
        if (lastDevices.contains(name)) {
            while(lastDevices.contains(name)){
                lastDevices.remove(name);
            }
        } else if (lastDevices.size() == 10) {
            lastDevices.remove(9);
        }
        lastDevices.add(0, name);
        String devices = "";
        for (int i = 0; i < lastDevices.size(); i++) {
            devices = devices + lastDevices.get(i);
            if (i < lastDevices.size() - 1) {
                devices = devices + "\n";
            }
        }
        save(devices, ConstantValues.LAST_DEVICES, activity);
    }

    /**
     * Clears the list of last devices.
     * @param activity The current Activity.
     */
    public static void clearLastDevices(Activity activity) {
        File file = new File(activity.getFilesDir(), ConstantValues.LAST_DEVICES);
        file.delete();
    }

    /**
     * Loads all the authentication value files from the internal storage and sets their current values to it.
     *
     * @param activity The current activity.
     */
    public static void loadAuthFiles(Activity activity) {
        AuthValues.setNonce(loadString(ConstantValues.FILE_NONCE, activity));
        AuthValues.setAuthenticationKey(loadString(ConstantValues.FILE_AUTH_KEY, activity));
        AuthValues.setAccessTokenKey(loadString(ConstantValues.FILE_ACCESS_TOKEN_KEY, activity));
        AuthValues.setAccessTokenSecret(loadString(ConstantValues.FILE_ACCESS_TOKEN_SECRET, activity));
        AuthValues.setVerifier(loadString(ConstantValues.FILE_VERIFIER, activity));
    }
}
