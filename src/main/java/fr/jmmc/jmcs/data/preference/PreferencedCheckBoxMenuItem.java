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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JCheckBoxMenuItem;

/**
 * Menu item with a check box representing a jMCS preference boolean property state.
 * 
 * @author Sylvain LAFRASSE, Guillaume MELLA.
 */
public class PreferencedCheckBoxMenuItem extends JCheckBoxMenuItem implements Observer, ActionListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;
    /** Menu item corresponding preference property */
    private String _preferenceProperty;
    /** Shared instance */
    private Preferences _preferences;

    /**
     * PreferencedCheckBoxMenuItem constructor
     *
     * @param title a string containing the label to be displayed in the menu
     * @param preferences the Preferences shared instance
     * @param preferenceProperty a string containing the reference to the boolean property to handle
     */
    public PreferencedCheckBoxMenuItem(String title, Preferences preferences, String preferenceProperty) {
        // Set the label of the Menu Item widget
        super(title);

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
     * Triggered if the menu item has been clicked.
     * @param evt
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        // If the widget changed, update the property value
        try {
            _preferences.setPreference(_preferenceProperty, isSelected());
        } catch (PreferencesException pe) {
            throw new RuntimeException(pe);
        }
    }

    /**
     * Triggered if the preference shared instance has been modified.
     * @param o
     * @param arg  
     */
    @Override
    public void update(Observable o, Object arg) {
        // Update the widget status if the property value changed
        setSelected(_preferences.getPreferenceAsBoolean(_preferenceProperty));
    }
}
/*___oOo___*/
