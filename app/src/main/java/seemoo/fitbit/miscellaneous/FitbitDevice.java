package seemoo.fitbit.miscellaneous;

import android.util.Log;

/**
 * Contains all needed values for an authentication to the fitbit server and about the
 * current connected device
 */
public class FitbitDevice {

    private final static String TAG = FitbitDevice.class.getSimpleName();

    public static String NONCE = null;
    public static String AUTHENTICATION_KEY = null;
    public static String ENCRYPTION_KEY = null;
    public static String SERIAL_NUMBER = null;
    public static String ACCESS_TOKEN_KEY = null;
    public static String ACCESS_TOKEN_SECRET = null;
    public static String VERIFIER = null;
    private static String MAC_ADDRESS = null;

    //assume encryption by default
    public static boolean ENCRYPTED = true;

    //Device type (Flex, Charge HR, etc.)
    public static int DEVICE_TYPE = 0;

    //Protocol for FW update (0x30, 0x31. etc.)
    public static int PROTOCOL = 0x30; //default (Flex)

    //Memory layout variables (Flex defaults, get updated with device type)
    public static String MEMORY_START = "08000000";
    public static String MEMORY_BSL = "08000200";
    public static String MEMORY_APP = "0800a000";
    public static String MEMORY_APP_END = "08040000";
    public static String MEMORY_EEPROM = "08080000";
    public static String MEMORY_EEPROM_END = "08082000";
    public static String MEMORY_KEY = "08080020";
    public static String MEMORY_KEY_END = "08080030";
    public static String MEMORY_SRAM = "20000000";
    public static String MEMORY_SRAM_END = "20007fff";
    public static String MEMORY_CONSOLE = "200043DD";
    public static String MEMORY_CONSOLE_END = "2000444DD";


    /**
     * Sets the value of nonce.
     *
     * @param value The value to set.
     */
    public static void setNonce(String value) {
        NONCE = value;
    }


    /**
     * Sets the value of encrypted flag.
     *
     * @param value The value to set.
     */
    public static void setEncrypted(boolean value) {
        ENCRYPTED = value;
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
     * Sets the value of encryptionKey.
     *
     * @param value The value to set.
     */
    public static void setEncryptionKey(String value) {
        ENCRYPTION_KEY = value;
    }


    /**
     * Sets the value of serialNumber.
     *
     * @param value The value to set.
     */
    public static void setSerialNumber(String value) {
        SERIAL_NUMBER = value;
        if (null != value) {
            DEVICE_TYPE = Utilities.hexStringToInt(SERIAL_NUMBER.substring(10, 12));
            Log.d(TAG, "set device type to " + DEVICE_TYPE);
            updateMemoryLayout();
            updateProtocol();
        }
    }

    private static void updateMemoryLayout() {

        if (DEVICE_TYPE == 0x07) { //Flex
            MEMORY_START = ConstantValues.MEMORY_FLEX_START;
            MEMORY_BSL = ConstantValues.MEMORY_FLEX_BSL;
            MEMORY_APP = ConstantValues.MEMORY_FLEX_APP;
            MEMORY_APP_END = ConstantValues.MEMORY_FLEX_APP_END;
            MEMORY_EEPROM = ConstantValues.MEMORY_FLEX_EEPROM;
            MEMORY_EEPROM_END = ConstantValues.MEMORY_FLEX_EEPROM_END;
            MEMORY_KEY = ConstantValues.MEMORY_FLEX_KEY;
            MEMORY_KEY_END = ConstantValues.MEMORY_FLEX_KEY_END;
            MEMORY_SRAM = ConstantValues.MEMORY_FLEX_SRAM;
            MEMORY_SRAM_END = ConstantValues.MEMORY_FLEX_SRAM_END;
            MEMORY_CONSOLE = ConstantValues.MEMORY_FLEX_CONSOLE;
            MEMORY_CONSOLE_END = ConstantValues.MEMORY_FLEX_CONSOLE_END;
        }
        else if (DEVICE_TYPE == 0x12) { //Charge HR
            MEMORY_START = ConstantValues.MEMORY_CHR_START;
            MEMORY_BSL = ConstantValues.MEMORY_CHR_BSL;
            MEMORY_APP = ConstantValues.MEMORY_CHR_APP;
            MEMORY_APP_END = ConstantValues.MEMORY_CHR_APP_END;
            MEMORY_EEPROM = ConstantValues.MEMORY_CHR_EEPROM;
            MEMORY_EEPROM_END = ConstantValues.MEMORY_CHR_EEPROM_END;
            MEMORY_KEY = ConstantValues.MEMORY_CHR_KEY;
            MEMORY_KEY_END = ConstantValues.MEMORY_CHR_KEY_END;
            MEMORY_SRAM = ConstantValues.MEMORY_CHR_SRAM;
            MEMORY_SRAM_END = ConstantValues.MEMORY_CHR_SRAM_END;
            MEMORY_CONSOLE = ConstantValues.MEMORY_CHR_CONSOLE;
            MEMORY_CONSOLE_END = ConstantValues.MEMORY_CHR_CONSOLE_END;
        }

        //otherwise keep previous values

    }

    private static void updateProtocol() {

        if (DEVICE_TYPE == 0x07) { //Flex
            PROTOCOL = 0x30;
        }
        else if (DEVICE_TYPE == 0x12) { //Charge HR
            PROTOCOL = 0x31;
        }

        //otherwise keep previous values

    }

    /*
    Convert device type ID from serial to its product name
     */
    public static String getDeviceType() {
        switch (DEVICE_TYPE) {
            case 0x01:
                return "Zip";
            case 0x05:
                return "One";
            case 0x07:
                return "Flex";
            case 0x08:
                return "Charge";
            case 0x12:
                return "Charge HR";
            case 0x15:
                return "Alta";
            case 0x10:
                return "Surge";
            case 0x11:
                return "Electron";
            case 0x1b:
                return "Ionic";
            default:
                return "Unknown";
        }
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

    public static String getMacAddress() {
        return MAC_ADDRESS;
    }

    public static void setMacAddress(String macAddress) {
        MAC_ADDRESS = macAddress;
    }

    /**
     * Sets all values to null;
     */
    public static void clearCache() {
        //FIXME this seems to break getting authentication credentials by setting the SN to NULL
        /*
        NONCE = null;
        AUTHENTICATION_KEY = null;
        ENCRYPTION_KEY = null;
        SERIAL_NUMBER = null;
        ACCESS_TOKEN_KEY = null;
        ACCESS_TOKEN_SECRET = null;
        VERIFIER = null;*/
    }
}
