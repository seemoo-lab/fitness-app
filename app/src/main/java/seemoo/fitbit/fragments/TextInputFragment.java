package seemoo.fitbit.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.Serializable;

import seemoo.fitbit.R;

public class TextInputFragment extends Fragment {
    private static final String ARG_PARAM_USER_HINT = "ARG_PARAM_USER_HINT";
    private static final String ARG_PARAM_INPUT_DEFAULT = "ARG_PARAM_INPUT_DEFAULT";
    private static final String ARG_PARAM_INTERFACE = "ARG_PARAM_INTERFACE";

    private String userHint = "";
    private String inputDefault = "";
    private OnOkButtonClickInterface onOkButtonClickInterface;

    public TextInputFragment() {
        // Required empty public constructor
    }

    public static TextInputFragment newInstance(String userHint, String inputDefault, OnOkButtonClickInterface onOkButtonClickInterface) {
        TextInputFragment fragment = new TextInputFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_USER_HINT, userHint);
        args.putString(ARG_PARAM_INPUT_DEFAULT, inputDefault);
        args.putSerializable(ARG_PARAM_INTERFACE, onOkButtonClickInterface);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userHint = getArguments().getString(ARG_PARAM_USER_HINT);
            inputDefault = getArguments().getString(ARG_PARAM_INPUT_DEFAULT);
            onOkButtonClickInterface = (OnOkButtonClickInterface) getArguments().getSerializable(ARG_PARAM_INTERFACE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_text_input, container, false);
        ((TextView) rootView.findViewById(R.id.fragment_text_input_textView)).setText(userHint);

        final EditText editText = (EditText) rootView.findViewById(R.id.fragment_text_input_editText);
        editText.setText(inputDefault);
        ((Button) rootView.findViewById(R.id.fragment_text_input_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOkButtonClickInterface.onOkButtonClick(editText.getText().toString());
            }
        });
        return rootView;
    }

    public interface OnOkButtonClickInterface extends Serializable {
        void onOkButtonClick(String enteredText);
    }

}
