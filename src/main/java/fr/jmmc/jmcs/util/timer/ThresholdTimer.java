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

import fr.jmmc.jmcs.util.NumberUtils;

/**
 * Special Timer with a threshold to separate low & high values.
 *
 * @author Laurent BOURGES (voparis).
 */
public final class ThresholdTimer extends AbstractTimer {

    /** Timer instance for the low values */
    private final Timer _low;
    /** Timer instance for the high values */
    private final Timer _high;
    /** High-value threshold */
    private final double _threshold;

    /**
     * Protected Constructor for ThresholdTimer objects : use the factory pattern
     *
     * @see TimerFactory.UNIT
     * @see TimerFactory#getTimer(String)
     * @param pCategory a string representing the kind of operation
     * @param pUnit MILLI_SECONDS or NANO_SECONDS
     * @param th threshold to detect an high value
     */
    protected ThresholdTimer(final String pCategory, final TimerFactory.UNIT pUnit, final double th) {
        super(pCategory, pUnit);
        _low = new Timer(pCategory, pUnit);
        _high = new Timer(pCategory, pUnit);
        _threshold = th;
    }

    /**
     * Add a time value given in double precision
     *
     * @param time value to add in statistics
     */
    @Override
    public void add(final double time) {
        if (time > 0d) {
            _usage++;
            if (time > _threshold) {
                _high.add(time);
            } else {
                _low.add(time);
            }
        }
    }

    /**
     * Return the Timer instance for the high values
     *
     * @return Timer instance for the high values
     */
    public Timer getTimerHigh() {
        return _high;
    }

    /**
     * Return the Timer instance for the low values
     *
     * @return Timer instance for the low values
     */
    public Timer getTimerLow() {
        return _low;
    }

    /**
     * Return the time statistics
     *
     * @return time statistics
     */
    @Override
    public StatLong getTimeStatistics() {
        return getTimerHigh().getTimeStatistics();
    }

    /**
     * toString() implementation using string builder
     *
     * Note: to override in child classes to append their fields
     *
     * @param sb string builder to append to
     * @param full true to get complete information; false to get main information (shorter)
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        super.toString(sb, full);

        sb.append("(threshold = ").append(NumberUtils.trimTo5Digits(_threshold)).append(' ').append(getUnit()).append(") {\n  Low  : ");
        _low.toString(sb, full);
        sb.append("\n  High : ");
        _high.toString(sb, full);
        sb.append("\n}");
    }
}
