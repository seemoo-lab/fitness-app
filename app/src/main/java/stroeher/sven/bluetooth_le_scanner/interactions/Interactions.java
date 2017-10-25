package stroeher.sven.bluetooth_le_scanner.interactions;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import stroeher.sven.bluetooth_le_scanner.R;
import stroeher.sven.bluetooth_le_scanner.activities.WorkActivity;
import stroeher.sven.bluetooth_le_scanner.https.AuthValues;
import stroeher.sven.bluetooth_le_scanner.information.InformationList;
import stroeher.sven.bluetooth_le_scanner.miscellaneous.ButtonHandler;
import stroeher.sven.bluetooth_le_scanner.commands.Commands;
import stroeher.sven.bluetooth_le_scanner.miscellaneous.ConstantValues;
import stroeher.sven.bluetooth_le_scanner.miscellaneous.Utilities;
import stroeher.sven.bluetooth_le_scanner.tasks.Tasks;

/**
 * Use this class to deal with Bluetooth interactions.
 */
public class Interactions {

    private final String TAG = this.getClass().getSimpleName();

    private WorkActivity activity;
    private Toast toast;
    private Commands commands;
    private BluetoothInteractionQueue mBluetoothInteractionQueue;

    private boolean authenticated = false;
    private boolean liveModeActive = false;

    private String currentInteraction;
    private Tasks tasks = null;

    /**
     * Creates an intance of interactions.
     *
     * @param activity      The current activity.
     * @param toast         The toast. to send messages to the user.
     * @param commands      The instance to commands.
     * @param buttonHandler The instance of the button handler.
     */
    public Interactions(WorkActivity activity, Toast toast, Commands commands, ButtonHandler buttonHandler) {
        this.activity = activity;
        this.toast = toast;
        this.commands = commands;
        mBluetoothInteractionQueue = new BluetoothInteractionQueue(buttonHandler, this, activity, toast);
    }

    /**
     * Finishes the current interaction.
     *
     * @return The data returned by the interactions finish method.
     */
    public Object interactionFinished() {
        Object result = null;
        if (mBluetoothInteractionQueue.getFirstBluetoothInteraction() != null) {
            Log.e(TAG, "Interaction finished: " + mBluetoothInteractionQueue.getFirstBluetoothInteraction().TAG);
            result = mBluetoothInteractionQueue.getFirstBluetoothInteraction().finish();
            mBluetoothInteractionQueue.interactionFinished();
            if (tasks == null) {
                tasks = activity.getTasks();
            }
            if (getCurrentInteraction().equals(tasks.getCurrentInteractionsTaskName())) {
                tasks.taskFinished();
            }
        } else {
            Log.e(TAG, "Interaction finished: null");
        }
        return result;
    }

    /**
     * Executes the first interaction in interaction queue.
     *
     * @param value
     * @return The interact result of the first instruction in queue. Null if there is no first instruction.
     */
    public InformationList interact(byte[] value) {
        if (mBluetoothInteractionQueue.getFirstBluetoothInteraction() != null) {
            return mBluetoothInteractionQueue.getFirstBluetoothInteraction().interact(value);
        } else {
            return null;
        }
    }

    /**
     * Checks, if the current interaction is finished.
     *
     * @return True, if there is no current interaction or it is finished.
     */
    public boolean isFinished() {
        return mBluetoothInteractionQueue.getFirstBluetoothInteraction() == null || mBluetoothInteractionQueue.getFirstBluetoothInteraction().isFinished();
    }

    /**
     * Returns, whether live mode is currently active.
     *
     * @return True, if the live mode is active.
     */
    public boolean liveModeActive() {
        return liveModeActive;
    }

    /**
     * Sets the value of authenticated.
     *
     * @param value The value to set authenticated to.
     */
    void setAuthenticated(boolean value) {
        authenticated = value;
    }

    /**
     * Returns, whether app is already authenticated to the device.
     *
     * @return True, if the app is already authenticated.
     */
    public boolean getAuthenticated() {
        return authenticated;
    }

    /**
     * Sets live mode active to the given value.
     *
     * @param value The value to set live mode active to.
     */
    void setLiveModeActive(boolean value) {
        liveModeActive = value;
    }

    /**
     * Sets the current interaction name to the given value.
     *
     * @param interaction The name to set the current interaction name to.
     */
    void setCurrentInteraction(String interaction) {
        currentInteraction = interaction;
    }

    /**
     * Returns the current interaction name.
     *
     * @return The current interaction name.
     */
    public String getCurrentInteraction() {
        return currentInteraction;
    }

    /**
     * <===============================================================================================================>
     * <=================================================> Interactions: <=============================================>
     * <===============================================================================================================>
     */

    //Always put an EmptyInteraction at last, to check if the last regular interaction in the interaction queue is working correctly.

    /**
     * Sets the instructions in the instruction queue, to establish an airlink with the device.
     */
    public void intEstablishAirlink() {
        mBluetoothInteractionQueue.addInteraction(new AirlinkInteraction(commands));
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }

    /**
     * Sets the instructions in the instruction queue, to get a microdump from the device.
     */
    public void intMicrodump() {
        intEstablishAirlink();
        mBluetoothInteractionQueue.addInteraction(new DumpInteraction(activity, toast, commands, 0));
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }

    /**
     * Sets the instructions in the instruction queue, to get a megadump from the device.
     */
    public void intMegadump() {
        intEstablishAirlink();
        mBluetoothInteractionQueue.addInteraction(new DumpInteraction(activity, toast, commands, 1));
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }

