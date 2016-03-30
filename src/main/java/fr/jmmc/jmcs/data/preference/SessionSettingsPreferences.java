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

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import java.io.File;
import java.util.Collections;
import java.util.List;
import fr.jmmc.jmcs.util.CollectionUtils;
import fr.jmmc.jmcs.util.FileUtils;
import java.awt.Dimension;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gathers :
 * - user preferences related to local folders in FileChooser;
 * - recently used file paths;
 * - windows size.
 *
 * @author Laurent BOURGES, Sylvain LAFRASSE.
 */
public final class SessionSettingsPreferences extends Preferences {

    /** Singleton instance */
    private static SessionSettingsPreferences _singleton = null;
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(SessionSettingsPreferences.class.getName());
    /** File name prefix */
    private static final String FILENAME_PREFIX = "fr.jmmc.jmcs.session_settings.";
    /** File name suffix */
    private static final String FILENAME_SUFFIX = ".properties";
    /** Recent directory for MIME type preference key prefix */
    private static final String RECENT_DIRECTORY_PREFIX = "recent_directory_for_MIME_type.";
    /** Recent file list preference key */
    private static final String RECENT_FILE_KEY = "recent_files";
    /** Dimension preference key prefix */
    private static final String DIMENSION_PREFIX = "dimension.";
    /** Application file storage preference */
    private static final String APPLICATION_STORAGE_LOCATION = "app.storage.location";

    /**
     * Private constructor that must be empty.
     */
    private SessionSettingsPreferences() {
        super(false); // No update notifications
    }

    /**
     * Return the singleton instance of SessionPersistencePreferences.
     * @return the singleton instance
     */
    synchronized static SessionSettingsPreferences getInstance() {
        // Build new reference if singleton does not already exist
        // or return previous reference
        if (_singleton == null) {
            _logger.debug("SessionPersistencePreferences.getInstance()");
            // disable notifications:
            _singleton = new SessionSettingsPreferences();
            // enable future notifications:
            _singleton.setNotify(true);
        }
        return _singleton;
    }

    /**
     * Return the default directory (user home)
     * @return default directory (user home)
     */
    private static String getDefaultDirectory() {
        return SystemUtils.USER_HOME;
    }

    /**
     * Define the default directory as user home directory for all known mime types.
     *
     * @throws PreferencesException if any preference value has a unsupported class type
     */
    @Override
    protected void setDefaultPreferences() throws PreferencesException {
        _logger.debug("SessionPersistencePreferences.setDefaultPreferences");

        final String defaultDirectory = getDefaultDirectory();

        for (MimeType mimeType : MimeType.values()) {
            setDefaultPreference(computeMimeTypeRecentDirectoryKey(mimeType), defaultDirectory);
        }

        final List<String> emptyList = Collections.emptyList();
        setDefaultPreference(RECENT_FILE_KEY, emptyList);

        // Default File storage location:
        final String fileStorageLocation = FileUtils.getPlatformDocumentsPath();
        setDefaultPreference(APPLICATION_STORAGE_LOCATION, fileStorageLocation
                + FileUtils.cleanupFileName(ApplicationDescription.getInstance().getProgramName())
                + File.separatorChar);
    }

    /**
     * Forge the preference filename according to the application name and company.
     *
     * @return preference filename.
     */
    @Override
    protected String getPreferenceFilename() {

        final ApplicationDescription applicationDataModel = ApplicationDescription.getInstance();
        final String shortCompanyName = applicationDataModel.getShortCompanyName();
        final String programName = applicationDataModel.getProgramName();

        String preferenceFileName = FILENAME_PREFIX + shortCompanyName + "." + programName + FILENAME_SUFFIX;
        preferenceFileName = preferenceFileName.replace(" ", "");
        preferenceFileName = preferenceFileName.toLowerCase();

        return preferenceFileName;
    }

    /**
     * @return preference version number.
     */
    @Override
    protected int getPreferencesVersionNumber() {
        return 1;
    }

    /**
     * @return the last directory used for files having this MIME type (by default, user home).
     * @param mimeType MIME type to look for
     */
    public static File getLastDirectoryForMimeTypeAsFile(final MimeType mimeType) {
        return new File(getLastDirectoryForMimeTypeAsPath(mimeType));
    }

    /**
     * @return the last directory used for files having this MIME type (by default, user home).
     * @param mimeType MIME type to look for
     */
    public static String getLastDirectoryForMimeTypeAsPath(final MimeType mimeType) {
        if (mimeType == null) {
            return getDefaultDirectory();
        }
        return getInstance().getPreference(computeMimeTypeRecentDirectoryKey(mimeType));
    }

