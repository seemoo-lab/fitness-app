package seemoo.fitbit.interactions;

import seemoo.fitbit.information.InformationList;

/**
 * Abstract version of bluetooth interaction.
 */
abstract class BluetoothInteraction {

    protected final String TAG = this.getClass().getSimpleName();

    //Stops executing interaction after timer milliseconds. A negative value pauses the interaction queue, until timer is override to a positive value.
    private int timer = 720000;

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
     * Gets executed once, when the interaction gets started.
     *
     * @return False, if execution was not correct and interaction should be aborted.
     */
    abstract boolean execute();

    /**
     * "Interacts" with the device, which means it sends data back to the device, each time data from the device got received.
     * Gets executed every time, when the 'onCharacteristicChanged' method gets called.
     *
     * @param value The received data.
     * @return The information gathered in the 'interaction' of null if there was no relevant information.
     */
    abstract InformationList interact(byte[] value);

    /**
     * Checks if the interaction is finished. Gets executed every time, when the 'onCharacteristicChanged' method gets called.
     *
     * @return True, if interaction is finished.
     */
    abstract boolean isFinished();

    /**
     * Gets executed once, when the interaction gets finished.
     *
     * @return The information gathered by the interaction or null if there was no relevant information.
     */
    abstract InformationList finish();
}
