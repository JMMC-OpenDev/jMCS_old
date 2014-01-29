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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Generic Job state (id, state, dates, duration).
 * Sub classes will extend this class to add specific attributes ...
 *
 * @author Laurent BOURGES (voparis).
 */
public final class RootContext extends RunContext implements Iterator<RunContext> {

    /** serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;
    // Members
    /** future used to be able to cancel the job */
    private transient Future<?> future = null;
    /** The user who owns this run (login) */
    private String _owner;
    /** Process working directory */
    private String _workingDir;
    /** Relative path to results of the job in either runner or archive */
    private String _relativePath;
    /** Child contexts (No cascade at all to have unary operation) */
    private final List<RunContext> _childContexts = new ArrayList<RunContext>(2);
    /** Current executed task position in the Child contexts */
    private int _currentTask = 0;

    /**
     * Creates a new RunContext object for JPA
     */
    public RootContext() {
        super();
    }

    /**
     * Creates a new RunContext object
     *
     * @param applicationName application identifier
     * @param id job identifier
     * @param workingDir user's temporary working directory
     */
    public RootContext(final String applicationName, final Long id, final String workingDir) {
        super(null, applicationName, id);
        _workingDir = workingDir;
    }

    /**
     * This method can be used to release resources
     */
    @Override
    public void close() {
        // clean up code :
    }

    /**
     * Simple toString representation : "job[id][state] duration ms."
     *
     * @return "job[id][state] duration ms."
     */
    @Override
    public String toString() {
        return super.toString() + ((getChildContexts() != null)
                ? (CollectionUtils.toString(getChildContexts())) : "");
    }

    /**
     * Return the future associated with this root context
     * @return future associated with this root context
     */
    public Future<?> getFuture() {
        return future;
    }

    /**
     * Define the future associated to the execution of this root context
     * @param pFuture future instance
     */
    public void setFuture(final Future<?> pFuture) {
        future = pFuture;
    }

    public String getOwner() {
        return _owner;
    }

    public void setOwner(final String owner) {
        _owner = owner;
    }

    /**
     * Returns the process working directory
     *
     * @return process working directory
     */
    @Override
    public String getWorkingDir() {
        return _workingDir;
    }

    public String getRelativePath() {
        return _relativePath;
    }

    public void setRelativePath(final String relativePath) {
        _relativePath = relativePath;
    }

    public List<RunContext> getChildContexts() {
        return _childContexts;
    }

    public RunContext getCurrentChildContext() {
        if (_currentTask < _childContexts.size()) {
            return _childContexts.get(_currentTask);
        }
        return null;
    }

    public void addChild(final RunContext childContext) {
        _childContexts.add(childContext);
    }

    @Override
    public boolean hasNext() {
        return _currentTask < _childContexts.size();
    }

    @Override
    public RunContext next() {
        return _childContexts.get(_currentTask);
    }

    public void goNext() {
        _currentTask++;
    }

    @Override
    public void remove() {
        /* no-op */
    }
}
