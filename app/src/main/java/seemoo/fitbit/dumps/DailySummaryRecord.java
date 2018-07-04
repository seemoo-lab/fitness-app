package seemoo.fitbit.dumps;

import java.sql.Timestamp;

public class DailySummaryRecord {
    private Timestamp timestamp;
    private int steps;

    public DailySummaryRecord(Timestamp timestamp, int steps) {
        this.timestamp = timestamp;
        this.steps = steps;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getSteps() {
        return steps;
    }

}
