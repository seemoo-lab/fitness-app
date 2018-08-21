package seemoo.fitbit.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import seemoo.fitbit.fragments.MainFragment;

public class FwDownloadDialog extends Dialog {

    private Activity mActivity;

    public FwDownloadDialog(@NonNull final Activity pActivity) {
        super(pActivity);
        this.mActivity = pActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new JSONFwFileParser(mActivity, this).execute();
        Toast.makeText(mActivity, "FWDOWNLOADDIALOG CReATED", Toast.LENGTH_LONG).show();
    }


    void onFwIndexfileResult(ArrayList<FirmwareFileDescriptor> list){
        String str = list.get(0).getDeviceName() + " ";
        for (FirmwareFileDescriptor x:list) {
            str += x.getVersion() + " ";
        }
        Toast.makeText(mActivity, str, Toast.LENGTH_SHORT).show();
    }
}


class JSONFwFileParser extends AsyncTask<Void, Void, Void> {

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