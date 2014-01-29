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
package fr.jmmc.jmcs.util.runner;

import fr.jmmc.jmcs.util.CollectionUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import fr.jmmc.jmcs.util.concurrent.CustomThreadPoolExecutor;
import fr.jmmc.jmcs.util.concurrent.FastSemaphore;
import fr.jmmc.jmcs.util.concurrent.ThreadExecutors;
import fr.jmmc.jmcs.util.runner.process.ProcessContext;
import fr.jmmc.jmcs.util.runner.process.ProcessRunner;
import fr.jmmc.jmcs.util.runner.process.RingBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job Management (queue & execution) on local machine
 *
 * @author Laurent BOURGES (voparis).
 */
public final class LocalLauncher {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(LocalLauncher.class.getName());
    /** initial capacity for queue */
    public static final int INITIAL_QUEUE_CAPACITY = 10;
    /** job ID generator (counter) */
    private static AtomicInteger JOBS_ID = new AtomicInteger(0);
    /** live job count */
    private static AtomicInteger JOBS_LIVE = new AtomicInteger(0);
    /** total queued job count */
    private static AtomicInteger JOBS_QUEUED = new AtomicInteger(0);
    /** total job count */
    private static AtomicInteger JOBS_TOTAL = new AtomicInteger(0);
    /** remove policy for queue : default automatic remove after job finished */
    private static boolean QUEUE_MANUAL_REMOVE_JOBS = false;
    /** queue semaphore */
    private static FastSemaphore QUEUE_SEM = new FastSemaphore(1);
    /** QUEUE for job management */
    private static Map<Long, RootContext> JOB_QUEUE = new LinkedHashMap<Long, RootContext>(INITIAL_QUEUE_CAPACITY);
    /** Job Listeners */
    private static Map<String, JobListener> JOB_LISTENER = new HashMap<String, JobListener>(4);
    /** Invalid executor type */
    public static final int ILLEGAL_STATE_ERROR_CODE = -1000;
    /** limit of lines in ring buffer */
    public final static int MAX_LINES = 100;
    /** last total logged */
    private static int _lastTotal = -1;
    /** last live logged */
    private static int _lastLive = -1;

    /**
     * Forbidden Constructor
     */
    private LocalLauncher() {
    }

    /**
     * Prepare thread executors
     * @see ThreadExecutors#startExecutors()
     */
    public static void startUp() {
        _logger.debug("LocalLauncher.startUp: enter");

        ThreadExecutors.startExecutors();

        _logger.debug("LocalLauncher.startUp: exit");
    }

    /**
     * Stop thread executors
     * @see ThreadExecutors#stopExecutors()
     */
    public static void shutdown() {
        // dump stats before shutdown:
        dumpStats();

        _logger.debug("LocalLauncher.shutdown: enter");

        ThreadExecutors.stopExecutors();

        _logger.debug("LocalLauncher.shutdown: exit");
    }

    /**
     * Register an application plugin / listener (interface) at runtime
     * @param applicationName name of the managed application
     * @param listener job listener
     */
    public static void registerJobListener(final String applicationName, final JobListener listener) {
        _logger.info("registerJobListener: application '{}': {}", applicationName, listener);
        JOB_LISTENER.put(applicationName, listener);
    }

    /**
     * Purge from the memory queue the finished jobs (must be called by a separate thread)
     *
     * @param delay time in milliseconds to wait after the job has finished before removing it from the queue
     */
    public static void purgeTerminated(final long delay) {
        _logger.debug("LocalLauncher.purgeTerminated: enter");

        int n = 0;
        final List<RootContext> list = getQueue();

        if (list != null) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("LocalLauncher.purgeTerminated: queue size: {}", list.size());
            }

            final long now = System.currentTimeMillis();

            long duration;

