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

/**
 * Job States
 * 
 * @author Laurent BOURGES (voparis).
 */
public enum RunState {

    /** undefined state */
    STATE_UNKNOWN("UNDEFINED"),
    /** pending state */
    STATE_PENDING("PENDING"),
    /** running state */
    STATE_RUNNING("RUNNING"),
    /** finished state with an error */
    STATE_FINISHED_ERROR("ERROR"),
    /** finished state */
    STATE_FINISHED_OK("OK"),
    /** interrupted state (shutdown) */
    STATE_INTERRUPTED("INTERRUPTED"),
    /** canceled state (user) */
    STATE_CANCELED("CANCELED"),
    /** killed state (user) */
    STATE_KILLED("KILLED");
    /** string representation */
    private final String _value;

    /**
     * Creates a new RunState Enumeration Literal
     *
     * @param v string representation
     */
    RunState(final String v) {
        _value = v;
    }

    /**
     * Return the string representation of this enum constant (value)
     * @return string representation of this enum constant (value)
     */
    public final String value() {
        return _value;
    }

    /**
     * Return the string representation of this enum constant (value)
     * @see #value()
     * @return string representation of this enum constant (value)
     */
    @Override
    public final String toString() {
        return value();
    }

    /**
     * Return the Cardinality enum constant corresponding to the given string representation (value)
     *
     * @param v string representation (value)
     *
     * @return Cardinality enum constant
     *
     * @throws IllegalArgumentException if there is no matching enum constant
     */
    public static RunState fromValue(final String v) {
        for (RunState c : RunState.values()) {
            if (c._value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException("RunState.fromValue : No enum const for the value : " + v);
    }
}
