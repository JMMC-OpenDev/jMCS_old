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

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.ColorEncoder;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.jmcs.util.StringUtils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Observable;
import java.util.Properties;
import javax.swing.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the mother class to manage preferences (a.k.a user defaults).
 * 
 * A singleton of yours SHALL derive from it, and implements all abstract methods.
 * Then use the observer/observable pattern to get notifications on value changes.
 *
 * Preferences are stored in key-value pairs. Keys are actually Object instances,
 * but their toString() representation is always used internally to identify each
 * value. SO WE STRONGLY ADVISE YOU TO USE RAW STRINGS AS IDENTIFIERS.
 * We used this trick in order to handle use of enumeration as key repository so
 * you don't have to explicitely call toSting() each time, while still handling
 * direct String key use.
 *
 * If your singleton is instantiated in App.initServices(), you can use the following actions
 * to save preferences to file or restore preferences to their default values:
 * &lt;menu label="Preferences"&gt;
 *  &lt;menu label="Save to file" classpath="fr.jmmc.jmcs.data.preference.Preferences" action="savePreferences"/&gt;
 *  &lt;menu label="Set default values" classpath="fr.jmmc.jmcs.data.preference.Preferences" action="restorePreferences"/&gt;
 * &lt;/menu&gt;
 * 
 * @author Sylvain LAFRASSE, Guillaume MELLA, Laurent BOURGES.
 */
public abstract class Preferences extends Observable {

    /** Logger - get from given class name */
    private static final Logger _logger = LoggerFactory.getLogger(Preferences.class.getName());
    /** separator used to encode list<String> preference values */
    private static final char LIST_SEPARATOR = '|';
    /** RegExp expression used to decode list<String> preference values */
    private static final String LIST_SPLITTER = "\\|";
    /* jMCS private stuff */
    /** Hidden internal preferences prefix managed by jMCS. */
    private static final String JMCS_PRIVATE_PREFIX = "JMCS_PRIVATE.";
    /** Store hidden preference structure version number name. */
    private static final String JMCS_STRUCTURE_VERSION_NUMBER_ID = JMCS_PRIVATE_PREFIX + "structure.version";

    /* jMCS public stuff */
    /** Public handled preferences prefix managed by jMCS. */
    private static final String JMCS_PUBLIC_PREFIX = "JMCS_PUBLIC.";
    /** Store hidden preference version number name. */
    private static final String PREFERENCES_VERSION_NUMBER_ID = JMCS_PUBLIC_PREFIX + "preference.version";
    /** Store hidden properties index prefix. */
    private static final String PREFERENCES_ORDER_INDEX_PREFIX = JMCS_PUBLIC_PREFIX + "order.index.";
    /** Store dimension width index prefix. */
    private static final String DIMENSION_WIDTH_PREFIX = JMCS_PUBLIC_PREFIX + "dimension.width.";
    /** Store dimension height index prefix. */
    private static final String DIMENSION_HEIGHT_PREFIX = JMCS_PUBLIC_PREFIX + "dimension.height.";

    /* members */
    /** Store preference filename. */
    private String _fullFilepath = null;
    /** Internal storage of preferences. */
    private Properties _currentProperties = new Properties();
    /** Default property. */
    private final Properties _defaultProperties = new Properties();
    /** Save to file action */
    protected final Action _savePreferences;
    /** Restore preferences that get one default value */
    protected final Action _restoreDefaultPreferences;
    /** Flag to enable/disable observer notifications */
    private boolean _notify;

    /**
     * Creates a new Preferences object.
     *
     * This will set default preferences values (by invoking user overridden
     * setDefaultPreferences()), then try to load the preference file, if any.
     */
    public Preferences() {
        this(true);
    }

    /**
     * Creates a new Preferences object.
     *
     * This will set default preferences values (by invoking user overridden
     * setDefaultPreferences()), then try to load the preference file, if any.
     *
     * @param notify flag to enable/disable observer notifications
     */
    public Preferences(final boolean notify) {
        setNotify(notify);

        computePreferenceFilepath();

        try {
            setDefaultPreferences();
        } catch (PreferencesException pe) {
            _logger.warn("Preference initialization FAILED.", pe);
        }

        loadFromFile();

        // Parent class name must be given to register one action per inherited Preference class
        _savePreferences = new SavePrefAction(this.getClass().getName());
        _restoreDefaultPreferences = new RestoreDefaultPrefAction(this.getClass().getName());
    }

    /**
     * Use this method to define all your default values for each of your preferences,
     * by calling any method of the 'setDefaultPreference()' family.
     *
     * @warning Classes that inherits from Preferences MUST overload this method
     * to set default preferences.
     *
     * @throws PreferencesException if any preference value has a unsupported class type
     */
    protected abstract void setDefaultPreferences() throws PreferencesException;

    /**
     * Use this method to define your preference file name.
     *
     * Should return the filename to be used for preference load and save. This
     * name should be in the form "com.company.application.properties".
     *
     * @warning Please note that the complete to the preference file is different
     * among the different desktop OS. This handles this for you !
     *
     * @return the preference filename, without any file separator.
     */
    protected abstract String getPreferenceFilename();

