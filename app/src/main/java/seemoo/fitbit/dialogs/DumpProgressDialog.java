package seemoo.fitbit.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.EventListener;

import seemoo.fitbit.R;
import seemoo.fitbit.events.DumpProgressEvent;

// TODO dialog closed -> second dump started -> dialog titled with second dump type, but showing first dump progress
// should be fixed by actually aborting dump

public class DumpProgressDialog extends Dialog {

    private final String TAG = "DumpProgressDialog";

    private TextView tv_dump_prog_val = null;
    private ProgressBar pb_dump_progress;
    private int prog_val = 0;

    public DumpProgressDialog(@NonNull Context context, String dialogTitle) {
        super(context);
        setContentView(R.layout.dialog_dump_progress);
        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        TextView tv_dump_prog_title = (TextView) findViewById(R.id.tv_dump_prog_title);
        tv_dump_prog_title.setText(dialogTitle);

        tv_dump_prog_val = (TextView) findViewById(R.id.tv_dump_prog_val);
        tv_dump_prog_val.setText("wait for it ...");

        //seems not to be necessary to change progressbar values, as by default it is indeterminate, just as we need it in this case
        pb_dump_progress = (ProgressBar) findViewById(R.id.pb_dump_progress);
        pb_dump_progress.setIndeterminate(true);
        pb_dump_progress.setActivated(true);
    }

    // TODO touch outside dialog -> show alert (abort? yesno)
    public void dismissDialog() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DumpProgressEvent event) {
        prog_val += event.getSize();
        if(!event.isDumpComplete()){
            tv_dump_prog_val.setText("" + prog_val + " byte received");
        }else{
            pb_dump_progress.setIndeterminate(false);
            tv_dump_prog_val.setText("Dump complete. Total bytes received: " + prog_val);
            // sometimes another dump request is pending. set progress 0 to avoid wrong values
            prog_val = 0;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        prog_val = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
