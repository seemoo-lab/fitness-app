package seemoo.fitbit.miscellaneous;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

/**
 * Contains most constant values.
 */
public class ConstantValues {

    private final static String TAG = ConstantValues.class.getSimpleName();

    // indicates, that the user wants to scan for all fitbit devices, instead of a certain one
    public static final int FLAG_SCAN = 99; // negative flags are not allowed, so its value must be larger, than the length last devices list


    //Names:
    public static final String[] NAMES = new String[]{"Flex", "Flex 2", "One", "Alta", "Alta HR", "Blaze", "Charge", "Charge HR", "Charge 2", "Surge", "Zip", "Aria", "Ultra", "Force", "Ionic"}; //add new names at the end!


    //UUIDs:
    //naming: type_ServiceNumber_CharacteristicNumber_DescriptorNumber
    //used for normal mode:
    public static final String CHARACTERISTIC_1_1 = "adabfb01-6e7d-4601-bda2-bffaa68956ba"; //fitbit identifier / handle 0x000b
    public static final String DESCRIPTOR_1_1_1 = "00002902-0000-1000-8000-00805f9b34fb"; //handle 0x000c
    public static final String CHARACTERISTIC_1_2 = "adabfb02-6e7d-4601-bda2-bffaa68956ba"; //handle 0x000e
    //used for live mode:
    public static final String CHARACTERISTIC_2_1 = "558dfa01-4fa8-4105-9f02-4eaa93e62980"; //handle 0x0011 <= gilt nur für Flex!
    public static final String DESCRIPTOR_2_1_1 = "00002902-0000-1000-8000-00805f9b34fb"; //handle 0x0012 <= gilt nur für Flex!
    //used for asking name:
    public static final String CHARACTERISTIC_3_1 = "00002a00-0000-1000-8000-00805f9b34fb"; //handle 0x0003 <= gilt nur für Flex!


    //Constant orders:
    public static final String MODE_ON = "0100";
    public static final String MODE_OFF = "0000";
    public static final String ESTABLISH_AIRLINK = "c00a0a00080010000000c80001";
    public static final String ESTABLISH_AIRLINK_ACK = "c014";
    public static final String CLOSE_AIRLINK = "c001";
    public static final String ACKNOWLEDGEMENT = "c002";
    public static final String NEG_ACKNOWLEDGEMENT = "c003";
    public static final String SET_DATE = "c004";
    public static final String BLINKING_LEDS = "c006";
    public static final String DUMP = "c010";
    public static final String READOUT_MEMORY = "c011";
    public static final String DUMP_BEGIN = "c041";
    public static final String DUMP_END = "c042";
    public static final String CONSOLEDUMP = "c043"; //special testing command, not implemented in standard firmware
    public static final String UPLOAD = "c024";
    public static final String ALARM_BEGINNING = "aaaa0000000000000000";
    public static final String ALARM_FILLER_1 = "1c0201";
    public static final String ALARM_FILLER_2 = "0dfc0fc0fc0fc0ffffc0fc0fc0fc00";
    public static final String UPLOAD_RESPONSE = "c012";
    public static final String UPLOAD_SECOND_RESPONSE = "c013";
    public static final String EMPTY_ALARM = "ffffff7f0000000000000000000000000000000000000000";
    public static final String AUTHENTICATION_INITIALIZE = "c050";
    public static final String AUTHENTICATION_CHALLENGE = "c051";
    public static final String AUTHENTICATION_RESPONSE = "c052";

    //Type orders:
    public static final String TYPE_FIRMWARE = "01";
    public static final String TYPE_MICRODUMP = "03";
    public static final String TYPE_MICRODUMP_UPLOAD = "UNKNOWN";
    public static final String TYPE_MEMORY = "09";
    public static final String TYPE_ALARMS = "0a";
    public static final String TYPE_MEGADUMP = "0d";
    public static final String TYPE_MEGADUMP_UPLOAD = "04";
    public static final String TYPE_CONSOLEDUMP = "";


    //OAuth:
    public static final String CONSUMER_KEY = "6555db3a89a5462599265b2be993da83";
    public static final String CONSUMER_SECRET = "45068a7637404fc798a208d6c1f29e4f";
    public static final String RANDOM_NUMBER = "01234567";
    public static final String API_BASE_URL = "api.fitbit.com";
    public static final String WEB_BASE_URL = "https://www.fitbit.com";
    public static final String OAUTH_TOKEN_URL_PART = "/oauth/authorize?oauth_token=";
    public static final String OAUTH_CREDENTIALS_URL = "https://android-cdn-api.fitbit.com/1/user/-/devices/tracker/generateBtleClientAuthCredentials.json";
    public static final String X_APP_VERSION = "2183061";
    public static final String TRACKER_SYNC_URL = "https://android-cdn-client.fitbit.com/1/devices/client/tracker/data/sync.json";
    public static final String EMPTY_URL = "about:blank";


