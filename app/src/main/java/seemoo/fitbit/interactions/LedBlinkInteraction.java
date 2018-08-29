package seemoo.fitbit.interactions;


import seemoo.fitbit.commands.Commands;
import seemoo.fitbit.information.InformationList;
import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.Utilities;

/**
 * Creates an airlink with the device.
 */
class LedBlinkInteraction extends BluetoothInteraction {

    private Commands commands;
    private boolean established = false;

    /**
     * Creates an airlink interaction.
     *
     * @param commands The instance of commands.
     */
    LedBlinkInteraction(Commands commands) {
        this.commands = commands;
        setTimer(1000);
    }

    /**
     * {@inheritDoc}
     * Enables notifications for service 1 and establishes an airlink.
     *
     * @return True.
     */
    @Override
    boolean execute() {
        commands.comAirlinkEstablish();
        commands.comBlinkingLEDs();
        return true;
    }

    /**
     * {@inheritDoc}
     * Sets 'established' to true, if the airlink establishment was acknowledged by the device.
     *
     * @param value The received data.
     * @return null
     */
    @Override
    InformationList interact(byte[] value) {
        if (Utilities.byteArrayToHexString(value).length() >= 4) {
            established = Utilities.byteArrayToHexString(value).substring(0, 4).equals(ConstantValues.ESTABLISH_AIRLINK_ACK);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * Returns wether the establishment was successful.
     *
     * @return True, if the establishment was successful.
     */
    @Override
    boolean isFinished() {
        return established;
    }

    /**
     * {@inheritDoc}
     * Disables notifications for service 1.
     *
     * @return Null.
     */
    @Override
    InformationList finish() {
        commands.comDisableNotifications1();
        return null;
    }
}
