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

import fr.jmmc.jmcs.util.PropertyUtils;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * Use this class to define new Actions that get data from resource file.
 * 
 * @author Sylvain LAFRASSE, Laurent BOURGES.
 */
public abstract class ResourcedAction extends AbstractAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /**
     * This constructor use the resource file to get text description and icon of action.
     * @param actionName name of the action as declared in the resource file
     */
    public ResourcedAction(final String actionName) {

        // Collect action info
        String text = PropertyUtils.getActionText(actionName);
        String desc = PropertyUtils.getActionDescription(actionName);
        ImageIcon icon = PropertyUtils.getActionIcon(actionName);
        KeyStroke accelerator = PropertyUtils.getActionAccelerator(actionName);

        // Init action    
        if (text != null) {
            putValue(Action.NAME, text);
        }

        if (desc != null) {
            putValue(Action.SHORT_DESCRIPTION, desc);
        }

        if (icon != null) {
            putValue(Action.SMALL_ICON, icon);
        }

        if (accelerator != null) {
            putValue(Action.ACCELERATOR_KEY, accelerator);
        }
    }
}
/*___oOo___*/