    /**
     * Each preference files shall have a version number in it, in order to let you
     * update it when you fix bug or simply change your mind on something stored.
     * You shall use this method to return your most up-to-date version number.
     *
     * The version number (a POSITIVE INTEGER GREATER THAN ZERO) returned must
     * be incremented BY YOU each time you change your preference structure.
     *
     * updatePreferencesVersion() execution will then be automatically triggered
     * in case an out-of-date preference file is loaded. You can then perform
     * corresponding updates to offer backward compatibility of your users preferences.
     *
     * @warning Classes that inherits from Preferences class MUST overload this
     * method to return specific file name.
     *
     * @return the most up-to-date preference file version number.
     */
    protected abstract int getPreferencesVersionNumber();

    /**
     * Hook to handle updates of older preference file version.
     *
     * This method is automatically triggered when the preference file loaded is
     * bound to a previous version of your Preference-derived object. Thus, you
     * have a chance to load previous values and update them if needed.
     *
     * @warning This method SHOULD be overridden in order to process older files.
     * Otherwise, default values will be loaded instead, potentially overwriting
     * previous users preferences !
     *
     * @warning This method MUST perform one update revision jump at a time. It
     * will be called as many time as needed to reach the latest revision state
     * (i.e, if loaded revision is 2 and current revision is 5, this method will
     * be called three times: once to update from rev. 2 to rev. 3; once to update
     * from rev. 3 to rev. 4; and once to update from rev. 4 to rev. 5).
     *
     * @param loadedVersionNumber the out-of-date version of the loaded preference file.
     *
     * @return should return true if the update went fine and new values should
     * be saved, false otherwise to automatically trigger default values load.
     */
    protected boolean updatePreferencesVersion(int loadedVersionNumber) {
        // By default, triggers default values load.
        return false;
    }

    /**
     * @sa getPreferencesVersionNumber() - same mechanism for INTERNAL preference structure updates.
     * 
     * @return the most up-to-date INTERNAL preference file version number.
     */
    private int getJmcsStructureVersionNumber() {
        return 3;
    }

    /**
     * @sa updatePreferencesVersion() - same mechanism for INTERNAL preference structure updates.
     * 
     * @param loadedVersionNumber the out-of-date INTERNAL version of the loaded preference file.
     *
     * @return should return true if the update went fine and new values should
     * be saved, false otherwise to automatically trigger default values load.
     */
    private boolean updateJmcsPreferencesVersion(int loadedVersionNumber) {
        switch (loadedVersionNumber) {

            // Add missing jMCS preference version number
            case 0:
                return updateJmcsVersionFrom0To1();

            // Add 'JMCS_PUBLIC' prefix to 'preference.version' key
            case 1:
                return updateJmcsVersionFrom1To2();

            // Remove all deprecated order indexes starting with 'MCSPropertyIndexes'
            case 2:
                return updateJmcsVersionFrom2To3();

            default:
                _logger.warn("Could not update JMCS preferences from version '{}'.", loadedVersionNumber);
                break;
        }

        // By default, triggers default values load.
        return false;
    }

    /**
     * INTERNAL Correction : Add missing jMCS preference version number.
     *
     * @return true if all went fine and should write to file, false otherwise.
     */
    private boolean updateJmcsVersionFrom0To1() {
        // Add missing jMCS structure version number
        try {
            setPreference(JMCS_STRUCTURE_VERSION_NUMBER_ID, 1);
        } catch (PreferencesException pe) {
            _logger.warn("Could not store preference '{}': ", JMCS_STRUCTURE_VERSION_NUMBER_ID, pe);
            return false;
        }

        // Commit change to file
        return true;
    }

    /**
     * INTERNAL Correction : Add 'JMCS_PUBLIC' prefix to 'preference.version' key.
     *
     * @return true if all went fine and should write to file, false otherwise.
     */
    private boolean updateJmcsVersionFrom1To2() {

        // Rename preference version name
        final String PREVIOUS_PREFERENCES_VERSION_NUMBER_ID = "preferences.version";
        return renamePreference(PREVIOUS_PREFERENCES_VERSION_NUMBER_ID, PREFERENCES_VERSION_NUMBER_ID);
    }

    /**
     * INTERNAL Correction : Remove all deprecated order indexes starting with 'MCSPropertyIndexes'.
     *
     * @return true if all went fine and should write to file, false otherwise.
     */
    private boolean updateJmcsVersionFrom2To3() {

        // For each ordered preference indices
        final String PREVIOUS_PREFERENCES_ORDER_INDEX_PREFIX = "MCSPropertyIndexes.";

        final Enumeration<String> indexes = getPreferences(PREVIOUS_PREFERENCES_ORDER_INDEX_PREFIX);
        while (indexes.hasMoreElements()) {
            // Get previous index value
            String oldPreferenceName = indexes.nextElement();
            // Delete previous preference
            removePreference(oldPreferenceName);
        }

        // Commit change to file
        return true;
    }

