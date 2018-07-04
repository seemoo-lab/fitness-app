package seemoo.fitbit.commands;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.Utilities;

/**
 * Use this class to deal with Bluetooth commands.
 */
public class Commands {

    private final String TAG = this.getClass().getSimpleName();

    private BluetoothGatt mBluetoothGatt;
    private BluetoothCommandQueue mBluetoothCommandQueue;
    private boolean liveModeEnabled = false;
    private boolean liveModeAccelReadout = false;
    private boolean notifications1 = false;
    private boolean notifications2 = false;

    //UUID of the service, which contains the unique fitbit characteristic
    private String service1 = null;
    //UUID of the service used for live mode
    private String service2 = null;
    //UUID of the service used for asking name
    private String service3 = null;

    /**
     * Returns, whether live mode reads out accelerometer data.
     *
     * @return True, if the live mode reads out accelerometer data.
     */
    public boolean isLiveModeAccelReadout() {
        return liveModeAccelReadout;
    }

    /**
     * Sets live mode accelerometer readout to the given value.
     *
     * @param value The value to set live mode accelerometer readout to.
     */
    void setLiveModeAccelReadout(boolean value) {
        liveModeAccelReadout = value;
    }

    /**
     * Creates an instance of Commands
     * @param mBluetoothGatt The bluetooth gatt.
     */
    public Commands(BluetoothGatt mBluetoothGatt) {
        this.mBluetoothGatt = mBluetoothGatt;
        mBluetoothCommandQueue = new BluetoothCommandQueue();
    }

    /**
     * Gets the the service corresponding to the characteristic_1_1.
     *
     * @return True, if the service was found.
     */
    private boolean service1Available() {
        if (service1 == null) {
            ArrayList<BluetoothGattService> services = (ArrayList<BluetoothGattService>) mBluetoothGatt.getServices();
            for (int i = 0; i < services.size(); i++) {
                if (services.get(i).getCharacteristic(UUID.fromString(ConstantValues.CHARACTERISTIC_1_1)) != null) {
                    service1 = services.get(i).getUuid().toString();
                    break;
                }
            }
        }
        return service1 != null;
    }

    /**
     * Gets the the service corresponding to the characteristic_2_1.
     *
     * @return True, if the service was found.
     */
    private boolean service2Available() {
        if (service2 == null) {
            ArrayList<BluetoothGattService> services = (ArrayList<BluetoothGattService>) mBluetoothGatt.getServices();
            for (int i = 0; i < services.size(); i++) {
                if (services.get(i).getCharacteristic(UUID.fromString(ConstantValues.CHARACTERISTIC_2_1)) != null) {
                    service2 = services.get(i).getUuid().toString();
                    break;
                }
            }
        }
        return service2 != null;
    }

    /**
     * Returns the bluetooth gatt.
     *
     * @return The bluetooth gatt.
     */
    public BluetoothGatt getmBluetoothGatt() {
        return mBluetoothGatt;
    }

    /**
     * States, that the current command is finished. (So the next one can be executed.)
     */
    public void commandFinished() {
//        Log.e(TAG, "Command finished: " + mBluetoothCommandQueue.getFirstBluetoothCommand().TAG);
        mBluetoothCommandQueue.commandFinished();
    }

    /**
     * Closes the bluetooth gatt.
     */
    public void close() {
        mBluetoothGatt.close();
    }

    /**
     * <===============================================================================================================>
     * <=================================================> Commands: <=================================================>
     * <===============================================================================================================>
     */

    //<=================================================> Service 1: <=================================================>

    /**
     * Sets the command in command queue, to discover the services of the bluetooth gatt.
     */
    public void comDiscoverServices() {
        mBluetoothCommandQueue.addCommand(new DiscoverServicesCommand(mBluetoothGatt));
    }

