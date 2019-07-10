/*******************************************************************************
 *                 jMCS project ( http://www.jmmc.fr/dev/jmcs )
 *******************************************************************************
 * Copyright (c) 2019, CNRS. All rights reserved.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This custom Thread class deals with thread interruption masking (ie defered interrupt signal) 
 * for critical sections.
 * 
 * For example:
 *     try {
 *         // Inhibits thread interrupt:
 *         InterruptableThread.setThreadMayInterruptIfRunning(false);
 *
 *         // critical section
 *
 *     } finally {
 *         // Restore thread interrupt, maybe now:
 *         InterruptableThread.setThreadMayInterruptIfRunning(true);
 *     }
 *
 * @author Laurent BOURGES.
 */
public final class InterruptableThread extends Thread {

    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(InterruptableThread.class.getName());

    /**
     * If the current thread is an InterruptableThread:
     * Define the flag indicating if thread interruption is immediate (true) or defered (false).
     * If an interruption signal is pending, the thread is interrupted now.
     * 
     * @param mayInterruptIfRunning immediate (true) or defered (false)
     */
    public static void setThreadMayInterruptIfRunning(final boolean mayInterruptIfRunning) {
        final Thread thread = Thread.currentThread();

        if (thread instanceof InterruptableThread) {
            ((InterruptableThread) thread).setMayInterruptIfRunning(mayInterruptIfRunning);
        }
    }

    /* members */
    /** flag indicating if thread interruption is immediate (true) or defered (false) */
    private boolean _mayInterruptIfRunning = true;
    /** flag indicating a pending interruption signal */
    private boolean _pendingInterrupt = false;

    InterruptableThread(final Runnable target, final String name) {
        super(target, name);
    }

    /**
     * Interrupts this thread immediately or later according to the mayInterruptIfRunning flag.
     */
    @Override
    public void interrupt() {
        if (_mayInterruptIfRunning) {
            _logger.debug("interrupt: do interrupt() now");

            super.interrupt();
        } else {
            _logger.debug("interrupt: skip interrupt() (pending)");

            _pendingInterrupt = true;
        }
    }

    /**
     * Define the flag indicating if thread interruption is immediate (true) or defered (false).
     * If an interruption signal is pending, the thread is interrupted now.
     * 
     * @param mayInterruptIfRunning immediate (true) or defered (false)
     */
    void setMayInterruptIfRunning(final boolean mayInterruptIfRunning) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("setMayInterruptIfRunning: {}", mayInterruptIfRunning);
        }

        this._mayInterruptIfRunning = mayInterruptIfRunning;

        if (mayInterruptIfRunning && _pendingInterrupt) {
            _pendingInterrupt = false;

            interrupt();
        }
    }

    /**
     * @return flag indicating if thread interruption is immediate (true) or defered (false)
     */
    boolean isMayInterruptIfRunning() {
        return _mayInterruptIfRunning;
    }
}
