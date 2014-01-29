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
package fr.jmmc.jmcs.util.runner.process;

import fr.jmmc.jmcs.util.CollectionUtils;
import fr.jmmc.jmcs.util.runner.RootContext;
import fr.jmmc.jmcs.util.runner.RunContext;

/**
 * Unix Process Job (command, buffer, process status, UNIX process wrapper)
 *
 * @author Laurent BOURGES (voparis).
 */
public final class ProcessContext extends RunContext {

    /** serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;
    /** command separator '  ' */
    private static final String DB_SEPARATOR = "  ";
    // Members
    /** Commands [UNIX command, arguments] */
    private String _command;
    /** Process status */
    private int _exitCode = -1;
    /** child UNIX process */
    private transient Process _process = null;

    /**
     * Creates a new ProcessContext object for JPA
     */
    public ProcessContext() {
        super();
    }

    /**
     * Creates a new ProcessContext object
     *
     * @param parent root context
     * @param name operation name
     * @param id job identifier
     * @param cmd command array
     */
    public ProcessContext(final RootContext parent, final String name, final Long id, final String[] cmd) {
        super(parent, name, id);

        _command = CollectionUtils.toString(CollectionUtils.asList(cmd), DB_SEPARATOR, "", "");
    }

    /**
     * this method destroys the child UNIX process
     */
    @Override
    public void kill() {
        // java process is killed => unix process is killed :
        ProcessRunner.kill(this);
    }

    /**
     * Simple toString representation : "job[id][state] duration ms. {command} - dir : workDir"
     *
     * @return "job[id][state] duration ms. {command} - dir : workDir"
     */
    @Override
    public String toString() {
        return super.toString() + " " + getCommand();
    }

    /**
     * Returns the command array
     *
     * @return command array
     */
    public String[] getCommandArray() {
        return _command.split(DB_SEPARATOR);
    }

    /**
     * Returns the command string
     *
     * @return command string
     */
    public String getCommand() {
        return _command;
    }

    /**
     * Returns the exit code or -1 if undefined
     *
     * @return exit code or -1 if undefined
     */
    public int getExitCode() {
        return _exitCode;
    }

    /**
     * Defines the exit code
     *
     * @param code exit code
     */
    void setExitCode(final int code) {
        _exitCode = code;
    }

    /**
     * Returns the UNIX Process
     *
     * @return UNIX Process
     */
    Process getProcess() {
        return _process;
    }

    /**
     * Defines the UNIX Process
     *
     * @param process UNIX Process
     */
    void setProcess(final Process process) {
        _process = process;
    }
}
