package seemoo.fitbit.commands;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.UUID;

/**
 * Reads the value of a characteristic.
 */
class ReadCharacteristicCommand extends BluetoothCommand {

    private BluetoothGatt mBluetoothGatt;

    private BluetoothGattCharacteristic mBluetoothGattCharacteristic;

    /**
     * Creates a read characteristic command.
     * @param mBluetoothGatt The bluetooth gatt.
     * @param serviceUUID The UUID of the corresponding service as a string.
     * @param characteristicUUID The UUID of the characteristic as a string.
     */
    ReadCharacteristicCommand(BluetoothGatt mBluetoothGatt, String serviceUUID, String characteristicUUID) {
        if (mBluetoothGatt != null) {
            this.mBluetoothGatt = mBluetoothGatt;
            mBluetoothGattCharacteristic = mBluetoothGatt.getService(UUID.fromString(serviceUUID)).getCharacteristic(UUID.fromString(characteristicUUID));
        } else {
            Log.e(TAG, "Error: " + TAG + " is null!");
        }
    }

    @Override
    public void execute() {
        if (mBluetoothGatt == null || mBluetoothGattCharacteristic == null) {
            Log.e(TAG, "Error: " + TAG + ".execute, object = null");
        } else if (!mBluetoothGatt.readCharacteristic(mBluetoothGattCharacteristic)) {
            Log.e(TAG, "Error: " + TAG + " was not executed correctly: " + mBluetoothGattCharacteristic.getUuid().toString());
        }
    }

    @Override
    public String getUUID() {
        if (mBluetoothGattCharacteristic == null) {
            return "not available";
        }
        return mBluetoothGattCharacteristic.getUuid().toString();
    }
}
