package seemoo.fitbit.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;
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

// TODO dialog closed -> second dump started -> dialog titled with second dump type, but showing first dump progress
// should be fixed by actually aborting dump

public class DumpProgressDialog extends Dialog {

    private final String TAG = "DumpProgressDialog";

    private boolean dumpComplete = false;

    private TextView tv_dump_prog_val = null;
    private ProgressBar pb_dump_progress;
    private int prog_val = 0;

    public DumpProgressDialog(@NonNull Context context, String dialogTitle) {
        super(context);
        setContentView(R.layout.dialog_dump_progress);
        Window window = this.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        setCanceledOnTouchOutside(false);

        TextView tv_dump_prog_title = (TextView) findViewById(R.id.tv_dump_prog_title);
        tv_dump_prog_title.setText(dialogTitle);

        tv_dump_prog_val = (TextView) findViewById(R.id.tv_dump_prog_val);
        tv_dump_prog_val.setText("wait for it ...");

        //seems not to be necessary to change progressbar values, as by default it is indeterminate, just as we need it in this case
        pb_dump_progress = (ProgressBar) findViewById(R.id.pb_dump_progress);
        pb_dump_progress.setIndeterminate(true);
        pb_dump_progress.setActivated(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DumpProgressEvent event) {
        prog_val += event.getSize();
        if(!event.isDumpComplete()){
            //TODO dump size differs from received bytes (duplicate TODO)
            tv_dump_prog_val.setText("" + prog_val + " byte received");
        }else{
            pb_dump_progress.setIndeterminate(false);
            tv_dump_prog_val.setText("Dump complete. Total bytes received: " + prog_val);
            dumpComplete = true;
            this.setCanceledOnTouchOutside(true);
            // sometimes another dump request is pending. set progress 0 to avoid wrong values
            prog_val = 0;
        }

    }

    @Override
    public void onBackPressed() {
        if(dumpComplete){
            Toast.makeText(getContext(),"dump Complete.",Toast.LENGTH_SHORT).show();
            DumpProgressDialog.super.onBackPressed();
        }else{
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.msg_abortdump)
                    .setTitle(R.string.caption_abortdump);
            builder.setPositiveButton(R.string.abort, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //TODO actually abort dump
                    Toast.makeText(getContext(),"dump aborted.",Toast.LENGTH_SHORT).show();
                    DumpProgressDialog.super.onBackPressed();
                }
            });
            builder.setNegativeButton(R.string.resume, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            dialog = builder.create();
            dialog.show();
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
