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

import fr.jmmc.jmcs.util.MCSExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Thread Factory for ThreadExecutors to create threads
 *
 * @see ThreadFactory
 * @author Laurent BOURGES (voparis).
 */
public final class CustomThreadFactory implements ThreadFactory {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(CustomThreadFactory.class.getName());
    // Members
    /** thread pool name */
    private final String _name;
    /** thread priority */
    private final int _priority;
    /** global thread counter */
    private final AtomicInteger _threadNumber = new AtomicInteger(1);
    /** thread name prefix */
    private final String _namePrefix;

    /**
     * Constructor with the given thread pool name and use the normal thread priority
     *
     * @param pPoolName thread pool name
     */
    public CustomThreadFactory(final String pPoolName) {
        this(pPoolName, Thread.NORM_PRIORITY);
    }

    /**
     * Constructor with the given thread pool name and thread priority
     *
     * @param pPoolName thread pool name
     * @param pPriority thread priority to set on created thread
     */
    public CustomThreadFactory(final String pPoolName, final int pPriority) {
        _name = pPoolName;
        _priority = pPriority;
        _namePrefix = pPoolName + "-thread-";
    }

    //~ Methods ----------------------------------------------------------------------------------------------------------
    /**
     * Creates a new Thread (PoolThread) with the name [thread pool name]-thread-[number] and set its
     * priority
     *
     * @param r Runnable task
     * @return new thread created
     */
    @Override
    public Thread newThread(final Runnable r) {
        _logger.debug("CustomThreadFactory.newThread : enter with task: {}", r);

        final Thread thread = new PoolThread(r, _namePrefix + _threadNumber.getAndIncrement());
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != _priority) {
            thread.setPriority(_priority);
        } else {
            thread.setPriority(Thread.NORM_PRIORITY);
        }

        // define UncaughtExceptionHandler :
        MCSExceptionHandler.installThreadHandler(thread);

        _logger.debug("CustomThreadFactory.newThread : exit with thread {} for task: {}", thread, r);
        return thread;
    }

    /**
     * Return the thread pool name
     * @return thread pool name
     */
    public String getName() {
        return _name;
    }

    /**
     * Return the thread priority (Thread.NORM_PRIORITY by default)
     * @see Thread#NORM_PRIORITY
     * @return thread priority
     */
    public int getPriority() {
        return _priority;
    }

    /**
     * Return the global thread counter
     * @return global thread counter
     */
    public AtomicInteger getThreadNumber() {
        return _threadNumber;
    }
}
