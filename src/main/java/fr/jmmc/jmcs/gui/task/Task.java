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

/**
 * This class represents a task with identifier, name and child tasks
 * 
 * @author Guillaume MELLA, Laurent BOURGES.
 */
public final class Task {
    /* members */

    /** task name */
    private final String _name;
    /** child tasks */
    private Task[] _childTasks = new Task[0];

    /**
     * Protected constructor
     * @param name task name
     */
    public Task(final String name) {
        _name = name;
    }

    /**
     * Return true only if the given object is a task and task names are equals
     * @param obj other object
     * @return true only if the given object is a task and task names are equals
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Task other = (Task) obj;
        return !((_name == null) ? (other.getName() != null) : !_name.equals(other.getName()));
    }

    /**
     * Return the hash code based on the task name
     * @return hash code based on the task name
     */
    @Override
    public int hashCode() {
        return (_name != null ? _name.hashCode() : 0);
    }

    @Override
    public String toString() {
        return "Task[" + getName() + ']';
    }

    /**
     * Return the task name
     * @return task name
     */
    public String getName() {
        return _name;
    }

    /**
     * Return the array of child tasks (read-only)
     * @return child tasks
     */
    public Task[] getChildTasks() {
        return _childTasks;
    }

    /**
     * Define the array of child tasks.
     * Only visible to this package
     * @param childTasks child tasks
     */
    void setChildTasks(final Task[] childTasks) {
        _childTasks = childTasks;
    }
}