    /**
     * Sets the command in command queue, to read the value of characteristic_1_1.
     */
    public void comReadCharacteristic() {
        mBluetoothCommandQueue.addCommand(new ReadCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_1));
    }

    /**
     * Sets the command in command queue, to establish an airlink with the device.
     */
    public void comAirlinkEstablish() {
        if (service1Available()) {
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(ConstantValues.ESTABLISH_AIRLINK)));
        }
    }

    /**
     * Sets the command in command queue, to close an airlink with the device.
     */
    public void comAirlinkClose() {
        if (service1Available()) {
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(ConstantValues.CLOSE_AIRLINK)));
        }
    }

    /**
     * Sets the command in command queue, to let the LEDs of a device blink.
     */
    public void comBlinkingLEDs() {
        if (service1Available()) {
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(ConstantValues.BLINKING_LEDS)));
        }
    }

    /**
     * Sets the commands in command queue, to enable the notifications for service 1.
     */
    public void comEnableNotifications1() {
        if (service1Available()) {
            BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID.fromString(service1)).
                    getCharacteristic(UUID.fromString(ConstantValues.CHARACTERISTIC_1_1));
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            mBluetoothCommandQueue.addCommand(new WriteDescriptorCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_1,
                    ConstantValues.DESCRIPTOR_1_1_1, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE));
        }
    }

    /**
     * Sets the commands in command queue, to disable the notifications for service 1.
     */
    public void comDisableNotifications1() {
        if (service1Available()) {
            BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID.fromString(service1)).
                    getCharacteristic(UUID.fromString(ConstantValues.CHARACTERISTIC_1_1));
            mBluetoothGatt.setCharacteristicNotification(characteristic, false);
            mBluetoothCommandQueue.addCommand(new WriteDescriptorCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_1,
                    ConstantValues.DESCRIPTOR_1_1_1, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE));
        }
    }

    /**
     * Sets the command in command queue, to send the get microdump command to the device.
     */
    public void comGetMicrodump() {
        if (service1Available()) {
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(ConstantValues.DUMP + ConstantValues.TYPE_MICRODUMP)));
        }
    }

    /**
     * Sets the command in command queue, to send the get megadump command to the device.
     */
    public void comGetMegadump() {
        if (service1Available()) {
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(ConstantValues.DUMP + ConstantValues.TYPE_MEGADUMP)));
        }
    }


    /**
     * Sets the command in command queue, to send the get alarms command to the device.
     */
    public void comGetAlarms() {
        if (service1Available()) {
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(ConstantValues.DUMP + ConstantValues.TYPE_ALARMS)));
        }
    }

    /**
     * Sets the command in command queue, to send the readout memory command to the device.
     *
     * @param address The start address of the wanted memory part.
     * @param length  The length of the wanted memory part.
     */
    public void comReadoutMemory(String address, String length) {
        if (service1Available()) {
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(ConstantValues.READOUT_MEMORY + Utilities.rotateBytes(Utilities.fixLength(address, 8)) + Utilities.rotateBytes(Utilities.fixLength(length, 8)))));
        }
    }

    /**
     * Sets the command in command queue, to send the start command for an authentication to the device.
     *
     * @param randomNumber A random number.
     * @param nonce        The nonce of the authentication.
     */
    public void comAuthenticateInitialize(String randomNumber, String nonce) {
        if (service1Available()) {
            Log.d(TAG, "comAuthenticateInitialize: " + ConstantValues.AUTHENTICATION_INITIALIZE + ", " + randomNumber + ", " + nonce);
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    (Utilities.hexStringToByteArray(ConstantValues.AUTHENTICATION_INITIALIZE + randomNumber + nonce))));
        }
    }

    /**
     * Sets the command in command queue, to send the response for an authentication to the device.
     *
     * @param cmac The calculated cmac of the authentication.
     */
    public void comAuthenticateResponse(String cmac) {
        if (service1Available()) {
            if (cmac != null) {
                mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                        Utilities.hexStringToByteArray(ConstantValues.AUTHENTICATION_RESPONSE + cmac)));
            }
        }
    }

    /**
     * Sets the command in command queue, to send a set date command to the device.
     *
     * @param date The date to set.
     */
    public void comSetDate(String date) {
        if (service1Available()) {
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(ConstantValues.SET_DATE + Utilities.rotateBytes(date))));
        }
    }

    /**
     * Sets the command in command queue, to send the start command for an upload to the device.
     *
     * @param extra The upload type, length and checksum.
     */
    public void comUploadInitialize(String extra) {
        if (service1Available()) {
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(ConstantValues.UPLOAD + extra)));
        }
    }

    /**
     * Sets the command in command queue, to upload data to the device.
     *
     * @param data The data to upload.
     */
    public void comUploadData(String data) {
        if (service1Available()) {
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(data)));
        }
    }

    /**
     * Sets the command in command queue, to send an acknowlegdement to the device.
     */
    public void comAcknowledgement() {
        if (service1Available()) {
            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(ConstantValues.ACKNOWLEDGEMENT)));
        }
    }

    //<=================================================> Service 2: <=================================================>

    /**
     * Sets the commands in command queue, to send an enable live mode command to the device.
     */
    public void comLiveModeEnable() {
        Log.i(TAG, "comLiveModeEnable: check if available");
        if (service2Available() && !liveModeEnabled) {
            Log.i(TAG, "comLiveModeEnable: is available");
            mBluetoothCommandQueue.addCommand(new WriteDescriptorCommand(mBluetoothGatt, service2, ConstantValues.CHARACTERISTIC_2_1, ConstantValues.DESCRIPTOR_2_1_1,
                    Utilities.hexStringToByteArray(ConstantValues.MODE_ON)));
            liveModeEnabled = true;
            comEnableNotifications2();
        }
    }

    /**
     * Sets the command in command queue, to collect the first data for the live mode from the device.
     */
    public void comLiveModeFirstValues() {
        if (service2Available() && liveModeEnabled) {
            mBluetoothCommandQueue.addCommand(new ReadCharacteristicCommand(mBluetoothGatt, service2, ConstantValues.CHARACTERISTIC_2_1));
        }
    }

    /**
     * Sets the commands in command queue, to send a disable live mode command to the device.
     */
    public void comLiveModeDisable() {
        if (service2Available() && liveModeEnabled) {
            comDisableNotifications2();
            mBluetoothCommandQueue.addCommand(new WriteDescriptorCommand(mBluetoothGatt, service2, ConstantValues.CHARACTERISTIC_2_1, ConstantValues.DESCRIPTOR_2_1_1,
                    Utilities.hexStringToByteArray(ConstantValues.MODE_OFF)));
            liveModeEnabled = false;
        }
    }

    /**
     * Sets the command in command queue, to send the get turn on Accelerometer Readout.
     */
    public void comSwitchAccelLiveMode() {
        if (service1Available()) {

            mBluetoothCommandQueue.addCommand(new WriteCharacteristicCommand(mBluetoothGatt, service1, ConstantValues.CHARACTERISTIC_1_2,
                    Utilities.hexStringToByteArray(ConstantValues.ACCEL_LIVE_MODE)));

            setLiveModeAccelReadout(!isLiveModeAccelReadout());
        }
    }

    /**
     * Sets the commands in command queue, to enable the notifications for service 2.
     */
    public void comEnableNotifications2() {
        if (service2Available() && !notifications2) {
            BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID.fromString(service2)).
                    getCharacteristic(UUID.fromString(ConstantValues.CHARACTERISTIC_2_1));
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);
            mBluetoothCommandQueue.addCommand(new WriteDescriptorCommand(mBluetoothGatt, service2, ConstantValues.CHARACTERISTIC_2_1,
                    ConstantValues.DESCRIPTOR_2_1_1, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE));
            notifications2 = true;
        }
    }

    /**
     * Sets the commands in command queue, to disable the notifications for service 2.
     */
    public void comDisableNotifications2() {
        if (service2Available() && notifications2) {
            BluetoothGattCharacteristic characteristic = mBluetoothGatt.getService(UUID.fromString(service2)).
                    getCharacteristic(UUID.fromString(ConstantValues.CHARACTERISTIC_2_1));
            mBluetoothGatt.setCharacteristicNotification(characteristic, false);
            mBluetoothCommandQueue.addCommand(new WriteDescriptorCommand(mBluetoothGatt, service2, ConstantValues.CHARACTERISTIC_2_1,
                    ConstantValues.DESCRIPTOR_2_1_1, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE));
            notifications2 = false;
        }
    }
}
