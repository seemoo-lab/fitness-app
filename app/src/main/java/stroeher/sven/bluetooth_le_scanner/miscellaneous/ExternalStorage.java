package stroeher.sven.bluetooth_le_scanner.miscellaneous;


import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Calendar;

import stroeher.sven.bluetooth_le_scanner.https.AuthValues;
import stroeher.sven.bluetooth_le_scanner.information.InformationList;

/**
 * Offers load and save methods for the external storage.
 */
public class ExternalStorage {

    private final static String TAG = ExternalStorage.class.getSimpleName();

    public static String DIRECTORY = "Android/data/stroeher.sven.bluetooth_le_scanner/files/" + Environment.DIRECTORY_DOCUMENTS; //default dir: sdcard/Android/data/stroeher.sven.bluetooth_le/files/Documents/...

    /**
     * Sets the directory for loading/saving files.
     * @param value The new directory.
     */
    public static void setDirectory(String value, Activity activity){
        if(value == null || value.equals("")){
            DIRECTORY = "Android/data/stroeher.sven.bluetooth_le_scanner/files/" + Environment.DIRECTORY_DOCUMENTS;
        } else {
            DIRECTORY = value;
        }
        InternalStorage.saveString(DIRECTORY, ConstantValues.SETTING_DIRECTORY, activity);
    }

    /**
     * Checks if external storage is available for read and write.
     * @return True, if the storage is read- and writable.
     */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to read.
     * @return True, if the storage is readable.
     */
    private static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    /**
     * Saves a string in the external storage.
     * @param string The string to save.
     * @param name The name of the string.
     * @param activity The current activity.
     */
    public static void saveString(String string, String name, Activity activity){
        if(isExternalStorageWritable()){
            File path = activity.getExternalFilesDir("../../../../" + DIRECTORY);
            File file = new File(path, name + "_" + AuthValues.SERIAL_NUMBER);
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(string.getBytes());
                outputStream.close();
                Log.e(TAG, "saved file on external storage: " + name + "_" + AuthValues.SERIAL_NUMBER);
            } catch(IOException e){
                Log.e(TAG, e.toString());
            }
        } else{
            Log.e(TAG, "Error: External storage not writable");
        }
    }

    /**
     * Saves an information list in the external storage.
     * @param list The list to save.
     * @param name The name of the list.
     * @param activity The current activity.
     */
    public static void saveInformationList(InformationList list, String name, Activity activity){
        Calendar calendar = Calendar.getInstance();
        String date = calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.MONTH) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" +
                calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE) + "_" + calendar.get(Calendar.SECOND);
        saveString(list.getBeautyData(), name + "_" + date, activity);
    }

    /**
     * Loads a string from external storage. The directory is defines by DIRECTORY.
     * @param name The name of the file.
     * @param activity The current activity.
     * @return The loaded string.
     */
    public static String loadString(String name, Activity activity){
        String result = "";
        if(isExternalStorageReadable()){
            File path = activity.getExternalFilesDir("../../../../" + DIRECTORY);
            File file = new File(path, name);
            try {
                FileInputStream inputStream = new FileInputStream(file);
                byte[] input = new byte[inputStream.available()];
                while (inputStream.read(input) != -1) {
                    result += new String(input);
                }
                inputStream.close();
                Log.e(TAG, "loaded file from external storage: " + name);
            } catch(FileNotFoundException e){
                Log.e(TAG, e.toString());
                return null;
            } catch(IOException e){
                Log.e(TAG, e.toString());
                return null;
            }
        } else{
            Log.e(TAG, "Error: External storage not readable");
            return null;
        }
        return result;
    }

    /**
     * Loads a string from external storage. The directory is defines by DIRECTORY.
     * @param name The name of the file.
     * @param activity The current activity.
     * @return The loaded string.
     */
    public static byte[] loadByteArray(String name, Activity activity){
        byte[] input;
        int read = 0;
        if(isExternalStorageReadable()){
            File path = activity.getExternalFilesDir("/Documents/");
            File file = new File(path, name);
            int size = (int) file.length();
            input = new byte[size];
            try {
                FileInputStream inputStream = new FileInputStream(file);

                while((read = inputStream.read(input)) != -1){

                }

                //inputStream.close();
                //input = FileUtils.readFileToByteArray(file);

                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                buf.read(input, 0, input.length);
                buf.close();
                Log.e(TAG, "loaded file from external storage: " + name);
            } catch(FileNotFoundException e){
                Log.e(TAG, e.toString());
                return null;
            } catch(IOException e){
                Log.e(TAG, e.toString());
                return null;
            }
        } else{
            Log.e(TAG, "Error: External storage not readable");
            return null;
        }
        return input;
    }
}