    /**
     * Define the last directory used for files having this MIME type.
     * @param mimeType mime type to look for
     * @param path file path to an existing directory
     */
    public static void setCurrentDirectoryForMimeType(final MimeType mimeType, final String path) {
        if (mimeType == null || path == null) {
            return;
        }

        final String oldPath = getLastDirectoryForMimeTypeAsPath(mimeType);
        if (path.equals(oldPath)) {
            return;
        }

        try {
            getInstance().setPreference(computeMimeTypeRecentDirectoryKey(mimeType), path);
        } catch (PreferencesException pe) {
            _logger.warn("Saving SessionPersistencePreferences failure:", pe);
        }
    }

    private static String computeMimeTypeRecentDirectoryKey(MimeType mimeType) {
        return RECENT_DIRECTORY_PREFIX + mimeType.getId();
    }

    /**
     * @return the recent file list, or null if none found.
     */
    public static List<String> getRecentFilePaths() {
        // Try to read paths list from preference
        List<String> paths = null;
        try {
            paths = getInstance().getPreferenceAsStringList(RECENT_FILE_KEY);
        } catch (MissingPreferenceException mpe) {
            _logger.info("No recent files found.", mpe);
        } catch (PreferencesException pe) {
            _logger.warn("Could not read preference for recent files", pe);
        }

        if ((paths == null) || (paths.isEmpty())) {
            _logger.debug("No recent files stored.");
            return null;
        }

        // Deserialize paths to recent file list
        _logger.debug("Found recent files '{}'.", CollectionUtils.toString(paths));
        return paths;
    }

    /**
     * @param paths path list to store in preferences
     */
    public static void setRecentFilePaths(final List<String> paths) {
        if (paths == null) {
            _logger.error("Null recent file list received");
            return;
        }

        // Try to store paths list to preference
        final SessionSettingsPreferences instance = getInstance();
        try {
            instance.setPreference(RECENT_FILE_KEY, paths);
        } catch (PreferencesException pe) {
            _logger.error("Could not store recent file list in preference", pe);
        }
    }

    /**
     * @return the application file storage
     */
    public static String getApplicationFileStorage() {
        return getInstance().getPreference(APPLICATION_STORAGE_LOCATION);
    }

    /**
     * Try to load the dimension associated with the given key.
     * @param key Unique string identifying a given dimension.
     * @return the sought dimension if found, null otherwise.
     */
    public static Dimension loadDimension(final String key) {
        if (key == null) {
            _logger.warn("Null dimension key received");
            return null;
        }

        try {
            return getInstance().getPreferenceAsDimension(computeDimensionKey(key), true);
        } catch (MissingPreferenceException mpe) {
            _logger.debug("No dimension found for window key '{}'", key, mpe);
        } catch (PreferencesException pe) {
            _logger.warn("Could not read dimension preference for window key '{}'", key, pe);
        }

        return null;
    }

    /**
     * Try to save the dimension associated with the given key.
     * @param key Unique string identifying a given dimension.
     * @param dimension the dimension to save.
     */
    public static void storeDimension(final String key, final Dimension dimension) {
        if (key == null) {
            _logger.warn("Null dimension key received");
            return;
        }
        if (dimension == null) {
            _logger.warn("Null dimension value received");
            return;
        }

        try {
            getInstance().setPreference(computeDimensionKey(key), dimension);
        } catch (PreferencesException pe) {
            _logger.error("Could not store dimension '{}' for key '{}' in preference", dimension, key, pe);
        }

        _logger.debug("saveDimension('{}') = {}", key, dimension);
    }

    private static String computeDimensionKey(final String key) {
        return DIMENSION_PREFIX + key;
    }

    /**
     * Try to save the session settings to file if needed.
     */
    public static void saveToFileIfNeeded() {
        if (_singleton != null) {
            try {
                _singleton.saveToFile();
            } catch (PreferencesException ex) {
                _logger.warn("Could not save session settings", ex);
            }
        }
    }

    /**
     * Run this program to generate the default file preference file.
     * @param args unused
     */
    public static void main(String[] args) {
        try {
            final SessionSettingsPreferences instance = SessionSettingsPreferences.getInstance();
            instance.setPreference("test", new Dimension(123, 456));
            instance.saveToFile();
            instance.setPreference("test", new Dimension(0, 0));
            System.out.println("dimension = " + instance.getPreferenceAsDimension("test"));
            instance.loadFromFile();
            System.out.println("dimension = " + instance.getPreferenceAsDimension("test"));
        } catch (PreferencesException pe) {
            _logger.error("property failure : ", pe);
        }
    }
}
