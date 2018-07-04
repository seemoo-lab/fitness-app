package seemoo.fitbit.interactions;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TimePicker;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;

import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.commands.Commands;
import seemoo.fitbit.events.TransferProgressEvent;
import seemoo.fitbit.information.Alarm;
import seemoo.fitbit.information.InformationList;
import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.Utilities;
import seemoo.fitbit.miscellaneous.Encoding;

/**
 * Uploads a data to the device.
 */
class UploadInteraction extends BluetoothInteraction {

    private WorkActivity activity;
    private Toast toast;
    private Commands commands;
    private String dataIn;
    private int type;
    private int answer;
    private boolean transmissionActive = false;
    private boolean failure = true;
    private ArrayList<String> sendingData = new ArrayList<>();
    private String typeCode;
    private InformationList alarms;
    private Interactions interactions;

    private int secondsAfterMidnight = 0;
    private int dayFlags = 0;
    private int position;
    private int customLength = -1;
    private int chunkNumber = 0;


    /**
     * Creates an instance of upload interaction for micro- and megadump uploads.
     *
     * @param activity The current activity.
     * @param toast    The toast, to send messages to the user.
     * @param commands The instance of commands.
     * @param type     The type of the upload. (1 = microdump, 2 = megadump)
     * @param dataIn   The data of the upload.
     */
    UploadInteraction(WorkActivity activity, Toast toast, Commands commands, int type, String dataIn) { //for micro-/megadumps
        this.activity = activity;
        this.toast = toast;
        this.commands = commands;
        this.dataIn = dataIn;
        this.type = type;
    }

    /**
     * Creates an instance of upload interaction for firmware uploads.
     *
     * @param activity     The current activity.
     * @param toast        The toast, to send messages to the user.
     * @param commands     The instance of commands.
     * @param interactions The instance of interactions.
     * @param dataIn       The data of the upload.
     * @param customLength The length of the data.
     */
    UploadInteraction(WorkActivity activity, Toast toast, Commands commands, Interactions interactions, String dataIn, int customLength) { //for firmware
        this.activity = activity;
        this.toast = toast;
        this.commands = commands;
        this.interactions = interactions;
        this.dataIn = dataIn;
        type = 0;
        this.customLength = customLength;
    }

    /**
     * Creates an instance of upload interaction for alarm uploads.
     *
     * @param activity     The current activity.
     * @param toast        The toast, to send messages to the user.
     * @param commands     The instance of commands.
     * @param interactions The instance of interactions.
     * @param position     The position of the alarm in the alarm list to upload.
     * @param alarms       The alarms to upload.
     */
    UploadInteraction(WorkActivity activity, Toast toast, Commands commands, Interactions interactions, int position, InformationList alarms) { //for alarms
        this.activity = activity;
        this.toast = toast;
        this.commands = commands;
        this.position = position;
        type = 3;
        this.alarms = alarms;
        this.interactions = interactions;
    }

    /**
     * {@inheritDoc}
     *
     * @return True, if the transmission to the device is finished.
     */
    @Override
    boolean isFinished() {
        return !transmissionActive;
    }

