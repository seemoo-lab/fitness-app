package seemoo.fitbit.miscellaneous;

import android.app.Activity;

import java.io.IOException;
import java.util.Arrays;

import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.XTEAEngine;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.modes.EAXBlockCipher;
import org.spongycastle.crypto.params.AEADParameters;
import android.util.Log;

public class Crypto {

    private final static String TAG = "Crypto";


    private static byte[] getKey() {
        return Utilities.hexStringToByteArray(FitbitDevice.ENCRYPTION_KEY);
    }

    //TODO tracker vs server dump have different headers (with / without serial ID)
    //TODO distinguish between XTEA/AES
    public static String decryptTrackerDump(byte[] dump, Activity activity) {


        int headerlength = 16;
        int inlength = 0;
        int plainlength = 0;
        int trailerlenght = 11;
        String outStr = "";


        byte[] header = new byte[headerlength];
        byte[] trailer = new byte[trailerlenght];
        inlength = dump.length;
        plainlength = inlength - headerlength-trailerlenght;
        byte[] plain = new byte[plainlength];

        System.arraycopy(dump, 0, header, 0, headerlength);
        System.arraycopy(dump, headerlength, plain, 0, plainlength);
        System.arraycopy(dump, inlength-trailerlenght, trailer,0,trailerlenght);

        //Remove Crypt-Byte in header
        header[4] = (byte) 0x00;


        // get the nonce from the dump
        byte[] nonce = Arrays.copyOfRange(header, 6, 10);

        int mac_len=8*8; //cmac (tag) length in bytes
        XTEAEngine engine = new XTEAEngine();
        EAXBlockCipher eax = new EAXBlockCipher(engine);
        Log.e(TAG, "key: " + getKey());
        Log.e(TAG, "nonce: " + Utilities.byteArrayToHexString(nonce));
        AEADParameters params = new AEADParameters(new KeyParameter(getKey()), mac_len, nonce, null);
        eax.init(false, params); //TODO switch true to false here to implement a decryption method, apply it to microdumps/megadumps

        byte[] result = new byte[eax.getOutputSize(plainlength)];

        int resultlength = eax.processBytes(plain, 0, plainlength, result, 0);


        //eax.doFinal(result, resultlength);



        byte[] out = new byte[inlength];
        System.arraycopy(header, 0, out, 0, headerlength);
        System.arraycopy(result, 0, out, headerlength, result.length);
        System.arraycopy(header, headerlength - 4, out, headerlength + result.length, 3);

        outStr = Utilities.byteArrayToHexString(out);

        //Log.e(TAG, outStr);

        return outStr;


    }



    /*
    Fitbit Flex/One/Charge firmware uses XTEA in EAX mode, we encrypt according to this.
     TODO: if we find a vulnerability for AES trackers, this method should also be able to use AES/EAX...
     Length fields must match, otherwise result can become null ...
     */
    public static String encryptDump(byte[] dump, Activity activity)  {
        Log.e(TAG, "Encrypting Dump");


        int headerlength = 14;
        int inlength = 0;
        int plainlength = 0;
        int trailerlenght = 11;
        String outStr = "";


        byte[] header = new byte[headerlength];
        byte[] trailer = new byte[trailerlenght];
        inlength = dump.length;
        plainlength = inlength - headerlength-trailerlenght;
        byte[] plain = new byte[plainlength];

        System.arraycopy(dump, 0, header, 0, headerlength);
        System.arraycopy(dump, headerlength, plain, 0, plainlength);
        System.arraycopy(dump, inlength-trailerlenght, trailer,0,trailerlenght);

        //Set Crypt-Byte in header
        header[4] = (byte) 0x01;

        //Nonce should not be zero... //TODO make nonce random
        //header[6] = (byte) 0xab;
        //header[7] = (byte) 0xcd;

        // get the nonce from the dump
        byte[] nonce = Arrays.copyOfRange(header, 6, 10);

        int mac_len=8*8; //cmac (tag) length in bytes
        XTEAEngine engine = new XTEAEngine();
        EAXBlockCipher eax = new EAXBlockCipher(engine);
        Log.e(TAG, "key: " + getKey());
        Log.e(TAG, "nonce: " + Utilities.byteArrayToHexString(nonce));
        AEADParameters params = new AEADParameters(new KeyParameter(getKey()), mac_len, nonce, null);
        eax.init(true, params); //TODO switch true to false here to implement a decryption method, apply it to microdumps/megadumps

        byte[] result = new byte[eax.getOutputSize(plainlength)];

        int resultlength = eax.processBytes(plain, 0, plainlength, result, 0);


        try {
            eax.doFinal(result, resultlength);
        }
        catch (Exception e) {
            Log.e(TAG, "Exception occurred when calculating CMAC");
        }


        byte[] out = new byte[inlength];
        System.arraycopy(header, 0, out, 0, headerlength);
        System.arraycopy(result, 0, out, headerlength, result.length);
        System.arraycopy(header, headerlength - 4, out, headerlength + result.length, 3);

        outStr = Utilities.byteArrayToHexString(out);

        Log.e(TAG, outStr);

        return outStr;

    }


    public static String encryptDumpFile(String pathname, Activity activity) throws InvalidCipherTextException {

       byte[] rawInput = {0} ;

       try {
            rawInput = Utilities.fullyReadFileToBytes(pathname);
       } catch(IOException e) {

       }

       return encryptDump(rawInput, activity);
    }


}
