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

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.util.WindowUtils;
import fr.jmmc.jmcs.util.StringUtils;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic search panel, with regexp support built-in.
 * You should an SearchPanelDelegate implementation to handle search through your data set.
 * Feel free to use the provided actions in your menu bar.
 *
 * TODO : get search token from dedicated pasteboard (Mac!, Windows?, Linux...).
 * TODO : Handle case-sensitive searches.
 *
 * @author Sylvain LAFRASSE, Laurent BOURGES, Guillaume MELLA.
 */
public final class SearchPanel extends javax.swing.JFrame {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(SearchPanel.class.getName());
    /** A very light red color. */
    private static final Color VERY_LIGHT_RED = new Color(0xFF, 0x80, 0x80);

    /** Quick search direction enumeration */
    public static enum SEARCH_DIRECTION {

        /** previous */
        PREVIOUS,
        /** next */
        NEXT,
        /** reset */
        UNDEFINED
    };
    // Regexp tokens
    /** tokens to replace from "([{\^-=$!|]})?*+." except "*?" */
    private final static String[] REGEXP_TOKEN_FROM = new String[]{
        "(", "[", "{", "\\", "^", "-", "=", "$", "!", "|", "]", "}", ")", "+", ".", "*", "?"
    };
    /** tokens to replace by */
    private final static String[] REGEXP_TOKEN_REPLACE = new String[]{
        "\\(", "\\[", "\\{", "\\\\", "\\^", "\\-", "\\=", "\\$", "\\!", "\\|", "\\]", "\\}", "\\)", "\\+", "\\.", ".*", ".?"
    };
    // Members
    /** Find action */
    private FindAction _findAction;
    /** Find Next action */
    private FindNextAction _findNextAction;
    /** Find Previous action */
    private FindPreviousAction _findPreviousAction;
    /** Search Controler */
    private final SearchPanelDelegate _searchDelegate;

    /** 
     * Creates new form SearchPanel 
     * @param searchDelegate search delegate instance
     */
    public SearchPanel(final SearchPanelDelegate searchDelegate) {
        super("Find");

        setupActions();
        initComponents();

        // Initialize normal background color (LAF issue):
        _searchField.setBackground(Color.WHITE);

        _searchDelegate = searchDelegate;

        WindowUtils.centerOnMainScreen(this);
        WindowUtils.setClosingKeyboardShortcuts(this);
    }

    /** Create required actions */
    private void setupActions() {
        final String classPath = SearchPanel.class.getName();

        _findAction = new FindAction(classPath, "_findAction");
        _findNextAction = new FindNextAction(classPath, "_findNextAction");
        _findPreviousAction = new FindPreviousAction(classPath, "_findPreviousAction");
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

        jPanel1 = new javax.swing.JPanel();
        _findLabel = new javax.swing.JLabel();
        _searchField = new javax.swing.JTextField();
        _regexpCheckBox = new javax.swing.JCheckBox();
        _previousButton = new javax.swing.JButton();
        _nextButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        _findLabel.setText("Find:");
        jPanel1.add(_findLabel, new java.awt.GridBagConstraints());

        _searchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _searchFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(_searchField, gridBagConstraints);

        _regexpCheckBox.setText("Use Regular Expression");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel1.add(_regexpCheckBox, gridBagConstraints);

        _previousButton.setAction(_findPreviousAction);
        _previousButton.setText("Previous");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(_previousButton, gridBagConstraints);

        _nextButton.setAction(_findNextAction);
        _nextButton.setText("Next");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        jPanel1.add(_nextButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void _searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__searchFieldActionPerformed
        doSearch(SEARCH_DIRECTION.UNDEFINED);
    }//GEN-LAST:event__searchFieldActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel _findLabel;
    private javax.swing.JButton _nextButton;
    private javax.swing.JButton _previousButton;
    private javax.swing.JCheckBox _regexpCheckBox;
    private javax.swing.JTextField _searchField;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    /**
     * (Dis)enable menu actions on demand.
     *
     * @param shouldBeEnabled Enables menu if true, disables them otherwise.
     */
    public void enableMenus(final boolean shouldBeEnabled) {
        _findAction.setEnabled(shouldBeEnabled);
        _findNextAction.setEnabled(shouldBeEnabled);
        _findPreviousAction.setEnabled(shouldBeEnabled);
    }

    /**
     * Handle search requests.
     * @param direction Going 'NEXT' or 'PREVIOUS', or reset in 'UNDEFINED'.
     */
    private void doSearch(final SEARCH_DIRECTION direction) {

        final String text = _searchField.getText().trim();
        if (!StringUtils.isEmpty(text)) {

            // Convert search token to standard regexp if not yet in this syntax
            final String regexp;
            if (_regexpCheckBox.isSelected()) {
                regexp = text; // Use given regexp straight away !
            } else {
                regexp = convertToRegExp(text); // Otherwise convert simple syntax to regexp
            }

            // Use insensitive regexp for the time being
            final Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);

            // Performance timer
            final long startTime = System.nanoTime();
            final boolean found = _searchDelegate.search(pattern, direction);
            if (!found) {
                _logger.info("Searched token '{}' not found.", text);
                _searchField.setBackground(VERY_LIGHT_RED);
            } else {
                _searchField.setBackground(Color.WHITE);
            }
            _logger.info("Search done in {} ms.", 1e-6d * (System.nanoTime() - startTime));
        }
    }

