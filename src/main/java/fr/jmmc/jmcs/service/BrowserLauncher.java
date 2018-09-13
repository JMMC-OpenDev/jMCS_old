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
package fr.jmmc.jmcs.service;

import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import edu.stanford.ejalbert.exceptionhandler.BrowserLauncherErrorHandler;
import static edu.stanford.ejalbert.BrowserLauncher.BROWSER_SYSTEM_PROPERTY;
import edu.stanford.ejalbert.browserprefui.BrowserPrefDialog;
import edu.stanford.ejalbert.launching.IBrowserLaunching;
import fr.jmmc.jmcs.data.preference.CommonPreferences;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.gui.util.WindowUtils;
import fr.jmmc.jmcs.logging.LoggingService;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import net.sf.wraplog.AbstractLogger;
import net.sf.wraplog.Level;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a function to open a web page with the default web browser of the user system.
 *
 * It uses <b>BrowserLauncher</b>.
 * 
 * @author Brice COLUCCI, Laurent BOURGES.
 */
public final class BrowserLauncher {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(BrowserLauncher.class.getName());
    /** debugging flag */
    private static final boolean DEBUG = false;
    /** launcher instance */
    private static edu.stanford.ejalbert.BrowserLauncher _launcher = null;

    /**
     * Return the BrowserLauncher instance
     * @return BrowserLauncher instance
     */
    static edu.stanford.ejalbert.BrowserLauncher getLauncher() {
        if (_launcher == null) {
            try {
                if (SystemUtils.IS_OS_WINDOWS && SystemUtils.JAVA_VERSION_FLOAT >= 9.0f) {
                    _logger.info("Fallback on windows OS (no registry) with java {}", SystemUtils.JAVA_VERSION_FLOAT);
                    
                    // disable registry access (module java.prefs does not "opens java.util.prefs" to unnamed module @662e5afd)
                    System.setProperty(IBrowserLaunching.WINDOWS_BROWSER_DISC_POLICY_PROPERTY, 
                            IBrowserLaunching.WINDOWS_BROWSER_DISC_POLICY_DISK);
                    
                    // or force using awt.Desktop: BrowserLauncher.BROWSER_USE_DESKTOP
                }
                
                final LoggerAdapter loggerAdapter = new LoggerAdapter();

                _launcher = new edu.stanford.ejalbert.BrowserLauncher(loggerAdapter, loggerAdapter);

                // Refresh the system property at startup:
                refreshBrowserPref();

            } catch (UnsupportedOperatingSystemException uose) {
                _logger.warn("Cannot initialize browser launcher : ", uose);
            } catch (BrowserLaunchingInitializingException bie) {
                _logger.warn("Cannot initialize browser launcher : ", bie);
            } catch (RuntimeException re) {
                _logger.warn("Cannot initialize browser launcher : ", re);
            }
        }
        return _launcher;
    }

    /**
     * Open the given URL in the default web browser.
     *
     * @param url URL to open in web browser.
     */
    public static void openURL(final String url) {
        final edu.stanford.ejalbert.BrowserLauncher launcher = getLauncher();
        if (launcher == null) {
            _logger.warn("Cannot open '{}' in web browser", url);
        } else {
            launcher.openURLinBrowser(url);
            _logger.debug("URL '{}' opened in web browser", url);
        }
    }

    /**
     * Return the browser selector action (based on the CommonPreferences.WEB_BROWSER preference)
     * @param parent the parent component displaying the action (used to lookup Window owning the JDialog)
     * @return browser selector action
     */
    public static Action getBrowserSelectorAction(final JComponent parent) {
        return new BrowserSelectorAction(parent);
    }

    static void refreshBrowserPref() {
        String prefBrowser = CommonPreferences.getInstance().getPreference(CommonPreferences.WEB_BROWSER);
        _logger.debug("Browser from preference: {}", prefBrowser);

        // Set system property before opening the browser dialog:
        System.setProperty(BROWSER_SYSTEM_PROPERTY, prefBrowser);
    }

    /**
     * Private constructor
     */
    private BrowserLauncher() {
        super();
    }

