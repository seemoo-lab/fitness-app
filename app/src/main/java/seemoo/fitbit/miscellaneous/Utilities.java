package seemoo.fitbit.miscellaneous;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;
import android.graphics.Color;

import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.pqc.math.ntru.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;

import seemoo.fitbit.commands.Commands;
import seemoo.fitbit.information.Information;
import seemoo.fitbit.information.InformationList;
import seemoo.fitbit.interactions.Interactions;

import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.ValueDependentColor;

/**
 * Provides several general tools.
 */
public class Utilities {

    private final static String TAG = Utilities.class.getSimpleName();

    /**
     * Converts hex string into byte array.
     *
     * @param hexString The hex string to convert.
     * @return The converted byte array.
     */
    public static byte[] hexStringToByteArray(String hexString) {
        if (hexString == null) {
            Log.e(TAG, "Error: Utilities.hexStringToByteArray, hexString = null");
        } else {
            if (hexString.length() % 2 != 0) {
                hexString = "0" + hexString; //adding leading zero, fixed CRC conversion issue...
                Log.e(TAG, "Number of chars is odd: adding 0");
            }
            int len = hexString.length();
            byte[] result = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                result[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
            }
            return result;
        }
        return null;
    }

    /**
     * Converts byte array into hex string.
     *
     * @param byteArray The byte array to convert.
     * @return The converted hex string.
     */
    public static String byteArrayToHexString(byte[] byteArray) {
        if (byteArray == null) {
            Log.e(TAG, "Error: Utilities.byteArrayToHexString, byteArray = null");
        } else {
            char[] hexArray = "0123456789abcdef".toCharArray();
            char[] hexChars = new char[byteArray.length * 2];
            for (int j = 0; j < byteArray.length; j++) {
                int v = byteArray[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
        return null;
    }

    /**
     * Converts integer into hex string.
     *
     * @param value The integer to convert.
     * @return The converted hex string.
     */
    public static String intToHexString(int value) {
        //fixed authentication for short nonces by adding leading zero...
        String hex = Integer.toHexString(value);
        if (hex.length() % 2 != 0)
            hex = 0 + hex;
        return hex;
    }

    /**
     * Converts long into hex string.
     *
     * @param value The long to convert.
     * @return The converted hex string.
     */
    public static String longToHexString(long value) {
        return Long.toHexString(value);
    }

    /**
     * Converts hex string to integer.
     *
     * @param hexString The hex String to convert.
     * @return The converted integer.
     */
    public static int hexStringToInt(String hexString) {
        if (hexString == null) {
            Log.e(TAG, "Error: Utilities.hexStringToByteArray, hexString = null");
            return -1;
        } else {
            return (int) Long.parseLong(hexString, 16);
        }
    }

    /**
     * Converts base 64 into hex string.
     *
     * @param base64 The base 64 string.
     * @return The converted hex string.
     */
    public static String base64ToHex(String base64) {
        byte[] decoded;
        try {
            decoded = Base64.decode(base64, 0);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "No correct base64 input.");
            return "";
        }
        return String.format("%040x", new BigInteger(1, decoded));
    }

    public static String hexToBase64(String hex) {
        String encoded;
        try {
            encoded = Base64.encodeToString(hexStringToByteArray(hex), 0);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "No correct hex input.");
            return "";
        }
        return encoded;
    }

    /**
     * Converts binary to hex string.
     *
     * @param binary The binary string.
     * @return The converted hex string.
     */
    public static String binaryToHex(String binary) {
        if (binary == null) {
            return "";
        } else {
            int decimal = Integer.parseInt(binary, 2);
            return Integer.toString(decimal, 16);
        }
    }

    /**
     * Converts integer to byte array.
     *
     * @param value The integer to convert.
     * @return The converted byte array.
     */
    public static byte[] intToByteArray(int value) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(value);
        return byteBuffer.array();
    }

    /**
     * Rotates the bytes of a hex string. First byte becomes last byte ...
     *
     * @param hexString The hex string to rotate.
     * @return The rotated hex string.
     */
    public static String rotateBytes(String hexString) {
        String result = "";
        for (int i = hexString.length(); i > 1; i = i - 2) {
            result = result + hexString.substring(i - 2, i);
        }
        if (hexString.length() % 2 != 0) {
            result = result + hexString.substring(0, 1);
        }
        return result;
    }

    /**
     * Converts string into integer.
     *
     * @param string The string to convert.
     * @return The converted integer.
     */
    public static int stringToInt(String string) {
        int result = -1;
        try {
            result = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            Log.e(TAG, "No or wrong value entered. Using length calculation.");
        }
        return result;
    }

    /**
     * Attaches zeroes to the input string until the wanted length is reached.
     *
     * @param input  The string to attach zeroes.
     * @param length The wanted length of the result string.
     * @return The input string with attached zeroes, which has the wanted length.
     */
    public static String fixLength(String input, int length) {
        while (length > input.length()) {
            input = "0" + input;
        }
        return input;
    }