    /** Show the Search window when user click the 'Find' menu. */
    protected class FindAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Protected constructor
         * @param classPath
         * @param fieldName 
         */
        FindAction(final String classPath, final String fieldName) {
            super(classPath, fieldName);
            setEnabled(false); // Will be (dis)enabled dynamically on CalibratorView::tableChanged()
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            setVisible(true);
        }
    }

    /** Tries to find the next occurrence of the current searched token. */
    protected class FindNextAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Protected constructor
         * @param classPath
         * @param fieldName 
         */
        FindNextAction(final String classPath, final String fieldName) {
            super(classPath, fieldName);
            setEnabled(false); // Will be (dis)enabled dynamically on CalibratorView::tableChanged()
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            doSearch(SEARCH_DIRECTION.NEXT);
        }
    }

    /** Tries to find the previous occurrence of the current searched token. */
    protected class FindPreviousAction extends RegisteredAction {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Protected constructor
         * @param classPath
         * @param fieldName 
         */
        FindPreviousAction(final String classPath, final String fieldName) {
            super(classPath, fieldName);
            setEnabled(false); // Will be (dis)enabled dynamically on CalibratorView::tableChanged()
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            doSearch(SEARCH_DIRECTION.PREVIOUS);
        }
    }

    /**
     * Convert the given string value to become one regexp:
     * - escape "([{\^-=$!|]})?*+." by using '\' prefix
     * - '*' replaced by '.*'
     * - '?' replaced by '.?'
     * @param value string value
     * @return regexp string
     */
    protected static String convertToRegExp(final String value) {
        final StringBuilder regexp = new StringBuilder(value.length() + 16);

        // Replace non regexp value to '*value*' to performs one contains operation (case sensitive)
        regexp.append("*").append(value).append("*");

        String token, replace;
        for (int i = 0, len = REGEXP_TOKEN_FROM.length; i < len; i++) {
            token = REGEXP_TOKEN_FROM[i];
            replace = REGEXP_TOKEN_REPLACE[i];
            replace(regexp, token, replace);
        }

        return regexp.toString();
    }

    /**
     * Replace the given source string by the destination string in the given string builder.
     * @param sb string builder to process
     * @param source source string
     * @param destination destination string
     */
    private static void replace(final StringBuilder sb, final String source, final String destination) {

        for (int from = 0, position; from != -1;) {
            position = sb.indexOf(source, from);

            if (position == -1) {
                break;
            }

            // ignore escaped string '\source'
            if ((position == 0) || (position > 0 && sb.charAt(position - 1) != '\\')) {
                sb.replace(position, position + source.length(), destination);

                // find from last replaced char (avoid reentrance):
                from = position + destination.length();
            } else {
                // find from last char (avoid reentrance):
                from = position + source.length();
            }
        }
    }
}
