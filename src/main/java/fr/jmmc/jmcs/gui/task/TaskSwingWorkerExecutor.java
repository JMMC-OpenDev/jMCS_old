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

import fr.jmmc.jmcs.util.MCSExceptionHandler;
import fr.jmmc.jmcs.util.concurrent.FixedThreadPoolExecutor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a customization of the standard SwingWorker to have a single
 * thread only processing workers because our computations require serialization
 * and cancellation
 *
 * @author Guillaume MELLA, Laurent BOURGES.
 */
public final class TaskSwingWorkerExecutor {

    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(TaskSwingWorkerExecutor.class.getName());
    /** flag to log debugging information */
    private final static boolean DEBUG_FLAG = false;
    /** singleton instance */
    private static TaskSwingWorkerExecutor _instance;
    /** running worker counter */
    private final static AtomicInteger _runningWorkerCounter = new AtomicInteger(0);
    // members
    /** Single threaded thread pool */
    private final ExecutorService _executorService;
    /** Current (or old) worker atomic reference for all tasks */
    private final Map<String, AtomicReference<TaskSwingWorker<?>>> _currentTaskWorkers;

    /**
     * Start the TaskSwingWorkerExecutor
     */
    public static void start() {
        getInstance();
    }

    /**
     * Shutdown the TaskSwingWorkerExecutor
     */
    public static synchronized void shutdown() {
        if (_instance != null) {
            _instance.stop();

            if (DEBUG_FLAG) {
                _logger.info("stopped SwingWorkerExecutor: {}", _instance);
            }
            _instance = null;
        }
    }

    /**
     * This code returns the singleton instance.
     *
     * @return TaskSwingWorkerExecutor
     */
    private static synchronized TaskSwingWorkerExecutor getInstance() {
        if (_instance == null) {
            _instance = new TaskSwingWorkerExecutor();

            if (DEBUG_FLAG) {
                _logger.info("created SwingWorkerExecutor: {}", _instance);
            }
        }
        return _instance;
    }

    /**
     * Schedules the given {@code TaskSwingWorker} for execution on a
     * <i>worker</i> thread.
     *
     * @see #execute(TaskSwingWorker)
     * @param worker TaskSwingWorker instance to execute
     */
    static void executeTask(final TaskSwingWorker<?> worker) {
        getInstance().execute(worker);
    }

    /**
     * Cancel any busy worker for the given task NOTE : No synchronization HERE
     * as it must be called from Swing EDT
     *
     * @param task task to find the current worker
     * @return true if one task was canceled
     */
    public static boolean cancelTask(final Task task) {
        return getInstance().cancel(task);
    }

    /**
     * Return true if there is at least one worker running
     *
     * @return true if there is at least one worker running
     */
    public static boolean isTaskRunning() {
        return _runningWorkerCounter.get() > 0;
    }
    
    /**
     * Increment the counter of running worker
     */
    static void incRunningWorkerCounter() {
        final int count = _runningWorkerCounter.incrementAndGet();

        if (DEBUG_FLAG) {
            _logger.info("runningWorkerCounter: {}", count);
        }
    }

    /**
     * Decrement the counter of running worker
     */
    static void decRunningWorkerCounter() {
        final int count = _runningWorkerCounter.decrementAndGet();

        if (DEBUG_FLAG) {
            _logger.info("runningWorkerCounter: {}", count);
        }
    }

    /**
     * Private constructor
     */
    private TaskSwingWorkerExecutor() {
        super();

        // Use an unsynchronized Map as map modifications are only made by Swing EDT (put) :
        _currentTaskWorkers = new HashMap<String, AtomicReference<TaskSwingWorker<?>>>(16);

        // Prepare the custom executor service with a single thread :
        _executorService = new SwingWorkerSingleThreadExecutor(this);
    }

    /**
     * Stop all active worker threads immediately (interrupted)
     */
    private void stop() {
        _executorService.shutdownNow();
    }

