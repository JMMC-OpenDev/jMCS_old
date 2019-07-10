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

import edu.stanford.ejalbert.launching.IBrowserLaunching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton object which handles jMCS common preferences.
 * 
 * @author Guillaume MELLA, Laurent BOURGES.
 */
public final class CommonPreferences extends Preferences {

    /** Singleton instance */
    private static CommonPreferences _singleton = null;
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(CommonPreferences.class.getName());
    /* Preferences */
    /** Name of the System property which overrides the UI scale */
    public static final String SYSTEM_UI_SCALE = "jmcs.ui.scale";
    /** Store the filename of the common preference file */
    public static final String PREFERENCES_FILENAME = "fr.jmmc.jmcs.properties";
    /** Name of the preference which stores the user email in the feedback report */
    public static final String FEEDBACK_REPORT_USER_EMAIL = "feedback_report.user_email";
    /** Name of the preference which stores the flag to display or not the splash screen */
    public static final String SHOW_STARTUP_SPLASHSCREEN = "startup.splashscreen.show";
    /** Name of the preference which stores the user selected browser */
    public static final String WEB_BROWSER = "web.browser";
    /** Name of the preference which stores the flag to show the unsupported JDK warning */
    public static final String SHOW_UNSUPPORTED_JDK_WARNING = "showUnsupportedJdkWarning";
    /** Name of the preference which stores the UI scale */
    public static final String UI_SCALE = "ui.scale";
    /** Name of the preference which stores the LAF class name */
    public static final String UI_LAF_CLASSNAME = "ui.laf.class";
    /* Proxy settings */
    /** HTTP proxy host */
    public static final String HTTP_PROXY_HOST = "http.proxyHost";
    /** HTTP proxy port */
    public static final String HTTP_PROXY_PORT = "http.proxyPort";

    /**
     * Return the singleton instance of CommonPreferences.
     * @return the singleton preference instance
     */
    public static CommonPreferences getInstance() {
        // Build new reference if singleton does not already exist
        // or return previous reference
        if (_singleton == null) {
            _logger.debug("CommonPreferences.getInstance()");

            _singleton = new CommonPreferences();
        }

        return _singleton;
    }

    /**
     * Try to save the preferences to file if needed.
     */
    public static void saveToFileIfNeeded() {
        if (_singleton != null) {
            try {
                _singleton.saveToFile();
            } catch (PreferencesException pe) {
                _logger.warn("Could not save common preferences", pe);
            }
        }
    }

    /* members */
    /** resolved System uiScale */
    private Float systemUiScale = null;

    /** Private constructor that must be empty. */
    private CommonPreferences() {
        super();
    }

    /**
     * Define the default properties used to reset default preferences.
     * @throws PreferencesException if any preference value has a unsupported class type
     */
    @Override
    protected void setDefaultPreferences() throws PreferencesException {
        // Display the splash screen during app launch.
        setDefaultPreference(SHOW_STARTUP_SPLASHSCREEN, true);
        setDefaultPreference(FEEDBACK_REPORT_USER_EMAIL, "");
        setDefaultPreference(WEB_BROWSER, IBrowserLaunching.BROWSER_DEFAULT);
        setDefaultPreference(SHOW_UNSUPPORTED_JDK_WARNING, true);
        setDefaultPreference(UI_SCALE, 1.0);
        setDefaultPreference(UI_LAF_CLASSNAME, "");
        setDefaultPreference(HTTP_PROXY_HOST, "");
        setDefaultPreference(HTTP_PROXY_PORT, "");
    }

    /**
     * Return the preference filename.
     * @return preference filename.
     */
    @Override
    protected String getPreferenceFilename() {
        return PREFERENCES_FILENAME;
    }

    /**
     * Return preference version number.
     * @return preference version number.
     */
    @Override
    protected int getPreferencesVersionNumber() {
        return 1;
    }

    public float getUIScale() {
        // handle overriden value via -Djmcs.ui.scale=...
        final Float systemPropUiScale = getSystemUiScale();
        if (!Float.isNaN(systemPropUiScale)) {
            return systemPropUiScale.floatValue();
        }
        return (float) getPreferenceAsDouble(CommonPreferences.UI_SCALE);
    }

    public void setSystemUiScale(Float uiScale) {
        Float value = Float.NaN;
        if (uiScale.floatValue() > 0.0f) {
            value = uiScale;
        }
        _logger.info("UI scale: {}", uiScale);
        this.systemUiScale = value;
    }

    private Float getSystemUiScale() {
        if (this.systemUiScale == null) {
            final String uiScale = System.getProperty(SYSTEM_UI_SCALE);
            Float parsedValue = Float.NaN;
            if (uiScale != null) {
                try {
                    parsedValue = Float.valueOf(uiScale);
                } catch (NumberFormatException nfe) {
                    _logger.error("Invalid float value: '{}'", uiScale);
                }
            }
            setSystemUiScale(parsedValue);
        }
        return this.systemUiScale;
    }

    /**
     * Run this program to generate the common preference file.
     * @param args CLI parameters.
     */
    public static void main(String[] args) {
        CommonPreferences.getInstance();
        saveToFileIfNeeded();
    }
}
