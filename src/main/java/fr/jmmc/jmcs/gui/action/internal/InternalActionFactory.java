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
package fr.jmmc.jmcs.gui.action.internal;

import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.gui.AboutBox;
import fr.jmmc.jmcs.gui.DependenciesView;
import fr.jmmc.jmcs.gui.FeedbackReport;
import fr.jmmc.jmcs.gui.HelpView;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.action.ShowReleaseNotesAction;
import fr.jmmc.jmcs.gui.component.ResizableTextViewFactory;
import fr.jmmc.jmcs.logging.LogbackGui;
import fr.jmmc.jmcs.gui.util.ResourceImage;
import fr.jmmc.jmcs.gui.util.WindowUtils;
import fr.jmmc.jmcs.service.BrowserLauncher;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initiate all actions needed by jMCS.
 * @author Sylvain LAFRASSE.
 */
public final class InternalActionFactory {

    /** Class path */
    private static final String CLASS_PATH = InternalActionFactory.class.getName();
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(CLASS_PATH);
    /** Singleton instance */
    private static InternalActionFactory _instance = null;
    // Members
    /** Acknowledgment handling action */
    final Action _showAcknowledgmentAction;
    /** Show About... box action */
    final Action _showAboutBoxAction;
    /** Show Feedback Report action */
    final Action _showFeedbackReportAction;
    /** Show help handling action */
    final Action _showHelpAction;
    /** Show hot news handling action */
    final Action _showHotNewsAction;
    /** Show release handling action */
    final Action _showReleaseNotesAction;
    /** Show FAQ handling action */
    final Action _showFaqAction;
    /** Show Dependencies action */
    final Action _showDependenciesAction;
    /** Show log GUI action */
    final Action _showLogGuiAction;
    /** default Open handling action */
    final Action _defaultOpenAction;
    /** Quit handling action */
    final Action _quitAction;

    /** Hidden constructor */
    private InternalActionFactory() {
        _showAcknowledgmentAction = new ShowAcknowledgmentAction(CLASS_PATH, "_showAcknowledgmentAction");
        _showAboutBoxAction = new ShowAboutBoxAction(CLASS_PATH, "_showAboutBoxsAction");
        _showFeedbackReportAction = new ShowFeedbackReportAction(CLASS_PATH, "_showFeedbackReportAction");
        _showHotNewsAction = new ShowHotNewsAction(CLASS_PATH, "_showHotNewsAction");
        _showReleaseNotesAction = new ShowReleaseNotesAction("_showReleaseNotesAction");
        _showFaqAction = new ShowFaqAction(CLASS_PATH, "_showFaqAction");
        _showHelpAction = new ShowHelpAction(CLASS_PATH, "_showHelpAction");
        _showDependenciesAction = new ShowDependenciesAction(CLASS_PATH, "_showDependenciesAction");
        _showLogGuiAction = new ShowLogGuiAction(CLASS_PATH, "_showLogGuiAction");
        _defaultOpenAction = new DefaultOpenAction(CLASS_PATH, "_defaultOpenAction");
        _quitAction = new QuitAction(CLASS_PATH, "_quitAction");
    }

    /** @return the singleton instance */
    private static synchronized InternalActionFactory getInstance() {
        // DO NOT MODIFY !!!
        if (_instance == null) {
            _instance = new InternalActionFactory();
        }
        return _instance;
        // DO NOT MODIFY !!!
    }

    /**
     * Create all internal actions.
     */
    public static void populate() {
        getInstance();
    }

    /**
     * Creates the action which open the About Box window.
     * @return action which open the about box window
     */
    public static Action showAboutBoxAction() {
        return getInstance()._showAboutBoxAction;
    }

    /**
     * Return the action which displays and copy acknowledgment to clipboard.
     * @return action which displays and copy acknowledgment to clipboard
     */
    public static Action showAcknowledgmentAction() {
        return getInstance()._showAcknowledgmentAction;
    }

    /**
     * Creates the feedback action which open the Feedback window.
     * @return feedback action which open the feedback window
     */
    public static Action showFeedbackReportAction() {
        return getInstance()._showFeedbackReportAction;
    }

    /**
     * Return the action which tries to display user help.
     * @return action which tries to display the help
     */
    public static Action showHelpAction() {
        return getInstance()._showHelpAction;
    }

    /**
     * Return the action which tries to display dependencies.
     * @return action which tries to display dependencies
     */
    public static Action showDependenciesAction() {
        return getInstance()._showDependenciesAction;
    }

    /**
     * Return the action dedicated to display hot news.
     * @return action dedicated to display hot news
     */
    public static Action showHotNewsAction() {
        return getInstance()._showHotNewsAction;
    }

    /**
     * Return the action dedicated to display release notes.
     * @return action dedicated to display release
     */
    public static Action showReleaseAction() {
        return getInstance()._showReleaseNotesAction;
    }

