package seemoo.fitbit.tasks;

/**
 * Does nothing. Needed to check if the last regular task in the task queue is working correctly.
 */
class EmptyTask extends Task {

    private Tasks tasks;

    /**
     * Creates an isntance of empty task.
     * @param tasks The instance of tasks.
     */
    EmptyTask(Tasks tasks){
        this.tasks = tasks;
    }

    /**
     * {@inheritDoc}
     * Finishes this task.
     */
    @Override
    public void execute() {
        tasks.taskFinished();
    }
}
