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

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.data.preference.Preferences;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import fr.jmmc.jmcs.gui.FeedbackReport;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

/**
 * Provides a custom message pane with a check box to hide that kind of message definitely.
 * 
 * @author Guillaume MELLA, Laurent BOURGES.
 */
public final class DismissableMessagePane {

    private static String composePreferenceName(final String preferenceName) {
        final String dontShowPreferenceName = "MCSGUI.DismissableMessagePane." + preferenceName + ".dontShow";
        return dontShowPreferenceName;
    }

    /**
     * Forbidden constructor
     */
    private DismissableMessagePane() {
        super();
    }

    /**
     * Show a message dialog until the user choose to hide this kind
     * of message permanently.
     *
     * @param message Message to display
     * @param preferences Reference to the dedicated Preferences singleton
     * @param preferenceName Name of the preference related to this message
     */
    public static void show(final String message, final Preferences preferences, final String preferenceName) {

        final String dontShowPreferenceName = composePreferenceName(preferenceName);
        try {
            // return false if the preference value is missing:
            boolean dontShow = preferences.getPreferenceAsBoolean(dontShowPreferenceName, true);

            if (!dontShow) {
                final JCheckBox checkbox = new JCheckBox("Do not show this message again.");
                final Object[] params = {message, checkbox};

                JOptionPane.showMessageDialog(App.getFrame(), params);

                dontShow = checkbox.isSelected();

                if (dontShow) {
                    preferences.setPreference(dontShowPreferenceName, dontShow);
                }
            }
        } catch (PreferencesException pe) {
            // Show the feedback report :
            FeedbackReport.openDialog(pe);
        }
    }

    /**
     * @return the state of the preference for the given preference name.
     * @param preferences Reference to the dedicated Preferences singleton
     * @param preferenceName Name of the preference related to this message
     */
    public static boolean getPreferenceState(final Preferences preferences, final String preferenceName) {

        // Return false if the preference value is missing:
        final String dontShowPreferenceName = composePreferenceName(preferenceName);
        final boolean dontShow = preferences.getPreferenceAsBoolean(dontShowPreferenceName, true);
        return dontShow;
    }

    /**
     * Set the state of the preference for the given preference name.
     *
     * @param preferences Reference to the dedicated Preferences singleton.
     * @param preferenceName Name of the preference related to this message.
     * @param state true to dismiss the message, false otherwise.
     */
    public static void setPreferenceState(final Preferences preferences, final String preferenceName, final boolean state) {

        // Return false if the preference value is missing:
        final String dontShowPreferenceName = composePreferenceName(preferenceName);
        try {
            preferences.setPreference(dontShowPreferenceName, state);
        } catch (PreferencesException pe) {
            // Show the feedback report :
            FeedbackReport.openDialog(pe);
        }
    }
}
