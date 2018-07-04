package seemoo.fitbit.interactions;

import android.support.annotation.Nullable;
import android.util.Log;
import seemoo.fitbit.commands.Commands;
import seemoo.fitbit.information.InformationList;
import seemoo.fitbit.miscellaneous.FitbitDevice;

/**
 * Switches to live mode.
 */
class DisconnectBluetoothInteraction extends BluetoothInteraction {

    private final String TAG = this.getClass().getSimpleName();

    private Commands commands;

    DisconnectBluetoothInteraction(Commands commands) {
        this.commands = commands;
        setTimer(600000);
    }

    @Override
    boolean isFinished() {
        return false;
    }

    @Override
    boolean execute() {
        Log.i(TAG, "Will now disconnect bluetooth");
        if(commands != null){
            commands.close();
        }
        FitbitDevice.clearCache();
        return true;
    }

    @Override
    @Nullable InformationList interact(byte[] value) {
        return null;
    }

    @Override
    @Nullable InformationList finish() {
        return null;
    }
}
