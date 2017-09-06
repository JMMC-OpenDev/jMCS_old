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
package fr.jmmc.jmcs.gui;

import fr.jmmc.jmcs.data.preference.Preferences;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.ComponentResizeAdapter;
import fr.jmmc.jmcs.gui.util.WindowUtils;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides one preference view containing tabbed panes.
 */
public class PreferencesView extends JDialog implements ActionListener {

    public static final int FRAME_WIDTH = 600;
    public static final int FRAME_HEIGHT = 500;
    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(PreferencesView.class.getName());
    /** Data model */
    Preferences _preferences = null;
    /** Preferences... action */
    public final ShowPreferencesAction _showPreferencesAction;
    /** "Restore to Default Settings" button */
    protected JButton _restoreDefaultButton = null;
    /** "Save Modifications" button */
    protected JButton _saveModificationButton = null;

    /**
     * Constructor.
     * @param preferences your application Preferences instance.
     * @param panels a map of tab title (string) -> panel (JPanel).
     */
    public PreferencesView(final JFrame parent, final Preferences preferences, final LinkedHashMap<String, JPanel> panels) {
        super(parent, "Preferences", false);

        // Check arguments validity
        if ((preferences == null) || (panels == null) || (panels.isEmpty())) {
            throw new IllegalArgumentException();
        }

        // Get and listen to data model modifications
        _preferences = preferences;

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Skip tabbed pane if only one panel to display
        if (panels.size() == 1) {
            final JPanel firstPanel = panels.values().iterator().next();
            contentPane.add(firstPanel);
        } else {
            // Build the tabbed pane
            JTabbedPane tabbedPane = new JTabbedPane();
            contentPane.add(tabbedPane);

            // Add each preferences pane
            for (Map.Entry<String, JPanel> entry : panels.entrySet()) {

                final String panelName = entry.getKey();
                final JPanel panel = entry.getValue();

                tabbedPane.add(panelName, panel);

                _logger.debug("Added '{}' panel to PreferenceView tabbed pane.", panelName);
            }
        }

        // Add the restore and sace buttons
        JPanel buttonsPanel = new JPanel();
        _restoreDefaultButton = new JButton("Restore Default Settings");
        buttonsPanel.add(_restoreDefaultButton);
        _saveModificationButton = new JButton("Save Modifications");
        buttonsPanel.add(_saveModificationButton);
        contentPane.add(buttonsPanel);

        // only hide on close as this view is reused by the application:
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        final Dimension dim = new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
        setMinimumSize(dim);
        addComponentListener(new ComponentResizeAdapter(dim));

        // pack and center window
        pack();

        _showPreferencesAction = new ShowPreferencesAction(getClass().getName(), "_showPreferencesAction");
    }

    private PreferencesView getPreferencesView() {
        return this;
    }

    public void init() {
        WindowUtils.setClosingKeyboardShortcuts(this);

        _restoreDefaultButton.addActionListener(this);
        _saveModificationButton.addActionListener(this);
    }

    /**
     * Free any resource or reference to this instance :
     * remove this instance form Preference Observers
     */
    @Override
    public void dispose() {
        _logger.debug("dispose: {}", this);

        // @TODO add deleteObserver(this) to dispose() to dereference each subview properly
        super.dispose();
    }

    /**
     * actionPerformed  -  Listener
     *
     * @param evt ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        _logger.trace("PreferencesView.actionPerformed");

        // If the "Restore to default settings" button has been pressed
        if (evt.getSource().equals(_restoreDefaultButton)) {
            _preferences.resetToDefaultPreferences();
        }

        // If the "Save modifications" button has been pressed
        if (evt.getSource().equals(_saveModificationButton)) {
            try {
                _preferences.saveToFile();
            } catch (PreferencesException pe) {
                _logger.warn("Could not save preferences.", pe);
            }
        }
    }

    /**
     * Called to show the preferences window.
     */
    protected class ShowPreferencesAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        ShowPreferencesAction(String classPath, String fieldName) {
            super(classPath, fieldName);
            flagAsPreferenceAction();
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            _logger.trace("ShowPreferencesAction.actionPerformed");

            WindowUtils.centerOnMainScreen(getPreferencesView());

            // Show the Preferences window
            setVisible(true);
        }
    }
}
