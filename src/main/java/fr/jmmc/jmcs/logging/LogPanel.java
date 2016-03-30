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
package fr.jmmc.jmcs.logging;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel displays both log content and refresh buttons
 *
 * @author Laurent BOURGES.
 */
public class LogPanel extends javax.swing.JPanel implements ActionListener, ChangeListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;
    /** default auto refresh period = 1 second */
    private static final int REFRESH_PERIOD = 1000;
    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(LogPanel.class.getName());
    /** double formatter for auto refresh period */
    private final static NumberFormat _df1 = new DecimalFormat("0.0");

    /* members */
    /** logger path */
    private final String _loggerPath;
    /** log buffer byte count */
    private int _logByteCount = 0;
    /* Swing components */
    /** refresh Swing timer */
    private final Timer _timerRefresh;

    /** 
     * Creates new form LogPanel for the application log
     */
    public LogPanel() {
        this(LoggingService.JMMC_APP_LOG);
    }

    /** 
     * Creates new form LogPanel for the given logger path
     * @param loggerPath logger path
     */
    public LogPanel(final String loggerPath) {
        _loggerPath = loggerPath;

        // Create the autoRefresh timer before any Swing component (see button action listeners):
        _timerRefresh = new Timer(REFRESH_PERIOD, this);
        _timerRefresh.setInitialDelay(0);

        initComponents();

        postInit();
    }

    /**
     * Initialize the Swing components
     */
    private void postInit() {

        if (SystemUtils.IS_OS_MAC_OSX) {
            setOpaque(false);
        }

        // Refresh buttons listener :
        jButtonRefreshLogs.addActionListener(this);
        jToggleButtonAutoRefresh.addActionListener(this);
        jSliderPeriod.addChangeListener(this);

        // set slider to 10 (1s):
        jSliderPeriod.setValue(10);

        // start autoRefresh timer by simulating one click:
        jToggleButtonAutoRefresh.doClick();
    }

    /**
     * Free any resource (timer) to this instance
     */
    public void onDispose() {
        _logger.debug("onDispose: {}", this);

        // stop anyway timer if started:
        enableAutoRefreshTimer(false);
    }

    /** 
     * Handle the stateChanged event from the slider.
     * @param ce slider change event
     */
    @Override
    public void stateChanged(final ChangeEvent ce) {
        final int milliseconds = 100 * jSliderPeriod.getValue();

        if (_logger.isDebugEnabled()) {
            _logger.debug("slider changed to: {} ms", milliseconds);
        }

        // update text value (rounded to 0.1s):
        jTextFieldPeriod.setText(_df1.format(0.001d * milliseconds) + " s");

        // apply new delay to the timer
        _timerRefresh.setDelay(milliseconds);
    }

    /**
     * Process any comboBox change event (level) to update Logger's state
     * @param ae action event
     */
    @Override
    public void actionPerformed(final ActionEvent ae) {
        if (ae.getSource() == _timerRefresh || ae.getSource() == jButtonRefreshLogs) {
            updateLog();
        } else if (ae.getSource() == jToggleButtonAutoRefresh) {
            final boolean autoRefresh = jToggleButtonAutoRefresh.isSelected();

            enableAutoRefreshTimer(autoRefresh);

            jButtonRefreshLogs.setEnabled(!autoRefresh);
        }
    }

    /**
     * Start/Stop the internal autoRefresh timer
     * @param enable true to enable it, false otherwise
     */
    private void enableAutoRefreshTimer(final boolean enable) {
        if (enable) {
            if (!_timerRefresh.isRunning()) {
                _logger.debug("starting timer: {}", _timerRefresh);
                _timerRefresh.start();
            }
        } else {
            if (_timerRefresh.isRunning()) {
                _logger.debug("stopping timer: {}", _timerRefresh);
                _timerRefresh.stop();
            }
        }
    }

    /**
     * Update the log
     */
    private void updateLog() {

        final boolean append = (_logByteCount > 0) ? true : false;

        // Get the partial application log as string starting at the given byteCount
        final LogOutput logOutput = LoggingService.getInstance().getLogOutput(_loggerPath, _logByteCount);

        // update byte count:
        _logByteCount = logOutput.getByteCount();

        final String content = logOutput.getContent();

        if (content.length() > 0) {
            if (append) {
                final Document doc = logTextArea.getDocument();
                try {
                    doc.insertString(doc.getLength(), content, null);
                } catch (BadLocationException ble) {
                    _logger.error("bad location: ", ble);
                }
            } else {
                logTextArea.setText(content);
            }
            // scroll to end:
            logTextArea.setCaretPosition(logTextArea.getText().length());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelLogButtons = new javax.swing.JPanel();
        jToggleButtonAutoRefresh = new javax.swing.JToggleButton();
        jSliderPeriod = new javax.swing.JSlider();
        jTextFieldPeriod = new javax.swing.JTextField();
        jButtonRefreshLogs = new javax.swing.JButton();
        logScrollPane = new javax.swing.JScrollPane();
        logTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout(0, 2));

        jPanelLogButtons.setOpaque(false);
        jPanelLogButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 0));

        jToggleButtonAutoRefresh.setText("Auto Refresh");
        jPanelLogButtons.add(jToggleButtonAutoRefresh);

        jSliderPeriod.setMajorTickSpacing(10);
        jSliderPeriod.setMinimum(1);
        jSliderPeriod.setToolTipText("auto refresh periodicity (100ms to 10s)");
        jPanelLogButtons.add(jSliderPeriod);

        jTextFieldPeriod.setColumns(6);
        jTextFieldPeriod.setEditable(false);
        jPanelLogButtons.add(jTextFieldPeriod);

        jButtonRefreshLogs.setText("Refresh");
        jPanelLogButtons.add(jButtonRefreshLogs);

        add(jPanelLogButtons, java.awt.BorderLayout.PAGE_START);

        logScrollPane.setOpaque(false);

        logTextArea.setEditable(false);
        logTextArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        logTextArea.setTabSize(4);
        logScrollPane.setViewportView(logTextArea);

        add(logScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonRefreshLogs;
    private javax.swing.JPanel jPanelLogButtons;
    private javax.swing.JSlider jSliderPeriod;
    private javax.swing.JTextField jTextFieldPeriod;
    private javax.swing.JToggleButton jToggleButtonAutoRefresh;
    private javax.swing.JScrollPane logScrollPane;
    private javax.swing.JTextArea logTextArea;
    // End of variables declaration//GEN-END:variables

    /**
     * Return the logger path
     * @return logger path
     */
    public String getLoggerPath() {
        return _loggerPath;
    }
}
