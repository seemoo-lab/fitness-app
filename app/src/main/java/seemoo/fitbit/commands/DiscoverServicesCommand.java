package seemoo.fitbit.commands;

import android.bluetooth.BluetoothGatt;
import android.util.Log;

/**
 * Discovers the services of a bluetooth gatt.
 */
class DiscoverServicesCommand extends BluetoothCommand {

    private BluetoothGatt mBluetoothGatt;

    /**
     * Creates a discover services command.
     * @param mBluetoothGatt The bluetooth gatt.
     */
    DiscoverServicesCommand(BluetoothGatt mBluetoothGatt) {
        this.mBluetoothGatt = mBluetoothGatt;
    }

    @Override
    public void execute() {
        if (mBluetoothGatt == null) {
            Log.e(TAG, "Error: " + TAG + ".execute, mBluetoothGatt = null");
        } else if (!mBluetoothGatt.discoverServices()) {
            Log.e(TAG, "Error: " + TAG + " was not executed correctly");
        }
    }

    @Override
    public String getUUID() {
        return "none";
    }
}