    /**
     * Loop over any required step to become up-to-date.
     */
    private void handlePreferenceUpdates() {
        boolean tryUpdate = false;
        boolean everythingWentFine = true;

        final String[] versionNumberKeys = {JMCS_STRUCTURE_VERSION_NUMBER_ID, PREFERENCES_VERSION_NUMBER_ID};

        for (String key : versionNumberKeys) {

            // Store whether we are in the process of updating internal preference file structure or not
            final boolean structuralUpdate = key.equals(JMCS_STRUCTURE_VERSION_NUMBER_ID);

            String logToken = " ";
            if (structuralUpdate) {
                logToken = " JMCS ";
            }

            // Get current preference file version number
            final int runtimeVersionNumber;
            if (structuralUpdate) {
                runtimeVersionNumber = getJmcsStructureVersionNumber();
            } else {
                runtimeVersionNumber = getPreferencesVersionNumber();
            }

            _logger.trace("Internal{}preference version number = '{}'.", logToken, runtimeVersionNumber);

            // Getting loaded preference file version number
            int fileVersionNumber = 0; // To be sure to be below most preferencesVersionNumber, as Java does not provide unsigned types to garanty positive values from getPreferencesVersionNumber() !!!
            try {
                fileVersionNumber = getPreferenceAsInt(key);

                _logger.trace("Loaded{}preference version number = '{}'.", logToken, fileVersionNumber);

            } catch (NumberFormatException nfe) {
                _logger.warn("Cannot get{}loaded preference version number.", logToken);
            } catch (MissingPreferenceException mpe) {
                _logger.debug("Missing {}loaded preference version number.", logToken, mpe);
            }

            if (fileVersionNumber < runtimeVersionNumber) {
                // If the preference file version is older than the current default version
                _logger.warn("Loaded an 'out-of-date'{}preference version, will try to update preference file.", logToken);

                tryUpdate = true;

                // Handle version differences
                int currentPreferenceVersion = fileVersionNumber;
                while (everythingWentFine && (currentPreferenceVersion < runtimeVersionNumber)) {

                    _logger.debug("Trying to update{}loaded preferences from revision '{}'.", logToken, currentPreferenceVersion);

                    if (structuralUpdate) {
                        everythingWentFine = updateJmcsPreferencesVersion(currentPreferenceVersion);
                    } else {
                        everythingWentFine = updatePreferencesVersion(currentPreferenceVersion);
                    }

                    currentPreferenceVersion++;
                }

            } else if (fileVersionNumber > runtimeVersionNumber) {
                // If the preference file version is newer than the current default version
                _logger.warn("Loaded a 'POSTERIOR to current version'{}preference version, falling back to default values instead.", logToken);

                // Use current default values instead
                resetToDefaultPreferences();
                return;
            } else {
                _logger.info("Preference file {}is up-to-date.", (structuralUpdate) ? "structure " : "");
            }
        }

        // If all updates went fine
        if (everythingWentFine) {
            if (tryUpdate) {
                // Save updated values to file
                try {
                    saveToFile();
                } catch (PreferencesException pe) {
                    _logger.warn("Cannot save preference to disk: ", pe);
                }
            }
        } else {
            // If any update went wrong (or was not handled)
            // Use default values instead
            resetToDefaultPreferences();
        }
    }

    /**
     * Load preferences from file if any, or reset to default values and
     * notify listeners.
     *
     * @warning Any preference value change not yet saved will be LOST.
     */
    final public void loadFromFile() {
        resetToDefaultPreferences(true);

        // Loading preference file
        _logger.info("Loading '{}' preference file.", _fullFilepath);

        InputStream inputFile = null;
        try {
            inputFile = new BufferedInputStream(new FileInputStream(_fullFilepath));
        } catch (FileNotFoundException fnfe) {
            _logger.warn("Cannot load '{}' preference file: ", _fullFilepath, fnfe.getMessage());
        }

        if (inputFile != null) {
            boolean ok = false;
            try {
                _currentProperties.loadFromXML(inputFile);
                ok = true;

            } catch (InvalidPropertiesFormatException ipfe) {
                _logger.error("Cannot parse '{}' preference file: ", _fullFilepath, ipfe);
            } catch (IOException ioe) {
                _logger.warn("Cannot load '{}' preference file: ", _fullFilepath, ioe);
            } finally {
                FileUtils.closeStream(inputFile);
            }

            if (ok) {
                handlePreferenceUpdates();
            } else {
                // Do nothing just default values will be into the preferences.
                _logger.info("Failed loading preference file, so fall back to default values instead.");

                resetToDefaultPreferences();
            }
        }

        // Notify all preferences listener of maybe new values coming from file.
        triggerObserversNotification();
    }

    /**
     * Save the preferences state in memory (i.e for the current session) to file.
     *
     * @throws PreferencesException if the preference file could not be written.
     */
    final public void saveToFile() throws PreferencesException {
        saveToFile(null);
    }

    /**
     * Save the preferences state in memory (i.e for the current session) to file.
     *
     * @param comment comment to be included in the preference file.
     *
     * @throws PreferencesException if the preference file could not be written.
     */
    final public void saveToFile(final String comment) throws PreferencesException {
        // Store current Preference object revision number
        setPreference(JMCS_STRUCTURE_VERSION_NUMBER_ID, getJmcsStructureVersionNumber());
        setPreference(PREFERENCES_VERSION_NUMBER_ID, getPreferencesVersionNumber());

        OutputStream outputFile = null;
        try {
            outputFile = new BufferedOutputStream(new FileOutputStream(_fullFilepath));

            _logger.info("Saving '{}' preference file.", _fullFilepath);

            _currentProperties.storeToXML(outputFile, comment);

        } catch (IOException ioe) {
            throw new PreferencesException("Cannot store preferences to file " + _fullFilepath, ioe);
        } finally {
            FileUtils.closeStream(outputFile);
        }
    }

    /**
     * Restore default values to preferences and notify listeners.
     */
    final public void resetToDefaultPreferences() {
        resetToDefaultPreferences(false);
    }

