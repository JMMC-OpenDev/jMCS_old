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
package fr.jmmc.jmcs.gui.util;

import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.utils.ThreadCheckingRepaintManager;
import fr.jmmc.jmcs.util.MCSExceptionHandler;
import java.util.Locale;
import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.ToolTipManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gather SWING related properties settings for our applications.
 *
 * This code is called during Bootstrapper initialization (always performed before any application code).
 *
 * @author Laurent BOURGES, Guillaume MELLA.
 */
public final class SwingSettings {

    /** logger */
    private final static Logger _logger = LoggerFactory.getLogger(SwingSettings.class.getName());
    /** enable/disable EDT violation detection */
    private final static boolean DEBUG_EDT_VIOLATIONS = false;
    /** flag to prevent multiple code execution */
    private static boolean _alreadyDone = false;

    /** Hidden constructor */
    private SwingSettings() {
    }

    /**
     * Initialize maximum of things to get uniform application running inn the scientific context.
     * Initialization are done only on the first call of this method (which should be from a main method)
     */
    public static void setup() {
        // avoid reentrance:
        if (_alreadyDone) {
            return;
        }
        _alreadyDone = true;

        installJideLAFExtensions();
        setSwingDefaults();

        // Install exception handlers :
        MCSExceptionHandler.installSwingHandler();

        _logger.info("Swing settings set.");
    }

    /**
     * Change locale of SWING and ToolTip related.
     */
    private static void setSwingDefaults() {
        // Force Locale for Swing Components :
        JComponent.setDefaultLocale(Locale.getDefault());

        _logger.debug("Set Locale.US for JComponents");

        // Let the tooltip stay longer (60s) :
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(60000);

        _logger.debug("Make tooltips appear more quickly and stay longer");

        if (DEBUG_EDT_VIOLATIONS) {
            RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());
        }
    }

    /**
     * Install JIDE Look And Feel extensions.
     * TODO: it has side-effects on date spinner ... maybe enable it only for applications requiring it (System property) ?
     */
    public static void installJideLAFExtensions() {
        // To ensure the use of TriStateCheckBoxes in the Jide CheckBoxTree
        SwingUtils.invokeAndWaitEDT(new Runnable() {
            @Override
            public void run() {
                // Install JIDE extensions (Swing workaround):
                LookAndFeelFactory.installJideExtension();
            }
        });
    }
}
