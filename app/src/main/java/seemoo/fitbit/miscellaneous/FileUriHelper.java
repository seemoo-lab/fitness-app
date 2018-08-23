package seemoo.fitbit.miscellaneous;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.widget.Toast;

public class FileUriHelper {

    public static String getPath(final Context context, final Uri uri) {
        if (DocumentsContract.isDocumentUri(context, uri) &&
                "com.android.externalstorage.documents".equals(uri.getAuthority())) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];
            if ("primary".equalsIgnoreCase(type)) {
                if(split.length > 1){
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    return Environment.getExternalStorageDirectory().getPath();
                }
            }
        }
        Toast.makeText(context, "Error. Only internal storage paths supported.",Toast.LENGTH_LONG).show();
        return "";
    }
}