    /**
     * Restore default values to preferences and notify listeners.
     * @param quiet display one info message log if true (debugging purpose).
     */
    final public void resetToDefaultPreferences(final boolean quiet) {
        _currentProperties = (Properties) _defaultProperties.clone();

        if (!quiet) {
            _logger.info("Restoring default preferences.");
        }

        // Notify all preferences listener.
        triggerObserversNotification();
    }

    /**
     * Set a preference value.
     * The listeners are notified only for preference changes (multiple calls with
     * the same value does NOT trigger notifications).
     *
     * @param preferenceName the preference name.
     * @param preferenceValue the preference value.
     *
     * @throws PreferencesException if any preference value has a unsupported class type.
     */
    final public void setPreference(Object preferenceName, Object preferenceValue) throws PreferencesException {
        setPreferenceToProperties(_currentProperties, preferenceName, preferenceValue);
    }

    /**
     * Set a default preference value.
     * The listeners are notified only for preference changes (multiple calls with
     * the same value does NOT trigger notifications).
     *
     * @param preferenceName the preference name.
     * @param preferenceValue the preference value.
     *
     * @throws PreferencesException if any preference value has a unsupported class type.
     */
    final public void setDefaultPreference(Object preferenceName, Object preferenceValue) throws PreferencesException {
        setPreferenceToProperties(_defaultProperties, preferenceName, preferenceValue);
    }

    /**
     * Set a preference in the given properties set.
     *
     * @param properties the properties set to modify.
     * @param preferenceName the preference name.
     * @param preferenceValue the preference value.
     *
     * @throws PreferencesException if any preference value has a unsupported class type.
     */
    private void setPreferenceToProperties(Properties properties, Object preferenceName, Object preferenceValue)
            throws PreferencesException {

        // Will automatically get -1 for a yet undefined preference
        int order = getPreferenceOrder(preferenceName);

        setPreferenceToProperties(properties, preferenceName, order, preferenceValue);
    }

    /**
     * Set a preference in the given properties set.
     *
     * @param properties the properties set to modify.
     * @param preferenceName the preference name.
     * @param preferenceIndex the order number for the property (-1 for no order).
     * @param preferenceValue the preference value.
     *
     * @throws PreferencesException if any preference value has a unsupported class type.
     */
    private void setPreferenceToProperties(Properties properties, Object preferenceName, int preferenceIndex, Object preferenceValue)
            throws PreferencesException {

        if (preferenceValue == null) {
            throw new PreferencesException("Cannot handle 'null' value for preference [" + preferenceName + "].");
        }
        final String preferenceNameString = preferenceName.toString();

        String currentValue = properties.getProperty(preferenceNameString);
        if (currentValue != null && currentValue.equals(preferenceValue.toString())) {
            // nothing to do
            _logger.debug("Preference '{}' not changed", preferenceName);
            return;
        }

        final Class<?> preferenceClass = preferenceValue.getClass();
        // If the constraint is a String object
        if (preferenceClass == String.class) {
            properties.setProperty(preferenceNameString, (String) preferenceValue);
        } // Else if the constraint is a Boolean object
        else if (preferenceClass == Boolean.class) {
            properties.setProperty(preferenceNameString, ((Boolean) preferenceValue).toString());
        } // Else if the constraint is an Integer object
        else if (preferenceClass == Integer.class) {
            properties.setProperty(preferenceNameString, ((Integer) preferenceValue).toString());
        } // Else if the constraint is a Double object
        else if (preferenceClass == Double.class) {
            properties.setProperty(preferenceNameString, ((Double) preferenceValue).toString());
        } // Else if the constraint is a Color object
        else if (preferenceClass == Color.class) {
            properties.setProperty(preferenceNameString, ColorEncoder.encode((Color) preferenceValue));
        } // Else if the constraint is a Dimension object
        else if (preferenceClass == Dimension.class) {
            final Dimension dimension = (Dimension) preferenceValue;
            final Double width = dimension.getWidth();
            final Double height = dimension.getHeight();
            setPreference(DIMENSION_WIDTH_PREFIX + preferenceNameString, width);
            setPreference(DIMENSION_HEIGHT_PREFIX + preferenceNameString, height);
        } // Else if the constraint is a List<String> object
        else if (List.class.isAssignableFrom(preferenceClass)) {
            @SuppressWarnings("unchecked")
            final List<?> list = (List<?>) preferenceValue;

            Object value;
            String str;
            final StringBuilder sb = new StringBuilder(256);
            for (int i = 0, len = list.size() - 1; i <= len; i++) {
                value = list.get(i);
                // reject null values
                if (value == null) {
                    throw new PreferencesException("Invalid null element in list preference [" + preferenceName + "].");
                }

                // reject not string values:
                if (value instanceof String) {
                    str = (String) value;
                    // reject values containing the list separator:
                    if (str.indexOf(LIST_SEPARATOR) != -1) {
                        throw new PreferencesException("Invalid element '" + str + "' containing reserved separator character '"
                                + LIST_SEPARATOR + "' in list preference [" + preferenceName + "].");
                    }

                    sb.append(str);
                } else {
                    throw new PreferencesException("Invalid element type '" + value.getClass().getName() + "' in list preference [" + preferenceName + "].");
                }
                if (i < len) {
                    sb.append(LIST_SEPARATOR);
                }
            }
            final String prefStringValue = sb.toString();

            _logger.debug("{} = {}", preferenceNameString, prefStringValue);

            properties.setProperty(preferenceNameString, prefStringValue);

        } // Otherwise we don't know how to handle the given object type
        else {
            throw new PreferencesException("Cannot handle the given '" + preferenceClass + "' preference value.");
        }

        // Add property index for order if needed
        setPreferenceOrderToProperties(properties, preferenceName, preferenceIndex);

        // Notify all preferences listener.
        triggerObserversNotification();
    }

