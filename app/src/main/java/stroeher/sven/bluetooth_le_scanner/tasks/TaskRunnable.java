package stroeher.sven.bluetooth_le_scanner.tasks;


import android.util.Log;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Executes the task, when it is its turn.
 */
class TaskRunnable implements Runnable {

    private final String TAG = this.getClass().getSimpleName();

    private Task mTask;
    private Semaphore mTaskLock;
    private TaskQueue mTaskQueue;

    /**
     * Creates a task runnable.
     *
     * @param task       The task to execute.
     * @param lock       The lock from the task queue.
     * @param mTaskQueue The task queue.
     */
    TaskRunnable(Task task, Semaphore lock, TaskQueue mTaskQueue) {
        mTask = task;
        mTaskLock = lock;
        this.mTaskQueue = mTaskQueue;
    }

    /**
     * {@inheritDoc}
     * Executes the task. If the execution is not finished after a time specified in the task, there is a timeout. The next task will be executed.
     * If the timer of a task is set to a negative value, the task is paused until it is set back to a positive value.
     */
    @Override
    public void run() {
        if (mTask == null || mTaskLock == null) {
            Log.e(TAG, "Error: " + TAG + ".run, object = null");
        } else if (mTaskQueue.getFirstTask() != null) {
            try {
                if (mTaskQueue.getFirstTask().getTimer() < 0) {
                    if (mTaskLock.tryAcquire(3000, TimeUnit.MILLISECONDS)) {
                        mTaskLock.release();
                    } else if (mTaskQueue.getFirstTask() != null) {
                        Log.e(TAG, "Task queue is paused, until " + mTaskQueue.getFirstTask().getClass().getSimpleName() + "-timer is set to a positive value.");
                    }
                    run();
                } else {
                    //Acquire semaphore lock to ensure no other operations can run until this one completed. Task aborted after TASK_TIMER milliseconds.
                    if (mTaskLock.tryAcquire(mTaskQueue.getFirstTask().getTimer(), TimeUnit.MILLISECONDS)) {
                        //Tell the task to start itself.
                        mTask.execute();
                        Log.e(TAG, "current Task = " + mTask.TAG);
                    } else {
                        Log.e(TAG, "Error: Timeout at task: " + mTaskQueue.getFirstTask().getClass().getSimpleName());
                        mTaskQueue.taskFinished();
                        run();
                    }
                }
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