    /**
     * Sets the instructions in the instruction queue, to get the alarms from the device. If the device name is 'Alta', this instruction gets not supported.
     */
    public void intGetAlarm() {
        if (commands.getmBluetoothGatt().getDevice().getAddress().equals(activity.getString(R.string.alta))) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast.setText("GetAlarm is not supported by this device!");
                    toast.show();
                }
            });
            Log.e(TAG, "GetAlarm is not supported by this device!");
        } else {
            intEstablishAirlink();
            mBluetoothInteractionQueue.addInteraction(new DumpInteraction(activity, toast, commands, 2));
            mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
        }
    }

    /**
     * Sets the instructions in the instruction queue, to readout a part of the memory.
     *
     * @param addressBegin The start address of the memory part.
     * @param addressEnd   The end address of the memory part
     * @param memoryName   The name of the memory part. (Needed for later identification)
     */
    public void intReadOutMemory(String addressBegin, String addressEnd, String memoryName) {
        intEstablishAirlink();
        mBluetoothInteractionQueue.addInteraction(new DumpInteraction(activity, toast, commands, 3, addressBegin, addressEnd, memoryName));
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }

    /**
     * Sets the instructions in the instruction queue, to set the alarms of the device.
     *
     * @param position        The position of the alarm in the alarm list.
     * @param informationList The alarms.
     */
    public void intSetAlarm(int position, InformationList informationList) {
        if (commands.getmBluetoothGatt().getDevice().getAddress().equals(activity.getString(R.string.alta))) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast.setText("SetAlarm is not supported by this device!");
                    toast.show();
                }
            });
            Log.e(TAG, "SetAlarm is not supported by this device!");
        } else {
            intEstablishAirlink();
            mBluetoothInteractionQueue.addInteraction(new UploadInteraction(activity, toast, commands, this, position, informationList));
            mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
        }
    }

    /**
     * Sets the instructions in the instruction queue, to clear the alarm lsit, which means to set all alarms to empty alarms.
     */
    public void intClearAlarms() {
        intEstablishAirlink();
        mBluetoothInteractionQueue.addInteraction(new UploadInteraction(activity, toast, commands, this, -1, new InformationList("")));
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }

    /**
     * Sets the instructions in the instruction queue, to upload a firmware onto the device.
     *
     * @param data         The data of the firmware to upload.
     * @param customLength The length of the data to upload. If it is set to a negative value, the length gets calculated.
     */
    public void intUploadFirmwareInteraction(String data, int customLength) {
        intEstablishAirlink();
        mBluetoothInteractionQueue.addInteraction(new UploadInteraction(activity, toast, commands, this, data, customLength));
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }

    /**
     * Sets the instructions in the instruction queue, to upload a microdump to the device.
     *
     * @param data The data of the alarm to upload.
     */
    public void intUploadMicroDumpInteraction(String data) {
        intEstablishAirlink();
        mBluetoothInteractionQueue.addInteraction(new UploadInteraction(activity, toast, commands, 1, data));
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }

    /**
     * Sets the instructions in the instruction queue, to upload a megadump to the device.
     *
     * @param data The data of the megadump to upload.
     */
    public void intUploadMegadumpInteraction(String data) {
        intEstablishAirlink();
        mBluetoothInteractionQueue.addInteraction(new UploadInteraction(activity, toast, commands, 2, data));
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }

    /**
     * Sets the instructions in the instruction queue, to authenticate to the device. Only, if the device is not already authenticated.
     * For this procedure, the serial number of the device is needed. If it is not set yet, it fetches a microdump from the device, to get it.
     */
    public void intAuthentication() {
        intEstablishAirlink();
        if (AuthValues.SERIAL_NUMBER == null) {
            mBluetoothInteractionQueue.addInteraction(new DumpInteraction(activity, toast, commands, 0));
        }
        if (!authenticated) {
            mBluetoothInteractionQueue.addInteraction(new AuthenticationInteraction(activity, toast, commands, this));
            if (AuthValues.NONCE == null) {
                mBluetoothInteractionQueue.addInteraction(new AuthenticationInteraction(activity, toast, commands, this));
            }
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast.setText("Already authenticated.");
                    toast.show();
                }
            });
            Log.e(TAG, "Already authenticated.");
        }
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }

    /**
     * Sets the instructions in the instruction queue, to switch to live mode.
     *
     * @param buttonHandler The instance of the button handler.
     * @param buttonID      The button ID of the live mode enter/exit button.
     */
    public void intLiveModeEnable(ButtonHandler buttonHandler, int buttonID) {
        mBluetoothInteractionQueue.addInteraction(new LiveModeInteraction(activity, commands, this, buttonHandler, buttonID));
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }

    /**
     * Sets the instructions in the instruction queue, to quit live mode.
     *
     * @param buttonHandler The instance of the button handler.
     * @param buttonID      The button ID of the live mode enter/exit button.
     */
    public void intLiveModeDisable(ButtonHandler buttonHandler, int buttonID) {
        interactionFinished();
        buttonHandler.setText("Live Mode", buttonID);
        liveModeActive = false;
    }

    /**
     * Sets the instructions in the instruction queue, to set the date of the device.
     */
    public void intSetDate() {
        intEstablishAirlink();
        mBluetoothInteractionQueue.addInteraction(new SetDateInteraction(activity, toast, commands));
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }

    /**
     * Sets the instructions in the instruction queue, to an empty interaction, which does nothing.
     */
    public void intEmptyInteraction(){
        mBluetoothInteractionQueue.addInteraction(new EmptyInteraction(this));
    }
}
