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
package fr.jmmc.jmcs.util.timer;

/**
 * This class defines an Abstract Timer Object to have statistics on time metrics
 *
 * @author Laurent BOURGES (voparis).
 */
public abstract class AbstractTimer {
    // Members

    /** category */
    private final String _category;
    /** unit */
    private final TimerFactory.UNIT _unit;
    /** usage counter */
    protected int _usage = 0;

    /**
     * Protected Constructor for AbstractTimer objects : use the factory pattern
     *
     * @see TimerFactory.UNIT
     * @see TimerFactory#getTimer(String)
     * @param pCategory a string representing the kind of operation
     * @param pUnit MILLI_SECONDS or NANO_SECONDS
     */
    protected AbstractTimer(final String pCategory, final TimerFactory.UNIT pUnit) {
        _category = pCategory;
        _unit = pUnit;
    }

    // ~ Methods
    // ----------------------------------------------------------------------------------------------------------
    /**
     * Add a time measure in milliseconds
     *
     * @param start t0
     * @param now t1
     * @see TimerFactory#elapsedMilliSeconds(long, long)
     */
    public final void addMilliSeconds(final long start, final long now) {
        add(TimerFactory.elapsedMilliSeconds(start, now));
    }

    /**
     * Add a time measure in nanoseconds
     *
     * @param start t0
     * @param now t1
     * @see TimerFactory#elapsedNanoSeconds(long, long)
     */
    public final void addNanoSeconds(final long start, final long now) {
        add(TimerFactory.elapsedNanoSeconds(start, now));
    }

    /**
     * Add a time value given in double precision
     *
     * @param time value to add in statistics
     */
    public abstract void add(final double time);

    /**
     * Return the category
     *
     * @return category
     */
    public final String getCategory() {
        return _category;
    }

    /**
     * Return the unit
     *
     * @return usage counter
     */
    public final TimerFactory.UNIT getUnit() {
        return _unit;
    }

    /**
     * Return the usage counter
     *
     * @return usage counter
     */
    public final int getUsage() {
        return _usage;
    }

    /**
     * Return the time statistics
     *
     * @return time statistics
     */
    public abstract StatLong getTimeStatistics();

    /**
     * Return a string representation like "Timer (#unit) [#n]"
     *
     * @return string representation
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder(128);
        toString(sb, true);
        return sb.toString();
    }

    /**
     * toString() implementation using string builder
     *
     * Note: to override in child classes to append their fields
     *
     * @param sb string builder to append to
     * @param full true to get complete information; false to get main information (shorter)
     */
    public void toString(final StringBuilder sb, final boolean full) {
        sb.append("Timer [").append(_category).append(" - ").append(_unit).append("] [").append(_usage).append("]\t");
    }
}
