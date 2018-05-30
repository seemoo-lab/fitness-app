package seemoo.fitbit.interactions;

import android.app.Activity;
import android.view.MenuItem;

import seemoo.fitbit.R;
import seemoo.fitbit.information.InformationList;
import seemoo.fitbit.miscellaneous.ButtonHandler;
import seemoo.fitbit.miscellaneous.Utilities;
import seemoo.fitbit.commands.Commands;

/**
 * Switches to live mode.
 */
class LiveModeInteraction extends BluetoothInteraction {

    private Activity activity;
    private Commands commands;
    private Interactions interactions;
    private ButtonHandler buttonHandler;

    /**
     * Creates an instance of live mode interaction.
     *
     * @param activity      The current activity.
     * @param commands      The instance of commands.
     * @param interactions  The instance of interactions.
     * @param buttonHandler The instance of the button handler.
     */
    LiveModeInteraction(Activity activity, Commands commands, Interactions interactions, ButtonHandler buttonHandler) {
        this.activity = activity;
        this.commands = commands;
        this.interactions = interactions;
        this.buttonHandler = buttonHandler;
        setTimer(600000);
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
        if (interactions.getAuthenticated()) {
            commands.comAirlinkClose();
            commands.comLiveModeEnable();
            commands.comLiveModeFirstValues();
            //buttonHandler.setText("End Live Mode", buttonID);
            ((MenuItem) activity.findViewById(R.id.nav_live_mode)).setTitle(R.string.caption_end_live_mode);
            //buttonHandler.setVisible(buttonID);
            interactions.setLiveModeActive(true);
        } else {
            ((MenuItem) activity.findViewById(R.id.nav_live_mode)).setTitle(R.string.caption_live_mode);
            //buttonHandler.setText("Live Mode", buttonID);
            buttonHandler.setAllVisible();
            interactions.interactionFinished();
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
