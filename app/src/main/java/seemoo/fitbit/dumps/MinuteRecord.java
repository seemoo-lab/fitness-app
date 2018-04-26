package seemoo.fitbit.dumps;

import java.sql.Timestamp;


/**
 * Created by stekreis on 26.04.18.
 */

public class MinuteRecord {

    public MinuteRecord(Timestamp timestamp, int steps) {
        this.timestamp = timestamp;
        this.steps = steps;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    private Timestamp timestamp;
    private int steps;

}
