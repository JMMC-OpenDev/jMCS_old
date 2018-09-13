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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Timer factory contains a map[key - Timer] to associate time metrics statistics to several
 * categories of operations
 *
 * @author Laurent BOURGES (voparis).
 */
public final class TimerFactory {

    // Constants
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(TimerFactory.class.getName());
    /** diagnostics flag for warmup sequence */
    public final static boolean WARMUP_DIAGNOSTICS = false;
    /** maximum number of warmup steps */
    public final static int WARMUP_STEPS = 10;
    /** warmup step cycles = 5000 */
    public final static int WARMUP_STEP_CYCLES = 10 * 1000;
    /** maximum number of calibration steps */
    public final static int CALIBRATION_STEPS = 5;
    /** calibration step cycles = 5000 */
    public final static int CALIBRATION_STEP_CYCLES = 20 * 1000;
    /** category for the warm-up to optimize the timer code */
    private final static String CATEGORY_CALIBRATE = "calibration";
    /** initial capacity = 64 */
    private final static int CAPACITY = 32;
    /** threshold for long time = 1s (ms) or 1 milliSeconds (ns) */
    private final static double THRESHOLD = 1 * 1000d;
    /** conversion ratio between nanoseconds and milliseconds */
    private final static double CONVERT_NS_INTO_MS = 1e-6d;
    /** calibration value for milliseconds unit */
    private static double CALIBRATION_MILLI_SECONDS = 0d;
    /** calibration value for nanoseconds unit */
    private static double CALIBRATION_NANO_SECONDS = 0d;
    /* shared state */
    /** guard lock for timer list and map to ensure thread integrity */
    private final static Object _lock = new Object();
    /** List[timer] */
    private static List<AbstractTimer> _timerList = new ArrayList<AbstractTimer>(CAPACITY);
    /** fast Map[key - timer] */
    private static Map<String, AbstractTimer> _timerMap = new HashMap<String, AbstractTimer>(CAPACITY);

    /** timer unit constants */
    public static enum UNIT {

        /** MilliSeconds */
        ms,
        /** NanoSeconds */
        ns
    }

    static {
        final long start = System.nanoTime();

        StatLong stat;

        // Adjust threshold to high values :
        StatLong.defineThreshold(100);

        // warm up loop :
        final StatLong globalStatNs = new StatLong();
        final StatLong globalStatMs = new StatLong();

        for (int i = 0; i < WARMUP_STEPS; i++) {
            // warm up to optimize code (hot spot) :
            stat = calibrateNanoSeconds(WARMUP_STEP_CYCLES);
            globalStatNs.add(stat);

            stat = calibrateMilliSeconds(WARMUP_STEP_CYCLES);
            globalStatMs.add(stat);

            if (WARMUP_DIAGNOSTICS && _logger.isWarnEnabled()) {
                _logger.warn("TimerFactory : warmup [{}] : {}", i, dumpTimers());
            }
            resetTimers();
        }

        if (WARMUP_DIAGNOSTICS && _logger.isWarnEnabled()) {
            _logger.warn("TimerFactory : global nanoseconds  statistics : {}", globalStatNs.toString());
            _logger.warn("TimerFactory : global milliseconds statistics : {}", globalStatMs.toString());
        }

        double delta;
        // calibration loop to get latency :
        for (int i = 0; i < CALIBRATION_STEPS; i++) {
            // nano :
            stat = calibrateNanoSeconds(CALIBRATION_STEP_CYCLES);
            delta = Math.min(stat.getMin(), stat.getAverage() - stat.getStdDevLow());

            CALIBRATION_NANO_SECONDS += delta;

            if (WARMUP_DIAGNOSTICS && _logger.isWarnEnabled()) {
                _logger.warn("TimerFactory : Nanoseconds   : ");
                _logger.warn("TimerFactory : calibration [{}] : {}", i, dumpTimers());
                _logger.warn("TimerFactory : min           : {}", stat.getMin());
                _logger.warn("TimerFactory : avg - stddev  : {}", (stat.getAverage() - stat.getStdDevLow()));
                _logger.warn("TimerFactory : delta         : {}", delta);
                _logger.warn("TimerFactory : nanoseconds  calibration correction : {}", CALIBRATION_NANO_SECONDS);
            }

            resetTimers();

            // milli :
            stat = calibrateMilliSeconds(CALIBRATION_STEP_CYCLES);
            delta = Math.min(stat.getMin(), stat.getAverage() - stat.getStdDevLow());

            CALIBRATION_MILLI_SECONDS += delta;

            if (WARMUP_DIAGNOSTICS && _logger.isWarnEnabled()) {
                _logger.warn("TimerFactory : Milliseconds   : ");
                _logger.warn("TimerFactory : calibration [{}] : {}", i, dumpTimers());
                _logger.warn("TimerFactory : min           : {}", stat.getMin());
                _logger.warn("TimerFactory : avg - stddev  : {}", (stat.getAverage() - stat.getStdDevLow()));
                _logger.warn("TimerFactory : delta         : {}", delta);
                _logger.warn("TimerFactory : milliseconds calibration correction : {}", CALIBRATION_MILLI_SECONDS);
            }

            resetTimers();
        }

        final long stop = System.nanoTime();

        if (_logger.isWarnEnabled()) {
            _logger.warn("TimerFactory : nanoseconds  calibration correction : {}", NumberUtils.trimTo5Digits(CALIBRATION_NANO_SECONDS));
            _logger.warn("TimerFactory : milliseconds calibration correction : {}", NumberUtils.trimTo5Digits(CALIBRATION_MILLI_SECONDS));
            _logger.warn("TimerFactory : calibration time (ms) : {}", TimerFactory.elapsedMilliSeconds(start, stop));
        }

        // Adjust threshold to low values :
        StatLong.defineThreshold(StatLong.DEFAULT_THRESHOLD_AVG);
    }

