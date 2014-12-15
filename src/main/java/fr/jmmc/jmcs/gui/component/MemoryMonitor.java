/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
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
    /** default refresh period = 1/4 second */
    private static final int REFRESH_PERIOD = 250;

    /* members */
    /** progress bar */
    private final JProgressBar progressBar;
    /** refresh Swing timer */
    private final Timer timerRefresh;

    MemoryMonitor() {
        super(new BorderLayout());

        final Dimension dim = new Dimension(80, 25);
        setMinimumSize(dim);
        setPreferredSize(dim);
        setMaximumSize(dim);

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
