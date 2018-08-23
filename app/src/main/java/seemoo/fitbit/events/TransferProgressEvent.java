package seemoo.fitbit.events;

public class TransferProgressEvent {

    public static final int EVENT_TYPE_DUMP = 0;
    public static final int EVENT_TYPE_FW = 1;

    private int event_type = EVENT_TYPE_DUMP;

    public static final byte STATE_START = 0;
    public static final byte STATE_TRNSFR = 1;
    public static final byte STATE_STOP = 2;

    public static final byte STATE_REBOOT_FIN = 3;

    private byte transferState = STATE_TRNSFR;

    private static long lastEvtTimestamp = Long.MAX_VALUE;
    private int size = 0;
    private int totalSize = 0;

    public TransferProgressEvent() {
        lastEvtTimestamp = System.currentTimeMillis() / 1000;
    }
    public TransferProgressEvent(int event_type) {
        this.event_type = event_type;
        lastEvtTimestamp = System.currentTimeMillis() / 1000;
    }
    public TransferProgressEvent(int event_type, int size) {
        this.event_type = event_type;
        this.size = size;
        lastEvtTimestamp = System.currentTimeMillis() / 1000;
    }

    public static long getLastEvtTimestamp() {
        return lastEvtTimestamp;
    }

    public boolean isRebootFinished() {return transferState == STATE_REBOOT_FIN;}

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


    public int getEvent_type() {
        return event_type;
    }

    public void setEvent_type(int event_type) {
        this.event_type = event_type;
    }
}
