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
import fr.jmmc.jmcs.util.ToStringable;

/**
 * Utility Class to store statistics : accumulated, average, accumulated delta, stddev.
 *
 * WARNING : Synchronization for coherence must be done OUTSIDE in the calling class !
 *
 * @author Laurent BOURGES (voparis).
 */
public final class StatLong implements ToStringable {

    /** default value for the average threshold */
    public final static int DEFAULT_THRESHOLD_AVG = 5;
    /** average threshold used to start considering that the average value is correct */
    private static int THRESHOLD_AVG;
    /** stddev threshold used to start computing the standard deviation : 2 x THRESHOLD_AVG */
    private static int THRESHOLD_STDDEV;
    /** Fixed Divisor for stddev : THRESHOLD_STDDEV - THRESHOLD_AVG + 1 */
    private static int THRESHOLD_STDDEV_N;

    /**
     * Define default threshold constants
     */
    static {
        defineThreshold(DEFAULT_THRESHOLD_AVG);
    }

    /**
     * Define the occurrence thresholds to compute average, standard deviation ...
     * @param thresholdAverage
     */
    public static void defineThreshold(final int thresholdAverage) {
        if (thresholdAverage > 0) {
            THRESHOLD_AVG = thresholdAverage;
            THRESHOLD_STDDEV = 2 * THRESHOLD_AVG;
            THRESHOLD_STDDEV_N = 1 + THRESHOLD_STDDEV - THRESHOLD_AVG;
        }
    }
    // Members
    /** occurrence counter */
    private int _counter;
    /** accumulator */
    private double _acc;
    /** average */
    private double _average;
    /** minimum value */
    private double _min;
    /** maximum value */
    private double _max;
    /** high occurrence counter */
    private int _counterHigh;
    /** delta accumulator (higher values in compare to the average value) */
    private double _accDeltaHigh;
    /** low occurrence counter */
    private int _counterLow;
    /** delta accumulator (lower values in compare to the average value) */
    private double _accDeltaLow;

    /**
     * Creates a new StatLong object.
     */
    public StatLong() {
        reset();
    }

    /**
     * reset values
     */
    public void reset() {
        _counter = 0;
        _acc = 0d;
        _average = 0d;
        _min = Double.MAX_VALUE;
        _max = Double.MIN_VALUE;
        _counterHigh = 0;
        _accDeltaHigh = 0d;
        _counterLow = 0;
        _accDeltaLow = 0d;
    }

    /**
     * Merge the given statistics in this instance
     *
     * @param stat statistics to add in this instance
     */
    public void add(final StatLong stat) {
        _counter += stat.getCounter();
        _acc += stat.getAccumulator();
        _average = _acc / _counter;
        if (stat.getMin() < _min) {
            _min = stat.getMin();
        }
        if (stat.getMax() > _max) {
            _max = stat.getMax();
        }
        _counterHigh += stat.getCounterHigh();
        _accDeltaHigh += stat.getDeltaAccumulatorHigh();
        _counterLow += stat.getCounterLow();
        _accDeltaLow += stat.getDeltaAccumulatorLow();
    }

    /**
     * Add the given value in statistics
     *
     * @param value integer value to add in statistics
     */
    public void add(final int value) {
        add((double) value);
    }

    /**
     * Add the given value in statistics
     *
     * @param value long value to add in statistics
     */
    public void add(final long value) {
        add((double) value);
    }

    /**
     * Add the given value in statistics
     *
     * @param value double value to add in statistics
     */
    public void add(final double value) {
        if (value < _min) {
            _min = value;
        }

        if (value > _max) {
            _max = value;
        }

        final int count = ++_counter;

        _acc += value;
        _average = _acc / count;

        if (count >= THRESHOLD_AVG) {
            /**
             * X-       =     (1/n) * Sum (Xn)
             * stdDev^2 = (1/(n-1)) * Sum [ (Xn - * X-)^2 ]
             */
            // the standard deviation is estimated with a clipping algorithm :
            final double delta = _average - value;
            if (delta > 0) {
                _counterLow++;
                // Sum of delta square :
                _accDeltaLow += delta * delta;
            } else {
                _counterHigh++;
                // Sum of delta square :
                _accDeltaHigh += delta * delta;
            }
        }
    }

    /**
     * Return the occurrence counter
     *
     * @return occurrence counter
     */
    public int getCounter() {
        return _counter;
    }

    /**
     * Return the accumulator value
     *
     * @return accumulator value
     */
    public double getAccumulator() {
        return _acc;
    }

    /**
     * Return the high occurrence counter
     *
     * @return high occurrence counter
     */
    public int getCounterHigh() {
        return _counterHigh;
    }

    /**
     * Return the delta accumulator value
     *
     * @return delta accumulator value
     */
    public double getDeltaAccumulatorHigh() {
        return _accDeltaHigh;
    }

    /**
     * Return the low occurrence counter
     *
     * @return low occurrence counter
     */
    public int getCounterLow() {
        return _counterLow;
    }

    /**
     * Return the delta accumulator value
     *
     * @return delta accumulator value
     */
    public double getDeltaAccumulatorLow() {
        return _accDeltaLow;
    }

    /**
     * Return the average value
     *
     * @return average value
     */
    public double getAverage() {
        return _average;
    }

    /**
     * Return the minimum value
     *
     * @return minimum value
     */
    public double getMin() {
        return _min;
    }

    /**
     * Return the maximum value
     *
     * @return maximum value
     */
    public double getMax() {
        return _max;
    }

    /**
     * Return the standard deviation (estimated)
     *
     * @return standard deviation
     */
    public double getStdDev() {
        double stddev = 0d;
        if (_counter >= THRESHOLD_STDDEV) {
            stddev = Math.sqrt((_accDeltaHigh + _accDeltaLow) / (_counter - THRESHOLD_STDDEV_N));
        }

        return stddev;
    }

    /**
     * Return the standard deviation (estimated)
     *
     * @return standard deviation
     */
    public double getStdDevHigh() {
        double stddev = 0d;
        if (_counterHigh >= THRESHOLD_STDDEV) {
            stddev = Math.sqrt(_accDeltaHigh / (_counterHigh - THRESHOLD_STDDEV_N));
        }

        return stddev;
    }

    /**
     * Return the standard deviation (estimated)
     *
     * @return standard deviation
     */
    public double getStdDevLow() {
        double stddev = 0d;
        if (_counterLow >= THRESHOLD_STDDEV) {
            stddev = Math.sqrt(_accDeltaLow / (_counterLow - THRESHOLD_STDDEV_N));
        }

        return stddev;
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
        sb.append("{num = ").append(_counter);
        sb.append(" :\tmin = ").append(NumberUtils.trimTo5Digits(_min));
        sb.append(",\tavg = ").append(NumberUtils.trimTo5Digits(_average));
        sb.append(",\tmax = ").append(NumberUtils.trimTo5Digits(_max));

        if (full) {
            sb.append(",\tacc = ").append(NumberUtils.trimTo5Digits(_acc));

            double v = getStdDev();
            if (v != 0d) {
                sb.append(",\tstd = ").append(NumberUtils.trimTo5Digits(v));
            }
            v = getStdDevLow();
            if (v != 0d) {
                sb.append(",\tstd low = ").append(NumberUtils.trimTo5Digits(v));
                sb.append(" [").append(_counterLow).append(']');
            }
            v = getStdDevHigh();
            if (v != 0d) {
                sb.append(",\tstd high = ").append(NumberUtils.trimTo5Digits(v));
                sb.append(" [").append(_counterHigh).append(']');
            }
        }
        sb.append('}');
    }
}
