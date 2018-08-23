package seemoo.fitbit.interactions;

import android.support.annotation.Nullable;
import android.util.Log;
import seemoo.fitbit.commands.Commands;
import seemoo.fitbit.fragments.MainFragment;
import seemoo.fitbit.information.InformationList;
import seemoo.fitbit.miscellaneous.FitbitDevice;

/**
 * Switches to live mode.
 */
class DisconnectBluetoothInteraction extends BluetoothInteraction {

    private final String TAG = this.getClass().getSimpleName();

    private Commands commands;
    private MainFragment mainFragment;

    DisconnectBluetoothInteraction(MainFragment mainFragment, Commands commands) {
        this.commands = commands;
        this.mainFragment = mainFragment;
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
        mainFragment.setBluetoothConnectionState(MainFragment.BluetoothConnectionState.DISCONNECTING);
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
