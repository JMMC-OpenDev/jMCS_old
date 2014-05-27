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

import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.jmcs.gui.MainMenuBar;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facility static class to properly center a window
 * on the main screen (handle multiple screen setups).
 * 
 * @author Brice COLUCCI, Guillaume MELLA, Laurent BOURGES.
 */
public final class WindowUtils {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(WindowUtils.class.getName());
    /** Screen width */
    private static int _screenWidth = 0;
    /** Screen height */
    private static int _screenHeight = 0;

    /**
     * Get screen properties
     * @throws NullPointerException on some platform (virtual box)
     */
    public static void getScreenProperties() throws NullPointerException {
        // Get main screen size
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        DisplayMode dm = gs.getDisplayMode();
        _screenWidth = dm.getWidth();
        _screenHeight = dm.getHeight();
    }

    /** 
     * Return the maximum viewable area or dimension if smaller. 
     * @param dimension frame dimension
     * @return dimension
     */
    public static Dimension getMaximumArea(final Dimension dimension) {
        Dimension max = new Dimension();
        max.height = Math.min(_screenHeight, dimension.height);
        max.width = Math.min(_screenWidth, dimension.width);
        return max;
    }

    /**
     * Center the given window on the main screen real estate.
     *
     * If the window is bigger than the main screen, it will be moved to the upper-left main screen corner.
     *
     * @param windowToCenter the window we want to center
     */
    public static void centerOnMainScreen(final Window windowToCenter) {
        if (windowToCenter != null) {
            // Using invokeAndWait to be in sync with this thread
            // note: invokeAndWaitEDT throws an IllegalStateException if any exception occurs
            SwingUtils.invokeAndWaitEDT(new Runnable() {
                /**
                 * Initializes Splash Screen in EDT
                 */
                @Override
                public void run() {
                    // Next try catch is mandatory to catach null pointer excpetion that
                    // can occure on some virtual machine emulation (at least virtualBox)
                    try {
                        getScreenProperties();

                        // Dimension of the window
                        Dimension windowSize = windowToCenter.getSize();

                        // Get centering point
                        Point point = getCenteringPoint(windowSize);

                        windowToCenter.setLocation(point);

                        _logger.debug("The window has been centered");

                    } catch (NullPointerException npe) {
                        _logger.warn("Could not center window");
                    }
                }
            });
        }
    }

    /**
     * Returns the centered point in order to center a frame on the screen
     * @param frameDimension frame size
     * @return centered point
     * @throws NullPointerException on some platform (virtual box)
     */
    public static Point getCenteringPoint(final Dimension frameDimension) throws NullPointerException {

        getScreenProperties();

        int x = (_screenWidth - frameDimension.width) / 2;
        x = Math.max(x, 0);

        int y = (_screenHeight - frameDimension.height) / 2;
        y = Math.max(y, 0);

        return new Point(x, y);
    }

    /**
     * Installs standard window-closing keyboard shortcuts (i.e ESC and ctrl-W).
     *
     * @param dialog the JDialog to close on keystroke.
     */
    public static void setClosingKeyboardShortcuts(final JDialog dialog) {
        setClosingKeyboardShortcuts(dialog.getRootPane(), dialog);
    }

    /**
     * Installs standard window-closing keyboard shortcuts (i.e ESC and ctrl-W).
     *
     * @param frame the JFrame to close on keystroke.
     */
    public static void setClosingKeyboardShortcuts(final JFrame frame) {
        setClosingKeyboardShortcuts(frame.getRootPane(), frame);
    }

    /**
     * Installs standard window-closing keyboard shortcuts (i.e ESC and ctrl-W).
     *
     * @param rootPane the pane to listen keystroke.
     * @param window the window to close on keystroke.
     */
    private static void setClosingKeyboardShortcuts(final JRootPane rootPane, final Window window) {

        if ((rootPane == null) || (window == null)) {
            throw new IllegalArgumentException();
        }

        // Trap Escape key
        KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        // Trap command-W key
        KeyStroke metaWStroke = KeyStroke.getKeyStroke(MainMenuBar.getSystemCommandKey() + "W");

        // Close window on either stroke
        final ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                _logger.trace("Handling close window shortcut.");

                // trigger standard closing action (@see JFrame.setDefaultCloseOperation)
                // i.e. hide or dispose the window:
                window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
            }
        };

        rootPane.registerKeyboardAction(actionListener, escapeStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
        rootPane.registerKeyboardAction(actionListener, metaWStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Restore, then automatically save window size changes.
     * @param window the window to monitor.
     * @param key the window identifier.
     */
    public static void rememberWindowSize(final Window window, final String key) {

        // Restore dimension from preferences
        final Dimension loadedDimension = SessionSettingsPreferences.loadDimension(key);

        // Using invokeAndWait to be in sync with this thread :
        // note: invokeAndWaitEDT throws an IllegalStateException if any exception occurs
        SwingUtils.invokeAndWaitEDT(new Runnable() {
            /**
             * Initializes Splash Screen in EDT
             */
            @Override
            public void run() {
                if (loadedDimension == null) {
                    window.pack();
                } else {
                    getScreenProperties();
                    window.setSize(getMaximumArea(loadedDimension));
                }
            }
        });

        window.addComponentListener(new WindowSizeAdapter(window, key));
    }

    /**
     * Private constructor
     */
    private WindowUtils() {
        super();
    }

    private static final class WindowSizeAdapter extends ComponentAdapter {

        /** component resize timer to avoid repeated calls */
        private final Timer _timer;

        /**
         * Constructor
         * @param window the window to monitor.
         * @param key the window identifier.
         */
        WindowSizeAdapter(final Window window, final String key) {

            // Triggered once timer definitly expires
            final ActionListener frameSizeChangedAction = new ActionListener() {
                // Weak to let window deallocation occur gracefully
                private final WeakReference<Window> _weakWindow = new WeakReference<Window>(window);
                private final String _key = key;

                @Override
                public void actionPerformed(ActionEvent evt) {
                    final Window window = _weakWindow.get();
                    if (window == null) {
                        return;
                    }

                    if (_logger.isDebugEnabled()) {
                        _logger.debug("Store window[{}] size = {}.", _key, window.getSize());
                    }

                    SessionSettingsPreferences.storeDimension(_key, window.getSize());
                }
            };

            // 1 second grace periods:
            _timer = new Timer(1000, frameSizeChangedAction);
            _timer.setRepeats(false);
        }

        @Override
        public void componentResized(final ComponentEvent e) {
            // Start timer once
            if (!_timer.isRunning()) {
                _timer.start();
                return;
            }

            // Or restart it until there is no more resizing events for at least the timer duration
            _timer.restart();
        }
    }
}
/*___oOo___*/
