package stroeher.sven.bluetooth_le_scanner.commands;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * The waiting queue for commands. When the execution of one command is finished, the next on in line gets executed.
 */
class BluetoothCommandQueue {

    private final String TAG = this.getClass().getSimpleName();

    private Semaphore mCommandLock;
    private ExecutorService mExecutorService;
    //FIFO list of commands which gets automatically executed.
    private ArrayList<BluetoothCommand> bluetoothCommands;

    /**
     * Creates a bluetooth command queue.
     */
    BluetoothCommandQueue() {
        mCommandLock = new Semaphore(1, true);
        mExecutorService = Executors.newSingleThreadExecutor();
        bluetoothCommands = new ArrayList<>();
    }


    /**
     * Adds a new command to command list.
     *
     * @param command The command to add.
     */
    void addCommand(BluetoothCommand command) {
        bluetoothCommands.add(command);
        BluetoothCommandRunnable runnable = new BluetoothCommandRunnable(command, mCommandLock, this);
        mExecutorService.execute(runnable);
    }

    /**
     * Deletes the first command from command list and releases execution lock.
     */
    void commandFinished() {
        if (bluetoothCommands.size() > 0) {
            bluetoothCommands.remove(0);
            mCommandLock.release();
        } else {
            Log.e(TAG, "Error: There is no command to remove!");
        }
    }

    /**
     * Returns the first command in command list.
     *
     * @return The first command.
     */
    BluetoothCommand getFirstBluetoothCommand() {
        if (bluetoothCommands.isEmpty()) {
            return null;
        }
        return bluetoothCommands.get(0);
    }
}
