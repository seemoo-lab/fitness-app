package seemoo.fitbit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import seemoo.fitbit.R;
import seemoo.fitbit.events.TransferProgressEvent;

public class TransferProgressDialog extends Dialog {

    public static final boolean TRANSFER_APP_TO_TRACKER = true;
    public static final boolean TRANSFER_TRACKER_TO_APP = !TRANSFER_APP_TO_TRACKER;

    private final String TAG = this.getClass().getSimpleName();

    private Resources res = null;

    private TimeoutTimer timer;
    private boolean transferStartStopState = false;
    private int totalSize = 0;

    private TextView tv_transfer_prog_val = null;
    private ProgressBar pb_transfer_progress;
    private int progVal = 0;

    public TransferProgressDialog(@NonNull Context context, String dialogTitle, boolean transferAppToTracker) {
        super(context);

        res = getContext().getResources();

        setContentView(R.layout.dialog_transfer_progress);
        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        setCanceledOnTouchOutside(false);

        TextView tv_transfer_prog_title = (TextView) findViewById(R.id.tv_transfer_prog_title);
        tv_transfer_prog_title.setText(dialogTitle);

        // transmission info
        tv_transfer_prog_val = (TextView) findViewById(R.id.tv_transfer_prog_val);
        if (transferAppToTracker) {
            tv_transfer_prog_val.setText(R.string.sending_data);
        } else {
            tv_transfer_prog_val.setText(R.string.wait_for_transmission);
        }

        // Progressbar. For a tracker->app transmission there is no length info given, so the bar does not give any information on the progress actually
        pb_transfer_progress = (ProgressBar) findViewById(R.id.pb_transfer_progress);
        pb_transfer_progress.setIndeterminate(true);
        pb_transfer_progress.setActivated(true);

        timer = new TimeoutTimer();
        timer.startTimer();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TransferProgressEvent event) {
        progVal += event.getSize();
        if (!event.startStopEvt()) {
            if (totalSize!=0){
                tv_transfer_prog_val.setText(String.format(res.getString(R.string.bytes_transmitted_wtotal), progVal, totalSize));
                pb_transfer_progress.setProgress(pb_transfer_progress.getProgress() + event.getSize());
            }else{
                tv_transfer_prog_val.setText(String.format(res.getString(R.string.bytes_transmitted), progVal));
            }
        } else {
            int totalSize = event.getTotalSize();
            if (totalSize != 0) {
                this.totalSize = totalSize;
                pb_transfer_progress.setMax(totalSize);
                pb_transfer_progress.setIndeterminate(false);
            } else {
                timer.stopTimer();
                this.dismiss();
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (transferStartStopState) {
            Toast.makeText(getContext(), R.string.transmission_complete, Toast.LENGTH_SHORT).show();
            TransferProgressDialog.super.onBackPressed();
        } else {
            Toast.makeText(getContext(), R.string.transmission_in_progress, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        progVal = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    // This timer checks regularly (every 100ms) whether the transfer is still in progress. If no progress is detected for 10 seconds, user gets asked whether the transmission should be aborted
    private class TimeoutTimer {

        // timer constraints/counters
        private final int TIMEOUT_MILLIS = 10000;
        private final int TIMEOUT_CHKINTVL = 100;
        private int timePassed = 0;
        private int lastProgVal = 0;

        // Thread handling
        private Handler timeoutHandler = null;
        private HandlerThread tHandlerThread = null;
        private boolean abortTimer = false;

        // create Timer with its own Thread, so it does not interfere with the UI
        private TimeoutTimer() {
            tHandlerThread = new HandlerThread("TransferTimeoutThread");
            tHandlerThread.start();
            timeoutHandler = new Handler(tHandlerThread.getLooper());
        }

        private void startTimer() {
            abortTimer = false;
            timePassed = 0;
            timeoutHandler.post(new Runnable() {
                @Override
                public void run() {
                    while (!abortTimer && (timePassed < TIMEOUT_MILLIS)) {
                        if (lastProgVal == progVal) {
                            // add the passed time to the counter
                            timePassed += TIMEOUT_CHKINTVL;
                        } else {
                            // update progress value
                            lastProgVal = progVal;
                            // prevent to sum up different minor stalls
                            timePassed = 0;
                        }
                        try {
                            Thread.sleep(TIMEOUT_CHKINTVL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!abortTimer) {
                        // show Dialog to either confirm transmission abort or extend the timeout
                        AlertDialog dialog;
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(R.string.msg_transfertimeout)
                                .setTitle(R.string.caption_transfertimeout);
                        builder.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(getContext(), R.string.transmission_aborted, Toast.LENGTH_SHORT).show();
                                TransferProgressDialog.super.onBackPressed();
                            }
                        });
                        builder.setPositiveButton(R.string.resume, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                startTimer();
                            }
                        });
                        dialog = builder.create();

                        // make sure dialog is closed via one of the dedicated buttons
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);

                        dialog.show();
                    }
                }
            });
        }

        // timer may not be required anymore, e.g. when transmission is complete
        private void stopTimer() {
            abortTimer = true;
        }

    }
}