            for (final RunContext job : list) {
                if ((job.getState() == RunState.STATE_FINISHED_ERROR) || (job.getState() == RunState.STATE_FINISHED_OK)) {
                    duration = (now - job.getEndDate().getTime());

                    if (duration > delay) {
                        removeFromQueue(job.getId());
                        n++;
                    }
                }
            }
        }

        _logger.debug("LocalLauncher.purgeTerminated: removed items: {}", n);
    }

    /**
     * Return the live job count
     * @return live job count
     */
    public static int getLiveJobs() {
        return JOBS_LIVE.get();
    }

    /**
     * Return the total queued job count
     * @return total queued job count
     */
    public static int getQueuedJobs() {
        return JOBS_QUEUED.get();
    }

    /**
     * Return the total job count 
     * @return total job count 
     */
    public static int getTotalJobs() {
        return JOBS_TOTAL.get();
    }

    /**
     * Defines the queue remove policy
     *
     * @param doManualRemove true if manual purge
     */
    public static void setQueueRemovePolicy(final boolean doManualRemove) {
        QUEUE_MANUAL_REMOVE_JOBS = doManualRemove;
    }

    /**
     * Logs the Launcher statistics
     */
    public static void dumpStats() {
        final int live = JOBS_LIVE.get();
        final int total = JOBS_TOTAL.get();

        if ((live > _lastLive) || (total > _lastTotal)) {
            // fast simple barrier :
            _lastLive = live;
            _lastTotal = total;

            if (_logger.isInfoEnabled()) {
                _logger.info("LocalLauncher: Live Jobs: {} / Total Jobs: {}", live, total);
            }
        }
    }

    /**
     * Create a ProcessContext with the given command and arguments
     *
     * @param appName application identifier
     * @param owner user name
     * @param workingDir working directory to use (null indicates current directory)
     * @param writeLogFile absolute file path to write STD OUT / ERR streams (null indicates not to use a file dump)
     *
     * @return created job context
     */
    public static RootContext prepareMainJob(final String appName, final String owner, final String workingDir, final String writeLogFile) {
        _logger.debug("LocalLauncher.prepareMainJob: enter");

        final Long id = Long.valueOf(JOBS_ID.decrementAndGet());

        final RootContext runCtx = new RootContext(appName, id, workingDir);
        runCtx.setOwner(owner);
        runCtx.setRing(new RingBuffer(MAX_LINES, writeLogFile));

        _logger.debug("LocalLauncher.prepareMainJob: exit: {}", runCtx);

        return runCtx;
    }

    /**
     * Create a ProcessContext with the given command and arguments
     *
     * @param parent root context of the given run context
     * @param name name of the operation
     * @param command unix command with arguments as an array
     *
     * @return created job context
     */
    public static RunContext prepareChildJob(final RootContext parent, final String name, final String[] command) {
        if (CollectionUtils.isEmpty(command)) {
            throw new IllegalArgumentException("Invalid command parameter !");
        }

        _logger.debug("LocalLauncher.prepareJob: enter");

        final Long id = Long.valueOf(JOBS_ID.decrementAndGet());

        final ProcessContext runCtx = new ProcessContext(parent, name, id, command);

        // set pending state :
        runCtx.setState(RunState.STATE_PENDING);

        runCtx.setRing(parent.getRing());

        _logger.debug("LocalLauncher.prepareJob: exit: {}", runCtx);

        return runCtx;
    }

    /**
     * Adds a job context in the queue and call the registered listener if job is accepted in the queue (pending).
     * The job will be executed by the Process Thread pool.
     * 
     * @param rootCtx root context to execute
     */
    public static void startJob(final RootContext rootCtx) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("LocalLauncher.startJob: starting job: {}", rootCtx.shortString());
        }

        // set pending state :
        rootCtx.setState(RunState.STATE_PENDING);

        // Get the registered job listener :
        final JobListener listener = JOB_LISTENER.get(rootCtx.getName());

        if (listener == null) {
            throw new IllegalStateException("No Job listener for application [" + rootCtx.getName() + "] !");
        }

        queueJob(rootCtx, listener);

        _logger.debug("LocalLauncher.startJob: exit");
    }

    /**
     * Adds a job context in the queue and call the given listener if job is accepted in the queue (pending).
     * The job will be executed by the Process Thread pool.
     * 
     * @param rootCtx root context to execute
     * @param listener job listener to use
     */
    public static void startJob(final RootContext rootCtx, final JobListener listener) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("LocalLauncher.startJob: starting job: {}", rootCtx.shortString());
        }

        // set pending state :
        rootCtx.setState(RunState.STATE_PENDING);

        queueJob(rootCtx, listener);

        _logger.debug("LocalLauncher.startJob: exit");
    }

    /**
     * Add a pending job to the queue
     * @param rootCtx job to add
     * @param listener job listener to use
     */
    private static void queueJob(final RootContext rootCtx, final JobListener listener) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Job listener for application '{}': ", rootCtx.getName(), listener);
        }

        // uses the runner thread pool to run the job :
        // throws IllegalStateException if the job is not queued or the thread pool is down :
        final ThreadExecutors e = ThreadExecutors.getRunnerExecutor();

        // The executor is ready to accept new tasks :
        final Future<?> future = e.submit(new JobRunner(e.getExecutor(), rootCtx, listener));

        // increment total counter :
        JOBS_TOTAL.incrementAndGet();

        // Here : job has been accepted and queued in ThreadExecutor (maybe already running) :

        // define the future associated to the root context :
        rootCtx.setFuture(future);

        // add in queue for monitoring :
        addInQueue(rootCtx);

        // call listener :
        if (listener != null) {
            listener.performJobEvent(rootCtx);
        }
    }

    /**
     * Cancel the job given its identifier if pending
     * or stop the job given its identifier and may kill it if running
     * @param id job id
     */
    public static void cancelOrKillJob(final Long id) {
        final RunContext runCtx = LocalLauncher.getJob(id);
        if (runCtx != null) {
            try {
                if (runCtx instanceof RootContext) {
                    // kill the root context :
                    final RootContext ctx = ((RootContext) runCtx);
                    if (ctx.getState() == RunState.STATE_PENDING) {
                        ctx.setState(RunState.STATE_CANCELED);
                        if (ctx.getFuture() != null) {
                            // cancel a pending task :
                            ctx.getFuture().cancel(true);
                        }
                    } else if (ctx.getState() == RunState.STATE_RUNNING) {
                        final RunContext child = ctx.getCurrentChildContext();

                        if (child != null) {
                            ctx.setState(RunState.STATE_KILLED);
                            child.kill();
                        }
                    }
                }
            } finally {
                // clear ring buffer :
                runCtx.close();
            }
        }
    }

    /**
     * Stop the job given its identifier and may kill it if running
     * @param id job id
     */
    public static void killJob(final Long id) {
        final RunContext runCtx = LocalLauncher.getJob(id);
        if (runCtx != null) {
            try {
                if (runCtx instanceof RootContext) {
                    // kill the root context :
                    final RootContext ctx = ((RootContext) runCtx);
                    if (ctx.getState() == RunState.STATE_RUNNING) {
                        final RunContext child = ctx.getCurrentChildContext();

                        if (child != null) {
                            ctx.setState(RunState.STATE_KILLED);
                            child.kill();
                        }
                    }
                }
            } finally {
                // clear ring buffer :
                runCtx.close();
            }
        }
    }

    /**
     * Cancel the job given its identifier if pending
     * @param id job id
     */
    public static void cancelJob(final Long id) {
        final RunContext runCtx = LocalLauncher.getJob(id);
        if (runCtx != null) {
            try {
                if (runCtx instanceof RootContext) {
                    // cancel the root context :
                    final RootContext ctx = ((RootContext) runCtx);
                    if (ctx.getState() == RunState.STATE_PENDING) {
                        ctx.setState(RunState.STATE_CANCELED);
                        if (ctx.getFuture() != null) {
                            // cancel a pending task :
                            ctx.getFuture().cancel(true);
                        }
                    }
                }

            } finally {
                // clear ring buffer :
                runCtx.close();
            }
        }
    }

    /**
     * Add a job context in the queue
     *
     * @param rootCtx job context
     */
    private static void addInQueue(final RootContext rootCtx) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("LocalLauncher.addInQueue: job queued: ", rootCtx.shortString());
        }

        try {
            // semaphore is acquired to protect queue :
            QUEUE_SEM.acquire();

            JOB_QUEUE.put(rootCtx.getId(), rootCtx);
        } catch (final InterruptedException ie) {
            _logger.error("LocalLauncher.addInQueue: interrupted: ", ie);
        } finally {
            // semaphore is released :
            QUEUE_SEM.release();
        }

        // increment queue counter :
        JOBS_QUEUED.incrementAndGet();
    }

    /**
     * Remove a job context with the given identifier from the queue
     *
     * @param id job identifier
     */
    public static void removeFromQueue(final Long id) {
        _logger.debug("LocalLauncher.removeFromQueue: job to remove: {}", id);

        try {
            // semaphore is acquired to protect queue :
            QUEUE_SEM.acquire();

            final RunContext runCtx = JOB_QUEUE.remove(id);

            if (runCtx == null) {
                _logger.warn("LocalLauncher.removeFromQueue: job not found in queue: {}", id);
            } else {
                if (_logger.isDebugEnabled()) {
                    _logger.debug("LocalLauncher.removeFromQueue: job removed from queue: ", runCtx.shortString());
                }
            }
        } catch (final InterruptedException ie) {
            _logger.error("LocalLauncher.removeFromQueue: interrupted: ", ie);
        } finally {
            // semaphore is released :
            QUEUE_SEM.release();
        }
    }

    /**
     * Return a copy of the current queue (used to display its state)
     *
     * @return List of job present in the queue when this method is called
     */
    public static List<RootContext> getQueue() {
        try {
            // semaphore is acquired to protect queue :
            QUEUE_SEM.acquire();

            return new ArrayList<RootContext>(JOB_QUEUE.values());
        } catch (final InterruptedException ie) {
            _logger.error("LocalLauncher.getQueue: interrupted: ", ie);
        } finally {
            // semaphore is released :
            QUEUE_SEM.release();
        }

        return null;
    }

    /**
     * Return a copy of the current queue (used to display its state)
     *
     * @return List of job present in the queue when this method is called
     */
    public static int queryActiveQueuedJobs() {
        int count = 0;
        try {
            // semaphore is acquired to protect queue :
            QUEUE_SEM.acquire();

            for (RootContext c : JOB_QUEUE.values()) {
                if ((c.getState() == RunState.STATE_PENDING || c.getState() == RunState.STATE_RUNNING)) {
                    count++;
                }
            }
        } catch (final InterruptedException ie) {
            _logger.error("LocalLauncher.queryActiveQueuedJobs: interrupted : ", ie);
        } finally {
            // semaphore is released :
            QUEUE_SEM.release();
        }

        return count;
    }

    /**
     * Return a copy of the current queue (used to display its state)
     *
     * @param user user id
     * @return List of job present in the queue when this method is called
     */
    public static int queryActiveQueuedJobs(final String user) {
        if (user == null) {
            return 0;
        }
        final String owner = user.trim();
        int count = 0;
        try {
            // semaphore is acquired to protect queue :
            QUEUE_SEM.acquire();

            for (RootContext c : JOB_QUEUE.values()) {
                if (owner.equals(c.getOwner())
                        && (c.getState() == RunState.STATE_PENDING || c.getState() == RunState.STATE_PENDING)) {
                    count++;
                }
            }
        } catch (final InterruptedException ie) {
            _logger.error("LocalLauncher.queryActiveQueuedJobs: interrupted : ", ie);
        } finally {
            // semaphore is released :
            QUEUE_SEM.release();
        }

        return count;
    }

    /**
     * Return a job context for the given identifier
     *
     * @param id job identifier
     *
     * @return job context or null if not present
     */
    public static RunContext getJob(final Long id) {
        try {
            // semaphore is acquired to protect queue :
            QUEUE_SEM.acquire();

            return JOB_QUEUE.get(id);
        } catch (final InterruptedException ie) {
            _logger.error("LocalLauncher.getJob: interrupted: ", ie);
        } finally {
            // semaphore is released :
            QUEUE_SEM.release();
        }

        return null;
    }

    /**
     * This class implements Runnable to run a job submitted in the queue
     */
    private static final class JobRunner implements Runnable {

        /** max number of executed child task (avoid indefinite loop) */
        public final static int MAX_TASKS = 10;
        //~ Members --------------------------------------------------------------------------------------------------------
        /** thread pool running this job used to get its status (running, shutdown, terminated) */
        private final CustomThreadPoolExecutor _executor;
        /** job context */
        private final RootContext _rootCtx;
        /** job listener */
        private final JobListener _listener;

        /**
         * Constructor for the given job context and listener
         * @param executorService thread pool running this job
         * @param rootCtx job context
         * @param listener job listener
         */
        protected JobRunner(final CustomThreadPoolExecutor executorService, final RootContext rootCtx, final JobListener listener) {
            this._executor = executorService;
            this._rootCtx = rootCtx;
            this._listener = listener;
        }

        /**
         * This method uses the job listener for the running & finished events and use the ProcessRunner to execute the job
         */
        @Override
        public void run() {
            _logger.debug("JobRunner.run : enter");

            if (_rootCtx.getState() != RunState.STATE_CANCELED) {
                // increment live counter :
                JOBS_LIVE.incrementAndGet();

                RunState lastState = null;
                boolean ok = true;
                try {
                    // set running state :
                    _rootCtx.setState(RunState.STATE_RUNNING);

                    // call listener :
                    if (_listener != null) {
                        _listener.performJobEvent(_rootCtx);
                    }

                    // Execute the tasks here :
                    int n = 0;
                    RunContext child = null;

                    while (ok && _rootCtx.hasNext() && n < MAX_TASKS) {
                        child = _rootCtx.next();

                        ok = false;

                        executeTask(child);

                        lastState = child.getState();

                        // call listener :
                        ok = true;
                        if (_listener != null) {
                            ok = _listener.performTaskDone(_rootCtx, child);
                        }

                        if (!ok) {
                            break;
                        }

                        // go forward in child contexts :
                        _rootCtx.goNext();
                        n++;
                    }

                } catch (RuntimeException re) {
                    _logger.error("JobRunner.run : runtime exception : ", re);
                    ok = false;
                } finally {

                    _rootCtx.getRing().add("Job '" + _rootCtx.getName() + "' Ended.");

                    // handle states :
                    if (RunState.STATE_INTERRUPTED == lastState && this._executor.isShutdown()) {
                        // interrupted due to thread pool shutdown :
                        _rootCtx.setState(RunState.STATE_INTERRUPTED);
                    } else {
                        if (_rootCtx.getState() != RunState.STATE_CANCELED && _rootCtx.getState() != RunState.STATE_KILLED) {
                            // set finished state :
                            _rootCtx.setState(ok ? RunState.STATE_FINISHED_OK : RunState.STATE_FINISHED_ERROR);
                        }
                    }

                    // call listener :
                    if (_listener != null) {
                        _listener.performJobEvent(_rootCtx);
                    }

                    // remove job from queue :
                    if (!QUEUE_MANUAL_REMOVE_JOBS) {
                        removeFromQueue(_rootCtx.getId());
                    }
                }

                // decrement live counter :
                JOBS_LIVE.decrementAndGet();
            }

            _logger.debug("JobRunner - thread.run : exit");
        }

        /**
         * This method uses the job listener for the running & finished events and use the ProcessRunner to execute the job
         * @param runCtx context to execute
         */
        private void executeTask(final RunContext runCtx) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("JobRunner.executeTask: enter: {}", runCtx.getId());
            }

            int status = ProcessRunner.STATUS_UNDEFINED;
            try {
                // set running state :
                runCtx.setState(RunState.STATE_RUNNING);

                // call listener :
                if (_listener != null) {
                    _listener.performTaskEvent(runCtx.getParent(), runCtx);
                }

                // starts program & waits for its end (and std threads) :
                // uses a ring buffer for stdout/stderr :

                if (runCtx instanceof ProcessContext) {
                    status = ProcessRunner.execute((ProcessContext) runCtx);
                } else {
                    status = ILLEGAL_STATE_ERROR_CODE;
                }

                _logger.debug("JobRunner.run: process return status: {}", status);

                // ring buffer is not synchronized because threads have finished their jobs in ProcessRunner.run(runCtx) :
                switch (status) {
                    case ProcessRunner.STATUS_NORMAL:
                        runCtx.getRing().add("Task '" + runCtx.getName() + "' Ended.");
                        break;
                    case ProcessRunner.STATUS_INTERRUPTED:
                        runCtx.getRing().add(ProcessRunner.ERR_PREFIX, "Task Interrupted.");
                        break;
                    case ProcessRunner.STATUS_UNDEFINED:
                    default:
                        runCtx.getRing().add(ProcessRunner.ERR_PREFIX, "Task Ended with an error code : " + status + ".");
                        break;
                }

            } finally {

                // set finished state :
                switch (status) {
                    case ProcessRunner.STATUS_NORMAL:
                        runCtx.setState(RunState.STATE_FINISHED_OK);
                        break;
                    case ProcessRunner.STATUS_INTERRUPTED:
                        runCtx.setState(RunState.STATE_INTERRUPTED);
                        break;
                    case ProcessRunner.STATUS_UNDEFINED:
                    default:
                        runCtx.setState(RunState.STATE_FINISHED_ERROR);
                        break;
                }

                // call listener :
                if (_listener != null) {
                    _listener.performTaskEvent(runCtx.getParent(), runCtx);
                }
            }

            if (_logger.isDebugEnabled()) {
                _logger.debug("JobRunner.executeTask: exit: {}", runCtx.getId());
            }
        }
    }
}
