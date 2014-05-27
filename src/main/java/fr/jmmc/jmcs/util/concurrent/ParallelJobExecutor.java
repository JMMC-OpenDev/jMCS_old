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
package fr.jmmc.jmcs.util.concurrent;

import ch.qos.logback.classic.Level;
import fr.jmmc.jmcs.util.JVMUtils;
import fr.jmmc.jmcs.logging.LoggingService;
import fr.jmmc.jmcs.util.MCSExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gathers one thread pool dedicated to execute parallel computation jobs
 *
 * @author Laurent BOURGES.
 */
public final class ParallelJobExecutor {

    /** debug flag */
    private static final boolean DEBUG_JOBS = false;
    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(ParallelJobExecutor.class.getName());
    /** singleton pattern */
    private static volatile ParallelJobExecutor _instance = null;
    /** The ThreadLocal storing thread indexes */
    private static final ThreadLocal<Integer> _localIndex = new ThreadLocal<Integer>();
    /* members */
    /** number of available processors */
    private final int _cpuCount;
    /** maximum number of running parallel job */
    private int _maxParallelJob;
    /** thread pool dedicated to this computation */
    private final FixedThreadPoolExecutor _parallelExecutor;

    /**
     * Return the singleton instance
     *
     * @return ParallelJobExecutor instance
     */
    public static synchronized ParallelJobExecutor getInstance() {
        if (_instance == null) {
            _instance = new ParallelJobExecutor();
        }
        return _instance;
    }

    /**
     * Shutdown the thread pool immediately.
     */
    public static synchronized void shutdown() {
        if (_instance != null) {
            _instance.getParallelExecutor().shutdownNow();
            _instance = null;
            _logger.info("ParallelJobExecutor stopped.");
        }
    }

    /**
     * Private constructor
     */
    private ParallelJobExecutor() {
        super();
        _cpuCount = JVMUtils.availableProcessors();
        _maxParallelJob = _cpuCount;

        // create any the thread pool even if there is only 1 CPU:
        final int threadCount = _cpuCount;
        _parallelExecutor = new FixedThreadPoolExecutor(threadCount, new JobWorkerThreadFactory());

        // create threads now:
        _parallelExecutor.prestartAllCoreThreads();

        _logger.info("ParallelJobExecutor ready with {} threads", _parallelExecutor.getMaximumPoolSize());

        if (DEBUG_JOBS) {
            LoggingService.setLoggerLevel(_logger, Level.DEBUG);
        }
    }

    /**
     * Return true if this machine has more than 1 CPU
     *
     * @return true if this machine has more than 1 CPU
     */
    public boolean isEnabled() {
        return _maxParallelJob > 1;
    }

    /**
     * Return the maximum number of running parallel job
     *
     * @return maximum number of running parallel job
     */
    public int getMaxParallelJob() {
        return _maxParallelJob;
    }

    /**
     * Define the maximum number of running parallel job
     *
     * @param maxParallelJob maximum number of running parallel job
     */
    public void setMaxParallelJob(final int maxParallelJob) {
        _maxParallelJob = (maxParallelJob > _cpuCount) ? _cpuCount : maxParallelJob;
    }

    /**
     * Return the number of available processors
     *
     * @return number of available processors
     */
    public int getCpuCount() {
        return _cpuCount;
    }

    /**
     * Return the thread pool dedicated to this computation
     *
     * @return thread pool dedicated to this computation
     */
    private ThreadPoolExecutor getParallelExecutor() {
        return _parallelExecutor;
    }

    /**
     * Return true if the current thread is a Job worker thread
     * @return true if the current thread is a Job worker thread  
     */
    public boolean isWorkerThread() {
        return Thread.currentThread() instanceof JobWorkerThread;
    }

    /**
     * Submit the given jobs and wait for their completion
     * If the current thread is interrupted (canceled), then futures are canceled too.
     * 
     * @param jobName job name used when throwing an exception
     * @param jobs callable jobs i.e. jobs that return results
     *
     * @throws InterruptedJobException if the current thread is interrupted (canceled)
     * @throws RuntimeException if any exception occurred during the computation
     */
    public void forkAndJoin(final String jobName, final Runnable[] jobs) throws InterruptedJobException, RuntimeException {
        forkAndJoin(jobName, jobs, true);
    }

    /**
     * Submit the given jobs and wait for their completion
     * If the current thread is interrupted (canceled), then futures are canceled too.
     * 
     * @param jobName job name used when throwing an exception
     * @param jobs callable jobs i.e. jobs that return results
     * @param useThreads flag to enable or disable thread pool usage (async)
     *
     * @throws InterruptedJobException if the current thread is interrupted (canceled)
     * @throws RuntimeException if any exception occurred during the computation.
     */
    public void forkAndJoin(final String jobName, final Runnable[] jobs, final boolean useThreads) throws InterruptedJobException, RuntimeException {
        if (jobs == null) {
            // illegal state ?
            return;
        }

        final Thread currentTh = Thread.currentThread(); // local var

        // fast interrupt :
        if (currentTh.isInterrupted()) {
            throw new InterruptedJobException(jobName + ": interrupted");
        }

        final int len = jobs.length;

        if (useThreads && len > 1) {
            // execute jobs in parallel:
            final Future<?>[] futures = fork(jobs);

            _logger.debug("wait for jobs to terminate ...");

            join(jobName, futures);

        } else {
            try {
                // execute job(s) using the current thread:
                for (int i = 0; i < len; i++) {
                    jobs[i].run();
                }
            } catch (Exception e) {
                throw new RuntimeException(jobName + ": failed:", e);
            }
        }

        // fast interrupt :
        if (currentTh.isInterrupted()) {
            throw new InterruptedJobException(jobName + ": interrupted");
        }
    }