    //Memory:
    public static final String MEMORY_FLEX_START = "08000000";
    public static final String MEMORY_FLEX_BSL = "08000200";
    public static final String MEMORY_FLEX_APP = "0800a000";
    public static final String MEMORY_FLEX_APP_END = "08040000";
    public static final String MEMORY_FLEX_EEPROM = "08080000";
    public static final String MEMORY_FLEX_EEPROM_END = "08082000";
    public static final String MEMORY_FLEX_KEY = "08080020";
    public static final String MEMORY_FLEX_KEY_END = "08080030";
    public static final String MEMORY_FLEX_SRAM = "20000000";
    public static final String MEMORY_FLEX_SRAM_END = "20007fff";
    public static final String MEMORY_FLEX_CONSOLE = "200043DD";
    public static final String MEMORY_FLEX_CONSOLE_END = "2000444DD";


    //Information:
    public static final String INFORMATION_MICRODUMP = "Mirodump";
    public static final String INFORMATION_MEGADUMP = "Megadump";
    public static final String INFORMATION_CONSOLEDUMP = "Consoledump";
    public static final String INFORMATION_ALARM = "Alarm";
    public static final String INFORMATION_MEMORY = "Memory";


    //File names:
    public static final String FILE_AUTH_KEY = "fitbit_auth_key";
    public static final String FILE_ENC_KEY = "fitbit_enc_key";
    public static final String FILE_NONCE = "fitbit_nonce";
    public static final String FILE_ACCESS_TOKEN_KEY = "fitbit_access_token_key";
    public static final String FILE_ACCESS_TOKEN_SECRET = "fitbit_access_token_secret";
    public static final String FILE_VERIFIER = "fitbit_verifier";
    public static final String LAST_DEVICES = "lastBLEDevices";
    public static final String SETTING_DIRECTORY = "setting_directory";
    public static final String LAST_DEVICE = "lastDevice";


    //Text:
    public static final String RAW_OUTPUT = "raw output:";
    public static final String ADDITIONAL_INFO = "additional information:";
    public static final String ASK_AUTH_PIN = "Please fill in the PIN:";
    public static final String ASK_FIRMWARE_FLASH_FILE = "Firmware FLASH binary path:";
    public static final String ASK_FIRMWARE_FLASH_APP = "Enter 'app' to flash APP, otherwise BSL is flashed.";
    //public static final String ASK_FIRMWARE_LENGTH = "Please enter your custom length\n(no input will result in length calculation):";
    public static final String ASK_DIRECTORY = "Custom directory\n(no input will result in default directory):";
    public static final String ASK_ENC_KEY = "Encryption key (hex):";
    public static final String ASK_AUTH_KEY = "Authentication key (hex):";
    public static final String ASK_AUTH_NONCE = "Authentication nonce (int):";


    //Error codes:
    public static final Map<String, String> ERROR_CODES;

