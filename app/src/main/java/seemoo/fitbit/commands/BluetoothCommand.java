package seemoo.fitbit.commands;

/**
 * The abstract version of a command.
 */
abstract class BluetoothCommand {

    protected final String TAG = this.getClass().getSimpleName();

    /**
     * Gets executed when the command gets executed.
     */
    public abstract void execute();

    /**
     * Returns the UUID of the corresponding type.
     *
     * @return The UUID.
     */
    public abstract String getUUID();
}
