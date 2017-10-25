package stroeher.sven.bluetooth_le_scanner.interactions;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Executes the interaction, when it is its turn.
 */
class BluetoothInteractionRunnable implements Runnable {

    private final String TAG = this.getClass().getSimpleName();

    private BluetoothInteraction mBluetoothInteraction;
    private Semaphore mInteractionLock;
    private BluetoothInteractionQueue mBluetoothInteractionQueue;
    private Interactions interactions;
    private Activity activity;
    private Toast toast;

    /**
     * Creates a bluetooth interaction runnable.
     *
     * @param interaction                The interaction to run.
     * @param lock                       The lock from the interaction queue.
     * @param interactions               The instance of interactions.
     * @param mBluetoothInteractionQueue The bluetooth interaction queue.
     * @param activity                   The current activity.
     * @param toast                      The toast to send messages to the user.
     */
    BluetoothInteractionRunnable(BluetoothInteraction interaction, Semaphore lock, Interactions interactions, BluetoothInteractionQueue mBluetoothInteractionQueue, Activity activity, Toast toast) {
        mBluetoothInteraction = interaction;
        mInteractionLock = lock;
        this.interactions = interactions;
        this.mBluetoothInteractionQueue = mBluetoothInteractionQueue;
        this.activity = activity;
        this.toast = toast;
    }

    /**
     * {@inheritDoc}
     * Executes the interaction. If the execution is not finished after a time specified in the interaction, there is a timeout. The next interaction will be executed.
     * If the timer of an interaction is set to a negative value, the interaction is paused until it is set back to a positive value.
     */
    @Override
    public void run() {
        if (mBluetoothInteraction == null || mInteractionLock == null) {
            Log.e(TAG, "Error: " + TAG + ".run, object = null");
        }
        if (mBluetoothInteractionQueue.getFirstBluetoothInteraction() != null) {
            try {
                if (mBluetoothInteractionQueue.getFirstBluetoothInteraction().getTimer() < 0) {
                    if (mInteractionLock.tryAcquire(3000, TimeUnit.MILLISECONDS)) {
                        mInteractionLock.release();
                    } else {
                        Log.e(TAG, "Interaction queue is paused, until " + mBluetoothInteractionQueue.getFirstBluetoothInteraction().getClass().getSimpleName() + "-timer is set to a positive value.");
                    }
                    run();
                } else {
                    //Acquire semaphore lock to ensure no other operations can run until this one completed. Interaction aborted after timer milliseconds.
                    if (mInteractionLock.tryAcquire(mBluetoothInteractionQueue.getFirstBluetoothInteraction().getTimer(), TimeUnit.MILLISECONDS)) {
                        if (mBluetoothInteractionQueue.getFirstBluetoothInteraction() != null) {
                            Log.e(TAG, "Interaction started: " + mBluetoothInteraction.TAG + ", timer: " + mBluetoothInteractionQueue.getFirstBluetoothInteraction().getTimer() + " milliseconds");
                        } else {
                            Log.e(TAG, "Interaction started: " + mBluetoothInteraction.TAG + ", timer: unknown");
                        }
                        //starts interaction
                        if (!mBluetoothInteraction.execute()) {
                            interactions.interactionFinished();
                            return;
                        }
                        interactions.setCurrentInteraction(mBluetoothInteraction.getClass().getSimpleName());
                    } else {
                        Log.e(TAG, "Error: Timeout after " + mBluetoothInteractionQueue.getFirstBluetoothInteraction().getTimer() + " milliseconds at interaction: " + mBluetoothInteractionQueue.getFirstBluetoothInteraction().getClass().getSimpleName());
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (mBluetoothInteractionQueue.getFirstBluetoothInteraction() != null) {
                                    toast.setText("Error: Timeout of " + mBluetoothInteractionQueue.getFirstBluetoothInteraction().getClass().getSimpleName().replaceFirst("Interaction", ""));
                                } else {
                                    toast.setText("Error: Timeout of last interaction");
                                }
                                toast.show();
                            }
                        });
                        interactions.interactionFinished();
                        run();
                    }
                }
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