    /**
     * {@inheritDoc}
     * Enables notifications and starts the upload process corresponding to the upload type.
     *
     * @return True, if there is data to upload.
     */
    @Override
    boolean execute() {
        commands.comEnableNotifications1();
        switch (type) {
            case 0: //firmware
                typeCode = ConstantValues.TYPE_FIRMWARE;
                if (dataIn == null) {
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            toast.setText("Error: No data to upload.");
                            toast.show();
                        }
                    });
                    failure = true;
                    Log.e(TAG, "Error: No data to upload.!");
                    return false;
                }
                String hex = Utilities.removeSpaces(dataIn);
                sendingData.addAll(Encoding.slip(hex.toLowerCase()));
                commands.comUploadInitialize(createExtra(hex));
                break;
            case 1: //microdump
                typeCode = ConstantValues.TYPE_MICRODUMP_UPLOAD;
                hex = Utilities.base64ToHex(dataIn);
                sendingData.addAll(Encoding.slip(hex));
                commands.comUploadInitialize(createExtra(hex));
                break;
            case 2: //megadump
                typeCode = ConstantValues.TYPE_MEGADUMP_UPLOAD;
                hex = Utilities.base64ToHex(dataIn);
                sendingData.addAll(Encoding.slip(hex));
                commands.comUploadInitialize(createExtra(hex));
                break;
            case 3: //alarms
                typeCode = ConstantValues.TYPE_ALARMS;
                if (position >= 0) {
                    selectDay();
                    setTimer(-1);
                } else {
                    while (alarms.size() < 8) {
                        alarms.add(new Alarm(ConstantValues.EMPTY_ALARM));
                    }
                    String data = createData();
                    sendingData.addAll(Encoding.slip(data));
                    commands.comUploadInitialize(createExtra(data));
                }
                break;
            default:
                Log.e(TAG, "Error: Wrong upload type!");
        }
        transmissionActive = true;
        answer = Utilities.hexStringToInt(typeCode) + 16;
        return true;
    }

    /**
     * {@inheritDoc}
     * Uploads 20 byte parts of data to the device until all data is sent, so the transmission is finished and positive or negative acknowledged by the device.
     *
     * @param value The received data.
     * @return Null.
     */
    @Override
    InformationList interact(byte[] value) {
        String result = Utilities.byteArrayToHexString(value);
        String strAnswer = "";
        String strAnwserFull = Utilities.intToHexString(answer);

        if(strAnwserFull.length() > 2) {
            strAnswer = strAnwserFull.substring(strAnwserFull.length()-2);
        } else {
            strAnswer = strAnwserFull;
        }


        if (result.equals(ConstantValues.ACKNOWLEDGEMENT)) { //transmission correctly finished
            transmissionActive = false;
            failure = false;
        } else if (result.length() >= 4 && result.substring(0, 4).equals(ConstantValues.NEG_ACKNOWLEDGEMENT)) { //transmission aborted

            if (result.length() >= 4 && result.substring(4, 4).equals("0420")){
                failure = false;
            } else {
                Log.e(TAG, "Error: " + Utilities.getError(result));
                transmissionActive = false;
                failure = true;
            }
        } else if (sendingData.size() == 0) { //data transmission finished -> sending ACKNOWLEDGEMENT
            commands.comAcknowledgement();
            TransferProgressEvent dumpProgEvent = new TransferProgressEvent();
            dumpProgEvent.setTransferState(true);
            dumpProgEvent.setTotalSize(0);
            EventBus.getDefault().post(dumpProgEvent);
        } else if (result.length() >= 10 && result.substring(0, 10).equals(ConstantValues.UPLOAD_RESPONSE + typeCode + "0000")) { //sending first part of data
            commands.comUploadData(sendingData.get(0));
            sendingData.remove(0);
            TransferProgressEvent dumpProgEvent = new TransferProgressEvent();
            dumpProgEvent.setTransferState(true);
            dumpProgEvent.setTotalSize(sendingData.size() * 5);
            EventBus.getDefault().post(dumpProgEvent);
        } else if (result.equals(ConstantValues.UPLOAD_SECOND_RESPONSE + strAnswer + "0000")) { //sending all other parts of data
            EventBus.getDefault().post(new TransferProgressEvent(value.length));
            commands.comUploadData(sendingData.get(0));
            sendingData.remove(0);
            answer = answer + 16;
        }

        chunkNumber++;
        Log.e(TAG, "ChunkNr: " + chunkNumber);

        return null;
    }

    /**
     * {@inheritDoc}
     * Disables notifications and shows the result of the upload.
     *
     * @return Null.
     */
    @Override
    InformationList finish() {
        if (!transmissionActive && !failure) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toast.setText("Upload to device successful.");
                    toast.show();
                }
            });
            Log.e(TAG, "Upload to device successful.");
        } else {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toast.setText("Error: Upload to device unsuccessful."); //FIXME also appears for successful firmware update
                    toast.show();
                }
            });
            Log.e(TAG, "Error: Upload to device unsuccessful.");
        }
        commands.comDisableNotifications1();
        return null;
    }

    /**
     * Creates the extra string, which has to be added to the initial upload byte sequence. It consists of the upload type, its length and its checksum.
     *
     * @return The extra string.
     */
    private String createExtra(String data) {
        String result = typeCode;
        if (customLength > 0) {
            result = result + Utilities.rotateBytes(Utilities.fixLength(Utilities.intToHexString(customLength), 8));
        } else {
            result = result + Utilities.rotateBytes(Utilities.fixLength(Utilities.intToHexString(data.length() / 2), 8));
        }
        result = result + Utilities.rotateBytes(Encoding.crc(data));
        result = result + Utilities.intToHexString(16);
        return result;
    }

    /*================================================================================================*/
    /*========================================== setAlarms: ==========================================*/
    /*================================================================================================*/

    /**
     * Creates the data string, which contains the ALARMS.
     *
     * @return The data string.
     */
    private String createData() {
        String result = createPreString();
        for (int i = 0; i < alarms.size(); i++) {
            result = result + ((Alarm) alarms.get(i)).getRawOutput();
        }
        result = result + createPostString(result);
        return result;
    }

    /**
     * Creates the pre string, which has to be added in front of the ALARMS.
     *
     * @return The pre string.
     */
    private String createPreString() {
        String result = ConstantValues.ALARM_BEGINNING;
        result = result + Utilities.rotateBytes(Utilities.fixLength(Utilities.intToHexString(8), 8));
        return result;
    }

    /**
     * Creates the post string, which has to be added after the ALARMS.
     *
     * @param data The data string without the post string.
     * @return The post string.
     */
    private String createPostString(String data) {
        String result = Utilities.rotateBytes(Utilities.fixLength(Encoding.crc(data.substring(20)), 16));
        result = result + Utilities.rotateBytes(Utilities.fixLength(Utilities.intToHexString(data.substring(20).length() / 2), 6));
        return result;
    }

    /**
     * Lets the user select day of the alarm to override.
     */
    private void selectDay() {
        dayFlags = 0;
        final String[] days = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setCancelable(false);
                builder.setTitle("Which day(s):");
                boolean[] checkedItems = ((Alarm) alarms.get(position)).getDays();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        dayFlags = dayFlags + (int) Math.pow(2, i); //exponential
                    }
                }
                builder.setMultiChoiceItems(days, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            dayFlags = dayFlags + (int) Math.pow(2, which);
                        } else {
                            dayFlags = dayFlags - (int) Math.pow(2, which);
                        }
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dayFlags != 0) {
                            selectTime();
                        } else {
                            alarms.set(position, new Alarm(ConstantValues.EMPTY_ALARM));
                            String data = createData();
                            sendingData.addAll(Encoding.slip(data));
                            commands.comUploadInitialize(createExtra(data));
                            interactions.intGetAlarm();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    /**
     * Lets the user select time of the alarm to override.
     */
    private void selectTime() {
        final int hours;
        final int minutes;
        if (((Alarm) alarms.get(position)).isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            hours = calendar.get(Calendar.HOUR_OF_DAY);
            minutes = calendar.get(Calendar.MINUTE);
        } else {
            hours = ((Alarm) alarms.get(position)).getHours();
            minutes = ((Alarm) alarms.get(position)).getMinutes();
        }
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                TimePickerDialog mTimePickerDialog = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        secondsAfterMidnight = hour * 3600 + minute * 60;
                        selectRepeat();
                    }
                }, hours, minutes, true);
                mTimePickerDialog.setTitle("Select time:");
                mTimePickerDialog.setButton(TimePickerDialog.BUTTON_NEGATIVE, "", mTimePickerDialog);
                mTimePickerDialog.setCancelable(false);
                mTimePickerDialog.show();
            }
        });
    }

    /**
     * Lets the user select if the alarm shall be repeated.
     */
    private void selectRepeat() {
        final String[] repeat = new String[]{"Yes", "No"};
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setCancelable(false);
                builder.setTitle("Repeat the alarm:");
                builder.setItems(repeat, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            dayFlags = dayFlags + 128; //128 = 2^6 = seventh bit
                        }
                        alarms.set(position, new Alarm(createAlarm()));
                        String data = createData();
                        sendingData.addAll(Encoding.slip(data));
                        commands.comUploadInitialize(createExtra(data));
                        interactions.intGetAlarm();

                    }
                });
                builder.show();
            }
        });
    }


    /**
     * Creates an alarm out of the users selection.
     *
     * @return The created alarm.
     */
    private String createAlarm() {
        String result = Utilities.rotateBytes(Utilities.fixLength(Utilities.intToHexString(secondsAfterMidnight), 8));
        result = result + ConstantValues.ALARM_FILLER_1;
        result = result + Utilities.fixLength(Utilities.intToHexString(dayFlags), 2);
        result = result + ConstantValues.ALARM_FILLER_2;
        int alarmIndex = activity.getAlarmIndexAndIncrement();
        result = result + Utilities.fixLength(Utilities.intToHexString(alarmIndex), 2);
        return result;
    }
}
