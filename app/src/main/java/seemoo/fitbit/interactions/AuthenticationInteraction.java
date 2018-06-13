package seemoo.fitbit.interactions;

import android.util.Log;
import android.widget.Toast;

import org.spongycastle.crypto.BlockCipher;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.XTEAEngine;
import org.spongycastle.crypto.macs.CMac;
import org.spongycastle.crypto.params.KeyParameter;

import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.miscellaneous.FitbitDevice;
import seemoo.fitbit.information.InformationList;
import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.Utilities;
import seemoo.fitbit.commands.Commands;

/**
 * Authenticates the app with the device and if necessary with the fitbit server.
 */
class AuthenticationInteraction extends BluetoothInteraction {

    private String acknowledgement;
    private WorkActivity activity;
    private Toast toast;
    private Commands commands;
    private Interactions interactions;
    private int deviceType;

    /**
     * Creates an authentication interaction.
     *
     * @param activity     The current activity.
     * @param toast        The toast, to send messages to the user.
     * @param commands     The instance of commands.
     * @param interactions The instance of interactions.
     */
    AuthenticationInteraction(WorkActivity activity, Toast toast, Commands commands, Interactions interactions) {
        this.activity = activity;
        this.toast = toast;
        this.commands = commands;
        this.interactions = interactions;
        deviceType = getDeviceType();
    }

    /**
     * {@inheritDoc}
     * Checks if authentication is finished.
     *
     * @return True, if authentication is finished.
     */
    boolean isFinished() {
        if (acknowledgement.equals(ConstantValues.ACKNOWLEDGEMENT)) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toast.setText(TAG + " successful.");
                    toast.show();
                }
            });
            Log.e(TAG, TAG + " successful.");
            interactions.setAuthenticated(true);
            return true;
        } else if (acknowledgement.length() >= 4 && acknowledgement.substring(0, 4).equals(ConstantValues.NEG_ACKNOWLEDGEMENT)) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //Log.e(TAG, "uzpwzpizpzwtpowz");
                    Log.e(TAG, "Error: " + Utilities.getError(acknowledgement));
                    toast.setText(TAG + " failed.");
                    toast.show();
                }
            });
            Log.e(TAG, TAG + " failed.");
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * If the serial number of the device is known, it checks if there was an successful authentication in the past, which means nonce and authenticationKey is stored.
     * If so, notifications get enabled and a start authentication command is send to the device.
     * If not, the web interface for an authentication to the fitbit server is started.
     *
     * @return True, if the serial number of the current device is known, which is mandatory for an authentication.
     */
    @Override
    boolean execute() {
        Log.e(TAG, "Nonce = " + FitbitDevice.NONCE);
        if (FitbitDevice.SERIAL_NUMBER == null) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setText("No serial number. Please get a Micro- or Megadump.");
                    toast.show();
                    toast.setDuration(Toast.LENGTH_SHORT);
                }
            });
            return false;
        } else if (FitbitDevice.NONCE != null) {
            commands.comEnableNotifications1();
            commands.comAuthenticateInitialize(ConstantValues.RANDOM_NUMBER, Utilities.rotateBytes(Utilities.intToHexString(Utilities.stringToInt(FitbitDevice.NONCE))));
        } else {
            setTimer(-1);
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    activity.startAuthentication();
//                    interactions.interactionFinished();
                }
            });
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * Sends the calculated cmac to the device.
     *
     * @param value The received data.
     * @return Null.
     */
    @Override
    InformationList interact(byte[] value) {
        String cmac = getCMAC(value);
        commands.comAuthenticateResponse(cmac);
        return null;
    }

    /**
     * {@inheritDoc}
     * Disables notifications.
     *
     * @return Null.
     */
    @Override
    InformationList finish() {
        commands.comDisableNotifications1();
        return null;
    }

    /**
     * Returns the device type specified by its name.
     *
     * @return The device type.
     */
    private int getDeviceType() {
        String name = commands.getmBluetoothGatt().getDevice().getName();
        //FIXME make this a feature array
        if (name != null) {
            if (name.equals(ConstantValues.NAMES[0]) || name.equals(ConstantValues.NAMES[2]) || name.equals(ConstantValues.NAMES[6]) || name.equals(ConstantValues.NAMES[7])) {
                return 1; //XTEA: Flex, One, Charge, Charge HR
            } else if (name.equals(ConstantValues.NAMES[3]) || name.equals(ConstantValues.NAMES[4]) || name.equals(ConstantValues.NAMES[14])) {
                return 2; //AES: Alta, Alta HR, Ionic
            }
        }
        return 2; //default to AES for unknown/new trackers
    }

    /**
     * Sets the acknowledgement and gets the cmac calculated.
     *
     * @param characteristicValue The characteristic value to calculate the cmac with.
     * @return The calculated cmac.
     */
    private String getCMAC(byte[] characteristicValue) {
        acknowledgement = Utilities.byteArrayToHexString(characteristicValue);
        return generateCMAC(characteristicValue, FitbitDevice.AUTHENTICATION_KEY);
    }

    /**
     * Calculates the cmac.
     *
     * @param sequenceNumber    The sequence number to calculate the cmac with.
     * @param authenticationKey The authentication key to calculate the cmac with.
     * @return The calculated cmac.
     */
    private String generateCMAC(byte[] sequenceNumber, String authenticationKey) {
        String seqNum = Utilities.byteArrayToHexString(sequenceNumber);
        if (seqNum.length() > 4 && seqNum.substring(0, 4).equals(ConstantValues.AUTHENTICATION_CHALLENGE)) {
            seqNum = seqNum.substring(20);
            seqNum = Utilities.rotateBytes(seqNum);
            byte[] authKey = Utilities.hexStringToByteArray(authenticationKey);
            BlockCipher cypher;
            switch (deviceType) {
                case 1:
                    cypher = new XTEAEngine();
                    break;
                case 2:
                    cypher = new AESEngine();
                    break;
                default:
                    cypher = null;
                    Log.e(TAG, "Error: AuthenticationInteraction key not available!");
            }
            if (cypher != null && authKey != null) {
                CMac mac = new CMac(cypher, 64);
                mac.init(new KeyParameter(authKey));
                byte[] counter = Utilities.intToByteArray(Integer.parseInt(seqNum, 16));
                mac.update(counter, 0, 4);
                byte[] bArr = new byte[8];
                mac.doFinal(bArr, 0);
                return Utilities.byteArrayToHexString(bArr);
            }
        }
        return null;
    }
}
