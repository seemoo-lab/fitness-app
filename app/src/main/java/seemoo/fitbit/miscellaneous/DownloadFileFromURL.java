package seemoo.fitbit.miscellaneous;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import seemoo.fitbit.dialogs.FwDownloadDialog;

public class DownloadFileFromURL extends AsyncTask<String, String, String> {

    private FwDownloadDialog dialog;
    private String localFilePath = null;

    public DownloadFileFromURL(FwDownloadDialog dialog) {
        this.dialog = dialog;
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(String... locations) {
        int count;
        try {
            URL url = new URL(locations[0]);
            String localFilename = locations[1];
            URLConnection connection = url.openConnection();
            connection.connect();

            int lengthOfFile = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);
            localFilePath = Environment
                    .getExternalStorageDirectory().toString()
                    + "/Download/" + localFilename;
            // Output stream
            OutputStream output = new FileOutputStream(localFilePath);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lengthOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }

    /**
     * Updating progress bar
     */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        dialog.setProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task Dismiss the dialog
     **/
    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded
        dialog.fileDownloaded(localFilePath);
    }

}
