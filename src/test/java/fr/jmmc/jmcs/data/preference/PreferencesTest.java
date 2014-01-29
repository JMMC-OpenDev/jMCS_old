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
package fr.jmmc.jmcs.data.preference;

import fr.jmmc.jmcs.Bootstrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class provides several tests on Preferences abstract class (
 * @author bourgesl
 */
public class PreferencesTest {

    static {

        // invoke App method to initialize logback now:
        Bootstrapper.getState();

    }

    /** Logger - get from given class name */
    private static final Logger _logger = LoggerFactory.getLogger(PreferencesTest.class.getName());
    public final static String PREF_BOOLEAN = "myBoolean";
    public final static boolean VAL_BOOLEAN = true;
    public final static String PREF_INTEGER = "myInteger";
    public final static int VAL_INTEGER = 13;
    public final static String PREF_DOUBLE = "myDouble";
    public final static double VAL_DOUBLE = Math.PI;
    public final static String PREF_STR = "myString";
    public final static String VAL_STR = getValString();
    public final static String PREF_STR_LIST = "myStrList";
    public final static List<String> VAL_STR_LIST = Arrays.asList(new String[]{"jmmc", "", "guillaume", "sylvain", "laurent"});

    public static String getValString() {
        String str = "";
        for (int i = 32; i < 127; i++) {
            str += (char) i;
        }
        return str;
    }

    /**
     * Test of getPreference method, of class Preferences.
     */
    @Test
    public void testGetPreference() throws Exception {
        System.out.println("getPreference");
        final Preferences instance = new PreferencesImpl();

        if (true) {
            Object preferenceName = "Undefined";
            try {
                instance.getPreference(preferenceName);

                fail("MissingPreferenceException expected.");

            } catch (MissingPreferenceException mpe) {
                _logger.info("MissingPreferenceException: expected: ", mpe);
            }
            String expResult = null;
            String result = instance.getPreference(preferenceName, true);
            assertEquals(expResult, result);
        }
        if (true) {
            Object preferenceName = PREF_STR;
            String expResult = VAL_STR;
            String result = instance.getPreference(preferenceName);
            assertEquals(expResult, result);
        }
        if (true) {
            Object preferenceName = PREF_STR;
            String expResult = "laurent";
            instance.setPreference(preferenceName, expResult);
            String result = instance.getPreference(preferenceName);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of getPreferenceAsBoolean method, of class Preferences.
     */
    @Test
    public void testGetPreferenceAsBoolean() throws Exception {
        System.out.println("getPreferenceAsBoolean");
        final Preferences instance = new PreferencesImpl();

        if (true) {
            Object preferenceName = PREF_BOOLEAN;
            boolean expResult = VAL_BOOLEAN;
            boolean result = instance.getPreferenceAsBoolean(preferenceName);
            assertEquals(expResult, result);
        }
        if (true) {
            Object preferenceName = PREF_BOOLEAN;
            boolean expResult = false;
            instance.setPreference(preferenceName, expResult);
            boolean result = instance.getPreferenceAsBoolean(preferenceName);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of getPreferenceAsInt method, of class Preferences.
     */
    @Test
    public void testGetPreferenceAsInt() throws Exception {
        System.out.println("getPreferenceAsInt");
        final Preferences instance = new PreferencesImpl();

        if (true) {
            Object preferenceName = PREF_INTEGER;
            int expResult = VAL_INTEGER;
            int result = instance.getPreferenceAsInt(preferenceName);
            assertEquals(expResult, result, 1e-10d);
        }
        if (true) {
            Object preferenceName = PREF_INTEGER;
            int expResult = 666;
            instance.setPreference(preferenceName, expResult);
            int result = instance.getPreferenceAsInt(preferenceName);
            assertEquals(expResult, result, 1e-10d);
        }
    }

    /**
     * Test of getPreferenceAsDouble method, of class Preferences.
     */
    @Test
    public void testGetPreferenceAsDouble() throws Exception {
        System.out.println("getPreferenceAsDouble");
        final Preferences instance = new PreferencesImpl();

        if (true) {
            Object preferenceName = PREF_DOUBLE;
            double expResult = VAL_DOUBLE;
            double result = instance.getPreferenceAsDouble(preferenceName);
            assertEquals(expResult, result, 1e-10d);
        }
        if (true) {
            Object preferenceName = PREF_DOUBLE;
            double expResult = 123456.789d;
            instance.setPreference(preferenceName, expResult);
            double result = instance.getPreferenceAsDouble(preferenceName);
            assertEquals(expResult, result, 1e-10d);
        }
    }

    /**
     * Test of getPreferenceAsStringList method, of class Preferences.
     */
    @Test
    public void testGetPreferenceAsStringList() throws Exception {
        System.out.println("getPreferenceAsStringList");
        final Preferences instance = new PreferencesImpl();

        if (true) {
            Object preferenceName = PREF_STR_LIST;
            List<String> expResult = VAL_STR_LIST;
            List<String> result = instance.getPreferenceAsStringList(preferenceName);
            assertEquals(expResult, result);
        }
        if (true) {
            Object preferenceName = PREF_STR_LIST;
            List<String> expResult = Arrays.asList(new String[]{"guillaume", "sylvain", "laurent"});
            instance.setPreference(preferenceName, expResult);
            List<String> result = instance.getPreferenceAsStringList(preferenceName);
            assertEquals(expResult, result);
        }

        // test list types:
        if (true) {
            Object preferenceName = PREF_STR_LIST;
            try {
                instance.setPreference(preferenceName, new String[]{"bad", "value"});

                fail("PreferencesException expected.");

            } catch (PreferencesException pe) {
                _logger.info("PreferencesException: expected: ", pe);
            }
        }
        if (true) {
            Object preferenceName = PREF_STR_LIST;
            List<String> expResult = new ArrayList<String>(Arrays.asList(new String[]{"guillaume", "sylvain", "laurent"}));
            instance.setPreference(preferenceName, expResult);
            List<String> result = instance.getPreferenceAsStringList(preferenceName);
            assertEquals(expResult, result);
        }
        if (true) {
            Object preferenceName = PREF_STR_LIST;
            List<String> expResult = new LinkedList<String>(Arrays.asList(new String[]{"guillaume", "sylvain", "laurent"}));
            instance.setPreference(preferenceName, expResult);
            List<String> result = instance.getPreferenceAsStringList(preferenceName);
            assertEquals(expResult, result);
        }

        // test invalid values in list:
        if (true) {
            Object preferenceName = PREF_STR_LIST;
            try {
                instance.setPreference(preferenceName, Arrays.asList(new String[]{"guillaume", null}));

                fail("PreferencesException expected.");

            } catch (PreferencesException pe) {
                _logger.info("PreferencesException: expected: ", pe);
            }
        }
        if (true) {
            Object preferenceName = PREF_STR_LIST;
            try {
                instance.setPreference(preferenceName, Arrays.asList(new String[]{"guillaume", "value|with|separator"}));

                fail("PreferencesException expected.");

            } catch (PreferencesException pe) {
                _logger.info("PreferencesException: expected: ", pe);
            }
        }
        if (true) {
            Object preferenceName = PREF_STR_LIST;
            try {
                instance.setPreference(preferenceName, Arrays.asList(new Object[]{"guillaume", new Object()}));

                fail("PreferencesException expected.");

            } catch (PreferencesException pe) {
                _logger.info("PreferencesException: expected: ", pe);
            }
        }
        if (true) {
            Object preferenceName = PREF_STR_LIST;
            try {
                instance.setPreference(preferenceName, Arrays.asList(new Object[]{"guillaume", 3.14d}));

                fail("PreferencesException expected.");

            } catch (PreferencesException pe) {
                _logger.info("PreferencesException: expected: ", pe);
            }
        }
    }

    private class PreferencesImpl extends Preferences {

        public void setDefaultPreferences() throws PreferencesException {
            setDefaultPreference(PREF_BOOLEAN, VAL_BOOLEAN);
            setDefaultPreference(PREF_INTEGER, VAL_INTEGER);
            setDefaultPreference(PREF_DOUBLE, VAL_DOUBLE);
            setDefaultPreference(PREF_STR, VAL_STR);
            setDefaultPreference(PREF_STR_LIST, VAL_STR_LIST);
        }

        public String getPreferenceFilename() {
            return "fr.jmmc.jmcs.test.properties";
        }

        public int getPreferencesVersionNumber() {
            return 1;
        }
    }
}
