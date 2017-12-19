package stroeher.sven.bluetooth_le_scanner.interactions;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import stroeher.sven.bluetooth_le_scanner.activities.WorkActivity;
import stroeher.sven.bluetooth_le_scanner.commands.Commands;
import stroeher.sven.bluetooth_le_scanner.https.AuthValues;
import stroeher.sven.bluetooth_le_scanner.information.Alarm;
import stroeher.sven.bluetooth_le_scanner.information.Information;
import stroeher.sven.bluetooth_le_scanner.information.InformationList;
import stroeher.sven.bluetooth_le_scanner.miscellaneous.ConstantValues;
import stroeher.sven.bluetooth_le_scanner.miscellaneous.InternalStorage;
import stroeher.sven.bluetooth_le_scanner.miscellaneous.Utilities;

/**
 * Gets a dump from the device.
 */
class DumpInteraction extends BluetoothInteraction {

    private WorkActivity activity;
    private Toast toast;
    private Commands commands;
    private int dumpType;
    private String address = null;
    private String length = null;
    private String memoryName;
    private String name = "";
    //inidcates that a dump transmission is currently active
    private boolean transmissionActive = false;
    private String data = "";
    private InformationList dataList = new InformationList("");
    private String begin = ConstantValues.DUMP_BEGIN;
    private String end = ConstantValues.DUMP_END;

    /**
     * Creates a dump interaction for a micro- / megadumps and alarms.
     *
     * @param activity The current activity.
     * @param toast    The toast, to send messages to the user.
     * @param commands The instance of commands.
     * @param dumpType The dump type. (0 = microdump, 1 = megadump, 2 = alarms, 3 = memory)
     */
    DumpInteraction(WorkActivity activity, Toast toast, Commands commands, int dumpType) {
        this.activity = activity;
        this.toast = toast;
        this.commands = commands;
        this.dumpType = dumpType;
    }

    /**
     * Creates a dump interaction for a memory part.
     *
     * @param activity     The current activity.
     * @param toast        The toast, to send messages to the user.
     * @param commands     The instance of commands.
     * @param dumpType     The dump type. (0 = microdump, 1 = megadump, 2 = alarms, 3 = memory)
     * @param addressBegin The start address of the memory part.
     * @param addressEnd   The end address of the memory part.
     * @param memoryName   The name of the memory part. (Needed for later identification)
     */
    DumpInteraction(Activity activity, Toast toast, Commands commands, int dumpType, String addressBegin, String addressEnd, String memoryName) {
        this.activity = (WorkActivity) activity;
        this.toast = toast;
        this.commands = commands;
        this.dumpType = dumpType;
        this.address = addressBegin;
        this.memoryName = memoryName;
        length = Utilities.intToHexString(Utilities.hexStringToInt(addressEnd) - Utilities.hexStringToInt(addressBegin));
    }