    /**
     * Set an ordered preference value.
     *
     * @param preferenceName the preference name.
     * @param preferenceIndex the order number for the property (-1 for no order).
     * @param preferenceValue the preference value.
     *
     * @throws PreferencesException if any preference value has a unsupported class type.
     */
    final public void setPreference(Object preferenceName, int preferenceIndex, Object preferenceValue) throws PreferencesException {
        setPreferenceToProperties(_currentProperties, preferenceName, preferenceIndex, preferenceValue);
    }

    /**
     * Set a preference default value.
     *
     * @param preferenceName the preference name.
     * @param preferenceIndex the order number for the property (-1 for no order).
     * @param preferenceValue the preference value.
     *
     * @throws PreferencesException if any preference value has a unsupported class type
     */
    final public void setDefaultPreference(String preferenceName, int preferenceIndex, Object preferenceValue) throws PreferencesException {
        setPreferenceToProperties(_defaultProperties, preferenceName,
                preferenceIndex, preferenceValue);
    }

    /**
     * Set a preference order.
     *
     * @param preferenceName the preference name.
     * @param preferenceIndex the order number for the property (-1 for no order).
     */
    final public void setPreferenceOrder(String preferenceName, int preferenceIndex) {

        setPreferenceOrderToProperties(_currentProperties, preferenceName, preferenceIndex);

        // Notify all preferences listener.
        triggerObserversNotification();
    }

    /**
     * Set a preference order in the given properties set.
     *
     * @param properties the properties set to modify.
     * @param preferenceName the preference name.
     * @param preferenceIndex the order number for the property (-1 for no order).
     */
    private void setPreferenceOrderToProperties(Properties properties, Object preferenceName, int preferenceIndex) {
        // Add property index for order if needed
        if (preferenceIndex > -1) {
            properties.setProperty(PREFERENCES_ORDER_INDEX_PREFIX + preferenceName.toString(), Integer.toString(preferenceIndex));
        } else {
            properties.setProperty(PREFERENCES_ORDER_INDEX_PREFIX + preferenceName.toString(), Integer.toString(-1));
        }
    }

    /**
     * Get a preference order.
     *
     * @param preferenceName the preference name.
     *
     * @return the order number for the property (-1 for no order).
     */
    final public int getPreferenceOrder(Object preferenceName) {
        // -1 is the flag value for no order found, so it is the default value.
        int result = -1;

        // If the asked order is NOT about an internal MCS index property
        if (!preferenceName.toString().startsWith(PREFERENCES_ORDER_INDEX_PREFIX)) {
            // Get the corresponding order as a String
            String orderString = _currentProperties.getProperty(PREFERENCES_ORDER_INDEX_PREFIX + preferenceName);

            // If an order token was found
            if (orderString != null) {
                // Convert the String in an int
                Integer orderInteger = NumberUtils.valueOf(orderString);
                result = orderInteger.intValue();
            }

            // Otherwise the default -1 value will be returned
        }

        return result;
    }

    /**
     * Get a preference value.
     *
     * @param preferenceName the preference name.
     *
     * @return the preference value.
     * @throws MissingPreferenceException if the preference value is missing.
     */
    final public String getPreference(final Object preferenceName) throws MissingPreferenceException {
        return getPreference(preferenceName, false);
    }

    /**
     * Get a preference value.
     *
     * @param preferenceName the preference name.
     * @param ignoreMissing true to return null when the property is missing.
     *
     * @return the preference value or null if the preference value is missing and the ignoreMissing argument is true.
     * @throws MissingPreferenceException if the preference value is missing and the ignoreMissing argument is false.
     */
    final public String getPreference(final Object preferenceName, final boolean ignoreMissing) throws MissingPreferenceException {
        final String value = _currentProperties.getProperty(preferenceName.toString());
        if ((value == null) && (!ignoreMissing)) {
            throw new MissingPreferenceException("Could not find '" + preferenceName + "' preference value.");
        }

        return value;
    }

    /**
     * Get a boolean preference value.
     *
     * @param preferenceName the preference name.
     *
     * @return one boolean representing the preference value.
     * @throws MissingPreferenceException if the preference value is missing.
     */
    final public boolean getPreferenceAsBoolean(final Object preferenceName) throws MissingPreferenceException {
        return getPreferenceAsBoolean(preferenceName, false);
    }

    /**
     * Get a boolean preference value.
     *
     * @param preferenceName the preference name.
     * @param ignoreMissing true to return false when the property is missing.
     *
     * @return one boolean representing the preference value or false if the preference value is missing and the ignoreMissing argument is true.
     * @throws MissingPreferenceException if the preference value is missing and the ignoreMissing argument is false.
     */
    final public boolean getPreferenceAsBoolean(final Object preferenceName, final boolean ignoreMissing) throws MissingPreferenceException {
        final String value = getPreference(preferenceName, ignoreMissing);
        if (value == null) {
            return false;
        }
        return Boolean.valueOf(value).booleanValue();
    }