    /**
     * Warm-up and calibrate timer code (HotSpot)
     *
     * @param cycles empty cycles to operate
     * @return calibration value in double precision
     */
    private static StatLong calibrateNanoSeconds(final int cycles) {
        final String catCalibrate = CATEGORY_CALIBRATE + "-" + UNIT.ns;

        long start;
        // EMPTY LOOP to force HotSpot compiler to optimize the code for Timer.* classes  :
        for (int i = 0, size = cycles; i < size; i++) {
            start = System.nanoTime();
            // ...
            TimerFactory.getSimpleTimer(catCalibrate, UNIT.ns).addNanoSeconds(start, System.nanoTime());
        }

        return TimerFactory.getTimer(catCalibrate).getTimeStatistics();
    }

    /**
     * Warm-up and calibrate timer code (HotSpot)
     *
     * @param cycles empty cycles to operate
     * @return calibration value in double precision
     */
    private static StatLong calibrateMilliSeconds(final int cycles) {
        final String catCalibrate = CATEGORY_CALIBRATE + "-" + UNIT.ms;

        long start;
        // EMPTY LOOP to force HotSpot compiler to optimize the code for Timer.* classes  :
        for (int i = 0, size = cycles; i < size; i++) {
            start = System.nanoTime();
            // ...
            TimerFactory.getSimpleTimer(catCalibrate, UNIT.ms).addMilliSeconds(start, System.nanoTime());
        }

        return TimerFactory.getTimer(catCalibrate).getTimeStatistics();
    }

    /**
     * Forbidden Constructor
     */
    private TimerFactory() {
        /* no-op */
    }

    /**
     * Clean up that class
     */
    public static void onExit() {
        // force GC :
        resetTimers();
        _timerList = null;
        _timerMap = null;
    }

    /**
     * Returns elapsed time between 2 time values get from System.nanoTime() in milliseconds
     *
     * @see System#nanoTime()
     * @param start t0
     * @param now t1
     * @return (t1 - t0) in milliseconds
     */
    public static double elapsedMilliSeconds(final long start, final long now) {
        return NumberUtils.trimTo5Digits(CONVERT_NS_INTO_MS * (now - start) - CALIBRATION_MILLI_SECONDS);
    }

    /**
     * Returns elapsed time between 2 time values get from System.nanoTime() in nanoseconds
     *
     * @see System#nanoTime()
     * @param start t0
     * @param now t1
     * @return (t1 - t0) in nanoseconds
     */
    public static double elapsedNanoSeconds(final long start, final long now) {
        return NumberUtils.trimTo5Digits((now - start) - CALIBRATION_NANO_SECONDS);
    }

    /**
     * Return an existing or a new ThresholdTimer for that category (lazy) with the default threshold
     * and unit (milliseconds)
     *
     * @see #THRESHOLD
     * @param category a string representing the kind of operation
     * @return timer instance
     */
    public static AbstractTimer getTimer(final String category) {
        return getTimer(category, UNIT.ms, THRESHOLD);
    }

    /**
     * Return an existing or a new ThresholdTimer for that category (lazy) with the default threshold
     * and the given unit
     *
     * @see #THRESHOLD
     * @see UNIT
     * @param category a string representing the kind of operation
     * @param unit MILLI_SECONDS or NANO_SECONDS
     * @return timer instance
     */
    public static AbstractTimer getTimer(final String category, final UNIT unit) {
        return getTimer(category, unit, THRESHOLD);
    }

    /**
     * Return an existing or a new Timer for that category (lazy) with the given unit
     *
     * @see UNIT
     * @param category a string representing the kind of operation
     * @param unit MILLI_SECONDS or NANO_SECONDS
     * @return timer instance
     */
    public static AbstractTimer getSimpleTimer(final String category, final UNIT unit) {
        return getTimer(category, unit, 0d);
    }

    /**
     * Return an existing or a new Timer for that category (lazy) with the given threshold
     *
     * @see UNIT
     * @param category a string representing the kind of operation
     * @param th threshold to detect an high value
     * @param unit MILLI_SECONDS or NANO_SECONDS
     * @return timer instance
     */
    public static AbstractTimer getTimer(final String category, final UNIT unit, final double th) {
        AbstractTimer timer = _timerMap.get(category);

        if (timer == null) {
            if (th > 0d) {
                timer = new ThresholdTimer(category, unit, th);
            } else {
                timer = new Timer(category, unit);
            }

            synchronized (_lock) {
                final AbstractTimer old = _timerMap.get(category);
                // memory thread visibility issue :
                if (old == null) {
                    _timerMap.put(category, timer);
                    _timerList.add(timer);
                } else {
                    timer = old;
                }
            }
        }

        return timer;
    }

    /**
     * Return a string representation for all timer instances present in the timerMap map
     *
     * @return string representation for all timer instances
     */
    public static String dumpTimers() {
        String res;

        synchronized (_lock) {
            if (_timerList.isEmpty()) {
                res = "";
            } else {
                final StringBuilder sb = new StringBuilder(1024);
                for (final AbstractTimer timer : _timerList) {
                    sb.append('\n');
                    timer.toString(sb, true);
                }
                res = sb.toString();
            }
        }
        return res;
    }

    /**
     * Reset all timer instances
     */
    public static void resetTimers() {
        synchronized (_lock) {
            _timerMap.clear();
            _timerList.clear();
        }
    }

    /**
     * Return true if there is no existing timer.<br>
     * This method is not thread safe but only useful before dumpTimers()
     *
     * @return true if there is no existing timer
     */
    public static boolean isEmpty() {
        return _timerList.isEmpty();
    }
}
