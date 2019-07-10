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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom ThreadPoolExecutor to add extensions
 * @author Laurent BOURGES (voparis).
 */
public final class CustomThreadPoolExecutor extends ThreadPoolExecutor {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(CustomThreadPoolExecutor.class.getName());
    /** debug flag to log thread activity */
    public static final boolean DO_DEBUG = false;
    // Members
    /** thread pool name */
    private final String _name;

    /**
     * Single constructor allowed
     *
     * @param pPoolName thread pool name
     * @param corePoolSize the number of threads to keep in the
     * pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the
     * pool.
     * @param keepAliveTime when the number of threads is greater than
     * the core, this is the maximum time that excess idle threads
     * will wait for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime
     * argument.
     * @param workQueue the queue to use for holding tasks before they
     * are executed. This queue will hold only the <tt>Runnable</tt>
     * tasks submitted by the <tt>execute</tt> method.
     * @param threadFactory the factory to use when the executor
     * creates a new thread.
     */
    public CustomThreadPoolExecutor(final String pPoolName,
            final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                threadFactory);
        _name = pPoolName;
    }

    /**
     * Return the thread pool name
     * @return thread pool name
     */
    public String getPoolName() {
        return _name;
    }

    /**
     * Called before a task is run
     * @param t thread used to run the task
     * @param r runnable task
     */
    @Override
    protected void beforeExecute(final Thread t, final Runnable r) {
        _logger.debug("{}.beforeExecute : runnable: {}", _name, r);

        if (DO_DEBUG) {
            _logger.warn("{}.beforeExecute : runnable: {}", _name, r);
        }
    }

    /**
     * Called after the task has completed
     * @param r the runnable that has completed.
     * @param th the exception that caused termination, or null if
     * execution completed normally.
     */
    @Override
    protected void afterExecute(final Runnable r, Throwable th) {
        if (th == null && r instanceof FutureTask) {
            final FutureTask task = (FutureTask) r;
            // If r is a FutureTask, the task is done
            // Use get() to retrieve possible exception
            try {
                task.get();
            } catch (InterruptedException ie) {
                _logger.debug("{}.afterExecute : runnable: {}", _name, ie);
            } catch (CancellationException ce) {
                _logger.debug("{}.afterExecute : runnable: {}", _name, ce);
            } catch (ExecutionException ee) {
                th = ee.getCause();
            }
        }
        if (th == null) {
            _logger.debug("{}.afterExecute : runnable: {}", _name, r);

            if (DO_DEBUG) {
                _logger.warn("{}.afterExecute : runnable: {}", _name, r);
            }
        } else {
            final Thread thread = Thread.currentThread();
            final Thread.UncaughtExceptionHandler handler = thread.getUncaughtExceptionHandler();

            if (handler != null) {
                _logger.debug("{}.afterExecute : uncaught exception: {}", _name, th);

                handler.uncaughtException(thread, th);
            } else {
                _logger.error("{}.afterExecute : uncaught exception: {}", _name, th);
            }
        }
    }

    /**
     * Method invoked when the Executor has terminated.  Default
     * implementation does nothing. Note: To properly nest multiple
     * overriding, subclasses should generally invoke
     * <tt>super.terminated</tt> within this method.
     */
    @Override
    protected void terminated() {
        _logger.debug("{}.terminated.", _name);
    }
}
