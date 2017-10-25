package stroeher.sven.bluetooth_le_scanner.https;

/**
 * Contains all needed values for an authentication to the fitbit server.
 */
public class AuthValues {

    private final static String TAG = AuthValues.class.getSimpleName();

    public static String NONCE = null;
    public static String AUTHENTICATION_KEY = null;
    public static String SERIAL_NUMBER = null;
    public static String ACCESS_TOKEN_KEY = null;
    public static String ACCESS_TOKEN_SECRET = null;
    public static String VERIFIER = null;

    /**
     * Sets the value of nonce.
     *
     * @param value The value to set.
     */
    public static void setNonce(String value) {
        NONCE = value;
    }

    /**
     * Sets the value of authenticationKey.
     *
     * @param value The value to set.
     */
    public static void setAuthenticationKey(String value) {
        AUTHENTICATION_KEY = value;
    }

    /**
     * Sets the value of serialNumber.
     *
     * @param value The value to set.
     */
    public static void setSerialNumber(String value) {
        SERIAL_NUMBER = value;
    }

    /**
     * Sets the value of accessTokenKey.
     *
     * @param value The value to set.
     */
    public static void setAccessTokenKey(String value) {
        ACCESS_TOKEN_KEY = value;
    }

    /**
     * Sets the value of accessTokenSecret.
     *
     * @param value The value to set.
     */
    public static void setAccessTokenSecret(String value) {
        ACCESS_TOKEN_SECRET = value;
    }

    /**
     * Sets the value of verifier.
     *
     * @param value The value to set.
     */
    public static void setVerifier(String value) {
        VERIFIER = value;
    }

    /**
     * Sets all values to null;
     */
    public static void clearCache() {
        NONCE = null;
        AUTHENTICATION_KEY = null;
        SERIAL_NUMBER = null;
        ACCESS_TOKEN_KEY = null;
        ACCESS_TOKEN_SECRET = null;
        VERIFIER = null;
    }
}