    /**
     * Checks if information collection for dump is finished.
     *
     * @return True, if dump is finished.
     */
    boolean isFinished() {
        if (data.length() != 0 && !transmissionActive) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toast.setText(TAG + " successful.");
                    toast.show();
                }
            });
            Log.e(TAG, TAG + " successful.");
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * Enables notifications and sends a dump command to the device.
     *
     * @return True, if the dump type was correct.
     */
    @Override
    boolean execute() {
        commands.comEnableNotifications1();
        switch (dumpType) {
            case 0: //Microdump
                commands.comGetMicrodump();
                begin = begin + ConstantValues.TYPE_MICRODUMP;
                end = end + ConstantValues.TYPE_MICRODUMP;
                name = ConstantValues.INFORMATION_MICRODUMP;
                break;
            case 1: //Megadump
                setTimer(60000);
                commands.comGetMegadump();
                begin = begin + ConstantValues.TYPE_MEGADUMP;
                end = end + ConstantValues.TYPE_MEGADUMP;
                name = ConstantValues.INFORMATION_MEGADUMP;
                break;
            case 2: //Alarms
                setTimer(10000);
                commands.comGetAlarms();
                begin = begin + ConstantValues.TYPE_ALARMS;
                end = end + ConstantValues.TYPE_ALARMS;
                name = ConstantValues.INFORMATION_ALARM;
                break;
            case 3: //Memory
                setTimer(Utilities.hexStringToInt(length) * 25);
                commands.comReadoutMemory(address, length);
                begin = begin + ConstantValues.TYPE_MEMORY;
                end = end + ConstantValues.TYPE_MEMORY;
                name = ConstantValues.INFORMATION_MEMORY + "_" + memoryName;
                break;
            case 4: //ConsoleDump
                setTimer(Utilities.hexStringToInt(length) * 25);
                commands.comReadoutMemory(address, length);
                begin = begin + ConstantValues.TYPE_MEMORY;
                end = end + ConstantValues.TYPE_MEMORY;
                name = ConstantValues.INFORMATION_MEMORY + "_" + memoryName;
            default:
                Log.e(TAG, "Error: Wrong dump type!");
                return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * Collects receiving data of the dump.
     *
     * @param value The received data.
     * @return Null.
     */
    @Override
    InformationList interact(byte[] value) {
        String temp = Utilities.byteArrayToHexString(value);
        if (temp.length() >= 6 && temp.substring(0, 6).equals(end)) {
            transmissionActive = false;
        }
        if (transmissionActive) {
            data = data + temp;
        }
        if (!transmissionActive && temp.equals(begin)) {
            transmissionActive = true;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * Returns the collected data and gets a readable translation for unencrypted parts.
     *
     * @return The collected data.
     */
    @Override
    InformationList finish() {
        InformationList result = new InformationList(name);
        if (!transmissionActive && data.length() > 0) {
            Log.e(TAG, name + " successful.");
            dataList.addAll(dataCut(data));
            if (dumpType != 3) {
                result.addAll(readOut());
                result.add(new Information(""));
                result.add(new Information(ConstantValues.RAW_OUTPUT));
                result.addAll(dataList);
            } else {
                result.addAll(dataList);
            }
        } else {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toast.setText(name + " failed.");
                    toast.show();
                }
            });
            dataList = null;
            Log.e(TAG, name + " failed.");
        }
        commands.comDisableNotifications1();
        return result;
    }

    /**
     * Checks if dump is encrypted.
     *
     * @return True, if dump is encrypted. False if alarm or memory dump.
     */
    private boolean encrypted() {
        if (dumpType == 0 || dumpType == 1) {
            return !dataList.get(0).toString().substring(8, 12).equals("0000");
        }
        return false;
    }

    /**
     * Reads out the information from a micro- or mega dump.
     *
     * @return The information or null if it is an alarm or memory dump.
     */
    private InformationList readOut() {
        InformationList result = new InformationList("");
        String temp = "";
        if (dumpType == 0 || dumpType == 1) { //Microdump || Megadump
            String model;
            String type;
            String id;
            temp = dataList.get(0).toString().substring(0, 8);
            switch (temp.substring(0, 2)) {
                case "2c":
                    model = "Megadump";
                    break;
                case "30":
                    model = "Microdump";
                    break;
                default:
                    model = temp.substring(0, 2);
            }
            switch (temp.substring(2, 4)) {
                case "02":
                    type = "Tracker";
                    break;
                default:
                    type = temp.substring(2, 4);
            }
            temp = dataList.get(0).toString().substring(30, 32);
            switch (temp.substring(0, 2)) {
                case "01":
                    id = "Galileo";
                    break;
                case "05":
                    id = "One";
                    break;
                case "07":
                    id = "Flex";
                    break;
                case "08":
                    id = "Charge";
                    break;
                case "12":
                    id = "Charge HR";
                    break;
                case "15":
                    id = "Alta";
                    break;
                default:
                    id = temp;
            }
            result.add(new Information("SiteProto: " + model + ", " + type));
            result.add(new Information("Encrypted: " + encrypted()));
            result.add(new Information("Nonce: " + Utilities.rotateBytes(dataList.get(0).toString().substring(12, 20))));
            String productCode = dataList.get(0).toString().substring(20, 30);
            result.add(new Information("Product Code: " + Utilities.rotateBytes(productCode)));
            AuthValues.setSerialNumber(productCode + temp.substring(0, 2));
            if (AuthValues.NONCE == null) {
                InternalStorage.loadAuthFiles(activity);
            }
            result.add(new Information("ID: " + id));
            if (!encrypted()) {
                result.add(new Information("Version: " + Utilities.rotateBytes(dataList.get(0).toString().substring(16, 20))));
            }
            temp = dataList.get(dataList.size() - 2).toString() + dataList.get(dataList.size() - 1).toString();
            result.add(new Information("Length: " + Utilities.hexStringToInt(Utilities.rotateBytes(temp.substring(temp.length() - 6, temp.length()))) + " byte"));
        } else { //Alarms
            ArrayList<Information> input = new ArrayList<>();
            input.addAll(dataList.getList());
            for (int i = 0; i < 11; i++) {
                temp = temp + input.get(i);
            }
            setAlarmIndex(temp);
            result.add(new Information("Alarms:"));
            for (int i = 0; i < 8; i++) {
                result.add(new Alarm(temp.substring(28 + i * 48, 28 + (i + 1) * 48)));
            }
            result.add(new Information(""));
            result.add(new Information(ConstantValues.ADDITIONAL_INFO));
            result.add(new Information("Number of Alarms: " + Utilities.hexStringToInt(Utilities.rotateBytes(input.get(0).toString().substring(20, 24)))));
            result.add(new Information("CRC_CCITT: " + Utilities.rotateBytes(temp.substring(412, 416))));
            result.add(new Information("Length: " + Utilities.hexStringToInt(Utilities.rotateBytes(temp.substring(428, 434))) + " byte"));
        }
        return result;
    }

    /**
     * Sets the alarm index in WorkActivity to the currently highest alarm index of the device plus one.
     *
     * @param input The hole data concatenated to a string.
     */
    private void setAlarmIndex(String input) {
        int highestValue = -1;
        if (activity != null) {
            for (int i = 0; i < 8; i++) {
                if (!input.substring(28 + i * 48, 28 + i * 48 + 48).equals(ConstantValues.EMPTY_ALARM)) {
                    int temp = Utilities.hexStringToInt(Utilities.rotateBytes(input.substring(28 + i * 48 + 46, 28 + i * 48 + 48)));
                    if (highestValue < temp) {
                        highestValue = temp;
                    }
                }
            }
            activity.setAlarmIndex(highestValue + 1);
        }
    }

    /**
     * Decrypts the eSLIP encrypted input and cuts it into 20 byte parts. Returns the result as information list.
     *
     * @param input The input data as a string.
     * @return An information list with decrypted input.
     */
    private InformationList dataCut(String input) {
        InformationList result = new InformationList("");
        String temp = "";
        int positionEncode = 0;
        while (positionEncode < input.length()) { //encoding of first two byte in line
            if (positionEncode + 40 < input.length()) { // line is 20 byte long
                if (input.substring(positionEncode, positionEncode + 4).equals("dbdc")) {
                    temp = temp + "c0" + input.substring(positionEncode + 4, positionEncode + 40);
                } else if (input.substring(positionEncode, positionEncode + 4).equals("dbdd")) {
                    temp = temp + "db" + input.substring(positionEncode + 4, positionEncode + 40);
                } else {
                    temp = temp + input.substring(positionEncode, positionEncode + 40);
                }
                positionEncode = positionEncode + 40;
            } else if (positionEncode + 4 < input.length()) { // line is between two and 20 byte long
                if (input.substring(positionEncode, positionEncode + 4).equals("dbdc")) {
                    temp = temp + "c0" + input.substring(positionEncode + 4, input.length());
                } else if (input.substring(positionEncode, positionEncode + 4).equals("dbdd")) {
                    temp = temp + "db" + input.substring(positionEncode + 4, input.length());
                } else {
                    temp = temp + input.substring(positionEncode, input.length());
                }
                break;
            } else { // line is shorter than two byte
                temp = temp + input.substring(positionEncode, input.length());
                break;
            }
        }
        int position = 0;
        while (position < temp.length()) { //cut in 20 byte parts
            if (position + 40 < temp.length()) {
                result.add(new Information(temp.substring(position, position + 40)));
                position = position + 40;
            } else {
                result.add(new Information(temp.substring(position, temp.length())));
                break;
            }
        }
        return result;
    }
}