package stroeher.sven.bluetooth_le_scanner.tasks;


import android.util.Log;

import stroeher.sven.bluetooth_le_scanner.activities.WorkActivity;
import stroeher.sven.bluetooth_le_scanner.miscellaneous.ConstantValues;
import stroeher.sven.bluetooth_le_scanner.miscellaneous.ExternalStorage;
import stroeher.sven.bluetooth_le_scanner.miscellaneous.InternalStorage;

/**
 * Loads settings stored in the internal storage.
 */
class LoadSettingsTask extends Task {

    private Tasks tasks;
    private WorkActivity activity;

    /**
     * Creates an isntance of load settings task.
     *
     * @param tasks    The instance of tasks.
     * @param activity The current activity.
     */
    LoadSettingsTask(Tasks tasks, WorkActivity activity) {
        this.tasks = tasks;
        this.activity = activity;
    }

    /**
     * {@inheritDoc}
     * Loads the stored settings.
     */
    @Override
    public void execute() {
        String directory = InternalStorage.loadString(ConstantValues.SETTING_DIRECTORY, activity);
        Log.e(TAG, "directory load = " + directory);
        ExternalStorage.setDirectory(directory, activity);
        tasks.taskFinished();
    }
}
