package stroeher.sven.bluetooth_le_scanner.miscellaneous;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.XTEAEngine;
import org.spongycastle.crypto.macs.CMac;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.spongycastle.crypto.modes.EAXBlockCipher;
import org.spongycastle.crypto.params.AEADParameters;



/**
 * Created by greyknight on 04.12.17.
 */

public class Cryption {

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final static String TAG = ExternalStorage.class.getSimpleName();

    private static String toHexString(byte[] b)
    {
        char[] out = new char[b.length * 2];
        int i;
        for(i = 0; i < b.length; i++)
        {
            int v = b[i] & 0xff;
            out[i*2] = hexArray[v >> 4];
            out[i*2+1] = hexArray[v & 0xf];
        }
        return new String(out);
    }

 /*   private static byte[] toByteArray(String s)
    {
        s = s.replace(" ", "");
        int i;
        int len = s.length();
        byte[] out = new byte[len / 2];
        for(i = 0; i < len; i+=2)
            out[i/2] = (byte)((Character.digit(s.charAt(i),  16) << 4) +
                    Character.digit(s.charAt(i+1),  16));
        return out;
    }

    public static List<String> splitEqually(String text, int size) {
        // Give the list the right capacity to start with. You could use an array
        // instead if you wanted.
        List<String> ret = new ArrayList<String>((text.length() + size - 1) / size);

        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return ret;
    }*/

    private static byte[] computeCounter(byte[] nonceCTR){
        CMac mac = new CMac(new XTEAEngine());
        mac.init(new KeyParameter(ConstantValues.FITBIT_KEY));
        byte input [] = new byte [8];
        mac.update(input, 0, 8);
        mac.update(nonceCTR, 0, 4);

        byte[] out = new byte[8];
        mac.doFinal(out, 0);
        return out;
    }

    /*private static byte[] loadToByteArray(String pathname){
        Path path = Paths.get(pathname);
        List<String> datastring= null;
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            datastring = Files.readAllLines(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for(String line: datastring){
            try {
                data.write(toByteArray(line));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return data.toByteArray();
    }*/

    public static byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis= new FileInputStream(f);;
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

    public static byte[] calculateCMAC(byte[] nonce, byte[] plainText, int plainTextLength) {
        int mac_len=8*8; //cmac (tag) length in bytes

        String noncestr = toHexString(nonce);
        String plainstr = toHexString(plainText);

        XTEAEngine engine = new XTEAEngine();
        EAXBlockCipher eax = new EAXBlockCipher(engine);
        AEADParameters params = new AEADParameters(new KeyParameter(ConstantValues.FITBIT_KEY), mac_len, nonce, null);
        //encEax.init(true, parameters);
        //decEax.init(false, parameters);
        eax.init(true, params);
        byte[] result = new byte[eax.getOutputSize(plainTextLength)];

        //TODO if you replace the strange "cc50" in plaintext with zeros, you get the expected encrypted result -> potentially a bug of old implementation and this one is correct?
        plainText[plainText.length-2] = (byte) 0;
        plainText[plainText.length-1] = (byte) 0;
        int resultlength = eax.processBytes(plainText, 0, plainTextLength, result, 0);

        try {
            eax.doFinal(result, resultlength);
        }catch(InvalidCipherTextException e) {

        }

        byte[] cmac = eax.getMac();

        return eax.getMac();
    }

    public static String encryptedFwUpdate(Activity activity) throws UnsupportedEncodingException{

        int headerlength = 14;
        int inlength = 0;
        int plainlength = 0;
        int trailerlenght = 11;
        String outStr = "";

        File bslupdate = new File("/sdcard/788-bsl-plain.bin");
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

        //Log.e(TAG, outStr);

        byte[] cmac = calculateCMAC(nonce, plain, plainlength);
        String cmacStr = toHexString(cmac);

        outStr = toHexString(out);

        System.arraycopy(cmac, 0, out, headerlength + encrypted.length, cmac.length);
        System.arraycopy(header, headerlength - 4, out, headerlength + encrypted.length + cmac.length, 3);

        outStr = toHexString(out);

        Log.e(TAG, outStr);

        String rawStr = toHexString(rawInput);
        return outStr;
        //return out;
    }

}
