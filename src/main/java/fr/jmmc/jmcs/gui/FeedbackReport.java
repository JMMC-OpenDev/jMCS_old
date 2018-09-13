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

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.App.ApplicationState;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.data.preference.CommonPreferences;
import fr.jmmc.jmcs.data.preference.PreferencedDocument;
import fr.jmmc.jmcs.data.preference.Preferences;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.task.JmcsTaskRegistry;
import fr.jmmc.jmcs.gui.task.TaskSwingWorker;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.gui.util.WindowUtils;
import fr.jmmc.jmcs.logging.LoggingService;
import fr.jmmc.jmcs.network.http.Http;
import fr.jmmc.jmcs.util.JVMUtils;
import fr.jmmc.jmcs.util.StringUtils;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.Timer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class opens a new feedback report window. It uses the model
 * called <b>FeedbackReportModel</b> to take the user informations,
 * the user system informations and the application logs and send all
 * using a HTTP POST request.
 *
 * @author Brice COLUCCI, Guillaume MELLA, Sylvain LAFRASSE, Laurent BOURGES.
 */
public class FeedbackReport extends javax.swing.JDialog implements KeyListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(FeedbackReport.class.getName());
    /** Feedback report type definition array */
    private static final String BUG_REPORT = "Bug Report";
    private static final String DOCUMENTATION_TYPO = "Documentation Typo";
    private static final String EVOLUTION_REQUEST = "Evolution Request";
    private static final String SUPPORT_REQUEST = "Support Request";
    private static final String[] _feedbackTypes = new String[]{BUG_REPORT, DOCUMENTATION_TYPO, EVOLUTION_REQUEST, SUPPORT_REQUEST};

    /**
     * Show a new FeedbackReport object (not modal).
     * Do not exit on close.
     */
    public static void openDialog() {
        openDialog(false, null);
    }

    /**
     * Show a new FeedbackReport object (not modal).
     * Do not exit on close.
     * @param exception exception
     */
    public static void openDialog(final Throwable exception) {
        openDialog(false, exception);
    }

    /**
     * Creates a new FeedbackReport object.
     * Do not exit on close.
     *
     * @param modal if true, this dialog is modal
     * @param exception exception
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void openDialog(final boolean modal, final Throwable exception) {
        if (Bootstrapper.isHeadless()) {
            _logger.info("[Headless] Unexpected exception:", exception);

            exit();
        } else {
            boolean shown = false;
            try {
                if (ApplicationDescription.getInstance() != null
                        && ApplicationDescription.getInstance().getFeedbackReportFormURL() != null) {
                    // Create Gui using EDT:
                    SwingUtils.invokeAndWaitEDT(new Runnable() {
                        @Override
                        public void run() {
                            // ensure window is visible (not iconified):
                            App.showFrameToFront();

                            // Display a new feedback report dialog:
                            new FeedbackReport(modal, exception).setVisible(true);
                        }
                    });

                    // feedback report is displayed:
                    shown = true;
                }
            } catch (Throwable th) {
                _logger.error("openDialog: unable to get ApplicationDescription: ", th);
            }

            if (!shown) {
                // Get logs early (fail safe):
                final String systemConfig = getSystemConfig();
                final String applicationLog = getApplicationLog();

                // If no feedback report form is available, show a standard error dialog instead...
                MessagePane.showErrorMessage("An unexpected error occured !"
                        + "\n\nPlease contact your application provider "
                        + "to submit a bug report with this information."
                        + MessagePane.getExceptionMessage(exception)
                        + "Application Log:\n" + applicationLog
                        + "\n\nSystem Config:\n" + systemConfig);
            }
        }
    }

    /* members */
    /** flag indicating if the dialog is disposed (avoid reentrance) */
    private boolean disposed = false;

    /* Swing components */
    /** The default combo box model */
    private final DefaultComboBoxModel _feedbackTypeDataModel;

    /**
     * Creates a new FeedbackReport object.
     * Do not exit on close.
     *
     * @param modal if true, this dialog is modal
     * @param exception any Throwable (Exception, RuntimeException and Error)
     */
    private FeedbackReport(final boolean modal, final Throwable exception) {
        super(App.getExistingFrame(), modal);

        final Throwable wrappedException = prepareException(exception);
        // Get logs early:
        final String systemConfig = getSystemConfig();
        // Note: must call getApplicationState() before to get the logs
        final String applicationState = getApplicationState();
        final String applicationLog = getApplicationLog();
        final String preferences = getPreferences();

        _feedbackTypeDataModel = new DefaultComboBoxModel(_feedbackTypes);

        initComponents();
        postInit(systemConfig, applicationLog, applicationState, preferences, wrappedException);

        // Force to dispose when the dialog closes :
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        _logger.debug("All feedback report properties have been set");
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     * @param systemConfig system config as text
     * @param applicationLog application log as text
     * @param applicationState application state as text
     * @param exception any Throwable (Exception, RuntimeException and Error)
     */
    private void postInit(final String systemConfig, final String applicationLog,
                          final String applicationState, final String preferences, final Throwable exception) {

        this.setMinimumSize(new Dimension(600, 600));
        this.setPreferredSize(new Dimension(600, 600));

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                // Just use dispose() as it is overriden to :
                // - exit if needed
                dispose();
            }
        });

        if (exception != null) {
            _logger.error("An exception was given to the feedback report: ", exception);

            final StringBuilder desc = new StringBuilder(1024);

            desc.append("Following exception occured:\n");
            desc.append((exception.getMessage() != null) ? exception.getMessage() : "no message");

            // try to get cause if possible
            Throwable thCause = exception.getCause();

            if (thCause != null) {
                // process all nested exceptions:
                while (thCause != null) {
                    if (thCause.getMessage() != null) {
                        desc.append("\n\nCause: ").append(thCause.getMessage());
                    }
                    thCause = thCause.getCause();
                }
            }
            desc.append("\n--\n");
            descriptionTextArea.setText(desc.toString());
            descriptionTextArea.setCaretPosition(0);

            _feedbackTypeDataModel.setSelectedItem(BUG_REPORT);
        } else {
            _feedbackTypeDataModel.setSelectedItem(EVOLUTION_REQUEST);
        }

        // Listen to key event to ensure
        // that send button is enable only if desc or summary is not null
        descriptionTextArea.addKeyListener(this);
        summaryTextField.addKeyListener(this);

        // Associate email to common preference
        emailTextField.setDocument(PreferencedDocument.getInstance(CommonPreferences.getInstance(), CommonPreferences.FEEDBACK_REPORT_USER_EMAIL, true));

        // and update ui
        keyReleased(null);

        final String headerMessage = ApplicationDescription.getInstance().getFeedbackReportHeaderMessage();
        headerLabel.setText(headerMessage);

        typeComboBox.setModel(_feedbackTypeDataModel);

        logTextArea.setText(applicationLog);
        exceptionTextArea.setText(getExceptionTrace(exception));
        systemTextArea.setText(systemConfig);
        stateTextArea.setText(applicationState);
        preferencesTextArea.setText(preferences);

        WindowUtils.setClosingKeyboardShortcuts(this);
        pack();
        WindowUtils.centerOnMainScreen(this);
    }

    /**
     * Close the dialog box if everything was correct or let the user retry.
     * This method is called by the worker using EDT.
     *
     * @param sent boolean flag indicating if the feedback report was sent
     */
    public void shouldDispose(final boolean sent) {
        loadProgressBar.setIndeterminate(false);

        if (sent) {
            _logger.info("Feedback report sent");

            loadProgressBar.setString("Thank you for your feedback.");

            // Use Timer to wait 2s before closing this dialog :
            final Timer timer = new Timer(2000, new ActionListener() {
                /**
                 * Handle the timer call
                 * @param ae action event
                 */
                @Override
                public void actionPerformed(final ActionEvent ae) {
                    // Just use dispose() as it is overriden to :
                    // - exit if needed
                    dispose();
                }
            });

            // timer runs only once :
            timer.setRepeats(false);
            timer.start();

        } else {
            MessagePane.showErrorMessage(
                    "Feedback Report message has not been sent.\nPlease check your internet connection.",
                    "Feedback Report Failed");

            submitButton.setEnabled(true);
            loadProgressBar.setString("Error during report sending.");
        }
    }

    /**
     * Free any resource or reference to this instance :
     * remove this instance from Preference Observers
     */
    @Override
    public final void dispose() {
        if (!disposed) {
            disposed = true;

            _logger.debug("dispose : {}", this);

            // do not kill the associated worker task to let the started job end properly
            // else we would have called:
            // TaskSwingWorkerExecutor.cancel(JmcsTaskRegistry.TASK_FEEDBACK_REPORT);
            // Exit or not the application
            exit();

            // dispose Frame :
            super.dispose();
        }
    }

    /**
     * Return the system config as text
     *
     * @return system config as text
     */
    private final String getSystemConfigText() {
        return systemTextArea.getText();
    }

    /**
     * Return the preferences text
     *
     * @return preferences as text
     */
    private final String getPreferencesText() {
        return preferencesTextArea.getText();
    }

    /**
     * Return the application log and state as text
     *
     * @return application log and state as text
     */
    private final String getApplicationLogAndStateText() {
        return logTextArea.getText() + "\n\nApplication State:\n" + stateTextArea.getText();
    }

    /**
     * Return the exception trace as text
     *
     * @return exception trace as text
     */
    private final String getExceptionTraceText() {
        return exceptionTextArea.getText();
    }

    /**
     * Return the mail value
     *
     * @return mail value
     */
    private final String getMail() {
        return emailTextField.getText();
    }

    /**
     * Return the description value
     *
     * @return description value
     */
    private final String getDescription() {
        return descriptionTextArea.getText();
    }

    /**
     * Return the summary value
     *
     * @return summary value
     */
    private final String getSummary() {
        return summaryTextField.getText();
    }

    /**
     * Append the given message to the description value
     * @param message to add
     * @return complete description value
     */
    public final String addDescription(final String message) {
        descriptionTextArea.append(message);
        return descriptionTextArea.getText();
    }

    /**
     * Return exception trace as a string
     *
     * @param exception any Throwable (Exception, RuntimeException and Error)
     * @return exception trace
     */
    private static final String getExceptionTrace(final Throwable exception) {
        String exceptionTrace = "No stack trace";

        // Check if the exception is not null
        if (exception != null) {
            final StringWriter stringWriter = new StringWriter(2048); // 2K buffer
            exception.printStackTrace(new PrintWriter(stringWriter));
            exceptionTrace = stringWriter.toString();
        }

        return exceptionTrace;
    }

    /** Exit the application if there was a fatal error */
    private static void exit() {
        // If the application is not ready, exit now :
        final boolean ready = (Bootstrapper.isInState(ApplicationState.APP_READY));

        _logger.debug("Application is ready : {}", ready);

        // Check if the existing application frame is visible:
        final JFrame appFrame = App.getExistingFrame();

        final boolean shouldExit = !ready || (appFrame == null) || !appFrame.isVisible();

        // Exit or not the application ?
        if (shouldExit) {
            // Exit the application
            Bootstrapper.stopApp(-1);
        }
    }

    /* Implementation of keylistener */
    @Override
    public final void keyTyped(final KeyEvent e) {
    }

    @Override
    public final void keyPressed(final KeyEvent e) {
    }

    /**
     * Enable submit button according description and summary fields.
     * @param e event thrown by description or summary updates.
     */
    @Override
    public final void keyReleased(final KeyEvent e) {
        final boolean hasDesc = !StringUtils.isEmpty(descriptionTextArea.getText());
        final boolean hasSummary = !StringUtils.isEmpty(summaryTextField.getText());
        submitButton.setEnabled(hasDesc && hasSummary);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane = new javax.swing.JTabbedPane();
        sendReportPanel = new javax.swing.JPanel();
        headerLabel = new javax.swing.JLabel();
        emailLabel = new javax.swing.JLabel();
        emailTextField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        summaryLabel = new javax.swing.JLabel();
        summaryTextField = new javax.swing.JTextField();
        descriptionScrollPane = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JTextArea();
        detailPanel = new javax.swing.JPanel();
        jTabbedPaneDetails = new javax.swing.JTabbedPane();
        logScrollPane = new javax.swing.JScrollPane();
        logTextArea = new javax.swing.JTextArea();
        exceptionScrollPane = new javax.swing.JScrollPane();
        exceptionTextArea = new javax.swing.JTextArea();
        systemScrollPane = new javax.swing.JScrollPane();
        systemTextArea = new javax.swing.JTextArea();
        statePanel = new javax.swing.JPanel();
        stateScrollPane = new javax.swing.JScrollPane();
        stateTextArea = new javax.swing.JTextArea();
        preferencesScrollPane = new javax.swing.JScrollPane();
        preferencesTextArea = new javax.swing.JTextArea();
        jPanelButtons = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        loadProgressBar = new javax.swing.JProgressBar();
        submitButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Feedback Report ");

        sendReportPanel.setLayout(new java.awt.GridBagLayout());

        headerLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        headerLabel.setText("<html>headerLabel<br> changed  by code</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        sendReportPanel.add(headerLabel, gridBagConstraints);

        emailLabel.setText("E-Mail:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        sendReportPanel.add(emailLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        sendReportPanel.add(emailTextField, gridBagConstraints);

        typeLabel.setText("Type:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        sendReportPanel.add(typeLabel, gridBagConstraints);

        typeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        sendReportPanel.add(typeComboBox, gridBagConstraints);

        summaryLabel.setText("* Summary:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        sendReportPanel.add(summaryLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        sendReportPanel.add(summaryTextField, gridBagConstraints);

        descriptionScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("* Description:"));

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setRows(5);
        descriptionScrollPane.setViewportView(descriptionTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        sendReportPanel.add(descriptionScrollPane, gridBagConstraints);

        jTabbedPane.addTab("Send report", sendReportPanel);

        detailPanel.setLayout(new java.awt.GridBagLayout());

        logTextArea.setEditable(false);
        logTextArea.setColumns(20);
        logTextArea.setRows(5);
        logScrollPane.setViewportView(logTextArea);

        jTabbedPaneDetails.addTab("Log content", logScrollPane);

        exceptionTextArea.setEditable(false);
        exceptionTextArea.setColumns(20);
        exceptionTextArea.setRows(5);
        exceptionScrollPane.setViewportView(exceptionTextArea);

        jTabbedPaneDetails.addTab("Exception message", exceptionScrollPane);

        systemTextArea.setEditable(false);
        systemTextArea.setColumns(20);
        systemTextArea.setRows(5);
        systemScrollPane.setViewportView(systemTextArea);

        jTabbedPaneDetails.addTab("System properties", systemScrollPane);

        statePanel.setLayout(new java.awt.BorderLayout());

        stateTextArea.setEditable(false);
        stateTextArea.setColumns(20);
        stateTextArea.setRows(5);
        stateScrollPane.setViewportView(stateTextArea);

        statePanel.add(stateScrollPane, java.awt.BorderLayout.CENTER);

        jTabbedPaneDetails.addTab("Application State", statePanel);

        preferencesTextArea.setEditable(false);
        preferencesTextArea.setColumns(20);
        preferencesTextArea.setRows(5);
        preferencesScrollPane.setViewportView(preferencesTextArea);

        jTabbedPaneDetails.addTab("Preferences", preferencesScrollPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        detailPanel.add(jTabbedPaneDetails, gridBagConstraints);

        jTabbedPane.addTab("Details", detailPanel);

        getContentPane().add(jTabbedPane, java.awt.BorderLayout.CENTER);

        jPanelButtons.setLayout(new java.awt.GridBagLayout());

        cancelButton.setText("Cancel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanelButtons.add(cancelButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanelButtons.add(loadProgressBar, gridBagConstraints);

        submitButton.setText("Submit");
        submitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanelButtons.add(submitButton, gridBagConstraints);

        getContentPane().add(jPanelButtons, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void submitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitButtonActionPerformed
        // update swing widgets
        loadProgressBar.setStringPainted(true);
        loadProgressBar.setIndeterminate(true);
        loadProgressBar.setString("Sending report...");
        submitButton.setEnabled(false);

        // launch a new worker
        new FeedbackReportWorker(this,
                getSystemConfigText() + getPreferencesText(),
                getApplicationLogAndStateText(),
                getExceptionTraceText(),
                (String) _feedbackTypeDataModel.getSelectedItem(),
                getMail(),
                getSummary(),
                getDescription()).executeTask();
    }//GEN-LAST:event_submitButtonActionPerformed

    /**
     * Returns system configuration
     *
     * @return sorted list of system properties
     */
    private static String getSystemConfig() {
        final StringBuilder sb = new StringBuilder(16384);
        sb.append(JVMUtils.getMemoryInfo());

        sb.append("\n\n\nSystem properties:\n");
        // Get all informations about the system running the application
        Preferences.dumpProperties(System.getProperties(), sb);

        sb.append("\n\nEnvironment settings:\n");
        Preferences.dumpProperties(System.getenv(), sb);

        return sb.toString();
    }

    /**
     * Return application state
     * @return application state
     */
    private static String getApplicationState() {
        String appState = null;

        // If the application is ready, get its state :
        if (Bootstrapper.isInState(ApplicationState.APP_READY)) {
            final App application = App.getInstance();

            if (application != null) {
                try {
                    // Get state:
                    appState = application.getStateForFeedbackReport();
                } catch (Throwable th) {
                    _logger.error("Unexpected exception when getting the application state:", th);
                }
            }
        }

        return (!StringUtils.isEmpty(appState)) ? appState : "None";
    }

    /**
     * Return application log
     * @return application log
     */
    private static String getApplicationLog() {
        // Note: must call getApplicationState() before to get the logs

        final String logOutput = LoggingService.getInstance().getLogOutput().getContent();

        _logger.debug("logOutput length = {}", logOutput.length());

        return (!StringUtils.isEmpty(logOutput)) ? logOutput : "None";
    }

    /**
     * Return application state
     * @return application state
     */
    private static String getPreferences() {
        StringBuilder sb = new StringBuilder(2048);

        // Add preferences:
        final Map<String, Preferences> registrar = Preferences.getRegistrar();

        final ArrayList<String> fileNames = new ArrayList<String>(registrar.keySet());
        Collections.sort(fileNames);

        for (String fileName : fileNames) {
            sb.append("\n\nPreferences [").append(fileName).append("]:\n");
            final Preferences prefs = registrar.get(fileName);
            if (prefs != null) {
                prefs.dumpCurrentProperties(sb);
            }
        }
        return sb.toString();
    }

    /**
     * Analyze the given exception and possibly return a wrapped exception with a customized message
     * @param th exception or null
     * @return (maybe another) exception or null
     */
    private static Throwable prepareException(final Throwable th) {
        if (th instanceof OutOfMemoryError) {
            final String warningMessage = "The application has not enough free memory to work properly. \n\n"
                    + "Please try first to download its latest release as a JAR file from the website:\n"
                    + ApplicationDescription.getInstance().getLinkValue()
                    + "\n\nStart it again using the command line:\n"
                    + "    java -Xms256m -Xmx1024m -jar [AppName-Version].jar\n\n"
                    + "To define the memory usage of the java program, your can use following options:\n"
                    + "    -Xms<size> to set initial Java heap size\n"
                    + "    -Xmx<size> to set maximum Java heap size\n"
                    + "\nIf this operation does not fix the problem, please send us a feedback report!";

            return new Throwable(warningMessage, th);
        }
        return th;
    }

    /**
     * Test the feedback Report dialog
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        openDialog();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JScrollPane descriptionScrollPane;
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JPanel detailPanel;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JTextField emailTextField;
    private javax.swing.JScrollPane exceptionScrollPane;
    private javax.swing.JTextArea exceptionTextArea;
    private javax.swing.JLabel headerLabel;
    private javax.swing.JPanel jPanelButtons;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTabbedPane jTabbedPaneDetails;
    private javax.swing.JProgressBar loadProgressBar;
    private javax.swing.JScrollPane logScrollPane;
    private javax.swing.JTextArea logTextArea;
    private javax.swing.JScrollPane preferencesScrollPane;
    private javax.swing.JTextArea preferencesTextArea;
    private javax.swing.JPanel sendReportPanel;
    private javax.swing.JPanel statePanel;
    private javax.swing.JScrollPane stateScrollPane;
    private javax.swing.JTextArea stateTextArea;
    private javax.swing.JButton submitButton;
    private javax.swing.JLabel summaryLabel;
    private javax.swing.JTextField summaryTextField;
    private javax.swing.JScrollPane systemScrollPane;
    private javax.swing.JTextArea systemTextArea;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * This worker aims to send the feedback mail in background.
     * It replaces the old FeedbackReportModel which was runnable.
     */
    private static final class FeedbackReportWorker extends TaskSwingWorker<Boolean> {

        /* members */
        /** feedback report dialog used for refreshUI callback */
        private final FeedbackReport feedbackReport;
        // Following members store the elements to send to remote scripts
        /** system config */
        private final String config;
        /** application log */
        private final String appLog;
        /** exception stack trace */
        private final String stackTrace;
        /** feedback report type */
        private final String type;
        /** user email address */
        private final String mail;
        /** report summary */
        private final String summary;
        /** user comments */
        private final String comments;

        /**
         * Hidden constructor
         *
         * @param feedbackReport feedback report dialog
         * @param config system config
         * @param log application log
         * @param stackTrace exception stack trace
         * @param type feedback report type
         * @param mail user email address
         * @param summary report summary
         * @param comments user comments
         */
        private FeedbackReportWorker(final FeedbackReport feedbackReport,
                                     final String config, final String log, final String stackTrace,
                                     final String type, final String mail, final String summary, final String comments) {
            super(JmcsTaskRegistry.TASK_FEEDBACK_REPORT);
            this.feedbackReport = feedbackReport;
            this.config = config;
            this.appLog = log;
            this.stackTrace = stackTrace;
            this.type = type;
            this.mail = mail;
            this.summary = summary;
            this.comments = comments;
        }

        /**
         * Send the feedback report using HTTP in background
         * This code is executed by a Worker thread (Not Swing EDT)
         * @return boolean status flag
         */
        @Override
        public Boolean computeInBackground() {
            boolean statusFlag = false;

            // Create an HTTP client to send report information to our PHP script
            final HttpClient client = Http.getHttpClient();

            final String feedbackReportUrl = ApplicationDescription.getInstance().getFeedbackReportFormURL();
            final PostMethod method = new PostMethod(feedbackReportUrl);

            try {
                _logger.debug("Http client and post method have been created");

                final ApplicationDescription applicationDataModel = ApplicationDescription.getInstance();

                String applicationName;
                String applicationVersion;

                // Compose HTML form parameters
                // Get informations to send with the report
                if (applicationDataModel != null) {
                    applicationName = applicationDataModel.getProgramName();
                    applicationVersion = applicationDataModel.getProgramVersion();
                } else {
                    applicationName = "Unknown";
                    applicationVersion = "Unknown";
                }

                method.addParameter("applicationName", applicationName);
                method.addParameter("applicationVersion", applicationVersion);
                method.addParameter("systemConfig", config);
                method.addParameter("applicationLog", appLog);
                method.addParameter("applicationSpecificInformation", stackTrace);

                // Get information from swing elements
                method.addParameter("userEmail", mail);
                method.addParameter("feedbackType", type);
                method.addParameter("comments", comments);
                method.addParameter("summary", summary);

                _logger.debug("All post parameters have been set");

                // Send feedback report to PHP script
                client.executeMethod(method);

                _logger.debug("The report mail has been send");

                // Get PHP script result (either SUCCESS or FAILURE)
                final String response = method.getResponseBodyAsString();

                _logger.debug("HTTP response : {}", response);

                statusFlag = (!response.contains("FAILED")) && (method.isRequestSent());

                if (_logger.isDebugEnabled()) {
                    _logger.debug("Report sent : {}", (statusFlag) ? "YES" : "NO");
                }

            } catch (IOException ioe) {
                _logger.error("Cannot send feedback report: ", ioe);
            } finally {
                // Release the connection.
                method.releaseConnection();
            }

            _logger.debug("Set ready to send to false");

            return (statusFlag) ? Boolean.TRUE : Boolean.FALSE;
        }

        /**
         * Refresh the feedback report dialog to update its status.
         * This code is executed by the Swing Event Dispatcher thread (EDT)
         * @param sent boolean flag indicating if the feedback report was sent
         */
        @Override
        public void refreshUI(final Boolean sent) {
            feedbackReport.shouldDispose(sent.booleanValue());
        }
    }
}
