package seemoo.fitbit.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import seemoo.fitbit.R;
import seemoo.fitbit.activities.WorkActivity;

public class FwDownloadDialog extends DialogFragment {

    public static final String REPO_BASE_URL = "https://raw.githubusercontent.com/seemoo-lab/fitness-app/feature/github_fw_download";

    public static final String FWDOWNLOAD_FRAGMENT_TAG = "FWDOWNLOAD_FRAGMENT_TAG";
    public static final String FLASHDIALOG_TAG = "FLASHDIALOG_TAG";
    public static final String WORKACTIVITY_TAG = "WORKACTIVITY_TAG";


    private WorkActivity mActivity;
    private FirmwareFlashDialog flashDialog;

    private ListView myListView;
    private ProgressBar pb_fwdownload;

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<FirmwareFileDescriptor> fwfiles = null;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mActivity = (WorkActivity) args.getSerializable(WORKACTIVITY_TAG);
        flashDialog = (FirmwareFlashDialog) args.getSerializable(FLASHDIALOG_TAG);
    }


    public FwDownloadDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_fwdownload, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel_fwdownload);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment prev = getFragmentManager().findFragmentByTag(FWDOWNLOAD_FRAGMENT_TAG);
                if (prev != null) {
                    DialogFragment df = (DialogFragment) prev;
                    df.dismiss();
                }
            }
        });

        getDialog().setTitle("Download firmware files");

        myListView = (ListView) view.findViewById(R.id.lv_list_fwfiles);

        ArrayList<String> strings = new ArrayList<String>(Arrays.asList("Loading firmware file index ..."));
        arrayAdapter
                = new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1, strings);

        myListView.setAdapter(arrayAdapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(fwfiles!=null){
                    FirmwareFileDescriptor file = fwfiles.get(position);
                    String url = REPO_BASE_URL + "/" + file.getLocation();
                    new DownloadFileFromURL(FwDownloadDialog.this).execute(url);
                }

            }
        });

        pb_fwdownload = (ProgressBar) view.findViewById(R.id.pb_fwdownload);

        new JSONFwFileParser(mActivity, this).execute();
    }

    void onFwIndexfileResult(ArrayList<FirmwareFileDescriptor> list) {
        fwfiles = list;
        ArrayList<String> strings = new ArrayList<String>();
        for (FirmwareFileDescriptor fwfile:list) {
            String curFile = fwfile.getDeviceName() + " " + fwfile.getFwshortname() + " " + fwfile.getDescription();
            strings.add(curFile);
        }
        //Toast.makeText(mActivity, str, Toast.LENGTH_SHORT).show();
        arrayAdapter.clear();
        arrayAdapter.addAll(strings);
        arrayAdapter.notifyDataSetChanged();
    }

    void fileDownloaded(String file_url){
        //TODO update FlashDialog file url
        if(file_url!=null && !file_url.matches("")){
            flashDialog.onFilePickerResult(file_url);
            Toast.makeText(mActivity, "Download completed. Stored at:\r\n" + file_url, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(mActivity, "Download failed." + file_url, Toast.LENGTH_LONG).show();
        }

        closeDialog();
    }
    void closeDialog(){
        Fragment prev = getFragmentManager().findFragmentByTag(FWDOWNLOAD_FRAGMENT_TAG);
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }
    }

    void setProgress(int progress){
        pb_fwdownload.setProgress(progress);
    }

    public void showDownloadToast() {
        Toast.makeText(mActivity, "Download started...", Toast.LENGTH_SHORT).show();
    }
}


class JSONFwFileParser extends AsyncTask<Void, Void, Void> {

    private final String url = FwDownloadDialog.REPO_BASE_URL + "/fw_index.json";
    private Activity activity;
    private FwDownloadDialog dialog;

    private ArrayList<FirmwareFileDescriptor> fwfiles;

    JSONFwFileParser(Activity activity, FwDownloadDialog dialog) {
        this.activity = activity;
        this.dialog = dialog;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        HttpHandler sh = new HttpHandler();
        // Making a request to url and getting response
        String jsonStr = sh.makeServiceCall(url);

        if (jsonStr != null) {
            try {
                JSONArray jsonArr = new JSONArray(jsonStr);

                fwfiles = new ArrayList<FirmwareFileDescriptor>();
                for (int pos = 0; pos < jsonArr.length(); pos++) {
                    JSONObject jsonObj = jsonArr.getJSONObject(pos);
                    String deviceName = jsonObj.getString("name");
                    JSONArray innerFwArray = jsonObj.getJSONArray("fwfiles");
                    for (int innerPos = 0; innerPos < innerFwArray.length(); innerPos++) {
                        JSONObject fwfile = innerFwArray.getJSONObject(innerPos);
                        String fwshortname = fwfile.getString("fwshortname");
                        String description = fwfile.getString("description");
                        String version = fwfile.getString("version");
                        String location = fwfile.getString("location");
                        fwfiles.add(new FirmwareFileDescriptor(deviceName, fwshortname, description, version, location));
                    }
                }
            } catch (final JSONException e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity.getApplicationContext(),
                                "JSON parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }).start();

            }

        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity.getApplicationContext(),
                            "Couldn't get JSON from server. Check LogCat for possible errors!",
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
    private String fwshortname = null;
    private String description = null;
    private String version = null;
    private String location = null;

    public FirmwareFileDescriptor(String deviceName, String fwshortname, String description, String version, String location) {
        this.deviceName = deviceName;
        this.fwshortname = fwshortname;
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

    public String getFwshortname() {
        return fwshortname;
    }

    public void setFwshortname(String fwshortname) {
        this.fwshortname = fwshortname;
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

class DownloadFileFromURL extends AsyncTask<String, String, String> {

    private FwDownloadDialog dialog;
    private String localFilePath= null;

    /**
     * Before starting background thread Show Progress Bar Dialog
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.showDownloadToast();
    }

    DownloadFileFromURL(FwDownloadDialog dialog){
        this.dialog = dialog;
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            URL url = new URL(f_url[0]);
            URLConnection conection = url.openConnection();
            conection.connect();

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);


            localFilePath = Environment
                    .getExternalStorageDirectory().toString()
                    + "/Download/downloadedfile.bin";
            // Output stream
            OutputStream output = new FileOutputStream(localFilePath);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }

    /**
     * Updating progress bar
     * */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        dialog.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task Dismiss the progress dialog
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded
        //dismissDialog(progress_bar_type);
        dialog.fileDownloaded(localFilePath);
    }

}