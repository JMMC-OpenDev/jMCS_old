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
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.logging.LogbackGui;
import fr.jmmc.jmcs.logging.LoggingService;
import fr.jmmc.jmcs.gui.util.ResourceImage;
import fr.jmmc.jmcs.service.BrowserLauncher;
import fr.jmmc.jmcs.util.ImageUtils;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.WeakReference;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;

/**
 * A status bar that can be shared all along an application.
 * 
 * @author Sylvain LAFRASSE, Samuel PRETTE, Guillaume MELLA, Laurent BOURGES.
 */
public final class StatusBar extends JPanel {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger _statusLogger = LoggingService.getInstance().getLogger(LoggingService.JMMC_STATUS_LOG);
    /** weak reference on the StatusBar singleton instance */
    private static volatile WeakReference<StatusBar> _weakSingleton = null;
    /* members */
    /** Status label */
    private final JLabel _statusLabel = new JLabel();

    /**
     * Return the StatusBar singleton instance or create a new one
     * @return StatusBar singleton instance
     */
    public static StatusBar getInstance() {
        return getInstance(true);
    }

    /**
     * Return the existing StatusBar singleton instance
     * @return StatusBar singleton instance or null if none defined
     */
    public static StatusBar getExistingInstance() {
        return getInstance(false);
    }

    /**
     * Set the status bar text if created.
     *
     * @param message the message to be displayed by the status bar.
     */
    public static void show(final String message) {
        final StatusBar instance = getExistingInstance();
        if (instance != null) {
            SwingUtils.invokeEDT(new Runnable() {
                /**
                 * Update the status bar using EDT
                 */
                @Override
                public void run() {
                    instance.setStatusLabel(message);
                }
            });
        }
    }

    /**
     * Set the status bar text if the current message equals the given previous message (ignore case)
     *
     * @param previous the previous message to override
     * @param message the message to be displayed by the status bar.
     */
    public static void showIfPrevious(final String previous, final String message) {
        final StatusBar instance = getExistingInstance();
        if (instance != null) {
            SwingUtils.invokeEDT(new Runnable() {
                /**
                 * Update the status bar using EDT
                 */
                @Override
                public void run() {
                    final String lastStatus = instance.getStatusLabel();
                    if (lastStatus != null && lastStatus.equalsIgnoreCase(previous)) {
                        instance.setStatusLabel(message);
                    }
                }
            });
        }
    }

    private static synchronized StatusBar getInstance(final boolean doCreate) {
        StatusBar instance = (_weakSingleton != null) ? _weakSingleton.get() : null;
        if ((instance == null) && doCreate) {
            // Check EDT
            if (!SwingUtils.isEDT()) {
                throw new IllegalStateException("StatusBar must be created using EDT !");
            }
            instance = new StatusBar();
            _weakSingleton = new WeakReference<StatusBar>(instance);
        }
        return instance;
    }

    /**
     * Private Constructor.
     *
     * Should be called at least once in order to allow usage.
     */
    private StatusBar() {
        super();

        // Layed out horizontally
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        final Border leftBorder = new EmptyBorder(0, 0, 4, 0);

        // Create logo
        final String logoURL = ApplicationDescription.getInstance().getCompanyLogoResourcePath();
        final ImageIcon imageIcon = ImageUtils.loadResourceIcon(logoURL);
        final ImageIcon scaledImageIcon = ImageUtils.getScaledImageIcon(imageIcon, 17, 0);
        final JLabel logo = new JLabel();
        logo.setIcon(scaledImageIcon);
        logo.setVisible(true);
        logo.setBorder(leftBorder);

        // Create text logo
        final JLabel textStatusBar = new JLabel();
        textStatusBar.setText("Provided by ");
        textStatusBar.setFont(new Font("Comic Sans MS", 2, 10));
        textStatusBar.setVisible(true);
        textStatusBar.setBorder(leftBorder);

        // Create status history button
        final ImageIcon historyIcon = ResourceImage.STATUS_HISTORY.icon();
        final JButton historyButton = new JButton(historyIcon);
        final Border historyBorder = new EmptyBorder(0, 4, 4, 0);
        historyButton.setBorder(historyBorder);
        historyButton.setToolTipText("Click to view status history");
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LogbackGui.showLogConsoleForLogger(LoggingService.JMMC_STATUS_LOG);
            }
        });

        // StatusBar elements placement
        final Box hBox = Box.createHorizontalBox();
        hBox.add(historyButton);
        hBox.add(new JLabel(" Status : "));
        hBox.add(_statusLabel);
        hBox.add(Box.createHorizontalGlue());
        hBox.add(textStatusBar);
        hBox.add(logo);

        /*
         * Add a space on the right bottom angle because Mac OS X corner is
         * already decored with its resize handle
         */
        if (SystemUtils.IS_OS_MAC_OSX) {
            hBox.add(Box.createHorizontalStrut(14));
        }

        add(hBox);

        // Get application data model to launch the default browser with the given link
        final ApplicationDescription applicationDataModel = ApplicationDescription.getInstance();
        if (applicationDataModel != null) {
            logo.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    BrowserLauncher.openURL(applicationDataModel.getLinkValue());
                }
            });
        }
    }

    /**
     * Change the content of the status bar
     * @param message message to display
     */
    private void setStatusLabel(final String message) {
        _statusLabel.setText(message);

        // use status log:
        _statusLogger.info(message);
    }

    /**
     * Return the content of the status bar 
     * Note: Must be called by EDT
     * @return content of the status bar 
     */
    private String getStatusLabel() {
        return _statusLabel.getText();
    }
}
/*___oOo___*/
