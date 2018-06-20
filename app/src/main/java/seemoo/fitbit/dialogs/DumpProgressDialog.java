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
import seemoo.fitbit.events.DumpProgressEvent;

public class DumpProgressDialog extends Dialog {

    public static final boolean DUMP_APP_TO_TRACKER = true;
    public static final boolean DUMP_TRACKER_TO_APP = false;

    private final String TAG = this.getClass().getSimpleName();

    private Resources res = null;

    private TimeoutTimer timer;
    private boolean dumpComplete = false;

    private TextView tv_dump_prog_val = null;
    private ProgressBar pb_dump_progress;
    private int progVal = 0;

    public DumpProgressDialog(@NonNull Context context, String dialogTitle, boolean dumpAppToTracker) {
        super(context);

        res = getContext().getResources();

        setContentView(R.layout.dialog_dump_progress);
        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        setCanceledOnTouchOutside(false);

        TextView tv_dump_prog_title = (TextView) findViewById(R.id.tv_dump_prog_title);
        tv_dump_prog_title.setText(dialogTitle);

        // transmission info
        tv_dump_prog_val = (TextView) findViewById(R.id.tv_dump_prog_val);
        if (dumpAppToTracker) {
            tv_dump_prog_val.setText(R.string.sending_data);
        } else {
            tv_dump_prog_val.setText(R.string.wait_for_data);
        }

        // Progressbar. For a tracker->app dump there is no length info given, so the bar does not give any information on the progress actually
        pb_dump_progress = (ProgressBar) findViewById(R.id.pb_dump_progress);
        pb_dump_progress.setIndeterminate(true);
        pb_dump_progress.setActivated(true);

        timer = new TimeoutTimer();
        timer.startTimer();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DumpProgressEvent event) {
        progVal += event.getSize();
        if (!event.isDumpComplete()) {
            tv_dump_prog_val.setText(String.format(res.getString(R.string.bytes_received), progVal));
        } else {
            timer.stopTimer();
            this.dismiss();
        }

    }

    @Override
    public void onBackPressed() {
        if (dumpComplete) {
            Toast.makeText(getContext(), R.string.dump_complete, Toast.LENGTH_SHORT).show();
            DumpProgressDialog.super.onBackPressed();
        } else {
            Toast.makeText(getContext(), R.string.dump_in_progress, Toast.LENGTH_SHORT).show();
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


    // This timer checks regularly (every 100ms) whether the dump is still in progress. If no progress is detected for 10 seconds, user gets asked whether dump should be aborted
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
            tHandlerThread = new HandlerThread("DumpTimeoutThread");
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
                        // show Dialog to either confirm dump abort or extend the timeout
                        AlertDialog dialog;
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage(R.string.msg_dumptimeout)
                                .setTitle(R.string.caption_dumptimeout);
                        builder.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(getContext(), R.string.dump_aborted, Toast.LENGTH_SHORT).show();
                                DumpProgressDialog.super.onBackPressed();
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

        // timer may not be required anymore, e.g. when dump is complete
        private void stopTimer() {
            abortTimer = true;
        }

    }
}
