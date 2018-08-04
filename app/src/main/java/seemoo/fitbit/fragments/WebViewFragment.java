package seemoo.fitbit.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import seemoo.fitbit.R;
import seemoo.fitbit.activities.WorkActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebViewFragment extends Fragment {

    public WebViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootFragmentView = inflater.inflate(R.layout.fragment_web_view, container, false);

        final Button finishWebViewButton = (Button) rootFragmentView.findViewById(R.id.fragment_webview_finish_button);
        finishWebViewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WorkActivity) getActivity()).finishClickWebView(true);
            }
        });

        WebView webView = (WebView) rootFragmentView.findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onLoadResource(WebView view, String url) {
                if (url.equals("https://www.fitbit.com/oauth") || url.equals("https://www.fitbit.com/oauth/oauth_login_allow")) {
                    Toast.makeText(getContext(),"Please copy the PIN.",Toast.LENGTH_LONG).show();

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    finishWebViewButton.setVisibility(View.VISIBLE);

                                }
                            });
                        }
                    }, 3000);
                }
            }
        });

        ((WorkActivity) getActivity()).getHttpsClient().getVerifier(webView);

        return rootFragmentView;
    }

    /**
     * Closes the web interface, when the app is unable to connect to the server.
     */
    public void webviewProblem() {
        Toast.makeText(getContext(),"Error: Unable to connect to Server!", Toast.LENGTH_SHORT);
        ((WorkActivity) getActivity()).finishClickWebView(false);
    }
}
