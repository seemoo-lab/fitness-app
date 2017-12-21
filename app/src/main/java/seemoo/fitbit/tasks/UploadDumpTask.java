package seemoo.fitbit.tasks;

import android.bluetooth.BluetoothDevice;

import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.https.HttpsClient;
import seemoo.fitbit.miscellaneous.ConstantValues;

/**
 * Uploads a dump to the fitbit server.
 */
class UploadDumpTask extends Task {

    private HttpsClient client;
    private BluetoothDevice device;
    private WorkActivity activity;
    private String type;
    private Tasks tasks;

    /**
     * Creates an instance of upload dump task.
     * @param client The instance of https client.
     * @param device The device where the dump comes from.
     * @param activity The current activity.
     * @param type The type of the dump.
     * @param tasks The instance of tasks.
     */
    UploadDumpTask(HttpsClient client, BluetoothDevice device, WorkActivity activity, String type, Tasks tasks){
        this.client = client;
        this.device = device;
        this.activity = activity;
        this.type = type;
        this.tasks = tasks;
    }

    /**
     * {@inheritDoc}
     * Uploads the dump to the fitbit server.
     */
    @Override
    public void execute() {
        String data = activity.getDataFromInformation(type);
        if(data != null) {
            int cutOff = data.indexOf(ConstantValues.RAW_OUTPUT);
            client.uploadDump(data.substring(cutOff + ConstantValues.RAW_OUTPUT.length()), device.getName(), tasks);
            activity.setInformationListAsAlreadyUploaded(type);
        } else{
            tasks.taskFinished();
        }
    }
}
