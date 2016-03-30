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

import fr.jmmc.jmcs.util.StringUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A text field for search/filter interfaces. The extra functionality includes
 * a placeholder string (when the user hasn't yet typed anything), and a button
 * to clear the currently-entered text.
 *
 * ORIGIN : Elliott Hughes
 *
 * TODO : add a menu of recent searches.
 * TODO : make recent searches persistent.
 *
 * @author Sylvain LAFRASSE, Laurent BOURGES.
 */
public class SearchField extends JTextField {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    protected static final Logger _logger = LoggerFactory.getLogger(SearchField.class.getName());
    /** disarm color */
    private static final Color DISARMED_GRAY = new Color(0.7f, 0.7f, 0.7f);
    /** Mac flag, true if running on a Mac OS X computer, false otherwise */
    private static final boolean MACOSX_RUNTIME = SystemUtils.IS_OS_MAC_OSX;
    /** debug flag to draw border area */
    private static final boolean DEBUG_AREA = false;
    /** default newline replacement character = ' ' */
    public static final char NEWLINE_DEFAULT_REPLACEMENT_CHAR = ' ';
    /* members */
    /** Store whether notifications should be sent every time a key is pressed */
    private boolean _sendsNotificationForEachKeystroke = false;
    /** Store whether a text should be drawn when nothing else in text field */
    private boolean _showingPlaceholderText = false;
    /** Store the text displayed when nothing in */
    private final String _placeholderText;
    /** Store the previous entered text */
    private String _previousText = "";
    /** Store whether the mouse is over the cancel cross */
    private boolean _armedCancelButton = false;
    /** Store whether the mouse is over the options button */
    private boolean _armedOptionsButton = false;
    /** Store the pop up men for options */
    private JPopupMenu _optionsPopupMenu = null;
    /** Store shape object representing the search button area */
    private Rectangle _searchButtonShape = null;
    /** Store optional shape object representing the cancel button area */
    private Ellipse2D.Double _cancelButtonShape = null;
    /** Store optional shape object representing the options button area */
    private Rectangle _optionsButtonShape = null;
    /** Store the rounded rectangle inner area of this search field */
    private final RoundRectangle2D.Double _roundedInnerArea = new RoundRectangle2D.Double();
    /** Store the rounded rectangle outer area of this search field */
    private final RoundRectangle2D.Double _outerArea = new RoundRectangle2D.Double();
    /** Store the rounded rectangle inner area of this search field */
    private final RoundRectangle2D.Double _innerArea = new RoundRectangle2D.Double();

    /**
     * Creates a new SearchField object with options.
     *
     * @param placeholderText the text displayed when nothing in.
     * @param options the pop up men for options, null if none.
     */
    public SearchField(final String placeholderText, final JPopupMenu options) {
        super(new CustomPlainDocument(), null, 8); // 8 characters wide by default

        _placeholderText = placeholderText;
        _optionsPopupMenu = options;

        addFocusListener(new PlaceholderText());
        initBorder();
        initKeyListener();
    }

    /**
     * Creates a new SearchField object.
     *
     * @param placeholderText the text displayed when nothing in.
     */
    public SearchField(final String placeholderText) {
        this(placeholderText, null);
    }

    /**
     * Creates a new SearchField object with a default "Search" place holder.
     */
    public SearchField() {
        this("Search", null);
    }

    /**
     * Draw the custom widget border.
     */
    private void initBorder() {
        // On Mac OS X, simply use the OS specific search textfield widget
        if (MACOSX_RUNTIME) {
            // http://developer.apple.com/mac/library/technotes/tn2007/tn2196.html
            putClientProperty("JTextField.variant", "search");

            // note: possible conflict with FindPopup
            putClientProperty("JTextField.Search.FindAction",
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _logger.debug("FindAction Mac OS X called.");
                            postActionEvent();
                        }
                    });
            putClientProperty("JTextField.Search.CancelAction",
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _logger.debug("CancelAction Mac OS X called.");
                            handleCancelEdit();
                        }
                    });
            if (_optionsPopupMenu != null) {
                putClientProperty("JTextField.Search.FindPopup", _optionsPopupMenu);
            }

            return;
        }

        // Fallback for platforms other than Mac OS X
        // Add the border that draws the magnifying glass and the cancel cross:
        final int left = 30;
        final int right = 22;
        if (DEBUG_AREA) {
            setBorder(new CompoundBorder(BorderFactory.createMatteBorder(4, left, 4, right, Color.YELLOW), new ButtonBorder()));
        } else {
            setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(4, left, 4, right), new ButtonBorder()));
        }

        final MouseInputListener mouseInputListener = new ButtonBorderMouseListener();
        addMouseListener(mouseInputListener);
        addMouseMotionListener(mouseInputListener);

        // We must be non-opaque since we won't fill all pixels.
        // This will also stop the UI from filling our background.
        setOpaque(false);
    }

    /**
     * Draw the dedicated custom rounded text field.
     *
     * @param g2 the graphical context to draw in.
     */
    @Override
    protected void paintComponent(final Graphics g2) {
        final long start = System.nanoTime();

        // On anything but Mac OS X
        if (!MACOSX_RUNTIME) {
            final int width = getWidth();
            final int height = getHeight();

            final Graphics2D g2d = (Graphics2D) g2;
            final Object savedAAHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            final Color savedColor = g2d.getColor();

            // force antialiasing On:
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // TODO: use buffered image caches to avoid drawing ops at each repaint (caret, mouse over ...)
            // Paint a rounded rectangle in the background surrounded by a black line.
            _outerArea.setRoundRect(0d, 0d, width, height, height, height);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fill(_outerArea);

            g2d.setColor(Color.GRAY);
            g2d.fillRoundRect(0, -1, width, height, height, height);

            // inner area (text field):
            _innerArea.setRoundRect(1d, 1d, width - 2d, height - 2d, height - 2d, height - 2d);
            g2d.setColor(getBackground());
            g2d.fill(_innerArea);

            // define clip for the following line only:
            g2.setClip(_outerArea);

            g2d.setColor(Color.GRAY);
            g2d.drawLine(0, 1, width, 1);

            // define clip as smaller rounded rectangle (GTK and nimbus LAF):
            _roundedInnerArea.setRoundRect(3d, 3d, width - 6d, height - 6d, height - 6d, height - 6d);
            g2.setClip(_roundedInnerArea);

            // restore g2d state:
            g2d.setColor(savedColor);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedAAHint);
        }

        // Now call the superclass behavior to paint the foreground.
        super.paintComponent(g2);

        if (_logger.isDebugEnabled()) {
            _logger.debug("paintComponent() - duration: {} ms.", (System.nanoTime() - start) / 1e6d);
        }
    }

    /**
     * Follow keystrokes to notify listeners.
     */
    private void initKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    handleCancelEdit();
                } else if (_sendsNotificationForEachKeystroke) {
                    maybeNotify();
                }
            }
        });
    }

    /**
     * Reset SearchField content and notify listeners.
     */
    private void handleCancelEdit() {
        if (isEnabled()) {
            if (!_showingPlaceholderText && !StringUtils.isEmpty(getText())) {
                // Field is NOT empty
                setText("");
            }
        } else {
            // cancel action:
            performCancel();
        }
    }

    protected void performCancel() {
        _logger.debug("performCancel invoked.");
    }

    /**
     * Display Options.
     * @param me mouse event to define menu location
     */
    private void handleShowOptions(final MouseEvent me) {
        if (_optionsPopupMenu != null) {
            // revalidate / repaint (support LAF change) because this component is created elsewhere:
            SwingUtilities.updateComponentTreeUI(_optionsPopupMenu);
            _optionsPopupMenu.show(this, me.getX() + 5, me.getY() + 10);
        }
    }

    /**
     * Sets the text of this <code>TextComponent</code>
     * to the specified text.
     *
     * This overrides the default behavior to tell the placeholder to use this new text value
     *
     * @param txt the new text to be set
     */
    @Override
    public void setText(final String txt) {
        super.setText(txt);

        if (!_placeholderText.equals(txt)) {
            _previousText = txt;
        }
    }

    /**
     * Returns the text contained in this <code>TextComponent</code>.
     *
     * If the text corresponds to the placeholder text then it returns "".
     *
     * @return the text, not the placeholder text
     */
    public final String getRealText() {
        final String txt = super.getText();

        if (_placeholderText.equals(txt)) {
            return "";
        }
        return txt;
    }

    /**
     * Trap notifications when showing place holder.
     */
    private void maybeNotify() {
        if (_showingPlaceholderText) {
            return;
        }

        postActionEvent();
    }

    @Override
    public final void postActionEvent() {
        final String cleanedText = cleanText(getRealText());
        setText(cleanedText);

        if (!StringUtils.isEmpty(cleanedText)) {
            super.postActionEvent();
        }
    }

    /**
     * Clean up the current text value before calling action listeners and update the text field.
     * @param text current text value
     * @return cleaned up text value
     */
    public String cleanText(final String text) {
        return StringUtils.cleanWhiteSpaces(text);
    }

    /**
     * Store whether notifications should be sent for each key pressed.
     *
     * @param eachKeystroke true to notify any key pressed, false otherwise.
     */
    public void setSendsNotificationForEachKeystroke(final boolean eachKeystroke) {
        _sendsNotificationForEachKeystroke = eachKeystroke;
    }

    /**
     * @return custom newLine replacement character
     */
    public char getNewLineReplacement() {
        final CustomPlainDocument doc = getCustomPlainDocument();
        return (doc != null) ? doc.getNewLineReplacement() : NEWLINE_DEFAULT_REPLACEMENT_CHAR;
    }

    /**
     * @param newLineReplacement custom newLine replacement character
     */
    public void setNewLineReplacement(final char newLineReplacement) {
        final CustomPlainDocument doc = getCustomPlainDocument();
        if (doc != null) {
            doc.setNewLineReplacement(newLineReplacement);
        }
    }

    private CustomPlainDocument getCustomPlainDocument() {
        final Document doc = getDocument();
        if (doc instanceof CustomPlainDocument) {
            return (CustomPlainDocument) doc;
        }
        return null;
    }

    private final static class CustomPlainDocument extends PlainDocument {

        private static final long serialVersionUID = 1L;

        /** custom newLine replacement character */
        private char newLineReplacement = NEWLINE_DEFAULT_REPLACEMENT_CHAR;

        CustomPlainDocument() {
            super();
        }

        char getNewLineReplacement() {
            return newLineReplacement;
        }

        void setNewLineReplacement(char newLineReplacement) {
            this.newLineReplacement = newLineReplacement;
        }

        /**
         * Inserts some content into the document.
         * Inserting content causes a write lock to be held while the
         * actual changes are taking place, followed by notification
         * to the observers on the thread that grabbed the write lock.
         * <p>
         * This method is thread safe, although most Swing methods
         * are not. Please see
         * <A HREF="http://docs.oracle.com/javase/tutorial/uiswing/concurrency/index.html">Concurrency
         * in Swing</A> for more information.
         *
         * @param offs the starting offset &gt;= 0
         * @param str the string to insert; does nothing with null/empty strings
         * @param a the attributes for the inserted content
         * @exception BadLocationException  the given insert position is not a valid
         *   position within the document
         * @see Document#insertString
         */
        @Override
        public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
            String val = str;
            // fields don't want to have multiple lines.  We may provide a field-specific
            // model in the future in which case the filtering logic here will no longer
            // be needed.
            final Object filterNewlines = getProperty("filterNewlines");
            if (Boolean.TRUE.equals(filterNewlines)) {
                if ((str != null) && (str.indexOf('\n') >= 0)) {
                    final char replaceChar = newLineReplacement;
                    final StringBuilder filtered = new StringBuilder(str);
                    int n = filtered.length();
                    for (int i = 0; i < n; i++) {
                        if (filtered.charAt(i) == '\n') {
                            filtered.setCharAt(i, replaceChar);
                        }
                    }
                    val = filtered.toString();
                }
            }
            super.insertString(offs, val, a);
        }
    }

    /**
     * Draws the cancel button (a gray circle with a white cross) and the magnifying glass icon ...
     */
    private final class ButtonBorder extends EmptyBorder {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;
        /** debug flag to draw shapes */
        private static final boolean DEBUG_SHAPES = false;

        /* members */
        /** arrow points (x coords) */
        private final int[] arrowXPoints = new int[3];
        /** arrow points (y coords) */
        private final int[] arrowYPoints = new int[3];
        /** rectangle area containing the options button */
        private final Rectangle optionsButtonRect = new Rectangle();
        /** rectangle area containing the search button */
        private final Rectangle searchButtonRect = new Rectangle();
        /** disk area containing the cancel button */
        private final Ellipse2D.Double cancelButtonEllipse = new Ellipse2D.Double();

        /**
         * Constructor
         */
        ButtonBorder() {
            super(0, 0, 0, 0);
        }

        /**
         * Paint this border
         * @param g2
         */
        @Override
        public void paintBorder(final Component c, final Graphics g2,
                                final int x, final int y, final int width, final int height) {

            final SearchField field = (SearchField) c;
            final Graphics2D g2d = (Graphics2D) g2;
            final Object savedAAHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
            final Color savedColor = g2d.getColor();

            // force antialiasing On:
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (DEBUG_SHAPES) {
                g2d.setColor(Color.BLUE);
                g2d.draw(new Rectangle(x, y, width, height));
            }
            final Color backgroundColor = field.getBackground();

            // TODO: use buffered image caches to avoid drawing ops at each repaint (caret, mouse over ...)
            // Draw magnifying glass lens:
            final int diskL = 10;
            final int diskX = x - diskL - 15;
            final int diskY = y + ((height - 1 - diskL) / 2);
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillOval(diskX, diskY, diskL, diskL);
            g2d.setColor(backgroundColor);
            g2d.fillOval(diskX + 2, diskY + 2, diskL - 4, diskL - 4);

            // Draw magnifying glass handle:
            final int downX = (diskX + diskL) - 3;
            final int downY = (diskY + diskL) - 3;
            final int upX = downX + 4;
            final int upY = downY + 4;
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawLine(downX, downY, upX, upY);
            g2d.drawLine(downX, downY, upX, upY);
            g2d.drawLine(downX + 1, downY, upX, upY);

            // draw the popup arrow if options are available
            if (_optionsPopupMenu != null) {
                // Draw shaded arrow
                g2d.setColor(_armedOptionsButton ? DISARMED_GRAY : Color.GRAY);

                final int size = 4;
                final int xOrigin = diskX + diskL + 3 + size;
                final int yOrigin = y + height / 2 + 1;
                arrowXPoints[0] = xOrigin - size;
                arrowYPoints[0] = yOrigin - size;
                arrowXPoints[1] = xOrigin + size;
                arrowYPoints[1] = yOrigin - size;
                arrowXPoints[2] = xOrigin;
                arrowYPoints[2] = yOrigin + size;
                g2d.fillPolygon(arrowXPoints, arrowYPoints, arrowXPoints.length);

                // add 1 pixel margin:
                optionsButtonRect.setBounds(diskX - 1, diskY - 1, diskL + 2 + 2 * size + 2, diskL + 2);
                _searchButtonShape = null;
                _optionsButtonShape = optionsButtonRect;
                if (DEBUG_SHAPES) {
                    g2d.setColor(Color.RED);
                    g2d.draw(_optionsButtonShape);
                }
            } else {
                // add 1 pixel margin:
                searchButtonRect.setBounds(diskX - 1, diskY - 1, diskL + 2, diskL + 2);
                _searchButtonShape = searchButtonRect;
                _optionsButtonShape = null;
                if (DEBUG_SHAPES) {
                    g2d.setColor(Color.RED);
                    g2d.draw(_searchButtonShape);
                }
            }

            if (!_showingPlaceholderText && !StringUtils.isEmpty(getText())) {
                // if NOT empty, draw the cancel cross

                // Draw shaded disk
                final int circleL = 14;
                final int circleX = (x + width) + (22 - 5) - circleL;
                final int circleY = y + ((height - circleL) / 2);

                cancelButtonEllipse.setFrame(circleX, circleY, circleL, circleL);
                _cancelButtonShape = cancelButtonEllipse;
                g2d.setColor(_armedCancelButton ? DISARMED_GRAY : Color.GRAY);
                g2d.fill(_cancelButtonShape);

                if (DEBUG_SHAPES) {
                    g2d.setColor(Color.RED);
                    g2d.draw(_cancelButtonShape);
                }

                // Draw white cross
                final int lineL = circleL - 8;
                final int lineX = circleX + 4;
                final int lineY = circleY + 4;
                g2d.setColor(backgroundColor);
                g2d.drawLine(lineX, lineY, lineX + lineL, lineY + lineL);
                g2d.drawLine(lineX, lineY + lineL, lineX + lineL, lineY);

            } else {
                // reset area:
                _cancelButtonShape = null;
            }

            // restore g2d state:
            g2d.setColor(savedColor);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, savedAAHint);
        }
    }

    /**
     * Handles a click on the cancel button by clearing the text and notifying
     * any ActionListeners.
     */
    private final class ButtonBorderMouseListener extends MouseInputAdapter {

        /**
         * Return true if the mouse is over the cancel or options button
         * @param me mouse event
         * @return true if any armed flag changed
         */
        private boolean isOverButtons(final MouseEvent me) {
            boolean changed = false;
            // If the button is down, we might be outside the component
            // without having had mouseExited invoked.
            if (!_roundedInnerArea.contains(me.getPoint())) {
                changed = _armedCancelButton || _armedOptionsButton;
                // reset:
                _armedCancelButton = false;
                _armedOptionsButton = false;
                setCursor(Cursor.getDefaultCursor());
                return changed;
            }

            // check if the mouse is over the search button:
            boolean armedSearchButton = false;
            if (_searchButtonShape != null) {
                armedSearchButton = _searchButtonShape.contains(me.getPoint());
            }

            // check if the mouse is over the cancel button:
            if (_cancelButtonShape != null) {
                final boolean armed = _cancelButtonShape.contains(me.getPoint());

                if (armed != _armedCancelButton) {
                    _armedCancelButton = armed;
                    changed = true;
                }
            }

            // check if the mouse is over the options button:
            if (_optionsButtonShape != null) {
                final boolean armed = _optionsButtonShape.contains(me.getPoint());

                if (armed != _armedOptionsButton) {
                    _armedOptionsButton = armed;
                    changed = true;
                }
            }

            setCursor((armedSearchButton || _armedCancelButton || _armedOptionsButton)
                    ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

            return changed;
        }

        /**
         * Handle mouse event i.e. test mouse over buttons (arm) and repaint if needed
         * @param me mouse event
         */
        private void handleMouseEvent(final MouseEvent me) {
            if (isOverButtons(me)) {
                repaint();
            }
        }

        @Override
        public void mouseMoved(final MouseEvent me) {
            handleMouseEvent(me);
        }

        @Override
        public void mouseDragged(final MouseEvent me) {
            handleMouseEvent(me);
        }

        @Override
        public void mouseEntered(final MouseEvent me) {
            handleMouseEvent(me);
        }

        @Override
        public void mouseExited(final MouseEvent me) {
            handleMouseEvent(me);
        }

        @Override
        public void mousePressed(final MouseEvent me) {
            handleMouseEvent(me);
        }

        @Override
        public void mouseReleased(final MouseEvent me) {
            isOverButtons(me);

            // enable actions only if the text field is enabled:
            if (SwingUtilities.isLeftMouseButton(me)) {
                if (_armedCancelButton) {
                    handleCancelEdit();
                }
                if (_armedOptionsButton && isEnabled()) {
                    handleShowOptions(me);
                }
            }
            repaint();
        }
    }

    /**
     * Replaces the entered text with a gray placeholder string when the
     * search field doesn't have the focus. The entered text returns when
     * we get the focus back.
     */
    private final class PlaceholderText implements FocusListener {

        /** color used when the field has the focus */
        private Color _previousColor;

        /**
         * Constructor
         */
        PlaceholderText() {
            // get initial text and colors:
            focusLost(null);
        }

        @Override
        public void focusGained(final FocusEvent fe) {
            setForeground(_previousColor);
            setText(_previousText);
            _showingPlaceholderText = false;
        }

        @Override
        public void focusLost(final FocusEvent fe) {
            _previousText = getRealText();
            _previousColor = getForeground();

            // if the field is empty :
            if (StringUtils.isEmpty(_previousText)) {
                _showingPlaceholderText = true;
                setForeground(Color.GRAY);
                setText(_placeholderText);
            }
        }
    }

    /**
     * Main - for StarResolverWidget demonstration and test only.
     * @param args unused
     */
    public static void main(final String[] args) {

        final boolean testOptions = true;

        // GUI initialization
        final JFrame frame = new JFrame("SearchField Demo");

        // Force to exit when the frame closes :
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        final SearchField searchField;
        if (testOptions) {
            final JPopupMenu optionsMenu = new JPopupMenu();

            // Add title
            JMenuItem menuItem = new JMenuItem("Choose Search Option:");
            menuItem.setEnabled(false);
            optionsMenu.add(menuItem);

            // And populate the options:
            menuItem = new JMenuItem("Test option");
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    _logger.info("e = '" + e + "'");
                }
            });

            optionsMenu.add(menuItem);

            searchField = new SearchField("placeHolder", optionsMenu);
        } else {
            searchField = new SearchField("placeHolder");
        }

        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final String value = e.getActionCommand();
                _logger.info("value = '" + value + "'");
            }
        });

        panel.add(searchField, BorderLayout.CENTER);

        frame.getContentPane().add(panel);

        frame.pack();
        frame.setVisible(true);
    }
}
/*___oOo___*/
