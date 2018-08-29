package seemoo.fitbit.miscellaneous;


import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import seemoo.fitbit.information.InformationList;

/**
 * Offers load and save methods for the external storage.
 */
public class ExternalStorage {

    private final static String TAG = ExternalStorage.class.getSimpleName();

    private static String directory = "";

    /**
     * Sets the directory for loading/saving files.
     * @param value The new directory.
     */
    public static void setDirectory(String value, Activity activity){
        if(value == null || value.equals("")){
            directory = getDefaultPath(activity);
        } else {
            directory = value;
        }
        InternalStorage.saveString(directory, ConstantValues.SETTING_DIRECTORY, activity);
    }

    private static String getDefaultPath(Context context) {
        return context.getExternalFilesDir(null).getPath();
    }

    public static String getDirectory(Context context){
        if(directory != ""){
            return directory;
        } else {
            return getDefaultPath(context);
        }
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
            if(directory == ""){
                directory = getDefaultPath(activity);
            }
            File file = new File(directory, name + "_" + FitbitDevice.getMacAddress());
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(string.getBytes());
                outputStream.close();
                Log.e(TAG, "saved file on external storage: " + directory + "\\" +
                        name + "_" + FitbitDevice.getMacAddress());
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        saveString(list.getBeautyData(), name + "_" + dateFormat.format(new Date()), activity);
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
            File path = activity.getExternalFilesDir("../../../../" + directory);
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