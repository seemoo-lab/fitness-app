package seemoo.fitbit.https;

import android.util.Log;

import com.fitbit.api.FitbitAPIException;
import com.fitbit.api.client.http.BASE64Encoder;
import com.fitbit.api.client.http.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import seemoo.fitbit.miscellaneous.ConstantValues;


/**
 * Creates and https message and sends it to the wanted address.
 */
class HttpsMessage {

    private final String TAG = this.getClass().getSimpleName();

    private String httpMethod;
    private String baseUrl;
    private String consumerKey = ConstantValues.CONSUMER_KEY;
    private String consumerSecret = ConstantValues.CONSUMER_SECRET;
    private String tokenKey;
    private String tokenSecret;
    private String verifier;

    private HashMap<String, String> properties = new HashMap<>();
    private String body = null;
    private long timestamp;
    private long nonce;
    private String signature;
    private HashMap<String, String> additionalParameter = new HashMap<>();
    private String urlAddition = "";
    private boolean gzipBody = false;

    /**
     * Creates an instance of https message.
     * @param httpMethod The https method (= POST or GET).
     * @param baseUrl The base destination URL.
     */
    HttpsMessage(String httpMethod, String baseUrl) {
        this.httpMethod = httpMethod;
        this.baseUrl = baseUrl;
        tokenKey = AuthValues.ACCESS_TOKEN_KEY;
        tokenSecret = AuthValues.ACCESS_TOKEN_SECRET;
        this.verifier = AuthValues.VERIFIER;
    }

