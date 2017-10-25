package stroeher.sven.bluetooth_le_scanner.commands;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.UUID;

/**
 * Writes the value of a characteristic.
 */
class WriteCharacteristicCommand extends BluetoothCommand {

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic;
    private byte[] value;

    /**
     * Creates a write characteristic command.
     * @param mBluetoothGatt The bluetooth gatt.
     * @param serviceUUID The UUID of the corresponding service as a string.
     * @param characteristicUUID The UUID of the characteristic as a string.
     * @param value The value to write.
     */
    WriteCharacteristicCommand(BluetoothGatt mBluetoothGatt, String serviceUUID, String characteristicUUID, byte[] value) {
        if (mBluetoothGatt != null) {
            this.mBluetoothGatt = mBluetoothGatt;
            mBluetoothGattCharacteristic = mBluetoothGatt.getService(UUID.fromString(serviceUUID)).getCharacteristic(UUID.fromString(characteristicUUID));
            this.value = value;
        } else {
            Log.e(TAG, "Error: " + TAG + " is null!");
        }
    }

    @Override
    public void execute() {
        if (mBluetoothGatt == null || value == null || mBluetoothGattCharacteristic == null) {
            Log.e(TAG, "Error: " + TAG + ".execute, object = null");
        } else {
            mBluetoothGattCharacteristic.setValue(value);
            if (!mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic)) {
                Log.e(TAG, "Error: " + TAG + " was not executed correctly: " + mBluetoothGattCharacteristic.getUuid().toString());
            }
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
