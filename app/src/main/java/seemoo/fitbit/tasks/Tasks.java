package seemoo.fitbit.tasks;


import android.bluetooth.BluetoothDevice;

import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.https.HttpsClient;
import seemoo.fitbit.interactions.Interactions;
import seemoo.fitbit.miscellaneous.ButtonHandler;
import seemoo.fitbit.miscellaneous.ConstantValues;

/**
 * Use this class to deal with tasks.
 */
public class Tasks {

    private final String TAG = this.getClass().getSimpleName();

    private Interactions interactions;
    private WorkActivity activity;
    private TaskQueue mTaskQueue;

    /**
     * Creates an instance of tasks.
     *
     * @param interactions  The instance of interactions
     * @param activity      The current activity.
     * @param buttonHandler The isntance of the button handler.
     */
    public Tasks(Interactions interactions, WorkActivity activity, ButtonHandler buttonHandler) {
        this.interactions = interactions;
        this.activity = activity;
        mTaskQueue = new TaskQueue(buttonHandler);
    }

    /**
     * Finishes the current task.
     */
    public void taskFinished() {
        mTaskQueue.taskFinished();
    }

    /**
     * Returns the name of the current interaction task.
     *
     * @return The name of the current interaction task. Else null.
     */
    public String getCurrentInteractionsTaskName() {
        if (mTaskQueue.getFirstTask() != null && mTaskQueue.getFirstTask().TAG.equals("InteractionsTask")) {
            return ((InteractionsTask) mTaskQueue.getFirstTask()).getInteractionName() + "Interaction";
        } else {
            return null;
        }
    }

    /**
     * Clears the task queue.
     */
    public void clearList() {
        mTaskQueue.clear();
    }


    /**
     * <===============================================================================================================>
     * <====================================================> Tasks: <=================================================>
     * <===============================================================================================================>
     */

    //Always put an EmptyTask at last, to check if the last regular task in the task queue is working correctly.

    /**
     * Sets the tasks in the task queue, to authenticate and get a dump, if necessary, upload it to the server and send the response back to the device.
     *
     * @param client The instance to https client.
     * @param device The device to upload to.
     * @param type   The type of the dump.
     */
    public void taskUploadDump(HttpsClient client, BluetoothDevice device, String type) {
        if (!interactions.getAuthenticated()) {
            mTaskQueue.addTask(new InteractionsTask(interactions, "authentication", this, activity));
        }
        if (activity.getDataFromInformation(type) == null || activity.wasInformationListAlreadyUploaded(type)) {
            mTaskQueue.addTask(new InteractionsTask(interactions, type, this, activity));
        }
        mTaskQueue.addTask(new UploadDumpTask(client, device, activity, type, this));
        if (type != ConstantValues.INFORMATION_MICRODUMP) {
            mTaskQueue.addTask(new InteractionsTask(interactions, type + "Upload", this, client));
        }
        mTaskQueue.addTask(new EmptyTask(this));
    }

    /**
     * Sets the tasks in the task queue, for the startup.
     * It gets a microdump to extract the serial number, loads the settings of the app from the internal storage and shows basic information about the device to the user.
     *
     * @param interactions The instance of interactions.
     * @param activity     The current activity.
     */
    public void taskStartup(Interactions interactions, WorkActivity activity) {
        mTaskQueue.addTask(new InteractionsTask(interactions, ConstantValues.INFORMATION_MICRODUMP, this, activity));
        mTaskQueue.addTask(new LoadSettingsTask(this, activity));
        mTaskQueue.addTask(new InformationTask(this, activity));
        mTaskQueue.addTask(new EmptyTask(this));
    }
}
