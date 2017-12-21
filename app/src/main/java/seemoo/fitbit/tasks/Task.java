package seemoo.fitbit.tasks;

/**
 * Abstract version of task.
 */
abstract class Task {

    protected final String TAG = this.getClass().getSimpleName();

    //Stops executing task after timer milliseconds. A negative value pauses the task queue, until timer is override to a positive value.
    private int timer = 5000;

    /**
     * Sets the timer to the given value.
     *
     * @param value The value to set the timer to.
     */
    void setTimer(int value) {
        timer = value;
    }

    /**
     * Return the value of the timer.
     *
     * @return The timer value.
     */
    int getTimer() {
        return timer;
    }

    /**
     * Gets executed when the task gets executed.
     */
    public abstract void execute();
}
