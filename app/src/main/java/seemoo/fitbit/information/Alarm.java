package seemoo.fitbit.information;

import java.math.BigInteger;

import seemoo.fitbit.miscellaneous.ConstantValues;
import seemoo.fitbit.miscellaneous.Utilities;

/**
 * An information of the special type alarm.
 */
public class Alarm extends Information {

    private boolean empty;
    private boolean[] days = new boolean[7];
    private int hours;
    private int minutes;
    private boolean repeat;

    /**
     * Creates an alarm.
     * @param data The data of the alarm.
     */
    public Alarm(String data) {
        super(data);
        translate();
    }

    /**
     * Translates the in the constructor given data into the corresponding format.
     */
    private void translate(){
        if(getData().equals(ConstantValues.EMPTY_ALARM)){
            empty = true;
        } else {
            hours = Utilities.hexStringToInt(Utilities.rotateBytes(getData().substring(0, 8))) / 3600;
            minutes = (Utilities.hexStringToInt(Utilities.rotateBytes(getData().substring(0, 8))) - hours * 3600) / 60;
            BigInteger tempInt = BigInteger.valueOf(Utilities.hexStringToInt(getData().substring(14, 16)));
            for (int i = 0; i < days.length; i++) {
                days[i] = tempInt.testBit(i);
            }
            repeat = tempInt.testBit(7);
        }
    }

    /**
     * Returns the data of the alarm as a readable string.
     * @return A readable string.
     */
    public String toString() {
        if (empty) {
            return "no alarm";
        } else {
            String result = "";
            if (days[0]) {
                result = result + "Mon ";
            }
            if (days[1]) {
                result = result + "Tue ";
            }
            if (days[2]) {
                result = result + "Wed ";
            }
            if (days[3]) {
                result = result + "Thu ";
            }
            if (days[4]) {
                result = result + "Fri ";
            }
            if (days[5]) {
                result = result + "Sat ";
            }
            if (days[6]) {
                result = result + "Sun ";
            }
            result = result + Utilities.fixLength("" + hours, 2) + ":" + Utilities.fixLength("" + minutes, 2);
            if (repeat) {
                result = result + " repeat";
            }
            return result;
        }
    }

    /**
     * Return the days of the alarm.
     * @return The days.
     */
    public boolean[] getDays(){
        return days;
    }

    /**
     * Returns if the alarm is empty.
     * @return True, is the alarm is empty.
     */
    public boolean isEmpty(){
        return empty;
    }

    /**
     * Returns the hours of the alarm.
     * @return The hours.
     */
    public int getHours(){
        return hours;
    }

    /**
     * Returns the minutes of the alarm.
     * @return The minutes.
     */
    public int getMinutes(){
        return minutes;
    }


    /**
     * Returns the raw data of the alarm as a string.
     * @return The raw data.
     */
    public String getRawOutput(){
        return super.getData();
    }

}