    /**
     * Return the action dedicated to display FAQ.
     * @return action dedicated to display FAQ
     */
    public static Action showFaqAction() {
        return getInstance()._showFaqAction;
    }

    /**
     * Return the action dedicated to display log GUI.
     * @return action dedicated to display log GUI
     */
    public static Action showLogGuiAction() {
        return getInstance()._showLogGuiAction;
    }

    /**
     * Return the action which tries to quit the application.
     * @return action which tries to quit the application
     */
    public static Action quitAction() {
        return getInstance()._quitAction;
    }

    /** Action to show application About... box. */
    protected static final class ShowAboutBoxAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;
        /** Data model */
        final ApplicationDescription _applicationData;
        /** AboutBox */
        private static AboutBox _aboutBox = null;

        /**
         * Public constructor
         * @param classPath the path of the class containing the field pointing to
         * the action, in the form returned by 'getClass().getName();'.
         * @param fieldName the name of the field pointing to the action.
         */
        ShowAboutBoxAction(final String classPath, final String fieldName) {
            super(classPath, fieldName, "About...");
            _applicationData = ApplicationDescription.getInstance();
        }

        /**
         * Handle the action event
         * @param evt action event
         */
        @Override
        public void actionPerformed(final ActionEvent evt) {
            if (_aboutBox != null) {
                if (!_aboutBox.isVisible()) {
                    // Center window on main screen
                    WindowUtils.centerOnMainScreen(_aboutBox);

                    _aboutBox.setVisible(true);
                } else {
                    _aboutBox.toFront();
                }
            } else {
                _aboutBox = new AboutBox();

                // Center window on main screen
                WindowUtils.centerOnMainScreen(_aboutBox);
            }
        }
    }

    /** Action to copy acknowledgment text to the clipboard. */
    protected static final class ShowAcknowledgmentAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;
        /** Data model */
        final ApplicationDescription _applicationData;
        /** Acknowledgment content */
        private String _acknowledgement = null;

        /**
         * Public constructor
         * @param classPath the path of the class containing the field pointing to
         * the action, in the form returned by 'getClass().getName();'.
         * @param fieldName the name of the field pointing to the action.
         */
        ShowAcknowledgmentAction(final String classPath, final String fieldName) {

            super(classPath, fieldName, "Copy Acknowledgement to Clipboard");
            _applicationData = ApplicationDescription.getInstance();
            _acknowledgement = _applicationData.getAcknowledgment();
            // If the application does not provide an acknowledgement
            if (_acknowledgement == null) {
                // Generate one instead
                final String compagny = _applicationData.getLegalCompanyName();
                final String appName = _applicationData.getProgramName();
                final String appURL = _applicationData.getLinkValue();
                _acknowledgement = "This research has made use of the " + compagny
                        + "\\texttt{" + appName
                        + "} service\n\\footnote{Available at " + appURL + "}";
            }
        }

        /**
         * Handle the action event
         * @param evt action event
         */
        @Override
        public void actionPerformed(final ActionEvent evt) {
            StringSelection ss = new StringSelection(_acknowledgement);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);

            final String delimiter = "---------------------------------------------------------------------------\n";
            final String message = "The previous message has already been copied to your clipboard, in order to\n"
                    + "let you conveniently paste it in your related publication.";
            final String windowTitle = _applicationData.getProgramName()
                    + " Acknowledgment Note";
            final String windowContent = delimiter + _acknowledgement + "\n"
                    + delimiter + "\n" + message;

            ResizableTextViewFactory.createTextWindow(windowContent, windowTitle, enabled);
        }
    }

    /** Action to show hot news RSS feed. */
    protected static final class ShowFeedbackReportAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;
        /** Data model */
        final ApplicationDescription _applicationData;

        /**
         * Public constructor
         * @param classPath the path of the class containing the field pointing to
         * the action, in the form returned by 'getClass().getName();'.
         * @param fieldName the name of the field pointing to the action.
         */
        ShowFeedbackReportAction(final String classPath, final String fieldName) {
            super(classPath, fieldName, "Report Feedback to " + ApplicationDescription.getInstance().getShortCompanyName() + "...");
            _applicationData = ApplicationDescription.getInstance();
        }

        /**
         * Handle the action event
         * @param evt action event
         */
        @Override
        public void actionPerformed(final ActionEvent evt) {
            if (_applicationData != null) {
                // Show the feedback report :
                FeedbackReport.openDialog();
            }
        }
    }

    /** Action to show hot news RSS feed. */
    protected static final class ShowHotNewsAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Public constructor
         * @param classPath the path of the class containing the field pointing to
         * the action, in the form returned by 'getClass().getName();'.
         * @param fieldName the name of the field pointing to the action.
         */
        ShowHotNewsAction(final String classPath, final String fieldName) {
            super(classPath, fieldName, "Hot News (RSS Feed)");
        }

        /**
         * Handle the action event
         * @param evt action event
         */
        @Override
        public void actionPerformed(final ActionEvent evt) {
            BrowserLauncher.openURL(ApplicationDescription.getInstance().getHotNewsRSSFeedLinkValue());
        }
    }

    /** Action to show FAQ. */
    protected static final class ShowFaqAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Public constructor
         * @param classPath the path of the class containing the field pointing to
         * the action, in the form returned by 'getClass().getName();'.
         * @param fieldName the name of the field pointing to the action.
         */
        ShowFaqAction(final String classPath, final String fieldName) {
            super(classPath, fieldName, "Frequently Asked Questions");
        }

        /**
         * Handle the action event
         * @param evt action event
         */
        @Override
        public void actionPerformed(final ActionEvent evt) {
            BrowserLauncher.openURL(ApplicationDescription.getInstance().getFaqLinkValue());
        }
    }

    /** Action to show dependencies. */
    protected static final class ShowDependenciesAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Public constructor
         * @param classPath the path of the class containing the field pointing to
         * the action, in the form returned by 'getClass().getName();'.
         * @param fieldName the name of the field pointing to the action.
         */
        ShowDependenciesAction(final String classPath, final String fieldName) {
            super(classPath, fieldName, "jMCS Dependencies Copyrights");
        }

        /**
         * Handle the action event
         * @param evt action event
         */
        @Override
        public void actionPerformed(final ActionEvent evt) {
            DependenciesView.display();
        }
    }

    /** Action to show help. */
    protected static final class ShowHelpAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;
        private String _documentationURL = null;

        /**
         * Public constructor
         * @param classPath the path of the class containing the field pointing to
         * the action, in the form returned by 'getClass().getName();'.
         * @param fieldName the name of the field pointing to the action.
         */
        ShowHelpAction(final String classPath, final String fieldName) {
            super(classPath, fieldName, "User Manual");

            // Set Icon only if not under Mac OS X
            if (!SystemUtils.IS_OS_MAC_OSX) {
                final ImageIcon helpIcon = ResourceImage.HELP_ICON.icon();
                putValue(SMALL_ICON, helpIcon);
            }

            // If Documentation web page URL not provided
            _documentationURL = ApplicationDescription.getInstance().getDocumentationLinkValue();
            if (_documentationURL == null) {
                // Try embedded HelpSet instead
                setEnabled(HelpView.isAvailable());
            }
        }

        /**
         * Handle the action event
         * @param evt action event
         */
        @Override
        public void actionPerformed(final ActionEvent evt) {
            if (_documentationURL != null) {
                BrowserLauncher.openURL(_documentationURL);
            } else {
                HelpView.setVisible(true);
            }
        }
    }

    /** Action to show log GUI. */
    protected static final class ShowLogGuiAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Public constructor
         * @param classPath the path of the class containing the field pointing to
         * the action, in the form returned by 'getClass().getName();'.
         * @param fieldName the name of the field pointing to the action.
         */
        ShowLogGuiAction(final String classPath, final String fieldName) {
            super(classPath, fieldName, "Show Log Console");
        }

        /**
         * Handle the action event
         * @param evt action event
         */
        @Override
        public void actionPerformed(final ActionEvent evt) {
            LogbackGui.showLogConsole();
        }
    }

    /** Action to correctly handle file opening. */
    protected static final class DefaultOpenAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Public constructor
         * @param classPath the path of the class containing the field pointing to
         * the action, in the form returned by 'getClass().getName();'.
         * @param fieldName the name of the field pointing to the action.
         */
        DefaultOpenAction(final String classPath, final String fieldName) {
            super(classPath, fieldName);

            // Disabled as this default implementation does nothing
            setEnabled(false);

            flagAsOpenAction();
        }

        /**
         * Handle the action event
         * @param evt action event
         */
        @Override
        public void actionPerformed(final ActionEvent evt) {
            _logger.warn("No handler for default file opening.");
        }
    }

    /** Action to correctly handle operations before closing application. */
    protected static final class QuitAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Public constructor
         * @param classPath the path of the class containing the field pointing to
         * the action, in the form returned by 'getClass().getName();'.
         * @param fieldName the name of the field pointing to the action.
         */
        QuitAction(final String classPath, final String fieldName) {
            super(classPath, fieldName, "Quit", "ctrl Q");

            flagAsQuitAction();
        }

        /**
         * Handle the action event
         * @param evt action event
         */
        @Override
        public void actionPerformed(final ActionEvent evt) {
            _logger.debug("Application is about to die, should we proceed ?");

            Bootstrapper.quitApp(evt);
        }
    }
}
