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
package fr.jmmc.jmcs.gui.action;

import fr.jmmc.jmcs.data.preference.Preferences;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RegisteredAction class customized to be bound to a preference-ed boolean.
 * 
 * @author Sylvain LAFRASSE.
 */
public class RegisteredPreferencedBooleanAction extends RegisteredAction
        implements Observer, ItemListener {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(RegisteredPreferencedBooleanAction.class.getName());
    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;
    /** Monitored Preference object */
    private Preferences _preferences;
    /** Name of the bound preference */
    private String _preferenceName;
    /** List of buttons bound to this action */
    private List<AbstractButton> _boundButtons;

    /**
     * Constructor.
     *
     * @param classPath the path of the class containing the field pointing to
     * the action, in the form returned by 'getClass().getName();'.
     * @param fieldName the name of the field pointing to the action.
     * @param actionName the name of the action.
     * @param preferences the Preferences object to monitor.
     * @param preferenceName the preference name of the value to get/set.
     */
    public RegisteredPreferencedBooleanAction(String classPath,
            String fieldName, String actionName, Preferences preferences,
            Object preferenceName) {
        super(classPath, fieldName, actionName);

        _boundButtons = new ArrayList<AbstractButton>();

        _preferenceName = preferenceName.toString();

        // Store the application preferences and register against it
        _preferences = preferences;
        _preferences.addObserver(this);
    }

    /**
     * Register the given button as one to update when observed preferences change.
     *
     * @param button the button to register.
     */
    public void addBoundButton(AbstractButton button) {
        _boundButtons.add(button);
        button.addItemListener(this);
    }

    /**
     * Automatically called whenever the observed Preferences object changed.
     * @param o
     * @param arg  
     */
    @Override
    public void update(Observable o, Object arg) {
        boolean state = _preferences.getPreferenceAsBoolean(_preferenceName);

        _logger.trace("{} value changed to become '{}'.", _preferenceName, state);

        for (AbstractButton button : _boundButtons) {
            button.setSelected(state);
        }
    }

    /**
     * Automatically called whenever any bound button is clicked.
     * @param e 
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getSource() instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) e.getSource();
            boolean isSelected = button.isSelected();

            _logger.trace("{} value was updated with new external state of '{}'.", _preferenceName, isSelected);
            try {
                _preferences.setPreference(_preferenceName, isSelected);
            } catch (PreferencesException pe) {
                _logger.warn("Cannot set preference '{}' to '{}'.", _preferenceName, isSelected, pe);
            }
        }
    }

    /**
     * Automatically called whenever any bound button state change.
     *
     * Added as it is the only reliable way to deal with ButtonGroup and JRadioButton.
     * ButtonGroups don't handle "UNSELECT" ActionEvent, whereas ItemEvent do !!!
     * @param e 
     * @link http://forums.sun.com/thread.jspa?forumID=257&threadID=173201
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);

        _logger.trace("{} value was updated with new internal state of '{}'.", _preferenceName, isSelected);
        try {

            _preferences.setPreference(_preferenceName, isSelected);
        } catch (PreferencesException pe) {
            _logger.warn("Cannot set preference '{}' to '{}'.", _preferenceName, isSelected, pe);
        }
    }
}
/*___oOo___*/
