package seemoo.fitbit.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import seemoo.fitbit.R;
import seemoo.fitbit.activities.MainActivity;
import seemoo.fitbit.events.TransferProgressEvent;
import seemoo.fitbit.fragments.MainFragment;

public class FirmwareFlashDialog extends Dialog {

    public static final int PICK_FWFILE_REQUEST = 673;
    private static String fw_path = "";

    private MainFragment mainFragment;
    private Activity mActivity;

    private ImageButton btn_fwfile_select;
    private Button btn_fwflash_cancel;
    private Button btn_flash;
    private EditText et_fwflash;

    private ArrayList<FirmwareFileDescriptor> fwfiles;

    public FirmwareFlashDialog(@NonNull final Activity pActivity, final MainFragment mainFragment) {
        super(pActivity);
        this.mainFragment = mainFragment;
        this.mActivity = pActivity;

        setContentView(R.layout.dialog_fwflash);
        setTitle(R.string.firmware_flash_dialog);

        btn_fwfile_select = (ImageButton) findViewById(R.id.btn_select_fwfile);
        btn_flash = (Button) findViewById(R.id.btn_flash);
        btn_fwflash_cancel = (Button) findViewById(R.id.btn_fwflash_cancel);
        et_fwflash = (EditText) findViewById(R.id.et_fwpath);
        final RadioButton rb_bsl_app = (RadioButton) findViewById(R.id.rdb_bsl_app);
        final RadioButton rb_bsl_only = (RadioButton) findViewById(R.id.rdb_bsl_only);
        final RadioButton rb_app_only = (RadioButton) findViewById(R.id.rdb_app_only);

        fw_path = et_fwflash.getText().toString();

        et_fwflash.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().matches("")) {
                    btn_flash.setEnabled(false);
                } else {
                    btn_flash.setEnabled(true);

                }
            }
        });

        btn_fwfile_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/octet-stream");
                mActivity.startActivityForResult(intent, PICK_FWFILE_REQUEST);
            }
        });

        btn_fwflash_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirmwareFlashDialog.this.cancel();
            }
        });

        btn_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fw_path = et_fwflash.getText().toString();
                if (rb_bsl_app.isChecked()) {
                    new TransferProgressDialog(mActivity, "FIRMWARE UPLOAD (BSL)", TransferProgressDialog.TRANSFER_APP_TO_TRACKER).show();
                    mainFragment.flashFirmware(fw_path, false);
                } else if (rb_bsl_only.isChecked()) {
                    new TransferProgressDialog(mActivity, "FIRMWARE UPLOAD (BSL)", TransferProgressDialog.TRANSFER_APP_TO_TRACKER).show();
                    mainFragment.flashFirmware(fw_path, false);
                    FirmwareFlashDialog.this.cancel();
                } else if (rb_app_only.isChecked()) {
                    new TransferProgressDialog(mActivity, "FIRMWARE UPLOAD (app)", TransferProgressDialog.TRANSFER_APP_TO_TRACKER).show();
                    mainFragment.flashFirmware(fw_path, true);
                    FirmwareFlashDialog.this.cancel();
                }


            }
        });

        new JSONParser(mActivity, this).execute();
        /*String str = "tmp";

        try {
            if (json != null) {
                str = ((JSONObject) json.get(0)).getString("name");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        Log.d("MYLOGTAG", "" + str);
*/

    }

    void onFwIndexfileResult(ArrayList<FirmwareFileDescriptor> list){
String str = list.get(0).getDeviceName() + " ";
        for (FirmwareFileDescriptor x:list) {
            str += x.getVersion() + " ";
        }
        Toast.makeText(mActivity, str, Toast.LENGTH_SHORT).show();
    }



    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    Toast.makeText(context, "Error. Only internal storage supported.", Toast.LENGTH_LONG).show();
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TransferProgressEvent event) {
        if (event.getEvent_type() == TransferProgressEvent.EVENT_TYPE_FW) {
            if (event.isStopEvent()) {
                new TransferProgressDialog(mActivity, "FIRMWARE UPLOAD (app)", TransferProgressDialog.TRANSFER_APP_TO_TRACKER).show();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mainFragment.flashFirmware(fw_path, true);

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void passActivityResult(Intent data) {
        String path = getPath(getContext(), data.getData());
        onFilePickerResult(path);
    }

    public void onFilePickerResult(String path) {
        et_fwflash.setText(path);
        if (path.matches("")) {
            btn_flash.setEnabled(false);
        } else {
            btn_flash.setEnabled(true);
        }
    }
}

class JSONParser extends AsyncTask<Void, Void, Void> {

    private Activity activity;
    private FirmwareFlashDialog dialog;

    private ArrayList<FirmwareFileDescriptor> fwfiles;

    public JSONParser(Activity activity, FirmwareFlashDialog dialog) {
        this.activity = activity;
        this.dialog = dialog;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        HttpHandler sh = new HttpHandler();
        // Making a request to url and getting response
        String url = "https://raw.githubusercontent.com/seemoo-lab/fitness-app/feature/github_fw_download/firmwares/fw_index.json";
        String jsonStr = sh.makeServiceCall(url);

        Log.e("mylogtag1", "Response from url: " + jsonStr);
        if (jsonStr != null) {
            try {
                JSONArray jsonArr = new JSONArray(jsonStr);

                fwfiles = new ArrayList<>();
                for (int pos = 0; pos < jsonArr.length(); pos++) {
                    JSONObject jsonObj = jsonArr.getJSONObject(pos);
                    String deviceName = jsonObj.getString("name");
                    JSONArray innerFwArray = jsonObj.getJSONArray("fwfiles");
                    for(int innerPos = 0; innerPos < innerFwArray.length(); innerPos++){
                        JSONObject fwfile = innerFwArray.getJSONObject(innerPos);
                        String description = fwfile.getString("description");
                        String version = fwfile.getString("version");
                        String location = fwfile.getString("location");
                        fwfiles.add(new FirmwareFileDescriptor(deviceName,description,version,location));
                    }
                }
            } catch (final JSONException e) {
                Log.e("MYLOGTAG0.5", "Json parsing error: " + e.getMessage());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity.getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }).start();

            }

        } else {
            Log.e("MYLOGTAG2", "Couldn't get json from server.");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity.getApplicationContext(),
                            "Couldn't get json from server. Check LogCat for possible errors!",
                            Toast.LENGTH_LONG).show();
                }
            }).start();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        dialog.onFwIndexfileResult(fwfiles);
    }

}

class HttpHandler {

    private static final String TAG = HttpHandler.class.getSimpleName();

    public HttpHandler() {
    }

    public String makeServiceCall(String reqUrl) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
}

class FirmwareFileDescriptor {
    private String deviceName = null;
    private String description = null;
    private String version = null;
    private String location = null;

    public FirmwareFileDescriptor(String deviceName, String description, String version, String location) {
        this.deviceName = deviceName;
        this.description = description;
        this.version = version;
        this.location = location;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}