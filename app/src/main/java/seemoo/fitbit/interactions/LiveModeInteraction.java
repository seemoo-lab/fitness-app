package seemoo.fitbit.interactions;

import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.information.InformationList;
import seemoo.fitbit.miscellaneous.Utilities;
import seemoo.fitbit.commands.Commands;

/**
 * Switches to live mode.
 */
class LiveModeInteraction extends BluetoothInteraction {

    private Commands commands;
    private int LiveModeCommandType;
    private Interactions interactions;

    /**
     * Creates an instance of live mode interaction.
     *
     * @param commands      The instance of commands.
     * @param interactions  The instance of interactions.
     * @param commandType   The type of command. Either turn live mode on/off or switch output of life mode.
     */
    LiveModeInteraction(Commands commands, Interactions interactions, int commandType) {
        this.commands = commands;
        this.interactions = interactions;
        this.LiveModeCommandType = commandType;
        setTimer(6000000);
    }

    /**
     * {@inheritDoc}
     *
     * @return False.
     */
    @Override
    boolean isFinished() {
        return false;
    }

    /**
     * {@inheritDoc}
     * Switches to live mode, if the app is authenticated to the device.
     *
     * @return True.
     */
    @Override
    boolean execute() {
        switch(LiveModeCommandType) {
            case 0:
                setTimer(500);
                commands.comSwitchAccelLiveMode();
                break;
            case 1:
                if (interactions.getAuthenticated()) {
                    commands.comAirlinkClose();
                    commands.comLiveModeEnable();
                    commands.comLiveModeFirstValues();
                    interactions.setLiveModeActive(true);
                } else {
                    interactions.interactionFinished();
                }
                break;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * Returns the input value (= live mode update) in a readable form. If the input is no correct live mode update, it return null.
     *
     * @param value The received data.
     * @return The input in a readable form. If the inut is no live mode update, the return value is null.
     */
    @Override
    InformationList interact(byte[] value) {
        if (value.length >= 16) {
            return Utilities.translate(value);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     * Quits live mode.
     *
     * @return Null.
     */
    @Override
    InformationList finish() {
        commands.comLiveModeDisable();
        return null;
    }
}
