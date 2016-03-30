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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Associate one string to a preference entry. This class should be associated
 * to Text widgets that change a string preference. After setModel call, the
 * preference will be automatically changed according user events and UI will be
 * automatically updated according preference change. Moreover actions should be
 * associated to implement application behavior associated to user events.
 *
 * @author Guillaume MELLA, Sylvain LAFRASSE, Laurent BOURGES.
 */
public final class PreferencedDocument extends javax.swing.text.PlainDocument
        implements Observer, DocumentListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class logger */
    private final static Logger _logger = LoggerFactory.getLogger(PreferencedDocument.class.getName());
    /** Store PreferencedDocument instances for a given preference name */
    private static final Map<String, PreferencedDocument> _instanceMap = Collections.synchronizedMap(new HashMap<String, PreferencedDocument>(8));
    /* members */
    /** Shared instance */
    private final Preferences _preferences;
    /** Preference property */
    private final String _preferenceProperty;
    /** auto-save timer */
    private final Timer _autoSaveTimer;

    /**
     * PreferencedButtonModel constructor
     *
     * @param preferences the preference that lists every entries
     * @param preferenceProperty the preference name
     * @param autoSave Tells if preference must be saved automatically or not
     * (default)
     */
    private PreferencedDocument(final Preferences preferences,
                                final String preferenceProperty, final boolean autoSave) {

        // Store the Preference shared instance of the main application
        _preferences = preferences;

        // Store the property name for later use
        _preferenceProperty = preferenceProperty;

        if (autoSave) {
            _autoSaveTimer = new Timer(2000, new ActionListener() {
                /* Invoked when timer action occurs. */
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        _preferences.saveToFile();
                    } catch (PreferencesException ex) {
                        throw new IllegalStateException("Can't set value for preference " + _preferenceProperty);
                    } finally {
                        _autoSaveTimer.stop();
                    }
                }
            });
        } else {
            _autoSaveTimer = null;
        }

        // Retrieve the property value and set the widget accordinaly
        setMyText(_preferences.getPreference(_preferenceProperty));

        // Register the object as its handler of any modification of its widget        
        addDocumentListener(this);

        // Register the object as the observer of any property value change
        _preferences.addObserver(this);
    }

    /**
     * Return one shared instance associated to the preference property name.
     *
     * @param preferences the preference that lists every entries
     * @param preferenceProperty the preference name
     * @param autosave Tells if preference must be saved automatically or not
     * (default)
     *
     * @return the PreferencedDocument singleton
     */
    public static PreferencedDocument getInstance(final Preferences preferences,
                                                  final String preferenceProperty, final boolean autosave) {

        PreferencedDocument d = _instanceMap.get(preferenceProperty);

        if (d == null) {
            d = new PreferencedDocument(preferences, preferenceProperty, autosave);
            _instanceMap.put(preferenceProperty, d);
        }

        return d;
    }

    /**
     * Return one shared instance associated to the preference property name
     * which preference is not saved automatically (autosave = false)
     *
     * @param preferences the preference that lists every entries
     * @param preferenceProperty the preference name
     * @return the PreferencedDocument singleton
     */
    public static PreferencedDocument getInstance(final Preferences preferences,
                                                  final String preferenceProperty) {
        return getInstance(preferences, preferenceProperty, false);
    }

    /**
     * Get the widget content.
     *
     * @return the widget content.
     */
    public String getMyText() {
        String content = "Error";
        try {
            content = this.getText(0, getLength());
        } catch (BadLocationException ex) {
            throw new IllegalStateException("Can't read data for preference " + _preferenceProperty, ex);
        }

        return content;
    }

    /**
     * Change the value of the widget.
     *
     * @param newValue new value to be written into the widget.
     */
    public void setMyText(final String newValue) {
        _logger.debug("setting new content to: {}", newValue);
        try {
            replace(0, getLength(), newValue, null);
        } catch (BadLocationException ex) {
            throw new IllegalStateException("Can't set value with preference " + _preferenceProperty);
        }
    }

    /**
     * Set new preference value.
     *
     * @param newValue new string value.
     */
    private void setPrefValue(final String newValue) {
        try {
            _preferences.setPreference(_preferenceProperty, newValue);
            if (_autoSaveTimer != null) {
                _autoSaveTimer.restart();
            }
        } catch (PreferencesException ex) {
            throw new IllegalStateException("Can't set value for preference " + _preferenceProperty);
        }
    }

    /**
     * Handle event.
     *
     * @param evt document event.
     */
    @Override
    public void changedUpdate(final DocumentEvent evt) {
        // this event is not used
        if (_logger.isTraceEnabled()) {
            _logger.trace("changeUpdate:\n event: {}\n text: {}", evt, getMyText());
        }
    }

    /**
     * Handle event.
     *
     * @param evt document event.
     */
    @Override
    public void insertUpdate(final DocumentEvent evt) {
        // Gives notification that there was an insert into the document.
        if (_logger.isTraceEnabled()) {
            _logger.trace("insertUpdate:\n event: {}\n text: {}", evt, getMyText());
        }
        setPrefValue(getMyText());
    }

    /**
     * Handle event.
     *
     * @param evt document event.
     */
    @Override
    public void removeUpdate(final DocumentEvent evt) {
        // Gives notification that a portion of the document has been removed.    
        if (_logger.isTraceEnabled()) {
            _logger.trace("removeUpdate:\n event: {}\n text: {}", evt, getMyText());
        }
        setPrefValue(getMyText());
    }

    /**
     * Triggered if the preference shared instance has been modified.
     *
     * @param o the Observable object
     * @param arg parameter
     */
    @Override
    public void update(final Observable o, final Object arg) {
        String currentValue = getMyText();
        // Update the widget view according property value changed if value changed
        final String nextValue = _preferences.getPreference(_preferenceProperty);
        if (currentValue.equals(nextValue)) {
            return;
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("Setting '{}' from {} to {}", _preferenceProperty, currentValue, nextValue);
        }
        setMyText(nextValue);
    }
}
/*
 * ___oOo___
 */
