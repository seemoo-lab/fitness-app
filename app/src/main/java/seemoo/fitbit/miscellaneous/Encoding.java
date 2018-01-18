package seemoo.fitbit.miscellaneous;

import java.util.ArrayList;
import android.util.Log;

/**
 * Created by jiska on 12/25/17.
 */

public class Encoding {
    private final static String TAG = Encoding.class.getSimpleName();

    /**
     * Calculates the CRC-CCITT (xModem) of the input string.
     * FIXME string version makes "0abc" an "abc" and then an "c0ab"...
     * @param input The input string.
     * @return The CRC-CCITT of the input string.
     */
    public static String crc(String input) {
        byte[] args = Utilities.hexStringToByteArray(input);
        int polynomial = 0x1021;   // 0x1021 = x^16 + x^12 + x^5 + 1
        int crc = 0x0000;
        for (byte b : args) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                // If coefficient of bit and remainder polynomial = 1 xor crc with polynomial
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        crc &= 0xffff;
        String crcFinal =  Utilities.intToHexString(crc);
        while (crcFinal.length()<4) {
            crcFinal = "0" + crcFinal;
        }
        Log.e(TAG, crcFinal);
        return crcFinal;
    }


    /**
     * SLIP encrypts the input and cuts it into 20 byte parts. Returns the result as an array list.
     *
     * @param input The input data as a string.
     * @return The array list withe encrypted input.
     */
    public static ArrayList<String> slip(String input) {
        ArrayList<String> result = new ArrayList<>();
        String tempChunk = "";
        int positionEncode = 0;
        int counter = 0;
        int length = 0;
        String substring = "";
        if (input == null) {
            return null;
        } else {
            /*while (positionEncode < input.length()) {//encoding of first byte in line
                if (positionEncode + 40 < input.length()) { // line is 20 byte long
                    if (input.substring(positionEncode, positionEncode + 2).equals("c0")) {
                        temp = temp + "dbdc" + input.substring(positionEncode + 2, positionEncode + 40);
                    } else if (input.substring(positionEncode, positionEncode + 2).equals("db")) {
                        temp = temp + "dbdd" + input.substring(positionEncode + 2, positionEncode + 40);
                    } else {
                        temp = temp + input.substring(positionEncode, positionEncode + 40);
                    }
                    positionEncode = positionEncode + 40;
                } else if (positionEncode + 4 < input.length()) { // line is between one and 20 byte long
                    if (input.substring(positionEncode, positionEncode + 2).equals("c0")) {
                        temp = temp + "dbdc" + input.substring(positionEncode + 2, input.length());
                    } else if (input.substring(positionEncode, positionEncode + 2).equals("db")) {
                        temp = temp + "dbdd" + input.substring(positionEncode + 2, input.length());
                    } else {
                        temp = temp + input.substring(positionEncode, input.length());
                    }
                    break;
                } else { // line is shorter than one byte
                    temp = temp + input.substring(positionEncode, input.length());
                    break;
                }
            }
            int position = 0;
            while (position < temp.length()) { //cut in 20 byte parts
                if (position + 40 < temp.length()) {
                    result.add(temp.substring(position, position + 40));
                    position = position + 40;
                } else {
                    result.add(temp.substring(position, temp.length()));
                    break;
                }
            }*/

            while(input.length() > 0) {

                if(counter == 2020) {
                    String waaaaht = " ";
                }

                if(input.length() >= 2) {

                    if (input.substring(0, 2).toLowerCase().equals("c0")) {
                        if(input.length() >= 40) {
                            tempChunk = "dbdc" + input.substring(2, 38);
                            input = input.substring(38);
                        } else if((input.length() < 40) && (input.length() >= 2)){

                            if((tempChunk.length() + 4) > 40 ) {

                                tempChunk = "dbdc" + input.substring(2, 38);
                                input = input.substring(38);

                            }else {

                                tempChunk = "dbdc" + input.substring(2);
                                input = "";
                            }

                        } else {
                            tempChunk = input;
                            input = "";
                        }

                    } else if (input.substring(0, 2).toLowerCase().equals("db")) {
                        if(input.length() >= 40) {
                            tempChunk = "dbdd" + input.substring(2, 38);
                            input = input.substring(38);
                        } else if((input.length() < 40) && (input.length() >= 2)){

                            if((tempChunk.length() + 4) > 40 ) {

                                tempChunk = "dbdd" + input.substring(2, 38);
                                input = input.substring(38);

                            }else {

                                tempChunk = "dbdd" + input.substring(2);
                                input = "";
                            }
                        } else {
                            tempChunk = input;
                            input = "";
                        }

                    } else {

                        if(input.length() >= 40) {
                            tempChunk = input.substring(0, 40);
                            input = input.substring(40);
                        } else {
                            tempChunk = input;
                            input = "";
                        }
                    }

                }

                result.add(tempChunk);

                tempChunk = "";
                counter++;
            }

            return result;
        }
    }

}
