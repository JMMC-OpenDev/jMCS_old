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

import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.gui.util.WindowUtils;
import fr.jmmc.jmcs.util.ImageUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class opens a new splash-screen window. Informations of this window
 * have been taken from the XML file called <b>ApplicationData.xml</b>.
 * This file is saved into the application module which extends <b>App</b>
 * class. There is a default XML file which having the same name and which is
 * saved into the <b>App</b> module in order to avoid important bugs.
 *
 * To access to the XML informations, this class uses
 * <b>ApplicationDescription</b> class. It's a class which has got getters
 * in order to do that and which has been written to abstract the way
 * to access to these informations.
 * 
 * @author Brice COLUCCI, Guillaume MELLA, Sylvain LAFRASSE, Laurent BOURGES.
 */
public class SplashScreen extends JFrame {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(SplashScreen.class.getName());
    /** Singleton instance */
    private static SplashScreen _instance = null;
    // Members
    /** Splash screen has got the same model than about box */
    private final ApplicationDescription _applicationDataModel;
    /** Logo label */
    private final JLabel _logoLabel = new JLabel();
    /** Panel */
    private final JPanel _panel = new JPanel();
    /** Program name label */
    private final JLabel _programNameLabel = new JLabel();
    /** Program version label */
    private final JLabel _programVersionLabel = new JLabel();

    /**
     * Creates a new SplashScreen object.
     */
    private SplashScreen() {
        _applicationDataModel = ApplicationDescription.getInstance();
        if (_applicationDataModel != null) {

            // Draw window
            setAllProperties();
            pack();
        }
    }

    /**
     * Create the window fulfilled with all the information included in the Application data model.
     * @param shouldShowSplashScreen true to effectively show the splash screen, false otherwise.
     */
    public static void display(final boolean shouldShowSplashScreen) {

        if (!shouldShowSplashScreen) {
            return;
        }

        if (_instance == null) {
            _instance = new SplashScreen();
        }

        WindowUtils.centerOnMainScreen(_instance);

        // Show window
        _instance.setVisible(true);

        // Use Timer to wait 2,5s before closing this dialog :
        final Timer timer = new Timer(2500, new ActionListener() {
            /**
             * Handle the timer call
             * @param ae action event
             */
            @Override
            public void actionPerformed(final ActionEvent ae) {
                // Just call close to hide and dispose this frame :
                SplashScreen.close();
            }
        });

        // timer runs only once :
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Close the splash screen
     */
    public static void close() {
        if (_instance == null) {
            return;
        }

        if (_instance.isVisible()) {
            _instance.setVisible(false);
            _instance.dispose();
        }

        // cleanup (helps GC):
        _instance = null;
    }

    /**
     * Calls all "set properties" methods
     */
    private void setAllProperties() {
        setLogoLabelProperties();
        setProgramNameLabelProperties();
        setProgramVersionLabelProperties();
        setPanelProperties();
        setFrameProperties();

        _logger.debug("Every JFrame properties have been initialized");
    }

    /** Sets panel properties */
    private void setPanelProperties() {
        _panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        _panel.setLayout(new BorderLayout());
        _panel.add(_logoLabel, BorderLayout.PAGE_START);
        _panel.add(_programNameLabel, BorderLayout.CENTER);
        _panel.add(_programVersionLabel, BorderLayout.PAGE_END);

        _logger.debug("Every panel properties have been initialized");
    }

    /** Sets logo properties */
    private void setLogoLabelProperties() {

        _logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        final String companyLogoResourcePath = _applicationDataModel.getCompanyLogoResourcePath();
        final ImageIcon companyLogo = ImageUtils.loadResourceIcon(companyLogoResourcePath);
        _logoLabel.setIcon(companyLogo);
        _logoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        _logger.debug("Every logo label properties have been initialized");
    }

    /** Sets program name label properties */
    private void setProgramNameLabelProperties() {
        _programNameLabel.setFont(new Font(null, 1, 28));
        _programNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        _programNameLabel.setText(_applicationDataModel.getProgramName());

        _logger.debug("Every program name label properties have been initialized");
    }

    /** Sets program version label properties */
    private void setProgramVersionLabelProperties() {
        _programVersionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Pattern : "v{version} - {copyright}"
        _programVersionLabel.setText("Version "
                + _applicationDataModel.getProgramVersion()
                + " - " + _applicationDataModel.getCopyrightValue());
        _programVersionLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        _logger.debug("Every program version label properties have been initialized");
    }

    /** Sets frame properties */
    private void setFrameProperties() {
        getContentPane().add(_panel, BorderLayout.CENTER);

        setTitle(_applicationDataModel.getProgramName());
        setResizable(false);
        setUndecorated(true);
        setAlwaysOnTop(true);
    }
}
/*___oOo___*/