    /**
     * This action allows the user to select its web browser.
     * @author Laurent BOURGES.
     */
    final static class BrowserSelectorAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;
        /** Class name. This name is used to register to the ActionRegistrar */
        public final static String _className = BrowserSelectorAction.class.getName();
        // Members:
        private final JComponent parent;

        BrowserSelectorAction(final JComponent parent) {
            super(_className, "BrowserSelectorAction", "Browser selector");
            this.parent = parent;

            final edu.stanford.ejalbert.BrowserLauncher launcher = getLauncher();
            if (launcher == null) {
                _logger.warn("Cannot get BrowserLauncher");
                setEnabled(false);
            } else {
                setEnabled(launcher.getBrowserList().size() > 1);
            }
        }

        /**
         * Handle the action event
         * @param evt action event
         */
        @Override
        public void actionPerformed(final ActionEvent evt) {
            // Refresh the system property before opening the browser dialog:
            refreshBrowserPref();

            // Get the parent frame:
            final JFrame frame = SwingUtils.getParentFrame(parent);
            // Create the browser dialog
            final BrowserPrefDialog dialog = new BrowserPrefDialog(frame, getLauncher());

            // Size the dialog.
            final Dimension dim = new Dimension(400, 250);
            dialog.setMinimumSize(dim);

            WindowUtils.setClosingKeyboardShortcuts(dialog);
            dialog.pack();

            // Center it :
            dialog.setLocationRelativeTo(dialog.getOwner());

            // Show it and waits until dialog is not visible or disposed:
            dialog.setVisible(true);

            // Get the selected browser:
            final String prefBrowser = dialog.getSelectedBrowser();

            if (prefBrowser != null) {
                _logger.debug("Browser selected: {}", prefBrowser);

                // Update the selected browser:
                System.setProperty(BROWSER_SYSTEM_PROPERTY, prefBrowser);
                try {
                    CommonPreferences.getInstance().setPreference(CommonPreferences.WEB_BROWSER, prefBrowser);
                } catch (PreferencesException pe) {
                    throw new RuntimeException(pe);
                }
            }
        }
    }

    protected final static class LoggerAdapter extends AbstractLogger implements BrowserLauncherErrorHandler {

        /** Logger */
        private static final Logger _log = LoggerFactory.getLogger(edu.stanford.ejalbert.BrowserLauncher.class.getName());

        LoggerAdapter() {
            ch.qos.logback.classic.Level logLevel = (DEBUG)
                    ? ch.qos.logback.classic.Level.DEBUG
                    : ch.qos.logback.classic.Level.WARN;

            LoggingService.setLoggerLevel(_log, logLevel);
        }

        @Override
        public boolean isEnabled(int logLevel) {
            switch (logLevel) {
                case Level.DEBUG:
                    return _log.isDebugEnabled();
                case Level.INFO:
                    return _log.isInfoEnabled();
                case Level.WARN:
                    return _log.isWarnEnabled();
                case Level.ERROR:
                    return _log.isErrorEnabled();
                default:
            }
            return true;
        }

        /**
         * Logs a message and optional error details.
         *
         * @param logLevel one of: Level.DEBUG, Level.INFO, Level.WARN,
         *   Level.ERROR
         * @param message the actual message; this will never be
         *   <code>null</code>
         * @param error an error that is related to the message; unless
         *   <code>null</code>, the name and stack trace of the error are logged
         */
        @Override
        protected void reallyLog(int logLevel, String message, Throwable error) {
            if (message == null) {
                message = "";
            }

            if (error != null) {
                switch (logLevel) {
                    case Level.DEBUG:
                        _log.debug(message, error);
                        return;
                    case Level.INFO:
                        _log.info(message, error);
                        return;
                    case Level.WARN:
                        _log.warn(message, error);
                        return;
                    default:
                    case Level.ERROR:
                        _log.error(message, error);
                        return;
                }
            }
            switch (logLevel) {
                case Level.DEBUG:
                    _log.debug(message);
                    return;
                case Level.INFO:
                    _log.info(message);
                    return;
                case Level.WARN:
                    _log.warn(message);
                    return;
                default:
                case Level.ERROR:
                    _log.error(message);
            }
        }

        @Override
        public void handleException(Exception e) {
            _log.error("BrowserLauncher error: ", e);
        }
    }
}