    /**
     * Schedules the given {@code TaskSwingWorker} for execution on a
     * <i>worker</i> thread. There is a Single <i>worker</i> thread available.
     * In the event the <i>worker</i> thread is busy handling other
     * {@code SwingWorkers} the given {@code SwingWorker} is placed in a waiting
     * queue. NOTE : No synchronization HERE as it must be called from Swing EDT
     *
     * @param worker TaskSwingWorker instance to execute
     */
    private void execute(final TaskSwingWorker<?> worker) {
        // note : there is no synchronisation here because this method must be called from Swing EDT
        final Task task = worker.getTask();

        _logger.debug("execute task: {} with worker = {}", task, worker);

        // cancel the running worker for the task and child tasks
        cancelRelatedTasks(task);

        // memorize the reference to the new worker before execution :
        defineReference(task, worker);

        _logger.debug("execute worker = {}", worker);

        // finally, execute the new worker with the custom executor service :
        _executorService.execute(worker);
    }

    /**
     * Cancel any busy worker related to the given task and its child tasks NOTE
     * : No synchronization HERE as it must be called from Swing EDT
     *
     * @param task to use
     */
    private void cancelRelatedTasks(final Task task) {
        if (DEBUG_FLAG) {
            _logger.info("cancel related tasks for = {}", task);
        }
        // cancel first any busy worker related to any child task :
        for (Task child : task.getChildTasks()) {
            cancel(child);
        }
        // cancel any busy worker related to the given task :
        cancel(task);
    }

    /**
     * Cancel any busy worker for the given task NOTE : No synchronization HERE
     * as it must be called from Swing EDT
     *
     * @param task task to find the current worker
     * @return true if one task was canceled
     */
    private boolean cancel(final Task task) {
        boolean cancelled = false;

        final AtomicReference<TaskSwingWorker<?>> workerRef = getReference(task);
        if (workerRef != null) {
            // get current worker and clear the reference :
            final TaskSwingWorker<?> currentWorker = workerRef.getAndSet(null);

            // cancel the current running worker for the given task :
            if (currentWorker != null) {
                cancelled = true;
                // worker is still running ...
                _logger.debug("cancel worker = {}", currentWorker);

                // note : if the worker was previously cancelled, it has no effect.
                // interrupt the thread to have Thread.isInterrupted() == true :
                currentWorker.cancel(true);
            }
        }
        return cancelled;
    }

    /**
     * Remove the given worker from the busy workers for its task. Useful when
     * the worker terminates its execution (canceled or not). NOTE : This
     * method is invoked by the thread that executed the task.
     *
     * @param worker worker to remove
     */
    private void clearWorker(final TaskSwingWorker<?> worker) {
        final AtomicReference<TaskSwingWorker<?>> workerRef = getReference(worker.getTask());
        if (workerRef != null) {
            // check if the reference points to the given worker and then clear the reference :
            if (workerRef.compareAndSet(worker, null)) {
                if (DEBUG_FLAG) {
                    _logger.info("cleared worker = {}", worker);
                }
            } else {
                if (DEBUG_FLAG) {
                    _logger.info("NOT cleared worker = {} - value is = {}", worker, workerRef.get());
                }
            }
        }
    }

    /**
     * Define the worker thread related to the given task
     *
     * @param task task to find
     * @param worker new worker
     */
    private void defineReference(final Task task, final TaskSwingWorker<?> worker) {
        final AtomicReference<TaskSwingWorker<?>> workerRef = getOrCreateReference(task);
        if (workerRef != null) {
            // check if the reference is undefined and then set the reference :
            if (workerRef.compareAndSet(null, worker)) {
                if (DEBUG_FLAG) {
                    _logger.info("set worker = {}", worker);
                }
            } else {
                if (DEBUG_FLAG) {
                    _logger.info("NOT set worker = {} - value is = {}", worker, workerRef.get());
                }
            }
        }
    }

    /**
     * Return the atomic reference corresponding to the given task
     *
     * @param task task to find
     * @return atomic reference corresponding to the given task
     */
    private AtomicReference<TaskSwingWorker<?>> getReference(final Task task) {
        return _currentTaskWorkers.get(task.getName());
    }

