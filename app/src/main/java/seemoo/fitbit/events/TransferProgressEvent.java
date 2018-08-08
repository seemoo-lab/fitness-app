package seemoo.fitbit.events;

public class TransferProgressEvent {

    public static final byte STATE_START = 0;
    public static final byte STATE_TRNSFR = 1;
    public static final byte STATE_STOP = 2;

    private byte transferState = STATE_TRNSFR;

    private static long lastEvtTimestamp = Long.MAX_VALUE;
    private int size = 0;
    private int totalSize = 0;

    public TransferProgressEvent() {
    }

    public TransferProgressEvent(int size) {
        this.size = size;
        lastEvtTimestamp = System.currentTimeMillis() / 1000;
    }


    public static long getLastEvtTimestamp() {
        return lastEvtTimestamp;
    }

    public boolean isStartEvent(){
        return transferState == STATE_TRNSFR;
    }

    public boolean isStopEvent(){
        return transferState == STATE_STOP;
    }

    public boolean isProgressEvent(){
        return transferState == STATE_TRNSFR;
    }

    public byte getTransferState(){
        return transferState;
    }

    public void setTransferState(byte state) {
        this.transferState = state;
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
