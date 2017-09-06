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

import java.awt.Font;
import org.apache.commons.lang.SystemUtils;

/**
 *
 * @author Laurent BOURGES.
 */
public final class SpecialChars {

    /** greek chars */
    /** delta (upper case) */
    public final static String DELTA_UPPER = "\u0394";
    /** lambda (lower case) */
    public final static String LAMBDA_LOWER = "\u03BB";
    /** mu (lower case) */
    public final static String MU_LOWER = "µ";
    /* symbols */
    /** copyright symbol (c) */
    public final static String SYMBOL_COPYRIGHT = "\u00A9";
    /** information symbol (lower case) */
    public final static String SYMBOL_INFO = (!SystemUtils.IS_OS_WINDOWS && canDisplay('\u2139')) ? "\u2139" : "i";
    /* units */
    /** micron unit (µm) (lower case) */
    public final static String UNIT_MICRO_METER = MU_LOWER + "m";
    /** Mega lambda unit (Ml) (lower case) */
    public final static String UNIT_MEGA_LAMBDA = "M" + LAMBDA_LOWER;

    /**
     * Private constructor (utility class)
     */
    private SpecialChars() {
        // no-op
    }

    /**
     * Test if the default font (SansSerif) can display the given character
     * @param ch character to check
     * @return true if the default font (SansSerif) can display the given character; false otherwise
     */
    private static boolean canDisplay(final char ch) {
        final Font font = new Font("SansSerif", Font.PLAIN, 12);
        return (font.canDisplay(ch));
    }
}
