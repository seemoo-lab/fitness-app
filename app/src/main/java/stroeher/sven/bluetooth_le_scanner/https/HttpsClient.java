package stroeher.sven.bluetooth_le_scanner.https;

import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.fitbit.api.client.http.BASE64Encoder;

import java.util.HashMap;

import stroeher.sven.bluetooth_le_scanner.activities.WorkActivity;
import stroeher.sven.bluetooth_le_scanner.interactions.Interactions;
import stroeher.sven.bluetooth_le_scanner.miscellaneous.ConstantValues;
import stroeher.sven.bluetooth_le_scanner.miscellaneous.Utilities;
import stroeher.sven.bluetooth_le_scanner.tasks.Tasks;

/**
 * The client to deal with https.
 */
public class HttpsClient {

    private final String TAG = this.getClass().getSimpleName();

    private Toast toast;
    private WorkActivity activity;
    private final OAuth auth;
    private String response;

    /**
     * Creates an instance of https client.
     * @param toast The toast to show messages to the user.
     * @param activity The current activity.
     */
    public HttpsClient(Toast toast, WorkActivity activity) {
        this.toast = toast;
        this.activity = activity;
        auth = new OAuth(toast, activity);
    }

    /**
     * Fetches the oAuth verifier value from the fitbit server.
     * In that process, the user has to login to the fitbit server and give read and write access to her/his/its account to this app.
     *
     * @param webView The webView instance that shall be used.
     */
    public void getVerifier(WebView webView) {
        auth.getVerifier(webView);
    }

    /**
     * Fetches the user name from the fitbit server.
     *
     * @param verifier     The oAuth verifier value.
     * @param interactions The current interactions instance.
     */
    public void getUserName(String verifier, Interactions interactions) {
        auth.getUserName(verifier, interactions);
    }

    /**
     * Sends a message, which contains the dump, to the fitbit server.
     *
     * @param data       The dump data.
     * @param deviceName The device name.
     * @param tasks      The tasks object.
     */
    public void uploadDump(final String data, final String deviceName, final Tasks tasks) {
        new Thread(new Runnable() {
            public void run() {
                HttpsMessage message = new HttpsMessage("POST", ConstantValues.TRACKER_SYNC_URL);
                HashMap<String, String> additionalParameter = new HashMap<>();
                additionalParameter.put("trigger", "client");
                additionalParameter.put("btleName", deviceName);
                message.setAdditionalParameter(additionalParameter);
                message.setUrlAddition("?trigger=client&btleName=" + deviceName);
                message.addProperty("Device-Data-Encoding", "base64");
                message.setGzipBody(true);
                message.setBody(new BASE64Encoder().encode(Utilities.hexStringToByteArray(data)));
                response = message.sendMessage();
                Log.e(TAG, "Response = " + response);
                if (response.equals("") || response.contains("error")) {
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            toast.setText("Error: Upload to server unsuccessful.");
                            toast.show();
                            Log.e(TAG, "Error: Upload to server unsuccessful.");
                        }
                    });
                } else {
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            toast.setText("Upload to server successful.");
                            toast.show();
                            Log.e(TAG, "Upload to server successful.");
                        }
                    });
                }
                tasks.taskFinished();
            }
        }).start();
    }

    /**
     * Return the response of a https dialog.
     *
     * @return The response.
     */
    public String getResponse() {
        return response;
    }

}
