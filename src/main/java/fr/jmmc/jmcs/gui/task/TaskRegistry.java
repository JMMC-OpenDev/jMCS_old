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
package fr.jmmc.jmcs.gui.task;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a simple task registry (add / get tasks and define child tasks for a particular task).
 * 
 * @author Guillaume MELLA, Laurent BOURGES.
 */
public class TaskRegistry {

    /** Class _logger */
    protected static final Logger _logger = LoggerFactory.getLogger(TaskRegistry.class.getName());
    /* members */
    /**registered tasks keyed by task name */
    private final Map<String, Task> _registeredTasks = new HashMap<String, Task>();

    /**
     * Protected constructor
     */
    protected TaskRegistry() {
        // no-op
    }

    /**
     * Add the given task in the task registry
     * @param task task to add
     */
    public final void addTask(final Task task) {
        if (_registeredTasks.containsKey(task.getName())) {
            _logger.warn("task already registered : {}", task);
        }
        _registeredTasks.put(task.getName(), task);
    }

    /**
     * Return the task given its name (unique)
     * @param name task name
     * @return task or null if not found
     */
    public final Task getTask(final String name) {
        return _registeredTasks.get(name);
    }

    /**
     * Define the child tasks of the given task
     * @param task task to modify
     * @param childTasks child tasks to set
     */
    public final void setChildTasks(final Task task, final Task[] childTasks) {
        task.setChildTasks(childTasks);
    }
}
