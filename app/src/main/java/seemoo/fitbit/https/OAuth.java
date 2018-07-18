package seemoo.fitbit.https;


import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.FitbitAPIEntityCache;
import com.fitbit.api.client.FitbitApiClientAgent;
import com.fitbit.api.client.FitbitApiCredentialsCache;
import com.fitbit.api.client.FitbitApiCredentialsCacheMapImpl;
import com.fitbit.api.client.FitbitApiEntityCacheMapImpl;
import com.fitbit.api.client.FitbitApiSubscriptionStorage;
import com.fitbit.api.client.FitbitApiSubscriptionStorageInMemoryImpl;
import com.fitbit.api.client.LocalUserDetail;
import com.fitbit.api.client.http.AccessToken;
import com.fitbit.api.client.http.TempCredentials;
import com.fitbit.api.client.service.FitbitAPIClientService;
import com.fitbit.api.common.model.user.UserInfo;
import com.fitbit.api.model.APIResourceCredentials;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import seemoo.fitbit.activities.MainFragment;
import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.interactions.Interactions;
import seemoo.fitbit.miscellaneous.FitbitDevice;
import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.InternalStorage;

class OAuth {

    private final String TAG = this.getClass().getSimpleName();

    private Toast toast;
    private MainFragment mainFragment;

    private FitbitApiClientAgent apiClientAgent;
    private FitbitAPIClientService<FitbitApiClientAgent> apiClientService;
    private TempCredentials credentials;

    /**
     * Creates an instance of OAuth.
     * @param toast The toast to show messages to the user.
     * @param mainFragment The current mainFragment.
     */
    OAuth(Toast toast, MainFragment mainFragment) {
        this.toast = toast;
        this.mainFragment = mainFragment;
    }

    /**
     * Fetches the oAuth verifier value from the fitbit server.
     * In that process, the user has to login to the fitbit server and give read and write access to her/his/its account to this app.
     * @param webView The webView instance that shall be used.
     */
    void getVerifier(final WebView webView) {
        toast.setText("Connecting to Server...");
        toast.show();
        new Thread(new Runnable() {
            public void run() {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mainFragment.getActivity().runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                mainFragment.reverseWebView();
                            }
                        });
                    }
                }, 10000);
                try {

                    FitbitAPIEntityCache entityCache = new FitbitApiEntityCacheMapImpl();
                    FitbitApiCredentialsCache credentialsCache = new FitbitApiCredentialsCacheMapImpl();
                    FitbitApiSubscriptionStorage subscriptionStore = new FitbitApiSubscriptionStorageInMemoryImpl();
                    apiClientAgent = new FitbitApiClientAgent(ConstantValues.API_BASE_URL, ConstantValues.WEB_BASE_URL, credentialsCache);
                    apiClientService = new FitbitAPIClientService<>(apiClientAgent, ConstantValues.CONSUMER_KEY, ConstantValues.CONSUMER_SECRET, credentialsCache, entityCache, subscriptionStore);
                    credentials = apiClientAgent.getOAuthTempToken();
                    final String token = credentials.getToken();
                    mainFragment.getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Log.e(TAG, ConstantValues.WEB_BASE_URL + ConstantValues.OAUTH_TOKEN_URL_PART + token);
                            webView.loadUrl(ConstantValues.WEB_BASE_URL + ConstantValues.OAUTH_TOKEN_URL_PART + token);
                        }
                    });
                    timer.cancel();
                } catch (FitbitAPIException e) {
                    Log.e(TAG, e.toString());
                    mainFragment.getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            mainFragment.reverseWebView();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Fetches the user name from the fitbit server and greets the user.
     * @param verifier The oAuth verifier value.
     * @param interactions The current interactions instance.
     */
    void getUserName(final String verifier, final Interactions interactions) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    AccessToken accessToken = apiClientAgent.getOAuthAccessToken(credentials, verifier);
                    FitbitDevice.setAccessTokenKey(accessToken.getToken());
                    FitbitDevice.setAccessTokenSecret(accessToken.getTokenSecret());
                    FitbitDevice.setVerifier(verifier);
                    InternalStorage.saveString(accessToken.getToken(), ConstantValues.FILE_ACCESS_TOKEN_KEY, mainFragment.getActivity());
                    InternalStorage.saveString(accessToken.getTokenSecret(), ConstantValues.FILE_ACCESS_TOKEN_SECRET, mainFragment.getActivity());
                    InternalStorage.saveString(verifier, ConstantValues.FILE_VERIFIER, mainFragment.getActivity());
                    APIResourceCredentials resourceCredentials = new APIResourceCredentials("1", accessToken.getToken(), accessToken.getTokenSecret());
                    resourceCredentials.setAccessToken(accessToken.getToken());
                    resourceCredentials.setAccessTokenSecret(accessToken.getTokenSecret());
                    resourceCredentials.setResourceId("1");

                    LocalUserDetail user = new LocalUserDetail("1");
                    apiClientService.saveResourceCredentials(user, resourceCredentials);
                    FitbitApiClientAgent agent = apiClientService.getClient();
                    final UserInfo userInfo = agent.getUserInfo(user);
                    mainFragment.getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            toast.setText("Hello " + userInfo.getFullName() + "!");
                            toast.show();
                        }
                    });
                    getCredentials(interactions);
                } catch (FitbitAPIException e) {
                    toast.setText("Error: Unable to get user name!");
                    toast.show();
                    Log.e(TAG, e.toString());
                }
            }
        }).start();
    }

    /**
     * Fetches the credentials (nonce, authentication key) from the fitbit server.
     * @param interactions The current instance of interactions.
     */
    private void getCredentials(Interactions interactions) {
        HttpsMessage message = new HttpsMessage("POST", ConstantValues.OAUTH_CREDENTIALS_URL);
        HashMap<String, String> additionalParameter = new HashMap<>();
        additionalParameter.put("serialNumber", FitbitDevice.SERIAL_NUMBER);
        message.setAdditionalParameter(additionalParameter);
        message.addProperty("Content-Type", "application/x-www-form-urlencoded");
        message.addProperty("Content-Length", "25");
        message.setBody("serialNumber=" + FitbitDevice.SERIAL_NUMBER);
        String btleClientAuthCredentials = message.sendMessage();
        Log.e(TAG, "btleClientAuthCredentials = " + btleClientAuthCredentials);
        if (btleClientAuthCredentials != null && btleClientAuthCredentials.contains("authSubKey")) {
            Log.e(TAG, "btleClientAuthCredentials = " + btleClientAuthCredentials);
            FitbitDevice.setAuthenticationKey(btleClientAuthCredentials.substring(44, 76));
            FitbitDevice.setNonce(btleClientAuthCredentials.substring(btleClientAuthCredentials.indexOf("\"nonce\":") + 8, btleClientAuthCredentials.length() - 3));
            InternalStorage.saveString(FitbitDevice.AUTHENTICATION_KEY, ConstantValues.FILE_AUTH_KEY, mainFragment.getActivity());
            InternalStorage.saveString(FitbitDevice.NONCE, ConstantValues.FILE_NONCE, mainFragment.getActivity());
        } else if(btleClientAuthCredentials != null && btleClientAuthCredentials.contains("Tracker with serialNumber")){
            mainFragment.getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    toast.setText("Error: Tracker has not yet been paired with the server!");
                    toast.show();
                }
            });
        }
        interactions.interactionFinished();
        interactions.intEmptyInteraction();
        message.logParameter();
    }

}
