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
package com.yourcompany.example;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.DismissableMessagePane;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.gui.util.WindowUtils;
import fr.jmmc.jmcs.network.interop.SampCapability;
import fr.jmmc.jmcs.network.interop.SampCapabilityAction;
import fr.jmmc.jmcs.network.interop.SampMessageHandler;
import fr.jmmc.jmcs.service.BrowserLauncher;
import fr.jmmc.jmcs.service.RecentFilesManager;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.astrogrid.samp.Message;
import org.astrogrid.samp.SampUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class of your application.
 */
public class Main extends App {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(Main.class.getName());
    /** Button to open jMCS web site in the default web browser */
    private JButton _openBrowserButton = null;
    /** Actions class */
    public RegisteredAction _openAction;
    /** Test button */
    private JButton _testDismissableMessagePane = null;
    private Actions _actions = null;

    /**
     * Constructor.
     * @param args
     */
    public Main(String[] args) {
        super(args);
    }

    /** Initialize application services */
    @Override
    protected void initServices() {
        // Set others preferences
        try {
            Preferences.getInstance().setPreference("MAIN", "main");
        } catch (Exception ex) {
            _logger.error("Failed setting preference.", ex);
        }
    }

    /** Initialize application GUI */
    @Override
    protected void setupGui() {
        _logger.warn("Initialize application objects");

        // Create actions:
        _actions = new Actions();
        _openAction = openAction();

        // Fake Samp action:
        new SampCapabilityAction(
                "com.yourcompany.example.FakeSampCapabilityAction",
                "FakeSampCapabilityAction",
                SampCapability.LOAD_VO_TABLE) {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public Map<?, ?> composeMessage() {

                        final Map<String, String> parameters = new HashMap<String, String>(2);
                        parameters.put("url", "http://www.myexperiment.org/files/910/versions/1/download/2MASS.vot");

                        return parameters;
                    }

                };

        // Create application's frame and set its title:
        getFrame().setTitle(ApplicationDescription.getInstance().getProgramNameWithVersion());

        // Create Buttons:
        _openBrowserButton = new JButton(openJMcsWebSiteAction());
        _testDismissableMessagePane = new JButton(dismissableMessagePaneAction());

        // Define application's frame widgets:
        final Container framePanel = getFramePanel();

        // Set borderlayout
        framePanel.setLayout(new BorderLayout());

        // Add buttons to panel
        framePanel.add(_openBrowserButton, BorderLayout.NORTH);
        framePanel.add(_testDismissableMessagePane, BorderLayout.CENTER);
        framePanel.add(new StatusBar(), BorderLayout.SOUTH);
    }

    /** Execute application body */
    @Override
    protected void execute() {
        _logger.info("Execute application body");

        SwingUtils.invokeLaterEDT(new Runnable() {
            /**
             * Show the application frame using EDT
             */
            @Override
            public void run() {
                _logger.debug("Main.execute() handler called.");

                // Center main window on the screen
                WindowUtils.centerOnMainScreen(getFrame());

                // Show the application frame
                showFrameToFront();
            }
        });

        StatusBar.show("Application ready.");

        RecentFilesManager.addFile(new File("/Users/lafrasse/test.scvot"));

        try {
            new SampMessageHandler("stuff.do") {
                @Override
                public void processMessage(String senderId, Message msg) {
                    // do stuff
                    _logger.info("Received 'stuff.do' message from '" + senderId + "' : '" + msg + "'.");
                    StatusBar.show("Received 'stuff.do' SAMP message.");

                    String name = (String) msg.getParam("name");
                    Map result = new HashMap();
                    result.put("name", name);
                    result.put("x", SampUtils.encodeFloat(3.141159));
                }
            };
            new SampMessageHandler(SampCapability.LOAD_VO_TABLE) {
                @Override
                public void processMessage(String senderId, Message msg) {
                    // do stuff
                    _logger.info("Received 'LOAD_VO_TABLE' message from '" + senderId + "' : '" + msg + "'.");
                    StatusBar.show("Received 'LOAD_VO_TABLE' SAMP message.");
                }
            };
        } catch (Exception ex) {
            _logger.error("SAMP error", ex);
        }
    }

    /** Execute operations before closing application */
    @Override
    public boolean canBeTerminatedNow() {
        _logger.warn("Execute operations before closing application");

        // Quit application
        return true;
    }

    /** Application cleanup */
    @Override
    protected void cleanup() {
        _openBrowserButton = null;
        _openAction = null;
        _actions = null;
        _testDismissableMessagePane = null;
    }

    /** Open help view action */
    private Action openJMcsWebSiteAction() {
        return new AbstractAction("Open jMCS Web Site") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent evt) {
                BrowserLauncher.openURL("http://www.jmmc.fr/dev/jmcs");
                StatusBar.show("jMCS web site open.");
            }
        };
    }

    /** action */
    private Action dismissableMessagePaneAction() {
        return new AbstractAction("Show dismissable message pane") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent evt) {
                DismissableMessagePane.show(
                        "Try to show a test message\n which can be deactivated by user!!",
                        Preferences.getInstance(), "msgTest");
            }
        };
    }

    /** Open file action */
    private RegisteredAction openAction() {
        RegisteredAction temp = new RegisteredAction(Main.class.getName(), "_openAction") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent evt) {
                StatusBar.show("Open action !!!");
                MessagePane.showMessage("test !");
            }
        };
        temp.flagAsOpenAction();
        return temp;
    }

    /**
     * Main
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Bootstrapper.launchApp(new Main(args), false, true);
    }
}
/*___oOo___*/
