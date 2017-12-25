package seemoo.fitbit.miscellaneous;

import android.app.Activity;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.XTEAEngine;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.modes.EAXBlockCipher;
import org.spongycastle.crypto.params.AEADParameters;


public class Crypto {

    private final static String TAG = ExternalStorage.class.getSimpleName();


    private static byte[] getKey() {
        return Utilities.hexStringToByteArray(AuthValues.ENCRYPTION_KEY);
    }


    public static byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis= new FileInputStream(f);
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        }  catch (IOException e){
            throw e;
        } finally {
            fis.close();
        }

        return bytes;
    }

    /*
    Fitbit Flex/One/Charge firmware uses XTEA in EAX mode, we encrypt according to this.
     TODO: if we find a vulnerability for AES trackers, this method should also be able to use AES/EAX...
     */
    public static String encryptDump(byte[] dump, Activity activity) throws InvalidCipherTextException {

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

        // get the nonce from the dump
        byte[] nonce = Arrays.copyOfRange(dump, 6, 10);

        int mac_len=8*8; //cmac (tag) length in bytes
        XTEAEngine engine = new XTEAEngine();
        EAXBlockCipher eax = new EAXBlockCipher(engine);
        AEADParameters params = new AEADParameters(new KeyParameter(getKey()), mac_len, nonce, null);
        eax.init(true, params); //TODO switch true to false here to implement a decryption method
        byte[] result = new byte[eax.getOutputSize(plainlength)];

        int resultlength = eax.processBytes(plain, 0, plainlength, result, 0);


        eax.doFinal(result, resultlength);



        byte[] out = new byte[inlength];
        System.arraycopy(header, 0, out, 0, headerlength);
        System.arraycopy(result, 0, out, headerlength, result.length);
        System.arraycopy(header, headerlength - 4, out, headerlength + result.length, 3);

        outStr = Utilities.byteArrayToHexString(out);

        //Log.e(TAG, outStr);

        return outStr;

    }


    public static String encryptDumpFile(String pathname, Activity activity) throws UnsupportedEncodingException, InvalidCipherTextException {


        File dump = new File(pathname);

        byte[] rawInput = {0} ;

       try {
            rawInput = fullyReadFileToBytes(dump);
        } catch(IOException e) {

        }

        return encryptDump(rawInput, activity);
    }


}
