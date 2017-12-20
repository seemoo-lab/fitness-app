package stroeher.sven.bluetooth_le_scanner.commands;

import android.util.Log;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Executes the command, when it is its turn.
 */
class BluetoothCommandRunnable implements Runnable {

    private final String TAG = this.getClass().getSimpleName();

    private BluetoothCommand mBluetoothCommand;
    private Semaphore mCommandLock;
    private BluetoothCommandQueue mBluetoothCommandQueue;

    //Stops executing commands after COMMAND_TIMER milliseconds.
    private static final long COMMAND_TIMER = 2000;

    /**
     * Creates a bluetooth command runnable.
     * @param command The command to execute.
     * @param lock The lock of the command queue.
     * @param mBluetoothCommandQueue The bluetooth command queue.
     */
    BluetoothCommandRunnable(BluetoothCommand command, Semaphore lock, BluetoothCommandQueue mBluetoothCommandQueue) {
        mBluetoothCommand = command;
        mCommandLock = lock;
        this.mBluetoothCommandQueue = mBluetoothCommandQueue;
    }

    /**
     * {@inheritDoc}
     * Executes the command. If the execution is not finished after a time specified in the command, there is a timeout. The next command will be executed.
     */
    @Override
    public void run() {
        if (mBluetoothCommand == null || mCommandLock == null) {
            Log.e(TAG, "Error: " + TAG + ".run, object = null");
        } else if (mBluetoothCommandQueue.getFirstBluetoothCommand() != null) {
            try {
                //Acquire semaphore lock to ensure no other operations can run until this one completed. Command aborted after COMMAND_TIMER milliseconds.
                if (mCommandLock.tryAcquire(COMMAND_TIMER, TimeUnit.MILLISECONDS)) {
                    //Tell the command to start itself.

                    Log.e(TAG, "Commands in queue: ");

                    mBluetoothCommand.execute();
                } else {
                    Log.e(TAG, "Error: Timeout at command: " + mBluetoothCommandQueue.getFirstBluetoothCommand().getClass().getSimpleName() +
                            ", UUID: " + mBluetoothCommandQueue.getFirstBluetoothCommand().getUUID());
                    mBluetoothCommandQueue.commandFinished();
                    run();
                }
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
