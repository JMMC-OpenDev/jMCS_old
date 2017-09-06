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

import com.jidesoft.plaf.LookAndFeelFactory;
import com.jidesoft.utils.ThreadCheckingRepaintManager;
import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.preference.CommonPreferences;
import fr.jmmc.jmcs.util.IntrospectionUtils;
import fr.jmmc.jmcs.util.MCSExceptionHandler;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.LookAndFeel;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gather SWING related properties settings for our applications.
 *
 * This code is called during Bootstrapper initialization (always performed before any application code).
 *
 * @author Laurent BOURGES, Guillaume MELLA.
 */
public final class SwingSettings {

    /** logger */
    private final static Logger _logger = LoggerFactory.getLogger(SwingSettings.class.getName());
    /** enable/disable EDT violation detection */
    private final static boolean DEBUG_EDT_VIOLATIONS = false;
    /** flag to prevent multiple code execution */
    private static boolean _alreadyDone = false;
    /** cache for initial font sizes */
    private final static Map<Object, Integer> INITIAL_FONT_SIZES = new HashMap<Object, Integer>(64);
    /** cached initial row height */
    private static int INITIAL_ROW_HEIGHT = 0;

    /** Hidden constructor */
    private SwingSettings() {
    }

    /**
     * Initialize maximum of things to get uniform application running inn the scientific context.
     * Initialization are done only on the first call of this method (which should be from a main method)
     */
    public static void setup() {
        // avoid reentrance:
        if (_alreadyDone) {
            return;
        }
        _alreadyDone = true;

        setSwingDefaults();

        if (DEBUG_EDT_VIOLATIONS) {
            RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());
        }

        // Install exception handlers:
        if (Bootstrapper.isHeadless()) {
            // Use logging exception handler:
            MCSExceptionHandler.installLoggingHandler();
        } else {
            // Use Swing exception handler:
            MCSExceptionHandler.installSwingHandler();

            // Apply UI scale & LaF settings:
            setLAFDefaults();

            setDefaultLookAndFeel();
        }

