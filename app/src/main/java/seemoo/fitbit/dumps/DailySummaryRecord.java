package seemoo.fitbit.dumps;

import java.sql.Timestamp;

public class DailySummaryRecord {
    private Timestamp timestamp;
    private int steps;
    private int unknown;

    public DailySummaryRecord(Timestamp timestamp, int steps, int unknown) {
        this.timestamp = timestamp;
        this.steps = steps;
        this.unknown = unknown;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getSteps() {
        return steps;
    }

    public int getUnknown() {
        return unknown;
    }

}