    /**
     * Submit the given jobs and wait for their completion
     * If the current thread is interrupted (canceled), then futures are canceled too.
     * 
     * @param jobName job name used when throwing an exception
     * @param jobs callable jobs i.e. jobs that return results
     * @return results as List<Object> or null if interrupted
     *
     * @throws InterruptedJobException if the current thread is interrupted (canceled)
     * @throws RuntimeException if any exception occurred during the computation
     */
    public List<?> forkAndJoin(final String jobName, final Callable<?>[] jobs) throws InterruptedJobException, RuntimeException {
        return forkAndJoin(jobName, jobs, true);
    }

    /**
     * Submit the given jobs and wait for their completion
     * If the current thread is interrupted (canceled), then futures are canceled too.
     * 
     * @param jobName job name used when throwing an exception
     * @param jobs callable jobs i.e. jobs that return results
     * @param useThreads flag to enable or disable thread pool usage (async)
     * @return results as List<Object> or null if interrupted
     *
     * @throws InterruptedJobException if the current thread is interrupted (canceled)
     * @throws RuntimeException if any exception occurred during the computation
     */
    public List<?> forkAndJoin(final String jobName, final Callable<?>[] jobs, final boolean useThreads) throws InterruptedJobException, RuntimeException {
        if (jobs == null) {
            // illegal state ?
            return null;
        }

        final Thread currentTh = Thread.currentThread(); // local var

        // fast interrupt :
        if (currentTh.isInterrupted()) {
            throw new InterruptedJobException(jobName + ": interrupted");
        }

        final List<Object> results;
        final int len = jobs.length;

        if (useThreads && len > 1) {
            // execute jobs in parallel:
            final Future<?>[] futures = fork(jobs);

            _logger.debug("wait for jobs to terminate ...");

            results = join(jobName, futures);

        } else {
            results = new ArrayList<Object>(len);
            try {
                // execute job(s) using the current thread:
                for (int i = 0; i < len; i++) {
                    results.add(jobs[i].call());
                }
            } catch (Exception e) {
                throw new RuntimeException(jobName + ": failed:", e);
            }
        }

        // fast interrupt :
        if (currentTh.isInterrupted()) {
            throw new InterruptedJobException(jobName + ": interrupted");
        }

        return results;
    }

    /**
     * Submit the given job to immediate execution and returns its Future object to wait for or cancel job.
     *
     * @param job runnable job.
     * @return Future object to wait for or cancel jobs.
     */
    public Future<?> fork(final Runnable job) {
        if (job == null) {
            // illegal state ?
            return null;
        }

        // start job:
        final Future<?> future = _parallelExecutor.submit(job);

        _logger.debug("started job: {}", future);

        return future;
    }

    /**
     * Submit the given jobs to immediate execution and returns Future objects to wait for or cancel jobs.
     *
     * @param jobs runnable jobs.
     * @return Future objects to wait for or cancel jobs.
     */
    public Future<?>[] fork(final Runnable[] jobs) {
        if (jobs == null) {
            // illegal state ?
            return null;
        }

        // start jobs:
        final boolean isLogDebug = _logger.isDebugEnabled();

        final int len = jobs.length;

        if (isLogDebug) {
            _logger.debug("starting {} jobs ...", len, new Throwable());
        }

        final Future<?>[] futures = new Future<?>[len];
        Future<?> future;

        for (int i = 0; i < len; i++) {
            future = _parallelExecutor.submit(jobs[i]);

            if (isLogDebug) {
                _logger.debug("started job: {}", future);
            }

            futures[i] = future;
        }

        if (isLogDebug) {
            _logger.debug("{} jobs started.", futures.length);
        }

        return futures;
    }

    /**
     * Submit the given jobs to immediate execution and returns Future objects to wait for or cancel jobs.
     *
     * @param jobs callable jobs i.e. jobs that return results.
     * @return Future objects to wait for or cancel jobs.
     */
    private Future<?>[] fork(final Callable<?>[] jobs) {
        if (jobs == null) {
            // illegal state ?
            return null;
        }

        // start jobs:
        final boolean isLogDebug = _logger.isDebugEnabled();

        final int len = jobs.length;

        if (isLogDebug) {
            _logger.debug("starting {} jobs ...", len, new Throwable());
        }

        final Future<?>[] futures = new Future<?>[len];
        Future<?> future;

        for (int i = 0; i < len; i++) {
            future = _parallelExecutor.submit(jobs[i]);

            if (isLogDebug) {
                _logger.debug("started job: {}", future);
            }

            futures[i] = future;
        }

        if (isLogDebug) {
            _logger.debug("{} jobs started.", futures.length);
        }

        return futures;
    }

