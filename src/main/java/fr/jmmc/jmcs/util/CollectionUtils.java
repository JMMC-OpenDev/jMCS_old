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
package fr.jmmc.jmcs.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.SystemUtils;

/**
 * Collection toString() methods
 *
 * @author Laurent BOURGES (from org.ivoa).
 */
public final class CollectionUtils {

    /** one line begin separator = { */
    public static final String ONE_LINE_BEGIN_SEPARATOR = "{";
    /** one line end separator = } */
    public static final String ONE_LINE_END_SEPARATOR = "}";
    /** one line separator string */
    public static final String ONE_LINE_VALUE_SEPARATOR = ", ";
    /** Line separator string */
    public final static String LINE_SEPARATOR = SystemUtils.LINE_SEPARATOR;
    /** begin separator = \n{\n */
    public final static String BEGIN_SEPARATOR = LINE_SEPARATOR + ONE_LINE_BEGIN_SEPARATOR + LINE_SEPARATOR;
    /** end separator = \n} */
    public final static String END_SEPARATOR = LINE_SEPARATOR + ONE_LINE_END_SEPARATOR;

    /**
     * Fill the given array with the given value
     * @param <T> Type of array elements
     * @param array array to fill
     * @param value value to use
     * @return filled array
     */
    public static <T> T[] fill(final T[] array, final T value) {
        Arrays.fill(array, value);
        return array;
    }

    /**
     * Converts the given array to a List. <br/>
     * If the array is empty, it gives the Collections.EMPTY_LIST
     *
     * @param <T> type of objects contained in the array
     * @param array array to convert
     * @return list (Collections.EMPTY_LIST if the array is empty)
     * @see java.util.Arrays#asList(Object...)
     */
    public static <T> List<T> asList(final T[] array) {
        List<T> res;
        if (isEmpty(array)) {
            res = Collections.emptyList();
        } else {
            res = Arrays.asList(array);
        }
        return res;
    }

    /**
     * Is the given map null or empty ?
     *
     * @param map map to test
     * @return true if the map is null or empty
     */
    public static boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Is the given collection null or empty ?
     *
     * @param col collection to test
     * @return true if the collection is null or empty
     */
    public static boolean isEmpty(final Collection<?> col) {
        return col == null || col.isEmpty();
    }

    /**
     * Is the given array null or empty ?
     *
     * @param array array to test
     * @return true if the array is null or empty
     */
    public static boolean isEmpty(final Object[] array) {
        return array == null || array.length == 0;
    }

    //~ Constructors -----------------------------------------------------------------------------------------------------
    /**
     * Creates a new CollectionUtils object
     */
    private CollectionUtils() {
        /* no-op */
    }

    //~ Methods ----------------------------------------------------------------------------------------------------------
    /**
     * toString method for a Collection instance Format : <code><br/>
     * {value, ...}
     * </code>
     *
     * @param c collection
     * @return string
     */
    public static String toLine(final Collection<?> c) {
        return (c != null) ? toString(c, ONE_LINE_VALUE_SEPARATOR, ONE_LINE_BEGIN_SEPARATOR, ONE_LINE_END_SEPARATOR) : "";
    }

    /**
     * toString method for a Collection instance Format : <code><br/>
     * {<br/>
     * value<br/>
     * ...<br/>
     * }
     * </code>
     *
     * @param o collection
     * @return string
     */
    public static String toString(final Object[] o) {
        return (o != null) ? toString(java.util.Arrays.asList(o), LINE_SEPARATOR, BEGIN_SEPARATOR, END_SEPARATOR) : "";
    }

    /**
     * toString method for a Collection instance Format : <code><br/>
     * {<br/>
     * value<br/>
     * ...<br/>
     * }
     * </code>
     *
     * @param c collection
     * @return string
     */
    public static String toString(final Collection<?> c) {
        return (c != null) ? toString(c, LINE_SEPARATOR, BEGIN_SEPARATOR, END_SEPARATOR) : "";
    }

    /**
     * toString method for a Collection instance Format : <code><br/>
     * {<br/>
     * value<br/>
     * ...<br/>
     * }
     * </code>
     *
     * @param sb buffer
     * @param c collection
     * @return buffer (sb)
     */
    public static StringBuilder toString(final StringBuilder sb, final Collection<?> c) {
        return toString(sb, c, LINE_SEPARATOR, BEGIN_SEPARATOR, END_SEPARATOR);
    }

