package seemoo.fitbit.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import seemoo.fitbit.R;
import seemoo.fitbit.activities.MainActivity;
import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.events.TransferProgressEvent;
import seemoo.fitbit.fragments.MainFragment;
import seemoo.fitbit.miscellaneous.FileUriHelper;

public class FirmwareFlashDialog extends Dialog implements Serializable {

    public static final int PICK_FWFILE_REQUEST = 673;
    private static String fw_path = "";

    private MainFragment mainFragment;
    private WorkActivity mActivity;

    private ImageButton btn_fwfile_select;
    private ImageButton btn_download_fwfile;
    private Button btn_fwflash_cancel;
    private Button btn_flash;
    private EditText et_fwflash;

    public FirmwareFlashDialog(@NonNull final WorkActivity pActivity, final MainFragment mainFragment) {
        super(pActivity);
        this.mainFragment = mainFragment;
        this.mActivity = pActivity;

        setContentView(R.layout.dialog_fwflash);
        setTitle(R.string.firmware_flash_dialog);

        btn_fwfile_select = (ImageButton) findViewById(R.id.btn_select_fwfile);
        btn_download_fwfile = (ImageButton) findViewById(R.id.btn_download_fwfile);
        btn_flash = (Button) findViewById(R.id.btn_flash);
        btn_fwflash_cancel = (Button) findViewById(R.id.btn_fwflash_cancel);
        et_fwflash = (EditText) findViewById(R.id.et_fwpath);
        final RadioButton rb_bsl_app = (RadioButton) findViewById(R.id.rdb_bsl_app);
        final RadioButton rb_bsl_only = (RadioButton) findViewById(R.id.rdb_bsl_only);
        final RadioButton rb_app_only = (RadioButton) findViewById(R.id.rdb_app_only);

        fw_path = et_fwflash.getText().toString();

        et_fwflash.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().matches("")) {
                    btn_flash.setEnabled(false);
                } else {
                    btn_flash.setEnabled(true);

                }
            }
        });

        btn_fwfile_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/octet-stream");
                mActivity.startActivityForResult(intent, PICK_FWFILE_REQUEST);
            }
        });

        btn_download_fwfile.setOnClickListener(new View.OnClickListener() {
            FwDownloadDialog fwDownloadDialog;
            @Override
            public void onClick(View view) {
                 fwDownloadDialog = new FwDownloadDialog();
                Bundle bundle = new Bundle();
                bundle.putSerializable(FwDownloadDialog.WORKACTIVITY_TAG, mActivity);
                bundle.putSerializable(FwDownloadDialog.FLASHDIALOG_TAG, FirmwareFlashDialog.this);
                fwDownloadDialog.setArguments(bundle);
                fwDownloadDialog.show(mActivity.getFragmentManager(), FwDownloadDialog.FWDOWNLOAD_FRAGMENT_TAG);
            }
        });

        btn_fwflash_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirmwareFlashDialog.this.cancel();
            }
        });

        btn_flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fw_path = et_fwflash.getText().toString();
                if (rb_bsl_app.isChecked()) {
                    new TransferProgressDialog(mActivity, "FIRMWARE UPLOAD (BSL)", TransferProgressDialog.TRANSFER_APP_TO_TRACKER).show();
                    mainFragment.flashFirmware(fw_path, false);
                } else if (rb_bsl_only.isChecked()) {
                    new TransferProgressDialog(mActivity, "FIRMWARE UPLOAD (BSL)", TransferProgressDialog.TRANSFER_APP_TO_TRACKER).show();
                    mainFragment.flashFirmware(fw_path, false);
                    FirmwareFlashDialog.this.cancel();
                } else if (rb_app_only.isChecked()) {
                    new TransferProgressDialog(mActivity, "FIRMWARE UPLOAD (app)", TransferProgressDialog.TRANSFER_APP_TO_TRACKER).show();
                    mainFragment.flashFirmware(fw_path, true);
                    FirmwareFlashDialog.this.cancel();
                }


            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TransferProgressEvent event) {
        if (event.getEvent_type() == TransferProgressEvent.EVENT_TYPE_FW) {
            if (event.isStopEvent()) {
                new TransferProgressDialog(mActivity, "FIRMWARE UPLOAD (app)", TransferProgressDialog.TRANSFER_APP_TO_TRACKER).show();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mainFragment.flashFirmware(fw_path, true);

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void passActivityResult(Intent data) {
        String path = FileUriHelper.getPath(getContext(), data.getData());
        onFilePickerResult(path);
    }

    public void onFilePickerResult(String path) {
        if (path != null) {
            et_fwflash.setText(path);
            if (path.matches("")) {
                btn_flash.setEnabled(false);
            } else {
                btn_flash.setEnabled(true);
            }
        }
    }
}
