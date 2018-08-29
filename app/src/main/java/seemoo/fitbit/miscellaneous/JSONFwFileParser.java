package seemoo.fitbit.miscellaneous;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import seemoo.fitbit.dialogs.FwDownloadDialog;

public class JSONFwFileParser extends AsyncTask<Void, Void, Void> {

    private final String url = FwDownloadDialog.REPO_BASE_URL + "/fw_index.json";
    private Activity activity;
    private FwDownloadDialog dialog;

    private ArrayList<FirmwareFileDescriptor> fwfiles;

    public JSONFwFileParser(Activity activity, FwDownloadDialog dialog) {
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
