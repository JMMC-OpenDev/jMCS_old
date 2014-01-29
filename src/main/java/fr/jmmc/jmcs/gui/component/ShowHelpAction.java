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

import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.gui.FeedbackReport;
import fr.jmmc.jmcs.gui.HelpView;
import fr.jmmc.jmcs.gui.util.ResourceImage;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

/**
 * Show the help item given a button label
 * 
 * @author Guillaume MELLA, Sylvain LAFRASSE, Laurent BOURGES.
 */
public class ShowHelpAction extends AbstractAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Help id associated to the given label.*/
    private String _helpID;
    /** flag to display only the first missing do in production */
    private static boolean _alreadyShown = false;

    /**
     * Constructor of the action that will show the help view on the page associated to the given label.
     * The label is used to retrieve one target from the documentationTOC.xml file.
     *
     * @param label the key used to retrieve the documentation page.
     */
    public ShowHelpAction(String label) {
        // Set Icon (without additional label)
        final ImageIcon helpIcon = ResourceImage.HELP_ICON.icon();
        this.putValue(SMALL_ICON, helpIcon);

        // If help is available, then try to get the HelpID that ends with given label
        boolean helpIsAvailable = HelpView.isAvailable();
        setEnabled(helpIsAvailable);

        if (helpIsAvailable) {
            _helpID = HelpView.getHelpID(label);

            // If no helpID found, then show one feedback report and disable action
            if (_helpID == null && (!_alreadyShown || ApplicationDescription.isBetaVersion())) {
                if (ApplicationDescription.isBetaVersion()) {
                    // Show the feedback report :
                    FeedbackReport.openDialog(new Exception(
                            "Documentation problem:\nNo helpID found for label '"
                            + label
                            + "'\nWe are working on this problem to solve it."));
                } else {
                    MessagePane.showErrorMessage(
                            "Sorry, documentation not found. This case often "
                            + "occurs \n in java 1.5 version and Java Web Start applications.",
                            "Documentation problem");

                }
                setEnabled(false);
                _alreadyShown = true;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        HelpView.show(_helpID);
    }
}
