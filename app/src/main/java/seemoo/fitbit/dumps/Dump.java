package seemoo.fitbit.dumps;

import android.util.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;

public class Dump {

    protected final String TAG = this.getClass().getSimpleName();
    private ArrayList<MinuteRecord> minuteRecords = new ArrayList<>();
    private ArrayList<DailySummaryRecord> dailySummaryArray = new ArrayList<>();
    private FITBIT_MODEL fitbitModel;
    private String demoDump = "";

    enum FITBIT_MODEL {Zip, One, Flex, Charge, Charge_HR, Alta, Surge, Electron, Ionic, Unknown}

    public Dump(String plaintext) {
        parseFitbitModel(plaintext);
        parseMinuteRecords(plaintext);
        parseDailySummary(plaintext);
    }

    private void parseFitbitModel(String plaintext) {
        String id = plaintext.substring(30, 32);
        switch (id) {
            case "01":
                fitbitModel = FITBIT_MODEL.Zip;
                break;
            case "05":
                fitbitModel = FITBIT_MODEL.One;
                break;
            case "07":
                fitbitModel = FITBIT_MODEL.Flex;
                break;
            case "08":
                fitbitModel = FITBIT_MODEL.Charge;
                break;
            case "12":
                fitbitModel = FITBIT_MODEL.Charge_HR;
                break;
            case "15":
                fitbitModel = FITBIT_MODEL.Alta;
                break;
            case "10":
                fitbitModel = FITBIT_MODEL.Surge;
                break;
            case "11":
                fitbitModel = FITBIT_MODEL.Electron;
                break;
            case "1b":
                fitbitModel = FITBIT_MODEL.Ionic;
                break;
            default:
                fitbitModel = FITBIT_MODEL.Unknown;
        }
    }

    private void parseMinuteRecords(String dumpPlaintext) {
        if (fitbitModel == FITBIT_MODEL.Flex && dumpPlaintext.split("c0c0dbdcdd").length >= 2) {
            String minuteRecordText = dumpPlaintext.split("c0c0dbdcdd")[1];
            String timestampStr = minuteRecordText.substring(0, 8);

            minuteRecordText = minuteRecordText.substring(8, minuteRecordText.length());

            Timestamp initialTimestamp = new Timestamp(Long.parseLong(timestampStr, 16));
            Date date = new Date(initialTimestamp.getTime() * 1000L);

            for (int j = 0; j + 8 <= minuteRecordText.length(); j = j + 8) {
                String record = minuteRecordText.substring(j, j + 8);
                String stepsStr = record.substring(4, 6);

                int steps = Integer.parseInt(stepsStr, 16);

                //System.out.println(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "Steps: " + steps);
                date = new Date(date.getTime() + 60000);
                minuteRecords.add(new MinuteRecord(new Timestamp(date.getTime()), steps));

            }
        } else {
            Log.i(TAG, "Problems during dump parsing: No MinuteRecords found.");
        }

    }

    private void parseDailySummary(String plaintext) {
        if ((fitbitModel == FITBIT_MODEL.Flex || fitbitModel == FITBIT_MODEL.One) &&
                plaintext.split("c0c0dbdcdd").length >= 4) {

            String dailySummary = plaintext.split("c0c0dbdcdd")[3];
            dailySummary = dailySummary.replaceAll("dbdc", "c0");
            dailySummary = dailySummary.replaceAll("dbdd", "db");

            int recordSize;
            switch (fitbitModel) {
                case Flex:
                    recordSize = 40;
                    break;
                case One:
                    recordSize = 32;
                    break;
                default:
                    recordSize = 40;
            }

            LinkedHashMap<String, DailySummaryRecord> uniqueDailySummaryRecords = new LinkedHashMap<>();
            for (int i = 0; i < dailySummary.length() - 16; i = i + recordSize) {
                String timeStamp = dailySummary.substring(i, i + 8);

                String reversedTimestamp = "";
                for (int j = 4; j > 0; j--) {
                    reversedTimestamp = reversedTimestamp + timeStamp.substring((j - 1) * 2, j * 2);
                }
                Timestamp currentTimestamp = new Timestamp(Long.parseLong(reversedTimestamp, 16));
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(currentTimestamp.getTime() * 1000);
                if ((calendar.get(Calendar.YEAR) < 2010 || calendar.get(Calendar.YEAR) > 2020)) {
                    break;
                }
                ;

                String steps = dailySummary.substring(i + 12, i + 16);

                // change order since both reverse
                String stepFinal = "";
                for (int t = 2; t > 0; t--) {
                    stepFinal = stepFinal + steps.substring((t - 1) * 2, t * 2);
                }


                DailySummaryRecord dailySummaryRecord = new DailySummaryRecord(currentTimestamp,
                        Integer.parseInt(stepFinal, 16));

                String day = new SimpleDateFormat("E dd.MM.yy").format(currentTimestamp.getTime() * 1000);

                uniqueDailySummaryRecords.put(day, dailySummaryRecord);
            }

            for (DailySummaryRecord dailySummaryRecord : uniqueDailySummaryRecords.values()) {
                dailySummaryArray.add(dailySummaryRecord);
            }
        }
    }

    public ArrayList<MinuteRecord> getMinuteRecords() {
        return minuteRecords;
    }

    public ArrayList<DailySummaryRecord> getDailySummaryArray() {
        return dailySummaryArray;
    }




}
