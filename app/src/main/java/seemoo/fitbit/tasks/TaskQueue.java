package seemoo.fitbit.tasks;


import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import seemoo.fitbit.miscellaneous.ButtonHandler;

/**
 * The waiting queue for tasks. When the execution of one task is finished, the next on in line gets executed.
 */
class TaskQueue {

    private final String TAG = this.getClass().getSimpleName();

    private ButtonHandler buttonHandler;
    private Semaphore mTaskLock;
    private ExecutorService mExecutorService;
    //FIFO list of tasks which gets automatically executed.
    private ArrayList<Task> taskList;

    /**
     * Creates a task queue.
     *
     * @param buttonHandler The instance of the button handler.
     */
    TaskQueue(ButtonHandler buttonHandler) {
        this.buttonHandler = buttonHandler;
        mTaskLock = new Semaphore(1, true);
        mExecutorService = Executors.newSingleThreadExecutor();
        taskList = new ArrayList<>();
    }


    /**
     * Adds a new task to task list.
     *
     * @param task The task to add.
     */
    void addTask(Task task) {
        buttonHandler.setAllGone();
        buttonHandler.setLock(true);
        taskList.add(task);
        TaskRunnable runnable = new TaskRunnable(task, mTaskLock, this);
        mExecutorService.execute(runnable);
    }

    /**
     * Deletes the first task from task list and releases execution lock.
     */
    void taskFinished() {
        if (taskList.size() > 0) {
            if (taskList.size() == 1) {
                buttonHandler.setLock(false);
                buttonHandler.setAllVisible();
            }
            taskList.remove(0);
            mTaskLock.release();
        } else {
            Log.e(TAG, "Error: There is no task to remove!");
        }
    }

    /**
     * Returns the first task of the task list.
     *
     * @return The first task of the task list. Null, if list is empty.
     */
    Task getFirstTask() {
        if (taskList.isEmpty()) {
            return null;
        }
        return taskList.get(0);
    }

    /**
     * Clears the task list.
     */
    void clear() {
        taskList.clear();
    }
}
