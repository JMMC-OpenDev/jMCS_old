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
package fr.jmmc.jmcs.service;

import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.util.collection.FixedSizeLinkedHashMap;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RecentFilesManager singleton class.
 * 
 * RecentFilesManager.addFile() must be called by application to feed the Recent File menu entry.
 * 
 * @author Sylvain LAFRASSE, Laurent BOURGES, Guillaume MELLA.
 */
public final class RecentFilesManager {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(RecentFilesManager.class.getName());
    /** Maximum number of recent files by MIME type */
    private static final int MAXIMUM_HISTORY_ENTRIES = 10;
    /** Singleton instance */
    private static volatile RecentFilesManager _instance = null;
    /* Members */
    /** Action registrar reference */
    private final ActionRegistrar _registrar;
    /** Flag to enable or disable this feature */
    boolean _enabled = true;
    /** Hook to the "Open Recent" sub-menu */
    final JMenu _menu;
    /** thread safe recent file repository */
    private final Map<String, String> _repository = Collections.synchronizedMap(new FixedSizeLinkedHashMap<String, String>(MAXIMUM_HISTORY_ENTRIES));

    /**
     * Return the singleton instance
     * @return singleton instance
     */
    static synchronized RecentFilesManager getInstance() {
        // DO NOT MODIFY !!!
        if (_instance == null) {
            _instance = new RecentFilesManager();
        }

        return _instance;
        // DO NOT MODIFY !!!
    }

    /**
     * Hidden constructor
     */
    protected RecentFilesManager() {
        _registrar = ActionRegistrar.getInstance();
        _menu = new JMenu("Open Recent");
        populateMenuFromPreferences();
    }

    /**
     * Enables or disables this feature
     * @param enabled false to disable
     */
    public static void setEnabled(final boolean enabled) {
        getInstance()._enabled = enabled;
    }

    /**
     * Return flag to enable or disable this feature
     * @return true if enabled; false otherwise
     */
    public boolean isEnabled() {
        return _enabled;
    }

    /**
     * Link RecentFilesManager menu to the "Open Recent" sub-menu
     * @return "Open Recent" sub-menu container
     */
    public static JMenu getMenu() {
        final RecentFilesManager rfm = getInstance();
        if (rfm.isEnabled()) {
            return rfm._menu;
        }
        return null;
    }

    /**
     * Add the given recent file for MIME type.
     * @param file file object or null
     */
    public static void addFile(final File file) {
        if (file == null) {
            return;
        }

        final RecentFilesManager rfm = getInstance();
        if (!rfm.isEnabled()) {
            return;
        }

        if (!rfm.storeFile(file)) {
            return;
        }

        rfm.refreshMenu();
        rfm.flushRecentFileListToPreferences();
    }

    /**
     * Store the given file in the recent file repository.
     * @param file file to be added in the file repository
     * @return  true if operation succeeded else false.
     */
    private boolean storeFile(final File file) {
        // Check parameter validity
        if ((file == null) || (!file.canRead())) {
            _logger.warn("Could not read file '{}'", file);
            return false;
        }

        // Check file path
        String path;
        try {
            path = file.getCanonicalPath();
        } catch (IOException ex) {
            _logger.warn("Could not resolve file path of file '{}'", file, ex);
            return false;
        }

        if ((path == null) || (path.length() == 0)) {
            _logger.warn("Could not resolve empty file path of file '{}'", file);
            return false;
        }

        // Check file name
        String name = file.getName();
        if ((name == null) || (name.length() == 0)) { // If no name found
            name = path; // Use path instead
        }

        // Store file (at first position if already referenced)
        _repository.remove(path);
        _repository.put(path, name);
        return true;
    }

    /**
     * Refresh content of "Open Recent" File menu entry.
     */
    private void refreshMenu() {

        // Clean, then re-fill sub-menu
        _menu.removeAll();
        _menu.setEnabled(false);

        // For each registered files
        final ListIterator<Map.Entry<String, String>> iter = new ArrayList<Map.Entry<String, String>>(_repository.entrySet()).listIterator(_repository.size());

        while (iter.hasPrevious()) {

            final Map.Entry<String, String> entry = iter.previous();
            final String currentName = entry.getValue();
            final String currentPath = entry.getKey();

            // Create an action to open it
            //final String currentName = _repository.get(currentPath);
            final AbstractAction currentAction = new AbstractAction(currentName) {
                /** default serial UID for Serializable interface */
                private static final long serialVersionUID = 1;

                @Override
                public void actionPerformed(ActionEvent ae) {
                    _registrar.getOpenAction().actionPerformed(new ActionEvent(_registrar, 0, currentPath));
                }
            };
            
            final JMenuItem menuItem = new JMenuItem(currentAction);
            menuItem.setToolTipText(currentPath);
            _menu.add(menuItem);
        }

        if (_menu.getItemCount() > 0) {
            addCleanAction();
            _menu.setEnabled(true);
        }
    }

    /**
     * Add a "Clear" item at end below a separator
     */
    private void addCleanAction() {

        final AbstractAction cleanAction = new AbstractAction("Clear History") {
            /** default serial UID for Serializable interface */
            private static final long serialVersionUID = 1;

            @Override
            public void actionPerformed(ActionEvent ae) {
                _repository.clear();

                flushRecentFileListToPreferences();

                _menu.removeAll();
                _menu.setEnabled(false);
            }
        };

        _menu.add(new JSeparator());
        _menu.add(new JMenuItem(cleanAction));
    }

    /**
     * Grab recent files from shared preference.
     */
    private void populateMenuFromPreferences() {

        final List<String> recentFilePaths = SessionSettingsPreferences.getRecentFilePaths();
        if (recentFilePaths == null) {
            _logger.warn("No recent file path found.");
            return;
        }

        for (String path : recentFilePaths) {
            storeFile(new File(path));
        }

        refreshMenu();
    }

    /**
     * Flush file list to shared preference.
     */
    private void flushRecentFileListToPreferences() {
        // Create list of paths
        if (_repository == null) {
            _logger.debug("Could not get recent file paths.");
            return;
        }
        final List<String> pathsList = new ArrayList<String>(_repository.keySet());

        // Put this to prefs
        SessionSettingsPreferences.setRecentFilePaths(pathsList);
    }
}
