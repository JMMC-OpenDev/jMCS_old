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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extends {@link java.lang.Thread} to add the logging system and logs on interrupt and
 * start methods
 * 
 * @see CustomThreadFactory
 * @author Laurent BOURGES (voparis).
 */
public final class PoolThread extends Thread {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(PoolThread.class.getName());

    /**
     * Allocates a new <code>Thread</code> object.
     * 
     * @param target the object whose <code>run</code> method is called.
     * @param name the name of the new thread.
     * @see java.lang.Thread#Thread(java.lang.ThreadGroup, java.lang.Runnable, java.lang.String)
     */
    public PoolThread(final Runnable target, final String name) {
        super(target, name);
    }

    /**
     * Log and Interrupt this thread.
     */
    @Override
    public void interrupt() {
        if (_logger.isDebugEnabled()) {
            _logger.debug("{} : interrupt", getName());
        }
        super.interrupt();
    }

    /**
     * Log and start this thread.
     */
    @Override
    public synchronized void start() {
        if (_logger.isDebugEnabled()) {
            _logger.debug("{} : start", getName());
        }
        super.start();
    }

    /**
     * Run method overridden to add logs and clean up
     */
    @Override
    public void run() {
        if (_logger.isDebugEnabled()) {
            _logger.debug("{} : before run()", getName());
        }
        try {
            super.run();
        } finally {
            if (_logger.isDebugEnabled()) {
                _logger.debug("{} : after run()", getName());
            }
        }

    }
}
