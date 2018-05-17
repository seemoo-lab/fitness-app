package seemoo.fitbit.dumps;


import android.util.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kenny on 26.04.18.
 */

public class Dump {



    ArrayList<MinuteRecord> minuteRecords = new ArrayList<>();
    ArrayList<String> dailySummaryArray = new ArrayList<>();
    String demoDump = "";
    protected final String TAG = this.getClass().getSimpleName();

    public Dump(String plaintext) {
        //parseMinuteRecords(plaintext);
    }


    public ArrayList<MinuteRecord> getMinuteRecords() {
        return minuteRecords;
    }

    public ArrayList<String> getDailySummaryArray() {
        return dailySummaryArray;
    }

    private void parseMinuteRecords(String dumpPlaintext) {

        //dumpPlaintext = demoDump;

        String id = dumpPlaintext.substring(30, 32);


        if(dumpPlaintext.split("c0c0dbdcdd").length>=2){

            String minuteRecordText = dumpPlaintext.split("c0c0dbdcdd")[1];


            if(!id.equals("05")) {


                String timestampStr = minuteRecordText.substring(0, 8);

                minuteRecordText = minuteRecordText.substring(8, minuteRecordText.length());


                Timestamp initialTimestamp = new Timestamp(Long.parseLong(timestampStr, 16));
                Date date = new Date(initialTimestamp.getTime() * 1000L);


                for (int j = 0; j < minuteRecordText.length(); j = j + 8) {

                    String record = minuteRecordText.substring(j, j + 8);
                    String stepsStr = record.substring(4, 6);


                    int steps = Integer.parseInt(stepsStr, 16);

                    //System.out.println(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "Steps: " + steps);
                    date = new Date(date.getTime() + 60000);
                    minuteRecords.add(new MinuteRecord(new Timestamp(date.getTime()), steps));

                }
            }

            String dailySummary = dumpPlaintext.split("c0c0dbdcdd")[3];
            dailySummary= dailySummary.replaceAll("dbdc","c0");
            dailySummary = dailySummary.replaceAll("dbdd", "db");

            for(int i = 0; i < dailySummary.length(); i = i + 32){
                String timeStamp = dailySummary.substring(i, i+8);
                String steps = dailySummary.substring(i+8, i+12);
                String unknown = dailySummary.substring(i+12, i+16);
                String reversedTimestamp = "";

                String stepFinal = "";
                String unknownFinal = "";
                for(int t = 2; t > 0; t--){
                    stepFinal = stepFinal +steps.substring((t-1)*2, t*2);
                    unknownFinal = unknownFinal + unknown.substring((t-1)*2, t*2);
                }

                for(int j = 4; j > 0; j++){
                    reversedTimestamp = reversedTimestamp + timeStamp.substring(j*2, (j-1)*2);
                }

                Timestamp currentTimestamp = new Timestamp(Long.parseLong(reversedTimestamp, 16));
                String timeStampString = new SimpleDateFormat("E dd.MM.yy HH").format(currentTimestamp.getTime()*1000);
                dailySummaryArray.add(timeStampString);
                dailySummaryArray.add("   Steps:" + Long.parseLong(stepFinal, 16) + "  Unknown:" +  Long.parseLong(unknownFinal, 16));
            }

        } else {
            Log.i(TAG, "Problems during dump parsing: No MinuteRecords found.");
        }

    }


}
