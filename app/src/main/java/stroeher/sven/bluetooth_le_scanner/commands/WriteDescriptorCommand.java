package stroeher.sven.bluetooth_le_scanner.commands;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import java.util.UUID;

/**
 * Writes the value of a descriptor.
 */
class WriteDescriptorCommand extends BluetoothCommand {

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattDescriptor mBluetoothGattDescriptor;
    private byte[] value;

    /**
     * Creates a write descriptor command.
     * @param mBluetoothGatt The bluetooth gatt.
     * @param serviceUUID The UUID of the corresponding service as a string.
     * @param characteristicUUID The UUID of the corresponding characteristic as a string.
     * @param descriptorUUID The UUID of the descriptor as a  string.
     * @param value The value to write.
     */
    WriteDescriptorCommand(BluetoothGatt mBluetoothGatt, String serviceUUID, String characteristicUUID, String descriptorUUID, byte[] value) {
        if (mBluetoothGatt != null) {
            this.mBluetoothGatt = mBluetoothGatt;
            mBluetoothGattDescriptor = mBluetoothGatt.getService(UUID.fromString(serviceUUID)).getCharacteristic(UUID.fromString(characteristicUUID)).getDescriptor(UUID.fromString(descriptorUUID));
            this.value = value;
        } else {
            Log.e(TAG, "Error: " + TAG + " is null!");
        }
    }

    @Override
    public void execute() {
        if (mBluetoothGatt == null || value == null || mBluetoothGattDescriptor == null) {
            Log.e(TAG, "Error: " + TAG + ".execute, object = null");
        } else {
            mBluetoothGattDescriptor.setValue(value);
            if (!mBluetoothGatt.writeDescriptor(mBluetoothGattDescriptor)) {
                Log.e(TAG, "Error: " + TAG + " was not executed correctly: " + mBluetoothGattDescriptor.getUuid().toString());
            }
        }
    }

    @Override
    public String getUUID() {
        if (mBluetoothGattDescriptor == null) {
            return "not available";
        }
        return mBluetoothGattDescriptor.getUuid().toString();
    }

}
