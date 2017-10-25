package stroeher.sven.bluetooth_le_scanner.tasks;


import stroeher.sven.bluetooth_le_scanner.activities.WorkActivity;

/**
 * Collects basic information from the device.
 */
class InformationTask extends Task {

    private Tasks tasks;
    private WorkActivity activity;

    /**
     * Creates an instance of information task.
     *
     * @param tasks    The instance of tasks.
     * @param activity The current activity.
     */
    InformationTask(Tasks tasks, WorkActivity activity) {
        this.tasks = tasks;
        this.activity = activity;
    }

    /**
     * {@inheritDoc}
     * Collects basic information from the device.
     */
    @Override
    public void execute() {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                activity.collectBasicInformation();
            }
        });
        tasks.taskFinished();
    }
}
