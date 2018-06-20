package seemoo.fitbit.miscellaneous;

import android.app.Activity;
import android.util.Log;

import org.apache.commons.lang.ArrayUtils;
import org.spongycastle.crypto.engines.XTEAEngine;
import org.spongycastle.crypto.modes.SICBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by jiska on 12/25/17.
 */

public class Firmware {

    private static final String TAG = "Firmware";


    /*
        Generate APP or BSL frame from firmware flash image.
     */
    public static String generateFirmwareFrame(String pathname, int start, int end, int address, boolean isBSL, Activity activity) {

        byte[] flash = {0} ;

        try {
            flash = Utilities.fullyReadFileToBytes(pathname);
        } catch(IOException e) {
            Log.e(TAG, "Could not read firmware file.");
            return "";
        }

        byte[] firmware = new byte[end-start];
        System.arraycopy(flash, start, firmware, 0, end-start);

        //this adds a CRC inside the firmware, fixes firmware version being displayed as 0.00 and RF_ERR_BSL_MISSING_OR_INVALID
        if (firmware.length > 0x204) {
            firmware[0x204] = 0; //flip this bit to zero

            byte[] crcPart;
            if (!isBSL && firmware.length >=0x26000) { //CRC end position for APP is 0x26000, which is an offset of 0x20 but why??
                crcPart = new byte[0x26000-8];
                System.arraycopy(firmware, 0, crcPart, 0, 0x200-1);
                System.arraycopy(firmware, 0x208, crcPart, 0x200, 0x26000-0x208);

            } else {
                crcPart = new byte[firmware.length-8];
                //firmware[0:0x200] + firmware[0x208:]
                System.arraycopy(firmware, 0, crcPart, 0, 0x200-1);
                System.arraycopy(firmware, 0x208, crcPart, 0x200, firmware.length-0x208);
            }




            byte[] crcFirmware = Utilities.hexStringToByteArray(Encoding.crc(Utilities.byteArrayToHexString(crcPart)));
            firmware[0x200] = crcFirmware[1];
            firmware[0x201] = crcFirmware[0];
        }

        byte[] header = {0x30, 0x02, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00};
        byte[] lengthMin4 = Utilities.intToByteArray(firmware.length+48);



        byte[] frame = ArrayUtils.addAll(header, lengthMin4);

        //depending on firmware parts, divide frame or don't...
        ArrayList<Integer> chunkLengths = new ArrayList<Integer>(1);
        if (isBSL) {
            chunkLengths.add(firmware.length); //just flash the BSL as a whole...
        } else {
            chunkLengths.add(0x6000);
            chunkLengths.add(0x10000);
            chunkLengths.add(0x10000);
        }



        int chunkNum = 0;
        int chunkPos = 0;



        byte fwtype;
        if (isBSL)
            fwtype = 0x01;
        else
            fwtype = 0x02;


        byte reboot;
        if (isBSL)
            reboot = 0x03;
        else
            reboot = 0x04;


        while (chunkNum < chunkLengths.size()) {
            byte[] chunkBytes = new byte[chunkLengths.get(chunkNum)];

            System.arraycopy(firmware, chunkPos, chunkBytes, 0, chunkLengths.get(chunkNum));

            frame = ArrayUtils.addAll(frame, generateFirmwareChunk(chunkBytes, address+chunkPos, fwtype));
            chunkPos += chunkLengths.get(chunkNum);
            chunkNum++;
        }


        //Log.e(TAG, Utilities.byteArrayToHexString(frame));


        byte[] emptyChunk = {0x07, fwtype, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] rebootChunk = {0x07, reboot, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        frame = ArrayUtils.addAll(frame, emptyChunk);
        frame = ArrayUtils.addAll(frame, rebootChunk);


        //CRC + zero padding (plaintext footer, this part will be overwritten by encryption)
        byte[] crcPart = new byte[frame.length-10];
        System.arraycopy(frame, 10, crcPart, 0, crcPart.length);
        byte[] crcFinal = Utilities.hexStringToByteArray(Encoding.crc(Utilities.byteArrayToHexString(crcPart)));
        frame = ArrayUtils.add(frame, crcFinal[1]);
        frame = ArrayUtils.add(frame, crcFinal[0]);

        byte[] padding = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        frame = ArrayUtils.addAll(frame, padding);


        byte[] frameLength = Utilities.intToByteArray(frame.length-18); //returns 4 bytes, we only need 3 bytes length (reverse byte order)
        frame = ArrayUtils.add(frame, frameLength[0]);
        frame = ArrayUtils.add(frame, frameLength[1]);
        frame = ArrayUtils.add(frame, frameLength[2]);

        //Log.e(TAG, Utilities.byteArrayToHexString(frame));

        return Utilities.byteArrayToHexString(frame);

    }

    /*
    Generate firmware chunk which flashes APP or BSL.
    TODO Currently Fitbit Flex only, hence starting with 0x07.
     */
    private static byte[] generateFirmwareChunk(byte[] firmware, int address, byte fwtype) {

        byte[] chunk = new byte[2];

        chunk[0] = 0x07; //Flex
        chunk[1] = fwtype; //BSL/APP

        chunk = ArrayUtils.addAll(chunk, Utilities.intToByteArray(address));
        chunk = ArrayUtils.addAll(chunk, Utilities.intToByteArray(firmware.length)); //length is required twice in frame format...
        chunk = ArrayUtils.addAll(chunk, Utilities.intToByteArray(firmware.length));
        chunk = ArrayUtils.addAll(chunk, Utilities.hexStringToByteArray(Utilities.rotateBytes(Encoding.crc(Utilities.byteArrayToHexString(firmware)))));
        chunk = ArrayUtils.addAll(chunk, firmware);


        //Log.e(TAG, Utilities.byteArrayToHexString(chunk));

        return chunk;

    }

    public static String rebootToBSL(Activity activity)  {
        return "todo";
        /*
            def rebootBsl(self):
        frame = [0x30, 0x02, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x30, 0x9E, 0x00, 0x00, 0x07, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xC5, 0x93, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x24, 0x00, 0x00]
        [fw, fwlen] = self.encodeFirmware(frame)
        fwlen = 0x39
        return [fw, fwlen]
         */
    }


    public static String rebootToApp(Activity activity)  {
        return "todo";
        /*
    def rebootApp(self):
        frame = [0x30, 0x02, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x30, 0x9e, 0x00, 0x00, 0x07, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x07, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x69, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x24, 0x00, 0x00]
        [fw, fwlen] = self.encodeFirmware(frame)
        fwlen = 0x39
        return [fw, fwlen]
         */
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
        cipher.init(true,new ParametersWithIV(new KeyParameter(Utilities.hexStringToByteArray(FitbitDevice.ENCRYPTION_KEY)), counter));


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
