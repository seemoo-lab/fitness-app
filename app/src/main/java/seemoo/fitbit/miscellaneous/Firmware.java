package seemoo.fitbit.miscellaneous;

import android.app.Activity;

import org.spongycastle.crypto.engines.XTEAEngine;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


/**
 * Created by jiska on 12/25/17.
 */

public class Firmware { //TODO


    public static String rebootToBSL(Activity activity) throws UnsupportedEncodingException {
        return "todo";
    }


    public static String rebootToBSLold(Activity activity) throws UnsupportedEncodingException {

        /*int headerlength = 14;
        int inlength = 0;
        int plainlength = 0;
        int trailerlenght = 11;*/
        String outStr = "";

        /*File bslupdate = new File("/sdcard/788-bsl-plain.bin");
        byte[] rawInput = {0} ; //ExternalStorage.loadByteArray("/sdcard/788-bsl-plain.bin", activity);

        try {
            rawInput = fullyReadFileToBytes(bslupdate);
            //rawInput = ExternalStorage.loadByteArray("fwup-bsl-plain.bin",activity);
        }catch(IOException e) {

        }

        byte[] header = new byte[headerlength];
        byte[] trailer = new byte[trailerlenght];
        inlength = rawInput.length;
        plainlength = inlength - headerlength-trailerlenght;
        byte[] plain = new byte[plainlength];

        System.arraycopy(rawInput, 0, header, 0, headerlength);
        System.arraycopy(rawInput, headerlength, plain, 0, plainlength);
        System.arraycopy(rawInput, inlength-trailerlenght, trailer,0,trailerlenght);


        // get the nonce from the dump
        byte[] nonce = Arrays.copyOfRange(rawInput, 6, 10);

        //compute the initial counter value using the nonce
        byte[] counter = computeCounter(nonce);

        // use the XTEA block cipher in counter mode (CTR)
        SICBlockCipher cipher = new SICBlockCipher(new XTEAEngine());
        // initialize using the key and the initial counter value.
        cipher.init(true,new ParametersWithIV(new KeyParameter(ConstantValues.FITBIT_KEY), counter));


        byte[] encrypted = new byte[plainlength];


        //decrypt the encrypted part of the megadump, that is starting after byte 16
        cipher.processBytes(plain, 0, plainlength, encrypted, 0);

        //Log.e(TAG, "Decryped Dump");
        String strenc = toHexString(encrypted);
        Log.e(TAG, strenc);

        byte[] out = new byte[inlength];

        System.arraycopy(header, 0, out, 0, headerlength);
        System.arraycopy(encrypted, 0, out, headerlength, encrypted.length);

        byte[] cmac = calculateCMAC(nonce, plain, plainlength);

        System.arraycopy(cmac, 0, out, headerlength + encrypted.length, cmac.length);
        System.arraycopy(header, headerlength - 4, out, headerlength + encrypted.length + cmac.length, 3);*/

        int bslHeaderLength = ConstantValues.REBOOT_TO_BSL_HEADER.length;
        int bslDataLength = ConstantValues.REBOOT_TO_BSL_DATA.length;

        byte[] command = new byte[bslHeaderLength + bslDataLength];

        byte[] nonce = Arrays.copyOfRange(ConstantValues.REBOOT_TO_BSL_HEADER, 6, 10);

        //compute the initial counter value using the nonce
        byte[] counter = {0};// Crypto.computeCounter(nonce);

        // use the XTEA block cipher in counter mode (CTR)
        SICBlockCipher cipher = new SICBlockCipher(new XTEAEngine());
        // initialize using the key and the initial counter value.
        cipher.init(true,new ParametersWithIV(new KeyParameter(Utilities.hexStringToByteArray(AuthValues.ENCRYPTION_KEY)), counter));


        byte[] encrypted = new byte[bslDataLength];


        //decrypt the encrypted part of the megadump, that is starting after byte 16
        cipher.processBytes(command, 0, bslDataLength, encrypted, 0);

        System.arraycopy(ConstantValues.REBOOT_TO_BSL_HEADER, 0, command, 0, bslHeaderLength);
        System.arraycopy(encrypted, 0, command, bslHeaderLength, bslDataLength);

        outStr = Utilities.byteArrayToHexString(command);

        //Log.e(TAG, outStr);

        return outStr.toLowerCase();
    }


}
