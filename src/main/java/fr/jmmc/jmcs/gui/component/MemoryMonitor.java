/*******************************************************************************
 *                 jMCS project ( http://www.jmmc.fr/dev/jmcs )
 *******************************************************************************
 * Copyright (c) 2017, CNRS. All rights reserved.
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

import fr.jmmc.jmcs.util.JVMUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple JVM memory monitor
 *
 * @author Laurent BOURGES.
 */
public final class MemoryMonitor extends JPanel implements Disposable {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(MemoryMonitor.class.getName());
    /** default refresh period = 1 second */
    private static final int REFRESH_PERIOD = 1000;

    /* members */
    /** progress bar */
    private final JProgressBar progressBar;
    /** refresh Swing timer */
    private final Timer timerRefresh;

    MemoryMonitor() {
        super(new BorderLayout());

        final Dimension dim = new Dimension(100, 25);
        setMinimumSize(dim);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        progressBar.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                System.gc();
                _logger.info("{}", getMemoryInfo());
            }

        });

        add(progressBar, BorderLayout.CENTER);

        // Create the timeline refresh timer:
        this.timerRefresh = new Timer(REFRESH_PERIOD, new ActionListener() {
            /**
             * Invoked when the timer action occurs.
             */
            @Override
            public void actionPerformed(final ActionEvent ae) {
                updateInfo();
            }
        });
        this.timerRefresh.setRepeats(true);

        enableRefreshTimer(true);
    }

    @Override
    public void dispose() {
        // disable refresh timer:
        enableRefreshTimer(false);
    }

    private void updateInfo() {
        final long freeMemory = JVMUtils.freeMemory();
        final long totalMemory = JVMUtils.totalMemory();

        final String text = String.format("%d M", freeMemory / (1024l * 1024l));

        final int usedMem = (int) Math.round(100d - ((100d * freeMemory) / totalMemory));

        if (_logger.isDebugEnabled()) {
            _logger.debug("updateInfo: text = {} {} %", text, usedMem);
        }

        progressBar.setValue(usedMem);
        progressBar.setString(text);
        progressBar.setToolTipText(getMemoryInfo());
    }

    private String getMemoryInfo() {
        return JVMUtils.getMemoryInfo();
    }

    /**
     * Start/Stop the internal refresh timer
     * @param enable true to enable it, false otherwise
     */
    private void enableRefreshTimer(final boolean enable) {
        if (enable) {
            if (!this.timerRefresh.isRunning()) {
                _logger.debug("Starting timer: {}", this.timerRefresh);

                this.timerRefresh.start();
            }
        } else {
            if (this.timerRefresh.isRunning()) {
                _logger.debug("Stopping timer: {}", this.timerRefresh);

                this.timerRefresh.stop();
            }
        }
    }

    public static void main(String args[]) {
        // GUI initialization
        final JFrame frame = new JFrame("MemoryMonitor Demo");

        // Force to exit when the frame closes :
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new MemoryMonitor();

        frame.getContentPane().add(panel);

        frame.pack();
        frame.setVisible(true);
    }

}
