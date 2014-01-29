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

import java.io.Serializable;

import java.util.Date;
import fr.jmmc.jmcs.util.runner.process.RingBuffer;

/**
 * Generic Job state (id, state, dates, duration).
 * Sub classes will extend this class to add specific attributes ...
 *
 * @author Laurent BOURGES (voparis).
 */
public class RunContext implements Serializable, Cloneable {

    /** serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;
    // Members
    /** Job identifier */
    private Long _id;
    /** Root Context reference (No cascade at all to have unary operation) */
    private RootContext _parent;
    /** Name of this task (useful to process task events) */
    private String _name;
    /** Job creation date */
    private Date _creationDate;
    /** Job queue date */
    private Date _queueDate = null;
    /** Job run date */
    private Date _runDate = null;
    /** Job end date */
    private Date _endDate = null;
    /** Job duration */
    private long _duration = 0L;
    /** Job state */
    private RunState _state;
    /** Ring Buffer for logs */
    private transient RingBuffer _ring = null;

    /**
     * Creates a new RunContext object for JPA
     */
    public RunContext() {
    }

    /**
     * Creates a new RunContext object
     *
     * @param parent root context
     * @param applicationName operation name
     * @param id job identifier
     */
    public RunContext(final RootContext parent, final String applicationName, final Long id) {
        _parent = parent;
        _id = id;
        _name = applicationName;
        // init :
        _state = RunState.STATE_UNKNOWN;
        _creationDate = new Date();

        if (parent != null) {
            parent.addChild(this);
        }
    }

    /**
     * Clones this instance via standard java Cloneable support
     *
     * @return cloned instance
     *
     * @throws CloneNotSupportedException
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * This method can be used to release resources : clear the ring buffer
     */
    public void close() {
        if (_ring != null) {
            _ring.close();
        }
    }

    /**
     * this method stops the execution of that context
     */
    public void kill() {
    }

    /**
     * Returns the process working directory
     *
     * @return process working directory
     */
    public String getWorkingDir() {
        return (getParent() != null) ? getParent().getWorkingDir() : null;
    }

    /**
     * Simple toString representation : "job[id][state] duration ms. - work dir : [workingDir]"
     *
     * @return "job[id][state] duration ms. - work dir : [workingDir]"
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getId() + "][" + getState() + "] "
                + ((getDuration() > 0L) ? (" : " + getDuration() + " ms.") : "")
                + " - work dir : " + getWorkingDir();
    }

    /**
     * Simple toString representation : "job[id][state]"
     *
     * @return "job[id][state]"
     */
    public String shortString() {
        return getClass().getSimpleName() + "[" + getId() + "][" + getState() + "]";
    }

    /**
     * Return the Root Context reference
     * @return Root Context reference
     */
    public final RootContext getParent() {
        return _parent;
    }

    /**
     * Returns the job identifier
     *
     * @return identifier
     */
    public final Long getId() {
        return _id;
    }

    /**
     * Set the job identifier
     *
     * @param pId identifier
     */
    protected final void setId(final Long pId) {
        _id = pId;
    }

    /**
     * Returns the job state
     *
     * @return job state
     */
    public final RunState getState() {
        return _state;
    }

    /**
     * Return true if the current state is STATE_RUNNING
     * @return true if the current state is STATE_RUNNING
     */
    public final boolean isRunning() {
        return getState() == RunState.STATE_RUNNING;
    }

    /**
     * Return true if the current state is STATE_PENDING
     * @return true if the current state is STATE_PENDING
     */
    public final boolean isPending() {
        return getState() == RunState.STATE_PENDING;
    }

    /**
     * Defines the job state and corresponding date
     *
     * @param state to set
     */
    protected final void setState(final RunState state) {
        _state = state;
        switch (state) {
            case STATE_PENDING:
                setQueueDate(new Date());

                break;

            case STATE_RUNNING:
                setRunDate(new Date());

                break;

            case STATE_CANCELED:
            case STATE_INTERRUPTED:
            case STATE_KILLED:
            case STATE_FINISHED_ERROR:
            case STATE_FINISHED_OK:
                setEndDate(new Date());

                break;

            default:
        }
    }

    /**
     * Return the job creation date
     * @return job creation date
     */
    public Date getCreationDate() {
        return _creationDate;
    }

    /**
     * Returns the job queue date
     *
     * @return job queue date
     */
    public final Date getQueueDate() {
        return _queueDate;
    }

    /**
     * Defines the job queue date
     *
     * @param queueDate date to set
     */
    private final void setQueueDate(final Date queueDate) {
        _queueDate = queueDate;
    }

    /**
     * Returns the job run date
     *
     * @return job run date
     */
    public final Date getRunDate() {
        return _runDate;
    }

    /**
     * Defines the job run date
     *
     * @param runDate run date to set
     */
    private final void setRunDate(final Date runDate) {
        _runDate = runDate;
    }

    /**
     * Returns the job end date
     *
     * @return job end date
     */
    public final Date getEndDate() {
        return _endDate;
    }

    /**
     * Defines the job end date
     *
     * @param endDate date to set
     */
    private final void setEndDate(final Date endDate) {
        _endDate = endDate;
    }

    /**
     * Returns the job duration in ms
     *
     * @return job duration
     */
    public final long getDuration() {
        return _duration;
    }

    /**
     * Defines the job duration in ms
     *
     * @param duration to set
     */
    public final void setDuration(final long duration) {
        _duration = duration;
    }

    /**
     * Returns the ring buffer
     *
     * @return ring buffer
     */
    public final RingBuffer getRing() {
        return _ring;
    }

    /**
     * Defines the ring buffer
     *
     * @param ring buffer to set
     */
    public final void setRing(final RingBuffer ring) {
        _ring = ring;
    }

    /**
     * Return the name of this context
     * @return name of this context
     */
    public final String getName() {
        return _name;
    }
}
