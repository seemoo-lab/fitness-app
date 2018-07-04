package seemoo.fitbit.events;

public class TransferProgressEvent {

    private static long lastEvtTimestamp = Long.MAX_VALUE;
    private int size = 0;
    private int totalSize = 0;
    private boolean startStopEvt = false;

    public TransferProgressEvent() {
    }

    public TransferProgressEvent(int size) {
        this.size = size;
        lastEvtTimestamp = System.currentTimeMillis() / 1000;
    }


    public static long getLastEvtTimestamp() {
        return lastEvtTimestamp;
    }

    public boolean startStopEvt() {
        return startStopEvt;
    }

    public void setTransferState(boolean startStopStatus) {
        this.startStopEvt = startStopStatus;
    }

    public int getSize() {
        return size;
    }


    public void setSize(int size) {
        this.size = size;
    }


    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int sizeInBytes) {
        totalSize = sizeInBytes;
    }
}
