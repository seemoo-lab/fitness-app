package seemoo.fitbit.interactions;

import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import seemoo.fitbit.activities.WorkActivity;

/**
 * The waiting queue for interactions. When the execution of one interaction is finished, the next on in line gets executed.
 */
class BluetoothInteractionQueue {

    private final String TAG = this.getClass().getSimpleName();

    private Interactions interactions;
    private Semaphore mInteractionLock;
    private ExecutorService mExecutorService;
    private WorkActivity activity;
    private Toast toast;
    //FIFO list of interactions which gets automatically executed.
    private ArrayList<BluetoothInteraction> bluetoothInteractions;

    /**
     * Creates an interaction queue.
     *
     * @param interactions  The instance of interactions.
     * @param activity      The current activity.
     * @param toast         The toast, to send messages to the user.
     */
    BluetoothInteractionQueue(Interactions interactions, WorkActivity activity, Toast toast) {
        this.interactions = interactions;
        this.activity = activity;
        this.toast = toast;
        mInteractionLock = new Semaphore(1, true);
        mExecutorService = Executors.newSingleThreadExecutor();
        bluetoothInteractions = new ArrayList<>();
    }


    /**
     * Adds a new interaction to interaction list.
     *
     * @param interaction The interaction to add.
     */
    void addInteraction(BluetoothInteraction interaction) {
        bluetoothInteractions.add(interaction);
        BluetoothInteractionRunnable runnable = new BluetoothInteractionRunnable(interaction, mInteractionLock, interactions, this, activity, toast);
        mExecutorService.execute(runnable);
    }

    /**
     * Deletes the first command from interaction list and releases execution lock.
     */
    void interactionFinished() {
        if (bluetoothInteractions.size() > 0) {
            bluetoothInteractions.remove(0);
            mInteractionLock.release();
        } else {
            Log.e(TAG, "Error: There is no interaction to remove!");
        }
    }

    /**
     * Returns the first interaction in the list.
     *
     * @return The first interaction in the list. Null. if the list is empty.
     */
    BluetoothInteraction getFirstBluetoothInteraction() {
        if (bluetoothInteractions.isEmpty()) {
            return null;
        }
        return bluetoothInteractions.get(0);
    }

    int getAllBluetoothInteractions() {
        if (bluetoothInteractions.isEmpty()) {
            return 0;
        }
        return bluetoothInteractions.size();
    }
}
