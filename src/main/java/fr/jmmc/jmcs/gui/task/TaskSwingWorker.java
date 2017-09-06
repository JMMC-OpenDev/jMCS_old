/*******************************************************************************
 *                 jMCS project ( http://www.jmmc.fr/dev/jmcs )
 *******************************************************************************
 * Copyright (c) 2013, CNRS. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the CNRS nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL CNRS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package fr.jmmc.jmcs.gui.task;

import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.MCSExceptionHandler;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends SwingWorker (Java 6+) to :
 * - define related task to cancel easily the task and its child tasks;
 * - simplify debugging / logging.
 *
 * @param <T> the result type returned by this {@code TaskSwingWorker}
 * 
 * @author Guillaume MELLA, Laurent BOURGES.
 */
public abstract class TaskSwingWorker<T> extends SwingWorker<T, Void> {

    /** Class _logger */
    protected static final Logger _logger = LoggerFactory.getLogger(TaskSwingWorker.class.getName());
    /** flag to log debugging information */
    protected final static boolean DEBUG_FLAG = false;
    // members
    /** related task */
    private final Task _task;
    /** log prefix using the format 'SwingWorker[" + task.name + "]" + logSuffix + "@hashcode' used by debugging statements */
    protected final String _logPrefix;
    /** running thread name (only defined during the background execution; null otherwise) */
    private volatile String threadName = null;

    /**
     * Create a new TaskSwingWorker instance
     * @param task related task
     */
    public TaskSwingWorker(final Task task) {
        this(task, "");
    }

    /**
     * Create a new TaskSwingWorker instance
     * @param task related task
     * @param logSuffix complementary suffix for log prefix
     */
    public TaskSwingWorker(final Task task, final String logSuffix) {
        _task = task;
        _logPrefix = (DEBUG_FLAG) ? ("SwingWorker[" + task.getName() + "]" + logSuffix + "@" + Integer.toHexString(hashCode())) : "SwingWorker[" + task.getName() + "]";
    }

    /**
     * Schedules this {@code TaskSwingWorker} for execution on a <i>worker</i>
     * thread.
     * @see TaskSwingWorkerExecutor#executeTask(TaskSwingWorker)
     */
    public final void executeTask() {
        // increment running worker :
        TaskSwingWorkerExecutor.incRunningWorkerCounter();

        // Cancel other task and execute this new task :
        TaskSwingWorkerExecutor.executeTask(this);
    }

    /**
     * Schedules this {@code TaskSwingWorker} for execution using EDT.
     * 
     * Note: this will block EDT so UI refresh can not happen meanwhile:
     * only applicable for very short task or to block EDT as wanted
     */
    public final void executeTaskInEDT() {
        // increment running worker :
        TaskSwingWorkerExecutor.incRunningWorkerCounter();

        // cancel other task:
        TaskSwingWorkerExecutor.cancelTask(this.getTask());

        // Just execute this new task with EDT (synchronously) :
        SwingUtils.invokeEDT(this);
    }

    /**
     * Return the task related to this SwingWorker
     * @return related task
     */
    public final Task getTask() {
        return _task;
    }

    @Override
    public final String toString() {
        return _logPrefix;
    }

    /**
     * @return running thread name (only defined during the background execution; null otherwise)
     */
    protected final String getThreadName() {
        return threadName;
    }

    /**
     * Define the running thread name.
     * Invoked by the TaskSwingWorkerExecutor
     * @param threadName thread name running this job 
     */
    final void setThreadName(final String threadName) {
        if (DEBUG_FLAG) {
            _logger.info("{}.setThreadName: {}", _logPrefix, threadName);
        }
        this.threadName = threadName;
    }

    /**
     * Custom cancel implementation to call beforeCancel() and then cancel(true) to interupt the thread
     * @return true if the task was cancelled; false otherwise
     */
    final boolean doCancel() {
        if (DEBUG_FLAG) {
            _logger.info("{}.doCancel: beforeCancel", _logPrefix);
        }
        beforeCancel();

        if (DEBUG_FLAG) {
            _logger.info("{}.doCancel: cancel(true)", _logPrefix);
        }

        // note : if the worker was previously cancelled, it has no effect.
        // Interrupt the thread to have Thread.isInterrupted() == true :
        return cancel(true);
    }

    /**
     * Perform cancellation preparation (network ...)
     */
    protected void beforeCancel() {
        // empty implementation
    }

    /**
     * Do some computation in background
     * @return data computed data
     */
    @Override
    public final T doInBackground() {
        if (DEBUG_FLAG) {
            _logger.info("{}.doInBackground : START", _logPrefix);
        }

        // compute the data :
        T data = computeInBackground();

        if (isCancelled()) {
            if (DEBUG_FLAG) {
                _logger.info("{}.doInBackground : CANCELLED", _logPrefix);
            }
            // no result if task was cancelled :
            data = null;
        } else {
            if (DEBUG_FLAG) {
                _logger.info("{}.doInBackground : DONE", _logPrefix);
            }
        }
        return data;
    }

    /**
     * Call the refreshUI with result if not canceled.
     * This code is executed by the Swing Event Dispatcher thread (EDT)
     */
    @Override
    public final void done() {
        // check if the worker was cancelled :
        if (isCancelled()) {
            if (DEBUG_FLAG) {
                _logger.info("{}.done : CANCELLED", _logPrefix);
            }
            refreshNoData(true);
        } else {
            try {
                // Get the computed results :
                final T data = get();

                if (data == null) {
                    if (DEBUG_FLAG) {
                        _logger.info("{}.done : NO DATA", _logPrefix);
                    }
                    refreshNoData(false);
                } else {
                    if (DEBUG_FLAG) {
                        _logger.info("{}.done : UI START", _logPrefix);
                    }

                    // refresh UI with data :
                    this.refreshUI(data);

                    if (DEBUG_FLAG) {
                        _logger.info("{}.done : UI DONE", _logPrefix);
                    }
                }

            } catch (InterruptedException ie) {
                _logger.debug("{}.done : interrupted failure :", _logPrefix, ie);
            } catch (ExecutionException ee) {
                handleException(ee);
            }
        }
        // decrement running worker :
        TaskSwingWorkerExecutor.decRunningWorkerCounter();
    }

    /**
     * Compute operation invoked by a Worker Thread (not Swing EDT) in background
     * Called by @see #doInBackground()
     * @return computed data
     */
    public abstract T computeInBackground();

    /**
     * Refresh GUI invoked by the Swing Event Dispatcher Thread (Swing EDT)
     * Called by @see #done()
     * @param data computed data NOT NULL
     */
    public abstract void refreshUI(final T data);

    /**
     * Refresh GUI when no data (null returned or cancelled) invoked by the Swing Event Dispatcher Thread (Swing EDT)
     * @param cancelled true if task cancelled; false if null returned by computeInBackground()
     */
    public void refreshNoData(final boolean cancelled) {
        // empty implementation
    }

    /**
     * Handle the execution exception that occurred in the compute operation : @see #computeInBackground()
     * This default implementation opens the feedback report (modal and do not exit on close).
     *
     * @param ee execution exception
     */
    public void handleException(final ExecutionException ee) {
        // Show feedback report (modal and do not exit on close):
        MCSExceptionHandler.runExceptionHandler(ee.getCause());
    }
}
