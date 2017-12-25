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

public class Firmware { //TODO implement galileo/firmware.py here

    /*
    Fitbit Flex CRC format is XMODEM, used for plaintext frame checksums and inner checksum in firmware.
    TODO did sven already implement this for c024?
     */
    public String crcCalc (byte[] data) {
        return "todo";
    }


    /*

     */
    public static String generateFirmwareFrame(byte[] start, byte[] end, byte[] address, boolean isBSL, Activity activity) {
        return "todo";

        //TODO move read file function to utilities, don't keep it in Crypto
        /*
    firmware_all = self.readBinaryFile(fwfile)

        #logger.debug(self.crcCalc(str(bytearray(firmware[fwstart:fwend]))))

        firmware = firmware_all[fwstart:fwend]

        #bit flip in flash, origin of this seems to be active readout
        if (len(firmware)>0x204) and (fwtype==1 or fwtype==2):
            firmware[0x204] = 0x00

            #there is even a checksum inside the firmware, this is needed for modified firmware, not for downgrades
            #-> fixes error: RF_ERR_BSL_MISSING_OR_INVALID
            #CRC end position for APP is 0x26000, which is an offset of 0x20 but why??
            # otherwise: RF_ERR_APP_MISSING_OR_INVALID
            if (fwtype == 1):
                checksum_in_firmware = self.crcCalc(str(bytearray(firmware[0:0x200] + firmware[0x208:])))
            else:
                checksum_in_firmware = self.crcCalc(str(bytearray(firmware_all[fwstart:fwstart+0x200] + firmware_all[fwstart+0x208:fwstart+0x26000])))

            checksum_in_firmware = map(ord, checksum_in_firmware.decode("hex"))
            firmware[0x200] = checksum_in_firmware[1]
            firmware[0x201] = checksum_in_firmware[0]

            # FIXME RF_ERR_APP_MISSING_OR_INVALID
        else:
            logger.debug('very short firmware, skipping checksum embedded in the firmware itself')



        #firmware_raw = firmware_raw[fwstart:fwend]



        header = [0x30, 0x02, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00]
        len_min_4 = i2lsba(len(firmware)+48, 4) #309e00
        #len_min_4 = [0xaa, 0xaa, 0xaa, 0xaa]
        #len_min_4 = [0x30, 0x9e, 0x00]
        #logger.debug(len_min_4)

        frame = header + len_min_4


        #BSL is one small chunk, APP is three chunks
        if (fwtype == 1): #BSL
            chunk_len = [0x100000]
        elif (fwtype == 2): #APP
            chunk_len = [0x6000, 0x10000, 0x10000]
        else: # we don't know
            chunk_len = [fwend - fwstart]


        chunk_num = 0
        chunk_pos = 0
        #logger.debug('frame before chunks: %s', a2x(frame))

        while (chunk_num < len(chunk_len)):
            logger.debug(chunk_num*chunk_len[chunk_num])
            frame = frame + self.generateFirmwareChunk(firmware[chunk_pos:chunk_pos+chunk_len[chunk_num]], address+chunk_pos, fwtype)
            chunk_pos = chunk_pos + chunk_len[chunk_num]
            chunk_num = chunk_num + 1

        #frame = frame + self.generateFirmwareChunk(firmware, address, 1)

        #logger.debug(a2x(firmware[0:20]))


        #logger.debug('Parsed firmware start 1: %s', a2x(frame[0:20]))
        #logger.debug('Parsed firmware start 2: %s', a2x(frame[20:40]))
        #logger.debug('Parsed firmware start 3: %s', a2x(frame[40:60]))


        footer1 = [0x07, fwtype, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]
        footer2 = [0x07, fwtype+2, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00]

        #TODO: i think 07 01 is bsl update, 07 03 is reboot to bsl, 07 02 is app update and 07 04 is app boot


        frame = frame + footer1 + footer2

        checksum_final = self.crcCalc(str(bytearray(frame[10:])))
        checksum_final = map(ord, checksum_final.decode("hex"))

        padding = [0x00, 0x00, 0x00, 0x00, 0x00, 0x00]

        len_end = i2lsba(len(frame) - 10, 3)

        frame.append(checksum_final[1])
        frame.append(checksum_final[0])
        frame = frame + padding + len_end
        #logger.debug('Parsed firmware end    : %s', a2x(frame[-20:]))

        #logger.debug(a2x(frame))



        return self.encodeFirmware(frame)

         */
    }

    /*
    Generate firmware chunk which flashes APP or BSL.
    TODO Currently Fitbit Flex only, hence starting with 0x07.
     */
    private static byte[] generateFirmwareChunk(byte[] firmware, byte[] address, boolean isBSL) {
        /*     def generateFirmwareChunk(self, firmware,  address, fwtype):
        whatever = [0x07, fwtype]
        fwaddress = i2lsba(address, 4)
        chunk_len = i2lsba(len(firmware), 4) #009e00

        #logger.debug(a2x(chunk_len))


        chunk_len = chunk_len + chunk_len #put this twice



        #checksum_chunk = self.crcCalc(firmware_raw) # this works!
        #logger.debug(checksum_chunk)
        checksum_chunk = self.crcCalc(str(bytearray(firmware)))
        #logger.debug(checksum_chunk)
        #logger.debug(len(firmware))
        checksum_chunk = map(ord, checksum_chunk.decode("hex"))


        frame = whatever + fwaddress + chunk_len
        #frame = whatever + fwaddress + [0xaa, 0xaa, 0xaa, 0xaa]
        frame.append(checksum_chunk[1]) #inverse checksum order
        frame.append(checksum_chunk[0])
        frame.extend(firmware)
        logger.debug('--- Parsed firmware chunk start: %s', a2x(frame[0:32]))

        return frame
    */
        return null;

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
