package stroeher.sven.bluetooth_le_scanner.miscellaneous;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import stroeher.sven.bluetooth_le_scanner.R;

/**
 * Offers several options to show messages to the user.
 */
public class Messenger {

    private static final String TAG = Messenger.class.getSimpleName();

    /**
     * Shows a message to the user.
     * @param activity The current activity.
     * @param iconId The id of the icon.
     * @param title The title of the message.
     * @param text The text of the message.
     * @param button The text of the button.
     */
    public static void message(Activity activity, Object iconId, Object title, Object text, Object button) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if(iconId instanceof Integer) {
            builder.setIcon((Integer) title);
        }
        if(title instanceof String) {
            builder.setTitle((String) title);
        } else if (title instanceof Integer){
            builder.setTitle((Integer) title);
        }
        if(text instanceof String) {
            builder.setMessage((String) text);
        } else if (text instanceof Integer){
            builder.setMessage((Integer) text);
        }
        if(button instanceof String) {
            builder.setNegativeButton((String) button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        } else if (button instanceof Integer){
            builder.setNegativeButton((Integer) button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
        builder.show();
    }

    /**
     * Shows a message to the user.
     * @param activity The current activity.
     * @param title The title of the message.
     * @param text The text of the message.
     * @param button The text of the button.
     */
    public static void message(Activity activity, Object title, Object text, Object button){
        Messenger.message(activity, null, title, text, button);
    }

    /**
     * Shows a message to the user.
     * @param activity The current activity.
     * @param text The text of the message.
     * @param button The text of the button.
     */
    public static void message(Activity activity, Object text, Object button){
        Messenger.message(activity, null, null, text, button);
    }

    /**
     * Shows a message to the user.
     * @param activity The current activity.
     * @param text The text of the message.
     */
    public static void message(Activity activity, Object text){
        Messenger.message(activity, null, null, text, R.string.ok);
    }
}
