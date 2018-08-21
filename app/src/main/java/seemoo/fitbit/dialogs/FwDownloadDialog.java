package seemoo.fitbit.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import seemoo.fitbit.R;
import seemoo.fitbit.activities.WorkActivity;

public class FwDownloadDialog extends DialogFragment {

    private WorkActivity mActivity;
    private ListView myListView;

    private ArrayAdapter<String> arrayAdapter;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mActivity = (WorkActivity) args.getSerializable("activity");
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
                Fragment prev = getFragmentManager().findFragmentByTag("FWDOWNLOAD_FRAGMENT_TAG");
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
        new JSONFwFileParser(mActivity, this).execute();
        Toast.makeText(mActivity, "FWDOWNLOADDIALOG CReATED", Toast.LENGTH_LONG).show();
    }

    void onFwIndexfileResult(HashMap<String, FirmwareFileDescriptor> list) {
        ArrayList<String> strings = new ArrayList();
        for (String fwshortname:list.keySet()) {
            FirmwareFileDescriptor file = list.get(fwshortname);
            String curFile = file.getDeviceName() + " " + file.getFwshortname() + " " + file.getDescription();
            strings.add(curFile);
        }
        //Toast.makeText(mActivity, str, Toast.LENGTH_SHORT).show();
        arrayAdapter.clear();
        arrayAdapter.addAll(strings);
        arrayAdapter.notifyDataSetChanged();
    }
}


class JSONFwFileParser extends AsyncTask<Void, Void, Void> {

    private Activity activity;
    private FwDownloadDialog dialog;

    private HashMap<String, FirmwareFileDescriptor> fwfiles;

    JSONFwFileParser(Activity activity, FwDownloadDialog dialog) {
        this.activity = activity;
        this.dialog = dialog;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        HttpHandler sh = new HttpHandler();
        // Making a request to url and getting response
        String url = "https://raw.githubusercontent.com/seemoo-lab/fitness-app/feature/github_fw_download/fw_index.json";
        String jsonStr = sh.makeServiceCall(url);

        if (jsonStr != null) {
            try {
                JSONArray jsonArr = new JSONArray(jsonStr);

                fwfiles = new HashMap<>();
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
                        fwfiles.put(fwshortname, new FirmwareFileDescriptor(deviceName, fwshortname, description, version, location));
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

