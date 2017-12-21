package seemoo.fitbit.miscellaneous;

import android.app.Activity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;

/**
 * Offers an easy way to handle several buttons accordingly.
 */
public class ButtonHandler {

    private SparseArray<Button> buttons;
    private SparseArray<Button> specialButtons;
    private Activity activity;
    private boolean lock = false; //locks setting all visible/gone

    /**
     * Easy way to handle several buttons accordingly.
     * @param activity The current activity.
     */
    public ButtonHandler(Activity activity) {
        this.activity = activity;
        buttons = new SparseArray<>();
        specialButtons = new SparseArray<>();
    }

    /**
     * Adds a button to the button handler.
     * @param buttonID The id of the new button.
     */
    public void addButton(int buttonID) {
        Button temp = (Button) activity.findViewById(buttonID);
        temp.setVisibility(View.GONE);
        buttons.put(buttonID, temp);
    }

    /**
     * Adds a special button to the button handler. Special buttons do not get visible/gone when all other buttons were set visible/gone (setAllVisible/setAllGone()).
     * @param buttonID The id of the new button.
     */
    public void addSpecialButton(int buttonID) {
        Button temp = (Button) activity.findViewById(buttonID);
        temp.setVisibility(View.GONE);
        specialButtons.put(buttonID, temp);
    }

    /**
     * Sets a button visible.
     * @param buttonID The id of the button.
     */
    public void setVisible(final int buttonID) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(buttons.get(buttonID) != null) {
                    buttons.get(buttonID).setVisibility(View.VISIBLE);
                } else{
                    specialButtons.get(buttonID).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Sets a button gone.
     * @param buttonID The id of the button.
     */
    public void setGone(final int buttonID) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(buttons.get(buttonID) != null) {
                    buttons.get(buttonID).setVisibility(View.GONE);
                } else{
                    specialButtons.get(buttonID).setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Sets all buttons visible, except for special buttons.
     */
    public void setAllVisible() {
        if(!lock) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < buttons.size(); i++) {
                        buttons.valueAt(i).setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    /**
     * Sets all buttons gone, except for special buttons.
     */
    public void setAllGone() {
        if(!lock) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0; i < buttons.size(); i++) {
                        buttons.valueAt(i).setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    /**
     * Sets the text of a button.
     * @param text The text to set.
     * @param buttonID The id of the button.
     */
    public void setText(final String text, final int buttonID){
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(buttons.get(buttonID) != null) {
                    buttons.get(buttonID).setText(text);
                } else{
                    specialButtons.get(buttonID).setText(text);
                }
            }
        });
    }

    /**
     * Sets a lock to the setAllVisible/setAllGone() methods.
     * @param value The state of the lock.
     */
    public void setLock(boolean value){
        lock = value;
    }
}