    /**
     * toString method for a Map instance Format : <code><br/>
     * {<br/>
     * key = value<br/>
     * ...<br/>
     * }
     * </code>
     *
     * @param m map
     * @return string
     */
    public static String toString(final Map<?, ?> m) {
        return toString(m, LINE_SEPARATOR, BEGIN_SEPARATOR, END_SEPARATOR);
    }

    /**
     * toString method for a Map instance Format : <code><br/>
     * {<br/>
     * key = value<br/>
     * ...<br/>
     * }
     * </code>
     *
     * @param sb buffer
     * @param m map
     * @return buffer (sb)
     */
    public static StringBuilder toString(final StringBuilder sb, final Map<?, ?> m) {
        return toString(sb, m, LINE_SEPARATOR, BEGIN_SEPARATOR, END_SEPARATOR);
    }

    /**
     * toString method for a Collection instance with the given line separator Format : <code>
     * value lineSep ...
     * </code>
     *
     * @param c collection
     * @param lineSep line separator
     * @return string
     */
    public static String toString(final Collection<?> c, final String lineSep) {
        return toString(c, lineSep, "", "");
    }

    /**
     * toString method for a Collection instance with the given line separator Format : <code>
     * value lineSep ...
     * </code>
     *
     * @param sb buffer
     * @param c collection
     * @param lineSep line separator
     * @return buffer (sb)
     */
    public static StringBuilder toString(final StringBuilder sb, final Collection<?> c, final String lineSep) {
        return toString(sb, c, lineSep, "", "");
    }

    /**
     * toString method for a Map instance with the given line separator Format : <code>
     * key = value lineSep ...
     * </code>
     *
     * @param m map
     * @param lineSep line separator
     * @return string
     */
    public static String toString(final Map<?, ?> m, final String lineSep) {
        return toString(m, lineSep, "", "");
    }

    /**
     * toString method for a Map instance with the given line separator Format : <code>
     * key = value lineSep ...
     * </code>
     *
     * @param sb buffer
     * @param m map
     * @param lineSep line separator
     * @return buffer (sb)
     */
    public static StringBuilder toString(final StringBuilder sb, final Map<?, ?> m, final String lineSep) {
        return toString(sb, m, lineSep, "", "");
    }

    /**
     * toString method for a Collection instance with the given start, line and end separators
     *
     * @param c collection
     * @param lineSep line separator
     * @param startSep start separator
     * @param endSep end separator
     * @return string
     */
    public static String toString(final Collection<?> c, final String lineSep, final String startSep,
            final String endSep) {
        final StringBuilder sb = new StringBuilder(255);
        toString(sb, c, lineSep, startSep, endSep);
        return sb.toString();
    }

    /**
     * toString method for a Collection instance with the given start, line and end separators
     *
     * @param sb buffer
     * @param c collection
     * @param lineSep line separator
     * @param startSep start separator
     * @param endSep end separator
     * @return buffer (sb)
     */
    public static StringBuilder toString(final StringBuilder sb, final Collection<?> c, final String lineSep,
            final String startSep, final String endSep) {
        final Iterator<?> it = c.iterator();

        sb.append(startSep);

        for (int i = 0, max = c.size() - 1; i <= max; i++) {
            sb.append(it.next());

            if (i < max) {
                sb.append(lineSep);
            }
        }

        return sb.append(endSep);
    }

    /**
     * toString method for a Map instance with the given start, line and end separators
     *
     * @param m map
     * @param lineSep line separator
     * @param startSep start separator
     * @param endSep end separator
     * @return string
     */
    public static String toString(final Map<?, ?> m, final String lineSep, final String startSep,
            final String endSep) {
        final StringBuilder sb = new StringBuilder(255);
        toString(sb, m, lineSep, startSep, endSep);
        return sb.toString();
    }

    /**
     * toString method for a Map instance with the given start, line and end separators
     *
     * @param sb buffer
     * @param m map
     * @param lineSep line separator
     * @param startSep start separator
     * @param endSep end separator
     * @return buffer (sb)
     */
    @SuppressWarnings("unchecked")
    public static StringBuilder toString(final StringBuilder sb, final Map<?, ?> m, final String lineSep,
            final String startSep, final String endSep) {
        final Iterator it = m.entrySet().iterator();

        sb.append(startSep);

        Map.Entry e;
        Object key;
        Object value;

        for (int i = 0, max = m.size() - 1; i <= max; i++) {
            e = (Map.Entry) it.next();
            key = e.getKey();
            value = e.getValue();
            sb.append(key).append(" = ").append(value);

            if (i < max) {
                sb.append(lineSep);
            }
        }

        return sb.append(endSep);
    }
}
//~ End of file --------------------------------------------------------------------------------------------------------