    /**
     * Get a double preference value.
     *
     * @param preferenceName the preference name.
     *
     * @return one double representing the preference value.
     * @throws MissingPreferenceException if the preference value is missing.
     */
    final public double getPreferenceAsDouble(final Object preferenceName) throws MissingPreferenceException {
        return getPreferenceAsDouble(preferenceName, false);
    }

    /**
     * Get a double preference value.
     *
     * @param preferenceName the preference name.
     * @param ignoreMissing true to return Double.NaN when the property is missing
     *
     * @return Double.NaN if the preference value is missing and the ignoreMissing argument is true.
     * @throws MissingPreferenceException if the preference value is missing and the ignoreMissing argument is false.
     */
    final public double getPreferenceAsDouble(final Object preferenceName, final boolean ignoreMissing) throws MissingPreferenceException {
        final String value = getPreference(preferenceName, ignoreMissing);
        if (value == null) {
            return Double.NaN;
        }
        return Double.valueOf(value).doubleValue();
    }

    /**
     * Get an integer preference value.
     *
     * @param preferenceName the preference name.
     *
     * @return one integer representing the preference value.
     * @throws MissingPreferenceException if the preference value is missing.
     */
    final public int getPreferenceAsInt(final Object preferenceName) throws MissingPreferenceException {
        return getPreferenceAsInt(preferenceName, false);
    }

    /**
     * Get an integer preference value.
     *
     * @param preferenceName the preference name.
     * @param ignoreMissing true to return 0 when the property is missing.
     *
     * @return one integer representing the preference value or 0 if the preference value is missing and the ignoreMissing argument is true.
     * @throws MissingPreferenceException if the preference value is missing and the ignoreMissing argument is false.
     */
    final public int getPreferenceAsInt(final Object preferenceName, final boolean ignoreMissing) throws MissingPreferenceException {
        final String value = getPreference(preferenceName, ignoreMissing);
        if (value == null) {
            return 0;
        }
        return NumberUtils.valueOf(value).intValue();
    }

    /**
     * Get a color preference value.
     *
     * @param preferenceName the preference name.
     *
     * @return one Color object representing the preference value.
     * @throws MissingPreferenceException if the preference value is missing.
     * @throws PreferencesException if the preference value is not a Color.
     */
    final public Color getPreferenceAsColor(final Object preferenceName) throws MissingPreferenceException, PreferencesException {
        final String value = getPreference(preferenceName);

        Color colorValue = null;
        try {
            colorValue = Color.decode(value);
        } catch (NumberFormatException nfe) {
            throw new PreferencesException("Cannot decode preference '" + preferenceName + "' value '" + value + "' as a Color.", nfe);
        }

        return colorValue;
    }

    /**
     * Get a Dimension preference value.
     *
     * @param preferenceName the preference name.
     *
     * @return one Dimension representing the preference value.
     * 
     * @throws MissingPreferenceException if the preference value is missing.
     */
    final public Dimension getPreferenceAsDimension(final Object preferenceName) throws MissingPreferenceException, PreferencesException {
        return getPreferenceAsDimension(preferenceName, false);
    }

    /**
     * Get a dimension preference value.
     *
     * @param preferenceName the preference name.
     * @param ignoreMissing true to return null when the property is missing
     *
     * @return one Dimension object representing the preference value, or null if none.
     *
     * @throws MissingPreferenceException if the preference value is missing.
     * @throws PreferencesException if the preference value is not a Dimension.
     */
    final public Dimension getPreferenceAsDimension(final Object preferenceName, final boolean ignoreMissing) throws MissingPreferenceException, PreferencesException {

        final Double width = getPreferenceAsDouble(DIMENSION_WIDTH_PREFIX + preferenceName, ignoreMissing);
        final Double height = getPreferenceAsDouble(DIMENSION_HEIGHT_PREFIX + preferenceName, ignoreMissing);

        if (width.isNaN() || height.isNaN()) {
            return null;
        }

        Dimension dimension = new Dimension();
        dimension.setSize(width, height);
        return dimension;
    }

