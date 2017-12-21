package seemoo.fitbit.tasks;


import android.util.Log;

import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.ExternalStorage;
import seemoo.fitbit.miscellaneous.InternalStorage;

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