    /**
     * Return the atomic reference corresponding to the given task. If it does
     * not exist in the currentTaskWorkers, it creates a new ones.
     *
     * @param task task to find
     * @return atomic reference corresponding to the given task
     */
    private AtomicReference<TaskSwingWorker<?>> getOrCreateReference(final Task task) {
        AtomicReference<TaskSwingWorker<?>> workerRef = getReference(task);
        if (workerRef == null) {
            workerRef = new AtomicReference<TaskSwingWorker<?>>();
            _currentTaskWorkers.put(task.getName(), workerRef);
        }
        return workerRef;
    }

    /**
     * Single threaded Swing Worker executor
     */
    private static final class SwingWorkerSingleThreadExecutor extends FixedThreadPoolExecutor {

        // members
        /** TaskSwingWorkerExecutor reference for clearWorker callback */
        private final TaskSwingWorkerExecutor _executor;

        /**
         * Create a single threaded Swing Worker executor
         *
         * @param executor TaskSwingWorkerExecutor reference for clearWorker
         * callback
         */
        protected SwingWorkerSingleThreadExecutor(final TaskSwingWorkerExecutor executor) {
            super(1, new SwingWorkerThreadFactory());

            _executor = executor;
        }

        /**
         * Method invoked prior to executing the given Runnable in the given
         * thread. This method is invoked by thread <tt>t</tt> that will execute
         * task <tt>r</tt>, and may be used to re-initialize ThreadLocals, or to
         * perform logging.
         *
         * <p>This implementation does nothing, but may be customized in
         * subclasses. Note: To properly nest multiple overriding, subclasses
         * should generally invoke <tt>super.beforeExecute</tt> at the end of
         * this method.
         *
         * @param t the thread that will run task r.
         * @param r the task that will be executed.
         */
        @Override
        protected void beforeExecute(final Thread t, final Runnable r) {
            if (DEBUG_FLAG) {
                _logger.info("beforeExecute: {}", r);
            }
        }

        /**
         * Method invoked upon completion of execution of the given Runnable.
         * This method is invoked by the thread that executed the task. If
         * non-null, the Throwable is the uncaught <tt>RuntimeException</tt> or
         * <tt>Error</tt> that caused execution to terminate abruptly.
         *
         * <p><b>Note:</b> When actions are enclosed in tasks (such as
         * {@link FutureTask}) either explicitly or via methods such as
         * <tt>submit</tt>, these task objects catch and maintain computational
         * exceptions, and so they do not cause abrupt termination, and the
         * internal exceptions are <em>not</em> passed to this method.
         *
         * <p>This implementation does nothing, but may be customized in
         * subclasses. Note: To properly nest multiple overriding, subclasses
         * should generally invoke <tt>super.afterExecute</tt> at the beginning
         * of this method.
         *
         * @param r the runnable that has completed.
         * @param t the exception that caused termination, or null if execution
         * completed normally.
         */
        @Override
        protected void afterExecute(final Runnable r, final Throwable t) {
            // clear interrupt flag: 
            super.afterExecute(r, t);

            if (DEBUG_FLAG) {
                if (t != null) {
                    _logger.info("afterExecute: {}", r, t);
                } else {
                    _logger.info("afterExecute: {}", r);
                }
            }
            if (r instanceof TaskSwingWorker<?>) {
                final TaskSwingWorker<?> worker = (TaskSwingWorker<?>) r;
                if (!worker.isCancelled()) {
                    _executor.clearWorker(worker);
                }
            }
        }
    }

    /**
     * Custom ThreadFactory implementation
     */
    private static final class SwingWorkerThreadFactory implements ThreadFactory {

        /** thread count */
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        /**
         * Constructs a new {@code Thread}.
         *
         * @param r a runnable to be executed by new thread instance
         * @return constructed thread, or {@code null} if the request to create
         * a thread is rejected
         */
        @Override
        public Thread newThread(final Runnable r) {
            final StringBuilder name = new StringBuilder("SwingWorker-pool-");
            name.append(threadNumber.getAndIncrement());

            final Thread thread = new Thread(r, name.toString());
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }

            // define UncaughtExceptionHandler :
            MCSExceptionHandler.installThreadHandler(thread);

            if (DEBUG_FLAG) {
                _logger.info("new thread: {}", thread.getName());
            }

            return thread;
        }
    }
}
