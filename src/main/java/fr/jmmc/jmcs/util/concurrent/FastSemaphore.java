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

import java.io.Serializable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Imported fast semaphore class
 * 
 * Fast pathed semaphore :
 * It only does acquires and releases of single permits though it would be
 * easy to make it release multiple permits.  Acquiring multiple permits would
 * be a little tricky based on the current Semaphore semantics (atomic acquire
 * of all requested permits).
 * 
 * It's a port from a C win32/posix version that's been around for a while.
 * 
 * @author Joseph Seigh jseigh_cp00 at xemaps.com
 */
public final class FastSemaphore implements Serializable {

    /** serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;
    /** semaphore count */
    private final AtomicInteger count;
    /** deferred cancelation count */
    private final AtomicInteger cancel;
    /** slow semaphore */
    private final Semaphore sem;

    /**
     * Creates a <tt>Semaphore</tt> with the given number of
     * permits and nonfair fairness setting.
     * @param permits the initial number of permits available. This
     * value may be negative, in which case releases must
     * occur before any acquires will be granted.
     */
    public FastSemaphore(final int permits) {
        this(permits, false);
    }

    /**
     * Creates a <tt>Semaphore</tt> with the given number of
     * permits and the given fairness setting.
     * @param permits the initial number of permits available. This
     * value may be negative, in which case releases must
     * occur before any acquires will be granted.
     * @param fair true if this semaphore will guarantee first-in
     * first-out granting of permits under contention, else false.
     */
    public FastSemaphore(final int permits, final boolean fair) {
        count = new AtomicInteger(permits);
        cancel = new AtomicInteger(0);
        sem = new Semaphore(0, fair);
    }

    /**
     * Acquires a permit from this semaphore, blocking until one is
     * available, or the thread is {@link Thread#interrupt interrupted}.
     *
     * <p>Acquires a permit, if one is available and returns immediately,
     * reducing the number of available permits by one.
     * <p>If no permit is available then the current thread becomes
     * disabled for thread scheduling purposes and lies dormant until
     * one of two things happens:
     * <ul>
     * <li>Some other thread invokes the {@link #release} method for this
     * semaphore and the current thread is next to be assigned a permit; or
     * <li>Some other thread {@link Thread#interrupt interrupts} the current
     * thread.
     * </ul>
     *
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@link Thread#interrupt interrupted} while waiting
     * for a permit,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * @throws InterruptedException if the current thread is interrupted
     *
     * @see Thread#interrupt
     */
    public void acquire() throws InterruptedException {
        if (count.addAndGet(-1) < 0) {
            try {
                sem.acquire();
            } catch (InterruptedException e) {
                // uncomment one and only one of the following 2 statements
                cancel.incrementAndGet();
                // processCancels(1);
                throw e;
            }
        }
    }

    /**
     * Acquires a permit from this semaphore, if one becomes available 
     * within the given waiting time and the
     * current thread has not been {@link Thread#interrupt interrupted}.
     * <p>Acquires a permit, if one is available and returns immediately,
     * with the value <tt>true</tt>,
     * reducing the number of available permits by one.
     * <p>If no permit is available then
     * the current thread becomes disabled for thread scheduling
     * purposes and lies dormant until one of three things happens:
     * <ul>
     * <li>Some other thread invokes the {@link #release} method for this
     * semaphore and the current thread is next to be assigned a permit; or
     * <li>Some other thread {@link Thread#interrupt interrupts} the current
     * thread; or
     * <li>The specified waiting time elapses.
     * </ul>
     * <p>If a permit is acquired then the value <tt>true</tt> is returned.
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@link Thread#interrupt interrupted} while waiting to acquire
     * a permit,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     * <p>If the specified waiting time elapses then the value <tt>false</tt>
     * is returned.
     * If the time is less than or equal to zero, the method will not wait 
     * at all.
     *
     * @param timeout the maximum time to wait for a permit
     * @param unit the time unit of the <tt>timeout</tt> argument.
     * @return <tt>true</tt> if a permit was acquired and <tt>false</tt>
     * if the waiting time elapsed before a permit was acquired.
     *
     * @throws InterruptedException if the current thread is interrupted
     *
     * @see Thread#interrupt
     *
     */
    public boolean tryAcquire(final long timeout, final TimeUnit unit) throws InterruptedException {
        boolean rc;
        if (count.addAndGet(-1) < 0) {
            try {
                rc = sem.tryAcquire(timeout, unit);
            } catch (InterruptedException e) {
                // uncomment one and only one of the following 2 statements
                cancel.incrementAndGet();
                // processCancels(1);
                throw e;
            }

            if (rc == false) {
                cancel.incrementAndGet();
                // processCancels(1);
            }
            return rc;
        }
        return true;
    }

    /**
     * Acquires a permit from this semaphore, only if one is available at the 
     * time of invocation.
     * <p>Acquires a permit, if one is available and returns immediately,
     * with the value <tt>true</tt>,
     * reducing the number of available permits by one.
     *
     * <p>If no permit is available then this method will return
     * immediately with the value <tt>false</tt>.
     *
     * <p>Even when this semaphore has been set to use a
     * fair ordering policy, a call to <tt>tryAcquire()</tt> <em>will</em>
     * immediately acquire a permit if one is available, whether or not
     * other threads are currently waiting. 
     * This &quot;barging&quot; behavior can be useful in certain 
     * circumstances, even though it breaks fairness. If you want to honor
     * the fairness setting, then use 
     * {@link #tryAcquire(long, TimeUnit) tryAcquire(0, TimeUnit.SECONDS) }
     * which is almost equivalent (it also detects interruption).
     *
     * @return <tt>true</tt> if a permit was acquired and <tt>false</tt>
     * otherwise.
     */
    public boolean tryAcquire() {
        int oldCount;

        do {
            oldCount = count.get();
        } while (oldCount > 0 && !count.compareAndSet(oldCount, oldCount - 1));

        return (oldCount > 0);
    }

    /**
     * Releases a permit, returning it to the semaphore.
     * <p>Releases a permit, increasing the number of available permits
     * by one.
     * If any threads are trying to acquire a permit, then one
     * is selected and given the permit that was just released.
     * That thread is (re)enabled for thread scheduling purposes.
     * <p>There is no requirement that a thread that releases a permit must
     * have acquired that permit by calling {@link #acquire}.
     * Correct usage of a semaphore is established by programming convention
     * in the application.
     */
    public void release() {
        if (cancel.get() > 0 && count.get() < 0) {
            processCancels(cancel.getAndSet(0));
        }

        if (count.addAndGet(1) <= 0) {
            sem.release();
        }
    }

    /**
     * processCancels - add cancelCount to current count
     *
     *   increment count by min(cancelCount, -(count)) if count < 0
     * @param cancelCount value
     */
    private void processCancels(final int cancelCount) {
        int newCancelCount = cancelCount;
        if (newCancelCount > 0) {
            int oldCount;
            int newCount;
            while ((oldCount = count.get()) < 0) {
                if ((newCount = oldCount + newCancelCount) > 0) {
                    newCount = 0;
                }

                if (count.compareAndSet(oldCount, newCount)) {
                    newCancelCount -= (newCount - oldCount);        // update cancelCount           
                    break;
                }
            }
        }

        // add any untransferred cancelCount back into cancel
        if (newCancelCount > 0) {
            cancel.addAndGet(cancelCount);
        }
    }

    /**
     * Returns the current number of permits available in this semaphore.
     * <p>This method is typically used for debugging and testing purposes.
     * @return the number of permits available in this semaphore.
     */
    public int availablePermits() {
        return count.get();
    }
}