    /**
     * Creates a message out of all given data to this class, sends it to the corresponding destination and returns the response.
     *
     * @return The response to the sent message.
     */
    String sendMessage() {
        try {
            URL urlTemp = new URL(baseUrl + urlAddition);
            HttpsURLConnection connection = (HttpsURLConnection) urlTemp.openConnection();
            connection.setDoInput(true);
            signature = createSignature();
            String header = "OAuth oauth_consumer_key=\"" + consumerKey + "\", oauth_nonce=\"" + nonce + "\", oauth_signature=\"" + encode(signature) +
                    "\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"" + timestamp + "\", oauth_token=\"" + tokenKey + "\", oauth_verifier=\"" + verifier + "\", oauth_version=\"1.0\"";
            Log.e(TAG, "httpsRequest header = " + header);
            connection.addRequestProperty("Authorization", header);
            connection.setRequestProperty("X-App-Version", ConstantValues.X_APP_VERSION);
            for (String key : properties.keySet()) {
                connection.setRequestProperty(key, properties.get(key));
            }
            connection.setDoOutput(true);
            if (body != null) {
                if (!gzipBody) {
                    connection.setRequestProperty("Content-Length", "" + body.length());
                    connection.getOutputStream().write(body.getBytes("UTF8"));
                } else {
                    connection.setRequestProperty("Accept-Encoding", "gzip");
                    connection.setRequestProperty("Content-Encoding", "gzip");
                    connection.setRequestProperty("Content-Type", "text/plain");
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
                    gzipOutputStream.write(body.getBytes("UTF-8"));
                    gzipOutputStream.close();
                    byte[] gzipBody = outputStream.toByteArray();
                    connection.setRequestProperty("Content-Length", "" + gzipBody.length);
                    connection.getOutputStream().write(gzipBody);
                }
            }
            Response response = new Response(connection);
            return "" + response.asString();
        } catch (IOException | FitbitAPIException e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    /**
     * Adds properties for the connection
     *
     * @param name  The name of the property to add.
     * @param value The value of the property to add.
     */
    void addProperty(String name, String value) {
        properties.put(name, value);
    }

    /**
     * Sets the value for the body.
     *
     * @param value The value for the body.
     */
    void setBody(String value) {
        body = value;
    }

    /**
     * Sets an addition to the base destination url defined in the constructor.
     *
     * @param addition The string to add to the base url.
     */
    void setUrlAddition(String addition) {
        urlAddition = addition;
    }

    /**
     * Collects alls information needed for the signature and afterwards generates and returns it.
     *
     * @return The created signature.
     */
    private String createSignature() {
        String collectedParameters = collectingParameters();
        String signatureBaseString = httpMethod + "&" + encode(baseUrl) + "&" + encode(collectedParameters);
        String signature = generateSignature(signatureBaseString);
        Log.e(TAG, "signatureBaseString = " + signatureBaseString);
        return signature;
    }

    /**
     * Collects all parameters and puts them into one string.
     *
     * @return A string containing all parameters.
     */
    private String collectingParameters() {
        String result = "";
        TreeMap<String, String> parameters = new TreeMap<>();
        timestamp = System.currentTimeMillis() / 1000;
        nonce = timestamp + new Random().nextInt();
        parameters.put("oauth_consumer_key", consumerKey);
        parameters.put("oauth_signature_method", "HMAC-SHA1");
        parameters.put("oauth_timestamp", "" + timestamp);
        parameters.put("oauth_nonce", "" + nonce);
        parameters.put("oauth_token", tokenKey);
        parameters.put("oauth_version", "1.0");
        parameters.put("oauth_verifier", verifier);
        for (String key : additionalParameter.keySet()) {
            parameters.put(key, additionalParameter.get(key));
        }
        for (String key : parameters.keySet()) {
            if (!key.equals(parameters.firstKey())) {
                result = result + "&";
            }
            result = result + encode(key) + "=" + encode(parameters.get(key));
        }
        return result;
    }

    /**
     * Percent encodes the input string.
     *
     * @param value The input string.
     * @return The percent encoded input string.
     */
    private String encode(String value) {
        if (value != null) {
            String encoded = "";
            try {
                encoded = URLEncoder.encode(value, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            StringBuilder sb = new StringBuilder(encoded.length());
            char focus;
            for (int i = 0; i < encoded.length(); i++) {
                focus = encoded.charAt(i);
                if (focus == '*') {
                    sb.append("%2A");
                } else if (focus == '+') {
                    sb.append("%20");
                } else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                    sb.append('~');
                    i += 2;
                } else {
                    sb.append(focus);
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * Generates a signature out of destination and parameters.
     *
     * @param signatureBaseString The string containing destination and parameters.
     * @return The generated signature.
     */
    private String generateSignature(String signatureBaseString) {
        byte[] byteHMAC = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            String signingKey = encode(consumerSecret) + '&' + encode(tokenSecret);
            SecretKeySpec spec = new SecretKeySpec(signingKey.getBytes(), "HmacSHA1");
            mac.init(spec);
            byteHMAC = mac.doFinal(signatureBaseString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BASE64Encoder().encode(byteHMAC);
    }

    /**
     * Sets if the body shall the gzipped or not
     *
     * @param value True, if the body shall be gzipped.
     */
    void setGzipBody(boolean value) {
        gzipBody = value;
    }

    /**
     * Logs the variables.
     */
    void logParameter() {
        Log.e(TAG, "consumer key = " + consumerKey);
        Log.e(TAG, "consumer secret = " + consumerSecret);
        Log.e(TAG, "timestamp = " + timestamp);
        Log.e(TAG, "nonce = " + nonce);
        Log.e(TAG, "token = " + tokenKey);
        Log.e(TAG, "token secret = " + tokenSecret);
        Log.e(TAG, "verifier = " + verifier);
        Log.e(TAG, "signature = " + signature);
        Log.e(TAG, "authentication key = " + AuthValues.AUTHENTICATION_KEY);
        Log.e(TAG, "final nonce = " + AuthValues.NONCE);
        for (String key : additionalParameter.keySet()) {
            Log.e(TAG, key + " = " + additionalParameter.get(key));
        }
    }

    /**
     * Sets additional parameter to the (always) needed ones.
     *
     * @param additionalParameter The additional parameter to set.
     */
    void setAdditionalParameter(HashMap<String, String> additionalParameter) {
        this.additionalParameter.putAll(additionalParameter);
    }
}