    /**
     * Converts the live mode byte array into a readable information list.
     *
     * @param value The byte array to convert.
     * @return A readable information list.
     */
    public static InformationList translate(byte[] value) {
        InformationList list = new InformationList("LiveMode");
        String data = Utilities.byteArrayToHexString(value);

        if(checkLiveModeReadout(value)) {
            try {
                list.add(new Information("X-Axis: 0x" + Utilities.rotateBytes(data.substring(0, 4))));
                list.add(new Information("Y-Axis: 0x" + Utilities.rotateBytes(data.substring(6, 10))));
                list.add(new Information("Z-Axis: 0x" + Utilities.rotateBytes(data.substring(12, 16))));

            } catch (Exception e) {
                Log.d(TAG, "translate: Live Mode contained insufficient data");
            }

        } else {

            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Utilities.hexStringToInt(Utilities.rotateBytes(data.substring(0, 8))) * 1000L);
                list.add(new Information("time: " + calendar.getTime()));
                list.add(new Information("steps: " + Utilities.hexStringToInt(Utilities.rotateBytes(data.substring(8, 16)))));
                list.add(new Information("distance: " + Utilities.hexStringToInt(Utilities.rotateBytes(data.substring(16, 24))) / 1000 + " m"));
                list.add(new Information("calories: " + Utilities.hexStringToInt(Utilities.rotateBytes(data.substring(24, 28))) + " METs"));
                list.add(new Information("elevation: " + Utilities.hexStringToInt(Utilities.rotateBytes(data.substring(28, 32))) / 10 + " floors"));

                //heart rate only available on some trackers, even the original app just solves this with if statement...
                if (data.length() > 32) {
                    list.add(new Information("very active minutes: " + Utilities.hexStringToInt(Utilities.rotateBytes(data.substring(32, 36)))));
                    list.add(new Information("heartRate: " + Utilities.hexStringToInt(Utilities.rotateBytes(data.substring(36, 38)))));
                    list.add(new Information("heartRateConfidence: " + Utilities.hexStringToInt(Utilities.rotateBytes(data.substring(38, 40)))));
                }
            } catch (Exception e) {
                Log.d(TAG, "translate: Live Mode contained insufficient data");
            }
        }
        return list;
    }

    /**
     * Converts the live mode byte array into a readable information list.
     *
     * @param value The byte array to convert.
     * @return A readable information list.
     */
    public static boolean checkLiveModeReadout(byte[] value) {
        String data = Utilities.byteArrayToHexString(value);
        boolean retValue;

        try {
           if(Utilities.rotateBytes(data.substring(26, 30)).compareTo("acc1") == 0) {
               retValue = true;
           } else {
               retValue = false;
           }

        } catch (Exception e) {
            retValue = false;
            Log.d(TAG, "translate: Live Mode contained insufficient data");
        }
        return retValue;
    }

    /**
     * Removes all spaces and new line commands from string.
     *
     * @param input The input string.
     * @return The input string without spaces and new line commands.
     */
    public static String removeSpaces(String input) {
        return input.replaceAll("\\s+", "");
    }

    /**
     * Returns the error corresponding to the error code.
     *
     * @param value The error code.
     * @return The error.
     */
    public static String getError(String value) {
        return ConstantValues.ERROR_CODES.get(value.substring(4, 8));
    }


    public static byte[] fullyReadFileToBytes(String pathname) throws IOException {

        File f = new File(pathname);
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis= new FileInputStream(f);

        byte error[] = {0};
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
            bytes = error;
        } finally {
            fis.close();
        }

        return bytes;
    }

    public static BarGraphSeries updateGraph(byte[] value) {

        String data = Utilities.byteArrayToHexString(value);

        Long xAxis = Long.parseLong(Utilities.rotateBytes(data.substring(0, 4)), 16);
        Long yAxis = Long.parseLong(Utilities.rotateBytes(data.substring(6, 10)), 16);
        Long zAxis = Long.parseLong(Utilities.rotateBytes(data.substring(12, 16)), 16);

        if(xAxis >= 32768) {
            xAxis = xAxis - 65535;
        }

        if(yAxis >= 32768) {
            yAxis = yAxis - 65535;
        }

        if(zAxis >= 32768) {
            zAxis = zAxis - 65535;
        }

        Log.d(TAG, "X: "+ xAxis.toString() + " Y: " + yAxis.toString() + " Z:" + zAxis.toString() );

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[] {
            new DataPoint(0.5,xAxis),
            new DataPoint(1,yAxis),
            new DataPoint(1.5,zAxis)
        });

        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                                          @Override
                                          public int get(DataPoint data) {

                                              int col = 0;

                                              if(data.getX() <= 0.6) {
                                                  col = Color.rgb(0,0,255);
                                              } else if((data.getX() >= 0.9 ) && (data.getX()<=1.1)) {
                                                  return Color.rgb(0,255,0);
                                              } else if(data.getX() >= 1.4) {
                                                  return Color.rgb(255,0,0);
                                              }

                                              return col;
                                          }
                                      });

        series.setSpacing(50);

        return series;
    }
}
