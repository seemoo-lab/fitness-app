package seemoo.fitbit.dialogs;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import seemoo.fitbit.R;
import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.miscellaneous.CustomFwfileArrayAdapter;
import seemoo.fitbit.miscellaneous.DownloadFileFromURL;
import seemoo.fitbit.miscellaneous.FirmwareFileDescriptor;
import seemoo.fitbit.miscellaneous.JSONFwFileParser;

public class FwDownloadDialog extends DialogFragment {

    public static final String REPO_BASE_URL = "https://raw.githubusercontent.com/seemoo-lab/fitness-firmware/master";

    public static final String FWDOWNLOAD_FRAGMENT_TAG = "FWDOWNLOAD_FRAGMENT_TAG";
    public static final String FLASHDIALOG_TAG = "FLASHDIALOG_TAG";
    public static final String WORKACTIVITY_TAG = "WORKACTIVITY_TAG";


    private WorkActivity mActivity;
    private FirmwareFlashDialog flashDialog;

    private ListView myListView;
    private ProgressBar pb_fwdownload;

    private ArrayAdapter arrayAdapter;
    private ArrayList<FirmwareFileDescriptor> fwfiles = null;

    public FwDownloadDialog() {
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mActivity = (WorkActivity) args.getSerializable(WORKACTIVITY_TAG);
        flashDialog = (FirmwareFlashDialog) args.getSerializable(FLASHDIALOG_TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.dialog_fwdownload, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel_fwdownload);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment prev = getFragmentManager().findFragmentByTag(FWDOWNLOAD_FRAGMENT_TAG);
                if (prev != null) {
                    DialogFragment df = (DialogFragment) prev;
                    df.dismiss();
                }
            }
        });

        getDialog().setTitle("Download firmware files");

        myListView = (ListView) view.findViewById(R.id.lv_list_fwfiles);

        ArrayList<String> strings = new ArrayList<String>(Arrays.asList("Loading firmware file index ..."));
        arrayAdapter
                = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, strings);

        myListView.setAdapter(arrayAdapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (fwfiles != null) {
                    FirmwareFileDescriptor file = fwfiles.get(position);
                    String url = REPO_BASE_URL + "/" + file.getLocation();
                    String localFilename = (file.getDeviceName() + "_" + file.getFwshortname() + ".bin").toLowerCase();
                    new DownloadFileFromURL(FwDownloadDialog.this).execute(url, localFilename);
                }

            }
        });

        pb_fwdownload = (ProgressBar) view.findViewById(R.id.pb_fwdownload);

        new JSONFwFileParser(mActivity, this).execute();
    }

    public void onFwIndexfileResult(ArrayList<FirmwareFileDescriptor> list) {

        fwfiles = list;
        arrayAdapter = new CustomFwfileArrayAdapter(mActivity, list);
        arrayAdapter.notifyDataSetChanged();
        myListView.setAdapter(arrayAdapter);
    }

    public void fileDownloaded(String file_url) {
        if (file_url != null && !file_url.matches("")) {
            flashDialog.onFilePickerResult(file_url);
            Toast.makeText(mActivity, "Download completed. Stored at:\r\n" + file_url, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mActivity, "Download failed.", Toast.LENGTH_LONG).show();
        }

        closeDialog();
    }

    public void closeDialog() {
        Fragment prev = getFragmentManager().findFragmentByTag(FWDOWNLOAD_FRAGMENT_TAG);
        if (prev != null) {
            DialogFragment df = (DialogFragment) prev;
            df.dismiss();
        }
    }

    public void setProgress(int progress) {
        pb_fwdownload.setProgress(progress);
    }

}