    /**
     * Get a string list preference value.
     *
     * @param preferenceName the preference name.
     *
     * @return one List<String> object representing the preference value.
     *
     * @throws MissingPreferenceException if the preference value is missing
     * @throws PreferencesException if the preference value is not a List<String>
     */
    final public List<String> getPreferenceAsStringList(final Object preferenceName) throws MissingPreferenceException, PreferencesException {

        final String value = getPreference(preferenceName);

        /* ignore empty string */
        if (StringUtils.isEmpty(value)) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(LIST_SPLITTER));
    }

    /**
     * Rename the given preference, keeping order if any.
     *
     * Copy old preference value and order to the new one, then delete the old.
     * 
     * @param previousName the current preference name.
     * @param newName the new desired name.
     * @return true is everything went fine, false otherwise.
     */
    public boolean renamePreference(final String previousName, final String newName) {

        // Get previous preference value and order
        String value = getPreference(previousName);
        int order = getPreferenceOrder(previousName);
        String orderToken = (order == -1 ? "" : "' , '" + order);

        // Store in new preference key-value
        try {
            setPreference(newName, order, value);

            if (_logger.isDebugEnabled()) {
                _logger.debug("Renaming ['{}' , '{}'] to ['{} , '{}'].",
                        previousName, (value + orderToken), newName, (value + orderToken));
            }
        } catch (PreferencesException pe) {
            _logger.warn("Could not store preference '{}' : ", newName, pe);
            return false;
        }

        // Delete previous preference
        removePreference(previousName);

        // Commit change to file
        return true;
    }

    /**
     * Remove the given preference.
     *
     * If the given preference belongs to an ordered preferences group,
     * preferences left will be re-ordered down to fulfill the place left empty,
     * as expected. For example, {'LOW', 'MEDIUM', 'HIGH'} - MEDIUM will become
     * {'LOW', 'HIGH'}, not {'LOW, '', 'HIGH'}.
     *
     * @param preference the preference name.
     */
    final public void removePreference(final Object preference) {
        String preferenceName = preference.toString();

        _logger.debug("Removing preference '{}'.", preferenceName);

        // Get the given preference order, if any
        int preferenceOrder = getPreferenceOrder(preferenceName);

        if (preferenceOrder != -1) {
            // Compute preference group prefix name
            String preferencesPrefix = null;
            int indexOfLastDot = preferenceName.lastIndexOf('.');

            if ((indexOfLastDot > 0) && (indexOfLastDot < (preferenceName.length() - 1))) {
                preferencesPrefix = preferenceName.substring(0, indexOfLastDot);
            }

            _logger.debug("Removing preference from ordered group '{}'.", preferencesPrefix);

            // For each group preferences
            Enumeration<String> orderedPreferences = getPreferences(preferencesPrefix);

            while (orderedPreferences.hasMoreElements()) {
                String orderedPreferenceName = orderedPreferences.nextElement();

                int preferenceIndex = getPreferenceOrder(orderedPreferenceName);

                if (preferenceIndex > preferenceOrder) {
                    int destinationIndex = preferenceIndex - 1;

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Re-ordering preference '{}' from index '{}' to index '{}'.",
                                orderedPreferenceName, preferenceIndex, destinationIndex);
                    }

                    setPreferenceOrder(orderedPreferenceName, destinationIndex);
                }
            }
        }

        // Removing the given preference and its ordering index
        _currentProperties.remove(preferenceName);
        _currentProperties.remove(PREFERENCES_ORDER_INDEX_PREFIX + preferenceName);
    }

    /**
     * Replace a given string token in a preference by another string (and trim the result).
     *
     * @param preferenceName the path of the preference value to update.
     * @param searchedToken the string token to be replaced in the preference value (you can add a trailing space to decipher between similarly names tokens - last occurrence will also be detected flawlessly in this case).
     * @param replacingToken the new string to put in the preference value.
     *
     * @return true if everything went fine, false otherwise.
     */
    public boolean replaceTokenInPreference(Object preferenceName, String searchedToken, String replacingToken) {
        final String preferencePath = preferenceName.toString();

        // Get the preference current value
        final String originalPreferenceValue = " " + getPreference(preferencePath) + " "; // Add a leading an a trailing space to also match first and last token if needed

        _logger.trace("Preference '{}' contains : '{}'.", preferencePath, originalPreferenceValue);

        // Search for the token and replace it
        if (_logger.isTraceEnabled()) {
            _logger.trace("Replacing '{}' with '{}' in '{}'.", searchedToken, replacingToken, preferencePath);
        }

        final String newPreferenceValue = originalPreferenceValue.replaceAll(searchedToken, replacingToken).trim(); // Trim any spare spaces

        // Store updated preference value
        try {
            setPreference(preferencePath, newPreferenceValue);

            if (_logger.isTraceEnabled()) {
                _logger.trace("Preference '{}' contains : '{}'.", preferencePath, getPreference(preferencePath));
            }
        } catch (PreferencesException pe) {
            _logger.warn("Could not store '{}' preference:", preferencePath, pe);

            return false;
        }

        return true;
    }

    /**
     * Remove a given string token in a preference (and trim the result).
     *
     * @param preferenceName the path of the preference value to update.
     * @param searchedToken the string token to be removed (you can add a trailing space to decipher between similarly named tokens - last occurrence will also be detected flawlessly in this case).
     *
     * @return true if everything went fine, false otherwise.
     */
    public boolean removeTokenInPreference(Object preferenceName, String searchedToken) {
        String cleaningToken = "";
        if (searchedToken.startsWith(" ") && searchedToken.endsWith(" ")) {
            cleaningToken = " ";
        }

        return replaceTokenInPreference(preferenceName, searchedToken, cleaningToken);
    }

    /**
     * Returns an Enumeration (ordered if possible) of preference names which
     * start with given string. One given empty string make all preference
     * entries returned.
     *
     * @param prefix 
     * @return Enumeration a string enumeration of preference names
     */
    final public Enumeration<String> getPreferences(final Object prefix) {
        final List<String> shuffledProperties = new ArrayList<String>();

        int size = 0;
        final Enumeration<?> e = _currentProperties.propertyNames();

        // Count the number of properties for the given index and store them
        while (e.hasMoreElements()) {
            String propertyName = (String) e.nextElement();

            if (propertyName.startsWith(prefix.toString())) {
                size++;
                shuffledProperties.add(propertyName);
            }
        }

        String[] orderedProperties = new String[size];

        int nError = 0;

        // Order the stored properties if needed
        for (String propertyName : shuffledProperties) {
            final int propertyOrder = getPreferenceOrder(propertyName);

            // If the property is ordered
            if (propertyOrder != -1) {
                // Check that no overlapping occurs:
                if (orderedProperties[propertyOrder] != null) {
                    nError++;

                    _logger.warn("Incorrect Property Order [{}]: current value is '{}' - new value is '{}'",
                            propertyOrder, orderedProperties[propertyOrder], propertyName);
                } else {
                    // Store it at the right position
                    orderedProperties[propertyOrder] = propertyName;
                }
            } else {
                // Break and return the shuffled enumeration
                return Collections.enumeration(shuffledProperties);
            }
        }

        // Fix nulls (overlap):
        if (nError != 0) {
            final String[] fixedProperties = new String[size - nError];

            int i = 0;
            for (String propertyName : orderedProperties) {
                if (propertyName != null) {
                    fixedProperties[i++] = propertyName;
                }
            }
            orderedProperties = fixedProperties;
        }

        // Get an enumeration by converting the array -> List -> Enumeration
        return Collections.enumeration(Arrays.asList(orderedProperties));
    }

    /**
     * Returns the path of file containing preferences values, as this varies
     * across different execution platforms.
     *
     * @return a string containing the full file path to the preference file,
     * according to execution platform.
     */
    final public String computePreferenceFilepath() {

        _fullFilepath = FileUtils.getPlatformPreferencesPath();
        _fullFilepath += getPreferenceFilename();
        _logger.debug("Computed preference file path = '{}'.", _fullFilepath);
        return _fullFilepath;
    }

    /**
     * Trigger a notification of change to all registered Observers.
     */
    final public void triggerObserversNotification() {

        if (isNotify()) {
            _logger.debug("triggerObserversNotification ...");

            // Use EDT to ensure that Swing component(s) is updated by EDT :
            SwingUtils.invokeEDT(new Runnable() {
                @Override
                public void run() {
                    // Notify all preferences listener of maybe new values coming from file.
                    setChanged();
                    notifyObservers();
                }
            });
        } else {
            _logger.debug("triggerObserversNotification disabled.");
        }
    }

    /**
     * Return the flag to enable/disable observer notifications
     * @return flag to enable/disable observer notifications
     */
    public final boolean isNotify() {
        return _notify;
    }

    /**
     * Define the flag to enable/disable observer notifications
     * @param notify flag to enable/disable observer notifications
     */
    public final void setNotify(final boolean notify) {
        _notify = notify;
    }

    /**
     * String representation. Print filename and preferences.
     *
     * @return the representation.
     */
    @Override
    final public String toString() {
        return "Preferences file '" + _fullFilepath + "' contains :\n" + _currentProperties;
    }

    /**
     * Return the Save to file action
     * @return Save to file action
     */
    public Action getSavePreferences() {
        return _savePreferences;
    }

    /**
     * Return the Restore default preferences action
     * @return Restore default preferences action
     */
    public Action getRestoreDefaultPreferences() {
        return _restoreDefaultPreferences;
    }

    /**
     * Save to file action
     */
    protected class SavePrefAction extends RegisteredAction {

        /**
         * default serial UID for Serializable interface
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * @param preferenceClasspath class path of concrete Preference instance
         */
        protected SavePrefAction(final String preferenceClasspath) {
            super(preferenceClasspath, "savePreferences");
        }

        @Override
        public void actionPerformed(final ActionEvent ae) {
            try {
                saveToFile();
            } catch (PreferencesException pe) {
                _logger.warn("saveToFile failure : ", pe);
            }
        }
    }

    /**
     * Restore default preferences action
     */
    protected class RestoreDefaultPrefAction extends RegisteredAction {

        /**
         * default serial UID for Serializable interface
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * @param preferenceClasspath class path of concrete Preference instance
         */
        protected RestoreDefaultPrefAction(String preferenceClasspath) {
            super(preferenceClasspath, "restorePreferences");
        }

        @Override
        public void actionPerformed(final ActionEvent ae) {
            resetToDefaultPreferences();
        }
    }

    /**
     * Dump all properties (sorted by keys)
     * @param properties properties to dump
     * @return string representation of properties using the format "{name} : {value}"
     */
    public static String dumpProperties(final Properties properties) {
        return dumpProperties(properties, new StringBuilder(2048)).toString();
    }

    /**
     * Dump all properties (sorted by keys) into the given buffer
     * @param properties properties to dump
     * @param sb buffer to append into
     * @return string representation of properties using the format "{name} : {value}"
     */
    public static StringBuilder dumpProperties(final Properties properties, final StringBuilder sb) {
        if (properties == null) {
            return sb;
        }

        // Sort properties
        final String[] keys = new String[properties.size()];
        properties.keySet().toArray(keys);
        Arrays.sort(keys);

        // For each property, we make a string like "{name} : {value}"
        for (String key : keys) {
            sb.append(key).append(" : ").append(properties.getProperty(key)).append("\n");
        }
        return sb;
    }

    /**
     * Dump current properties (for debugging purposes)
     * @return string representation of properties using the format "{name} : {value}"
     */
    public String dumpCurrentProperties() {
        return dumpProperties(_currentProperties);
    }

    /**
     * Dump default properties (for debugging purposes)
     * @return string representation of properties using the format "{name} : {value}"
     */
    public String dumpDefaultProperties() {
        return dumpProperties(_defaultProperties);
    }
}