    /**
     * Waits for all threads to complete computation.
     * If the current thread is interrupted (canceled), then futures are canceled too.
     *
     * @param jobName job name used when throwing an exception
     * @param futures Future objects to wait for
     * @return results as List<Object> or null if interrupted
     *
     * @throws InterruptedJobException if the current thread is interrupted (canceled).
     * @throws RuntimeException if any exception occurred during the computation.
     */
    public List<Object> join(final String jobName, final Future<?>[] futures) throws InterruptedJobException, RuntimeException {
        if (futures == null) {
            // illegal state ?
            return null;
        }

        // join futures:
        final boolean isLogDebug = _logger.isDebugEnabled();

        final int len = futures.length;

        if (isLogDebug) {
            _logger.debug("join {} jobs ...", len, new Throwable());
        }

        final List<Object> results = new ArrayList<Object>(len);

        int done = 0;
        boolean doCancel = false;
        Future<?> future;
        try {
            // Wait on running job:
            for (int i = 0; i < len; i++) {
                future = futures[i];

                if (isLogDebug) {
                    _logger.debug("wait for job: {}", future);
                }

                results.add(future.get());
                done++;
            }
        } catch (ExecutionException ee) {
            doCancel = true;
            throw new RuntimeException(jobName + ": failed:", ee.getCause());
        } catch (CancellationException ce) {
            if (isLogDebug) {
                _logger.debug("join: task cancelled:", ce);
            }
            doCancel = true;
            throw new InterruptedJobException(jobName + ": interrupted", ce);
        } catch (InterruptedException ie) {
            if (isLogDebug) {
                _logger.debug("join: waiting thread cancelled:", ie);
            }
            doCancel = true;
            throw new InterruptedJobException(jobName + ": interrupted", ie);
        } finally {
            if (doCancel) {
                _logger.debug("cancel jobs:");

                // Cancel and interrupt any running job:
                // note: in reverse order to avoid starting new jobs while cancelling them:
                for (int i = len - 1; i >= done; i--) {
                    future = futures[i];

                    if (isLogDebug) {
                        _logger.debug("cancel job: {}", future);
                    }

                    // do interrupt thread if running:
                    future.cancel(true);
                }

                // Wait for threads to finish their task (cancellation):
                _parallelExecutor.waitForTaskFinished();

                // Anyway: interrupt this thread again anyway:
                Thread.currentThread().interrupt();
            }
        }

        if (isLogDebug) {
            _logger.debug("{} jobs joined.", len);
        }

        return results;
    }

    /**
     * Custom ThreadFactory implementation
     */
    private static final class JobWorkerThreadFactory implements ThreadFactory {

        /** thread index (starting from 0) */
        private final AtomicInteger threadNumber = new AtomicInteger(0);

        /**
         * Constructs a new {@code Thread}.
         *
         * @param r a runnable to be executed by new thread instance
         * @return constructed thread, or {@code null} if the request to create
         * a thread is rejected
         */
        @Override
        public Thread newThread(final Runnable r) {

            final int threadIndex = threadNumber.getAndIncrement();

            final Thread thread = new JobWorkerThread(r, threadIndex);
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }

            // define UncaughtExceptionHandler :
            MCSExceptionHandler.installThreadHandler(thread);

            if (_logger.isDebugEnabled()) {
                _logger.debug("new thread: {}", thread.getName());
            }

            return thread;
        }
    }

    /**
     * Custom Thread implementation keeping the thread index in the thread pool
     */
    private static final class JobWorkerThread extends Thread {

        /** thread index */
        private final Integer _index;

        /**
         * Protected constructor
         * @param target the object whose {@code run} method is invoked when this thread
         *         is started. If {@code null}, this thread's run method is invoked.
         * @param index thread index in the thread pool
         */
        JobWorkerThread(final Runnable target, final Integer index) {
            super(target, "JobWorker-" + index);
            _index = index;
        }

        /**
         * Return the thread index
         * @return thread index
         */
        public Integer getIndex() {
            return _index;
        }

        @Override
        public void run() {
            // store the thread index in thread local:
            _localIndex.set(_index);

            if (_logger.isDebugEnabled()) {
                _logger.debug("thread[{}] run", _index);
            }
            try {
                super.run();
            } finally {
                // always perform cleanup:
                _localIndex.remove();
            }
            if (_logger.isDebugEnabled()) {
                _logger.debug("thread[{}] done", _index);
            }
        }
    }

    /**
     * Returns the current thread's index modulo number of jobs so returns an int value between [0; nJobs[
     * 
     * Note: this methods should always ensure thread separation ie
     * thread indexes must be consecutive and exclusive ie only 1 thread returns the same thread index
     *
     * @param nJobs number of parallel jobs
     * @return the current thread's index in [0; nJobs[
     */
    public static int currentThreadIndex(final int nJobs) {
        final Integer threadIndex = _localIndex.get();
        if (threadIndex == null) {
            return 0;
        }
        return threadIndex.intValue() % nJobs;
    }
}
