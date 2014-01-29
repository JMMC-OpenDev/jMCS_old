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

import fr.jmmc.jmcs.gui.FeedbackReport;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.DefaultButtonModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Menu item with a check box representing a MCS preference boolean property
 * state. This class should be associated to AbstractButton widgets that change
 * a boolean preference. After setModel call, the preference will be
 * automatically changed according user events and UI will be automatically
 * updated according preference change. Moreover actions should be associated to
 * implement application behavior associated to user events.
 * 
 * @author Guillaume MELLA, Sylvain LAFRASSE, Laurent BOURGES.
 */
public final class PreferencedButtonModel extends DefaultButtonModel
        implements Observer, ActionListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class logger */
    private final static Logger _logger = LoggerFactory.getLogger(PreferencedButtonModel.class.getName());
    /** Store PreferencedButtonModel instances for a given preference name */
    private static Map<String, PreferencedButtonModel> _instanceMap = Collections.synchronizedMap(new HashMap<String, PreferencedButtonModel>(8));
    /* members */
    /** Shared instance */
    private final Preferences _preferences;
    /** Preference property */
    private final String _preferenceProperty;

    /**
     * PreferencedButtonModel constructor
     *
     * @param preferences the preference that lists every entries
     * @param preferenceProperty the preference name
     */
    private PreferencedButtonModel(final Preferences preferences,
            final String preferenceProperty) {
        // Store the Preference shared instance of the main application
        _preferences = preferences;

        // Store the property name for later use
        _preferenceProperty = preferenceProperty;

        // Retrieve the property boolean value and set the widget accordinaly
        setSelected(_preferences.getPreferenceAsBoolean(_preferenceProperty));

        // Register the object as its handler of any modification of its widget
        addActionListener(this);
        // Register the object as the observer of any property value change
        _preferences.addObserver(this);
    }

    /**
     * Return one shared instance associated to the preference property name.
     *
     * @param preferences the preference that lists every entries
     * @param preferenceProperty the preference name
     *
     * @return the PreferencedButtonModel singleton
     */
    public static PreferencedButtonModel getInstance(final Preferences preferences,
            final String preferenceProperty) {

        PreferencedButtonModel bm = _instanceMap.get(preferenceProperty);

        if (bm == null) {
            bm = new PreferencedButtonModel(preferences, preferenceProperty);
            _instanceMap.put(preferenceProperty, bm);
        }

        return bm;
    }

    /**
     * Triggered if the button has been clicked.
     * @param evt action event (swing)
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        // Because actionPerformed is called before selected flag change :
        // invert isSelected returned value
        final boolean nextValue = !isSelected();

        // If the widget changed due to user action,
        // update the property value
        if (evt.getActionCommand() != null) {
            if (evt.getActionCommand().equals("internalUpdate")) {
                _logger.trace("This event is due to a preference update and does nothing");
                return;
            }
        }

        _logger.trace("This event is due to a user interaction");
        _logger.debug("Setting preference '{}' to {}", _preferenceProperty, nextValue);

        try {
            _preferences.setPreference(_preferenceProperty, nextValue);
        } catch (PreferencesException pe) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, pe);
        }
    }

    /**
     * Triggered if the preference shared instance has been modified.
     * @param o
     * @param arg  
     */
    @Override
    public void update(final Observable o, final Object arg) {
        // Notify event Listener (telling this that it is an internal update)
        _logger.debug("Fire action listeners ");

        fireActionPerformed(new ActionEvent(this, SELECTED, "internalUpdate"));

        // Update the widget view according property value changed
        final boolean nextValue = _preferences.getPreferenceAsBoolean(_preferenceProperty);

        _logger.debug("Setting selected to {}", nextValue);

        setSelected(nextValue);
    }
}
/*___oOo___*/