        _logger.info("Swing settings set.");
    }

    private static void setDefaultLookAndFeel() {
        final String className = CommonPreferences.getInstance().getPreference(CommonPreferences.UI_LAF_CLASSNAME);
        _logger.debug("LAF class: {}", className);

        // Note: use the main thread (not EDT) to avoid any deadlock during bootstrapping:
        setLookAndFeel(className, false);
    }

    public static void setLookAndFeel(final String className, final boolean force) {
        if (className != null
                && (force
                || (!className.isEmpty() && !className.equals(UIManager.getLookAndFeel().getClass().getName())))) {

            _logger.info("Use Look & Feel: {}", className);
            try {
                final LookAndFeel newLaf = (LookAndFeel) IntrospectionUtils.getInstance(className);
                UIManager.setLookAndFeel(newLaf);

                // Re-apply LAF defaults:
                setLAFDefaults();

                if (force) {
                    // Only update existing Frames if the force flag is true
                    // to avoid bootstrap issues:
                    final Frame mainFrame = App.getExistingFrame();
                    if (mainFrame != null) {
                        final float uiScale = CommonPreferences.getInstance().getUIScale();

                        updateComponentTree(mainFrame, uiScale);

                        updateComponentTreeLAF(mainFrame);
                    }
                }
            } catch (UnsupportedLookAndFeelException ulafe) {
                throw new RuntimeException(ulafe);
            }
        }
    }

    private static void updateComponentTreeLAF(final Frame f) {
        if (f != null) {
            f.requestFocus();
            updateComponentTreeRecurseLAF(f);

            for (Window window : f.getOwnedWindows()) {
                _logger.debug("updateComponentTreeLAF: Window: {}", window.getName());
                updateComponentTreeRecurseLAF(window);
            }
        }
    }

    private static void updateComponentTreeRecurseLAF(final Window w) {
        SwingUtilities.updateComponentTreeUI(w);
        w.pack();
    }

    /**
     * Define Swing defaults: 
     * - Change locale of SWING and ToolTip related.
     */
    private static void setSwingDefaults() {
        // Force Locale for Swing Components :
        JComponent.setDefaultLocale(Locale.getDefault());

        _logger.debug("Set Locale.US for JComponents");

        // Let the tooltip stay longer (60s) :
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(60000);

        _logger.debug("Make tooltips appear more quickly and stay longer");
    }

    /**
     * Define LAF defaults: 
     * - adjust font sizes
     * - install JIDE extensions
     */
    static void setLAFDefaults() {
        _logger.debug("setLAFDefaults: begin");

        // Fix fonts for the current LAF:
        final float uiScale = CommonPreferences.getInstance().getUIScale();
        _logger.info("UI scale: {}", uiScale);

        fixUIFonts(UIManager.getLookAndFeelDefaults(), uiScale);
        fixUIFonts(UIManager.getDefaults(), uiScale);

        installJideLAFExtensions();

        _logger.debug("setLAFDefaults: end");
    }

    private static synchronized void fixUIFonts(final UIDefaults uidef, final float uiScale) {
        for (Entry<Object, Object> e : uidef.entrySet()) {
            final Object key = e.getKey();

            if (key instanceof String) {
                final String strKey = ((String) key);

                _logger.debug("ui default: {} = {}", strKey, e.getValue());

                if (strKey.contains("font") || strKey.contains("Font")) {
                    Font font = uidef.getFont(key);

                    _logger.debug("font default: {} = {}", key, font);

                    if (font != null) {
                        int size = font.getSize();
                        final Integer initialSize = INITIAL_FONT_SIZES.get(key);

                        if (initialSize == null) {
                            INITIAL_FONT_SIZES.put(key, Integer.valueOf(size));
                        } else {
                            size = initialSize.intValue();
                        }

                        final int newSize = Math.round(uiScale * size);
                        final int newStyle = (uiScale > 1.2f) ? (font.isItalic() ? Font.ITALIC : Font.PLAIN) : font.getStyle();
                        final String name = font.getName();

                        // Force using Monospaced font for Tree & TextArea:
                        final String newName = ("Tree.font".equals(strKey)
                                || "TextArea.font".equals(strKey)) ? "Monospaced" : name;

                        // Derive new font:
                        final Font newFont = new Font(newName, newStyle, newSize);
                        _logger.debug("font fixed: {} = {}", key, newFont);

                        uidef.put(key, new FontUIResource(newFont));
                    }
                }
            }
        }
    }

    public static int setAndGetInitialRowHeight(final int height) {
        if (INITIAL_ROW_HEIGHT == 0) {
            _logger.debug("INITIAL_ROW_HEIGHT: {}", height);
            INITIAL_ROW_HEIGHT = height;
        }
        return INITIAL_ROW_HEIGHT;
    }

    public static int getInitialRowHeight() {
        return INITIAL_ROW_HEIGHT;
    }

    private static void updateComponentTree(final Frame f, final float uiScale) {
        if (f != null) {
            _logger.debug("updateComponentTree: begin {}", f.getName());
            updateWindowTree(f, uiScale);
            for (Window window : f.getOwnedWindows()) {
                updateWindowTree(window, uiScale);
            }
            _logger.debug("updateComponentTree: end");
        }
    }

    private static void updateWindowTree(final Window w, final float uiScale) {
        _logger.debug("Window: {}", w.getName());
        updateComponentTreeRecurse(w, uiScale);
        w.pack();
    }

    private static void updateComponentTreeRecurse(final Component c, final float uiScale) {
        if (c instanceof JComponent) {
            updateJComponent((JComponent) c, uiScale);
        }

        Component[] children = null;
        if (c instanceof Container) {
            children = ((Container) c).getComponents();
        }
        if (children != null) {
            for (Component child : children) {
                updateComponentTreeRecurse(child, uiScale);
            }
        }
    }

    private static void updateJComponent(final JComponent c, final float uiScale) {
        if (c instanceof JTable) {
            final JTable table = (JTable) c;
            _logger.debug("table[{}] : {}", table.getName(), table.getRowHeight());

            if (INITIAL_ROW_HEIGHT != 0) {
                SwingUtils.adjustRowHeight(table, INITIAL_ROW_HEIGHT);

                _logger.debug("table[{}] fixed : {}", table.getName(), table.getRowHeight());
            }
        }
        if (c instanceof JTree) {
            final JTree tree = (JTree) c;
            _logger.debug("tree[{}] : {}", tree.getName(), tree.getRowHeight());

            if (INITIAL_ROW_HEIGHT != 0) {
                SwingUtils.adjustRowHeight(tree, INITIAL_ROW_HEIGHT);

                _logger.debug("tree[{}] fixed : {}", tree.getName(), tree.getRowHeight());
            }
        }

        if (c instanceof JList) {
            final JList list = (JList) c;
            _logger.debug("list[{}] : {}", list.getName(), list.getFixedCellHeight());

            // for core: force cache invalidation by temporarily setting fixed height
            list.setFixedCellHeight(10);
            list.setFixedCellHeight(-1);
        }

        if (c.isFontSet()) {
            final Font font = c.getFont();
            _logger.debug("component[{} @ {}] : {}", c.getName(), c.getClass().getSimpleName(), font);

            String key = c.getClass().getSimpleName();

            if (c instanceof JPanel) {
                key = "Panel";
            } else if (c instanceof JButton) {
                key = "Button";
            } else if (c instanceof JTextField) {
                key = "TextField";
            } else if (c instanceof JLabel) {
                key = "Label";
            } else if (c instanceof JList) {
                key = "List";
            } else if (c instanceof JPopupMenu) {
                key = "PopupMenu";
            } else if (c instanceof JMenuBar) {
                key = "MenuBar";
            } else if (c instanceof JViewport) {
                key = "Viewport";
            }

            if (key.length() == 0) {
                key = c.getClass().getName();
            }
            if (key.startsWith("J")) {
                key = key.substring(1);
            }
            key += ".font";

            final Integer initialSize = INITIAL_FONT_SIZES.get(key);

            if (initialSize != null) {
                final int newSize = Math.round(uiScale * initialSize);
                final int newStyle = (uiScale > 1.2f) ? (font.isItalic() ? Font.ITALIC : Font.PLAIN) : font.getStyle();
                final String name = font.getName();

                // Derive new font:
                final Font newFont = new Font(name, newStyle, newSize);
                _logger.debug("font fixed: {} = {}", key, newFont);

                // Fix font:
                c.setFont(newFont);

            } else {
                _logger.warn("font[{}]: {}", key, initialSize);
            }
        }
    }

    /**
     * Install JIDE Look And Feel extensions.
     * TODO: it has side-effects on date spinner ... 
     * maybe enable it only for applications requiring it (System property) ?
     */
    public static void installJideLAFExtensions() {
        // To ensure the use of TriStateCheckBoxes in the Jide CheckBoxTree
        SwingUtils.invokeAndWaitEDT(new Runnable() {
            @Override
            public void run() {
                // Install JIDE extensions (Swing workaround):
                LookAndFeelFactory.installJideExtension();
            }
        });
    }
}