    static {
        Hashtable<String, String> temp = new Hashtable<>();
        temp.put("0000", "RF_ERR_SUCCESS");
        temp.put("0010", "RF_ERR_GENERAL_FAILURE");
        temp.put("0110", "RF_ERR_NOT_IMPLEMENTED");
        temp.put("0210", "RF_ERR_FATAL_ERROR");
        temp.put("0310", "RF_ERR_BUFFER_OVERFLOW");
        temp.put("0410", "RF_ERR_NOT_ENOUGH_DATA");
        temp.put("0510", "RF_ERR_INVALID_DATA");
        temp.put("0610", "RF_ERR_UNRECOGNIZED_COMMAND");
        temp.put("0710", "RF_ERR_WRITE_FAILURE");
        temp.put("0810", "RF_ERR_READ_FAILURE");
        temp.put("0910", "RF_ERR_INVALID_REQUEST");
        temp.put("0a10", "RF_ERR_INVALID_STATE");
        temp.put("0020", "RF_ERR_INVALID_REBOOT_REQ");
        temp.put("0120", "RF_ERR_BSL_MISSING_OR_INVALID");
        temp.put("0220", "RF_ERR_APP_MISSING_OR_INVALID");
        temp.put("0320", "RF_ERR_CANNOT_REBOOT_TO_BSL_FROM_BSL");
        temp.put("0420", "RF_ERR_CANNOT_REBOOT_TO_APP_FROM_APP");
        temp.put("0520", "RF_ERR_FIRMWARE_UPDATE_FAILED");
        temp.put("0620", "RF_ERR_RX_PACKET_NOT_HANDLED");
        temp.put("0720", "RF_ERR_INVALID_RING_ID");
        temp.put("0820", "RF_ERR_UNRECOGNIZED_SITE_COMMAND");
        temp.put("0920", "RF_ERR_BAD_HEADER_PROTOCOL_CODE");
        temp.put("0a20", "RF_ERR_BAD_HEADER_ENCRYPTION_CODE");
        temp.put("0b20", "RF_ERR_TRAILER_LENGTH_MISMATCH");
        temp.put("0c20", "RF_ERR_TRAILER_SIGNATURE_MISMATCH");
        temp.put("0d20", "RF_ERR_BAD_SECTION_HEADER_LENGTH");
        temp.put("0e20", "RF_ERR_INVALID_SECTION_DATA_TYPE");
        temp.put("0f20", "RF_ERR_TOO_MUCH_SECTION_DATA");
        temp.put("1020", "RF_ERR_SECTION_LENGTH_MISMATCH");
        temp.put("1120", "RF_ERR_SECTION_CRC_MISMATCH");
        temp.put("1220", "RF_ERR_INVALID_PRODUCT_ID");
        temp.put("1320", "RF_ERR_INVALID_SYNC_DELAY");
        temp.put("1420", "RF_ERR_MISSING_CRYPTO_KEY");
        temp.put("1520", "RF_ERR_AUTH_FAILED");
        temp.put("1620", "RF_ERR_AUTH_REQUIRED");
        temp.put("1720", "RF_ERR_CRYPTO_REQUIRED");
        ERROR_CODES = Collections.unmodifiableMap(temp);
    }

    //Device
    //public static final byte[] FITBIT_KEY = {
    //        (byte) 0xe5, (byte) 0x1f, (byte) 0x3e, (byte) 0x71, (byte) 0x36, (byte) 0x9c, (byte) 0x40, (byte) 0xb7,
    //        (byte) 0xa5, (byte) 0x25, (byte) 0xc9, (byte) 0x16, (byte) 0xeb, (byte) 0xe7, (byte) 0x51, (byte) 0x54};

    public static final byte[] FW_UPDATE_HEADER = {
            (byte) 0x30, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x31, (byte) 0x00, (byte) 0x00, (byte) 0x00}; //0x31

    /*public static final byte[] FW_UPDATE_DATA = {
            (byte)0x07,(byte)0x02,(byte)0xF0,(byte)0x9F,(byte)0x00,(byte)0x08,(byte)0x01,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x83,(byte)0x2c,(byte)0x00, //0x64, 0x5c
            (byte)0x07,(byte)0x02,(byte)0xF0,(byte)0xFF,(byte)0x00,(byte)0x08,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x07,(byte)0x02,(byte)0xF0,(byte)0xFF,(byte)0x01,(byte)0x08,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x07,(byte)0x02,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x07,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x7B,(byte)0x19,(byte)0x00,(byte)0x00,(byte)0x00};*/

    public static final byte[] FW_UPDATE_DATA = {
            (byte) 0x07, (byte) 0x02, (byte) 0xF0, (byte) 0xFF, (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x07, (byte) 0x02, (byte) 0xF0, (byte) 0xFF, (byte) 0x01, (byte) 0x08, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x07, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x07, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x7B, (byte) 0x19, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    public static final byte[] REBOOT_TO_BSL_HEADER = {
            (byte) 0x30, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00/*crypt*/, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x30, (byte) 0x9E, (byte) 0x00, (byte) 0x00}; //0x30, 0x9E

    public static final byte[] REBOOT_TO_BSL_DATA = {
            (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x07, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0xC5, (byte) 0x93, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x24, (byte) 0x00, (byte) 0x00};

    public static final byte[] REBOOT_TO_APP_HEADER = {
            (byte)0x30,(byte)0x02,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x30,(byte)0x9E,(byte)0x00,(byte)0x00};

    public static final byte[] REBOOT_TO_APP_DATA = {
            (byte)0x07,(byte)0x02,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x07,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x08,(byte)0x69,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            (byte)0x24,(byte)0x00,(byte)0x00};
}