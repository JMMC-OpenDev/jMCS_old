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

import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.StringUtils;
import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;

/**
 * Extends the swing JTable and fixes the default behavior to provide ergonomic scientific softwares.
 * Most tables should use this class as custom creation code.
 * 
 * @author Guillaume MELLA, Laurent BOURGES.
 */
public class NumericJTable extends javax.swing.JTable {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /**
     * Override constructor to change default behavior (default editor, single selection)
     */
    public NumericJTable() {
        super();

        // use custom double editor to fix focus problems:
        final DoubleEditor editor = new DoubleEditor();

        // set one click edition on following table and show all decimals in numerical values
        editor.setClickCountToStart(1);

        setDefaultEditor(Double.class, editor);

        // single table selection :
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Fix lost focus issues on JTable :
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    @Override
    public Component prepareEditor(TableCellEditor editor, int row, int column) {
        final Component c = super.prepareEditor(editor, row, column);

        if (c instanceof JTextComponent) {
            /* use invokeLater because of mouse events default behavior (caret ...) */
            SwingUtils.invokeLaterEDT(new Runnable() {
                @Override
                public void run() {
                    ((JTextComponent) c).selectAll();
                }
            });
        }
        return c;
    }

    /**
     * Custom Number editor fixing focus lost problems on empty cell
     */
    static class DoubleEditor extends DefaultCellEditor {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;
        /** Double value (can be null) */
        private Double _value;

        /**
         * Double editor constructor
         */
        DoubleEditor() {
            super(new JTextField());
            ((JTextField) getComponent()).setHorizontalAlignment(JTextField.RIGHT);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected,
                int row, int column) {
            _value = null;

            ((JComponent) getComponent()).setBorder(new LineBorder(Color.black));

            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            return _value;
        }

        @Override
        public boolean stopCellEditing() {
            final String str = (String) super.getCellEditorValue();
            // Here we are dealing with the case where a user
            // has deleted the string value in a cell, possibly
            // after a failed validation. Return null, so that
            // they have the option to replace the value with
            // null or use escape to restore the original.
            // For Strings, return "" for backward compatibility.
            if (StringUtils.isTrimmedEmpty(str)) {
                // Fix focus lost problem:
                _value = null;
                return super.stopCellEditing();
            }

            try {
                _value = new Double(str);
            } catch (NumberFormatException nfe) {
                ((JComponent) getComponent()).setBorder(new LineBorder(Color.red));
                return false;
            }
            return super.stopCellEditing();
        }
    }
}
