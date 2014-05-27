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
package fr.jmmc.jmcs.gui.component;

import fr.jmmc.jmcs.data.preference.CommonPreferences;
import fr.jmmc.jmcs.data.preference.Preferences;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import fr.jmmc.jmcs.util.StringUtils;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic panel to display check boxes associated with boolean preferences.
 * @author Sylvain LAFRASSE
 */
public class BooleanPreferencesView extends JPanel implements Observer, ChangeListener {

    // Public constants
    public static final String SAVE_AND_RESTART_MESSAGE = "Please save modifications and restart the application to apply changes.";

    // Internal constants
    private static final long serialVersionUID = 1L;
    private static final Logger _logger = LoggerFactory.getLogger(BooleanPreferencesView.class.getName());
    private final Preferences _preferences;
    private final Map<Object, JCheckBox> _booleanPreferencesHashMap;
    private boolean _programaticUpdateUnderway = false;
    private final String _message;

    /**
     * Constructor.
     * @param preferences the PReferences instance to work on.
     * @param booleanPreferencesHashMap the ordered map linking preference key to its check box label.
     */
    public BooleanPreferencesView(Preferences preferences, LinkedHashMap<Object, String> booleanPreferencesHashMap) {
        this(preferences, booleanPreferencesHashMap, null);
    }

    /**
     * Constructor.
     * @param preferences the PReferences instance to work on.
     * @param booleanPreferencesHashMap the ordered map linking preference key to its check box label.
     * @param message (optional) string added at the bottom of the pane, null otherwise.
     */
    public BooleanPreferencesView(Preferences preferences, LinkedHashMap<Object, String> booleanPreferencesHashMap, String message) {

        super();

        // Check arguments validity
        if ((preferences == null) || (booleanPreferencesHashMap == null) || (booleanPreferencesHashMap.size() < 1)) {
            throw new IllegalArgumentException("Invalid parameter received");
        }

        // Decipher message availability
        if (StringUtils.isEmpty(message)) {
            _message = null;
        } else {
            _message = message;
        }

        _preferences = preferences;

        _booleanPreferencesHashMap = new LinkedHashMap<Object, JCheckBox>();
        for (Map.Entry<Object, String> entry : booleanPreferencesHashMap.entrySet()) {

            final Object preferenceKey = entry.getKey();
            final String checkBoxLabel = entry.getValue();

            final JCheckBox newCheckBox = new JCheckBox(checkBoxLabel);
            _booleanPreferencesHashMap.put(preferenceKey, newCheckBox);
        }
    }

    /** MANDATORY call after construction. */
    public void init() {

        _preferences.addObserver(this);

        JPanel checkBoxesPanel = new JPanel();
        checkBoxesPanel.setOpaque(false);

        // Layout management
        checkBoxesPanel.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridx = 0;

        // Layout each checkbox
        int biggestMinimumCheckBoxesWidth = 0;
        for (JCheckBox checkBox : _booleanPreferencesHashMap.values()) {

            checkBoxesPanel.add(checkBox, gridBagConstraints);
            gridBagConstraints.gridy++;

            final int currentMinimumCheckBoxWidth = checkBox.getMinimumSize().width;
            biggestMinimumCheckBoxesWidth = Math.max(biggestMinimumCheckBoxesWidth, currentMinimumCheckBoxWidth);

            checkBox.addChangeListener(this);
        }

        // Set checkboxes panel width to center properly
        final Dimension dimension = new Dimension(biggestMinimumCheckBoxesWidth, 0);
        checkBoxesPanel.setMaximumSize(dimension);

        // Layout the checkboxes panel centered at the top
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(checkBoxesPanel);
        add(Box.createVerticalGlue());

        // Add the centered bottom label (if any)
        if (_message != null) {
            final JLabel label = new JLabel(_message);
            label.setAlignmentX(CENTER_ALIGNMENT);
            add(label);
        }

        // Synchronize checkboxes state with their associated preference values
        update(null, null);
    }

    @Override
    public void update(Observable observable, Object parameter) {

        _programaticUpdateUnderway = true;

        for (Map.Entry<Object, JCheckBox> entry : _booleanPreferencesHashMap.entrySet()) {

            final JCheckBox currentCheckBox = entry.getValue();
            final String currentCheckBoxName = currentCheckBox.getText();
            final boolean currentCheckBoxState = currentCheckBox.isSelected();
            final Object currentPreferenceKey = entry.getKey();
            final boolean currentPreferenceState = _preferences.getPreferenceAsBoolean(currentPreferenceKey);

            if (_logger.isDebugEnabled()) {
                _logger.debug("Set checkbox '{}' to '{}' (was '{}').",
                        currentCheckBoxName, currentPreferenceState, currentCheckBoxState);
            }
            currentCheckBox.setSelected(currentPreferenceState);
        }

        _programaticUpdateUnderway = false;
    }

    /**
     * Update preferences according buttons change
     * @param ce change event
     */
    @Override
    public void stateChanged(ChangeEvent ce) {

        JCheckBox clickedCheckBox = (JCheckBox) ce.getSource();
        if (clickedCheckBox == null) {
            _logger.error("Could not retrieve event source: {}", ce);
            return;
        }

        final String clickedCheckBoxName = clickedCheckBox.getText();
        _logger.debug("Checkbox '{}' state changed.", clickedCheckBoxName);

        if (_programaticUpdateUnderway) {
            _logger.trace("Programatic update underway, SKIPPING.");
            return;
        }

        for (Map.Entry<Object, JCheckBox> entry : _booleanPreferencesHashMap.entrySet()) {

            final JCheckBox currentCheckBox = entry.getValue();
            if (!clickedCheckBox.equals(currentCheckBox)) {
                continue;
            }

            final Object currentPreferenceKey = entry.getKey();
            final boolean currentPreferenceState = _preferences.getPreferenceAsBoolean(currentPreferenceKey);
            final boolean clickedCheckBoxState = currentCheckBox.isSelected();

            if (clickedCheckBoxState == currentPreferenceState) {
                _logger.trace("State did not trully changed ({} == {}), SKIPPING.", clickedCheckBoxState, currentPreferenceState);
                return;
            }

            try {
                _logger.debug("State did changed ({} -> {}), WRITING.", currentPreferenceState, clickedCheckBoxState);
                _preferences.setPreference(currentPreferenceKey, clickedCheckBoxState);
            } catch (PreferencesException pe) {
                _logger.warn("Could not set preference: ", pe);
            }

            return;
        }
    }

    public static void main(String[] args) {

        CommonPreferences preferences = CommonPreferences.getInstance();

        LinkedHashMap<Object, String> booleanSettings = new LinkedHashMap<Object, String>();
        booleanSettings.put(CommonPreferences.SHOW_STARTUP_SPLASHSCREEN, "Show splashscreen at startup");
        // And so on...

        final BooleanPreferencesView generalSettingsView = new BooleanPreferencesView(preferences, booleanSettings, "For testing purpose only !");
        generalSettingsView.init();

        JFrame frame = new JFrame("BooleanPreferencesView Demo");
        frame.add(generalSettingsView);
        frame.pack();
        frame.setVisible(true);
    }
}
