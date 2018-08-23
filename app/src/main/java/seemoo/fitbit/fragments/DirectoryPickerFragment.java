package seemoo.fitbit.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import seemoo.fitbit.R;
import seemoo.fitbit.activities.WorkActivity;
import seemoo.fitbit.miscellaneous.FileUriHelper;

import static android.app.Activity.RESULT_OK;

public class DirectoryPickerFragment extends Fragment {
    private static final String ARG_PARAM_INPUT_DEFAULT = "ARG_PARAM_INPUT_DEFAULT";
    public static final int PATH_REQUEST_CODE = 7753;

    private String inputDefault = "";
    private EditText editText;

    public DirectoryPickerFragment() {
        // Required empty public constructor
    }

    public static DirectoryPickerFragment newInstance(String inputDefault) {
        DirectoryPickerFragment fragment = new DirectoryPickerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_INPUT_DEFAULT, inputDefault);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            inputDefault = getArguments().getString(ARG_PARAM_INPUT_DEFAULT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_directory_picker, container, false);

        editText = (EditText) rootView.findViewById(R.id.fragment_directory_picker_editText);
        editText.setText(inputDefault);
        ((Button) rootView.findViewById(R.id.fragment_directory_picker_ok_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null){
                    ((WorkActivity) getActivity()).directorySelected(editText.getText().toString());
                }
            }
        });

        final ImageButton pathButton = (ImageButton) rootView.findViewById(R.id.fragment_directory_picker_image_button);
        pathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPathSelectionDialog();
            }
        });

        return rootView;
    }

    public void startPathSelectionDialog() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(i,  PATH_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== DirectoryPickerFragment.PATH_REQUEST_CODE &&
                resultCode==RESULT_OK) {
            Uri uri = data.getData();
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                    DocumentsContract.getTreeDocumentId(uri));
            String path = FileUriHelper.getPath(getContext(), docUri);
            onDirectoyDialogResult(path);
        }
    }

    public void onDirectoyDialogResult(String path){
        editText.setText(path);
    }

}
