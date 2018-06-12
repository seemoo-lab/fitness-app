package seemoo.fitbit.events;

public class DumpProgressEvent {

    private static long lastEvtTimestamp = Long.MAX_VALUE;
    private int size = 0;
    private boolean dumpComplete = false;

    public DumpProgressEvent() {}

    public DumpProgressEvent(int size) {
        this.size = size;
        lastEvtTimestamp = System.currentTimeMillis()/1000;
    }


    public static long getLastEvtTimestamp() {
        return lastEvtTimestamp;
    }

    public boolean isDumpComplete() {
        return dumpComplete;
    }

    public void setDumpState(boolean dumpComplete) {
        this.dumpComplete = dumpComplete;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
