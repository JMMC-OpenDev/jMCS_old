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

import fr.jmmc.jmcs.util.StringUtils;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class is useful to adjust the column width of a JTable according to its content.
 * 
 * @author Laurent Bourges.
 */
public final class AutofitTableColumns {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(AutofitTableColumns.class.getName());
    /** internal debug flag */
    private static final boolean DEBUG = false;
    /** maximum width for a column header */
    private static final int MAX_WIDTH_HEADER = 50;
    /** header padding */
    private static final int DEFAULT_COLUMN_PADDING_HEADER = 20;
    /** padding */
    private static final int DEFAULT_COLUMN_PADDING = 20;

    /**
     * Hidden constructor
     */
    private AutofitTableColumns() {
        // not used
    }

    /**
     * Adjust columns width taking into account the content of the table and the used font with default column padding
     * @param aTable the JTable to auto-resize the columns on
     * @return table width
     */
    public static int autoResizeTable(final JTable aTable) {
        return autoResizeTable(aTable, true, true);
    }

    /**
     * Adjust columns width taking into account the content of the table and the used font with default column padding
     * @param aTable the JTable to auto-resize the columns on
     * @param useRendererText flag to use renderer text value instead of JTable cell value
     * @return table width
     */
    public static int autoResizeTable(final JTable aTable, final boolean useRendererText) {
        return autoResizeTable(aTable, true, useRendererText);
    }

    /**
     * Adjust header and columns width taking into account the content of the table and the used font with default column padding
     * @param aTable the JTable to auto-resize the columns on
     * @param includeColumnHeaderWidth use the Column Header width as a minimum width
     * @param useRendererText flag to use renderer text value instead of JTable cell value
     * @return table width
     */
    public static int autoResizeTable(final JTable aTable, final boolean includeColumnHeaderWidth, final boolean useRendererText) {
        return autoResizeTable(aTable, includeColumnHeaderWidth, DEFAULT_COLUMN_PADDING, useRendererText);
    }

    /**
     * Adjust header and columns width taking into account the content of the table and the used font
     * @param aTable the JTable to auto-resize the columns on
     * @param includeColumnHeaderWidth use the Column Header width as a minimum width
     * @param columnPadding how many extra pixels do you want on the end of each column
     * @param useRendererText flag to use renderer text value instead of JTable cell value
     * @return table width
     */
    private static int autoResizeTable(final JTable aTable, final boolean includeColumnHeaderWidth, final int columnPadding,
            final boolean useRendererText) {

        final long startTime = System.nanoTime();

        int tableWidth = 0;

        final int columnCount = aTable.getColumnCount();

        // must have columns :
        if (columnCount > 0) {
            final Dimension interCellSpacing = aTable.getIntercellSpacing();

            // STEP ONE : Work out the column widths

            final int columnWidth[] = new int[columnCount];

            for (int i = 0; i < columnCount; i++) {
                columnWidth[i] = getMaxColumnWidth(aTable, i, includeColumnHeaderWidth, columnPadding, useRendererText);
                tableWidth += columnWidth[i];
            }

            // account for cell spacing too
            tableWidth += ((columnCount - 1) * interCellSpacing.width);

            // STEP TWO : Dynamically resize each column

            // try changing the size of the column names area
            final JTableHeader tableHeader = aTable.getTableHeader();
            final Dimension headerDim = tableHeader.getPreferredSize();
            headerDim.width = tableWidth;
            tableHeader.setMinimumSize(headerDim);
            tableHeader.setPreferredSize(headerDim);

            final TableColumnModel tableColumnModel = aTable.getColumnModel();
            TableColumn tableColumn;

            for (int i = 0; i < columnCount; i++) {
                tableColumn = tableColumnModel.getColumn(i);
                tableColumn.setPreferredWidth(columnWidth[i]);
            }

            aTable.getPreferredSize().width = tableWidth;

            aTable.invalidate();
            aTable.doLayout();
            aTable.repaint();
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("autoResizeTable done in {} ms.", 1e-6d * (System.nanoTime() - startTime));
        }

        return tableWidth;
    }

    /**
     * Computes the maximum column width according to the content of the table column. 
     * If the table size is big, then process only some of them (1/20)
     * @param aTable the JTable to auto-resize the columns on
     * @param columnNo the column number, starting at zero, to calculate the maximum width on
     * @param includeColumnHeaderWidth use the Column Header width as a minimum width
     * @param columnPadding how many extra pixels do you want on the end of each column
     * @param useRendererText flag to use renderer text value instead of JTable cell value
     * @return table width
     */
    private static int getMaxColumnWidth(final JTable aTable, final int columnNo,
            final boolean includeColumnHeaderWidth, final int columnPadding, final boolean useRendererText) {

        int maxWidth = 0;
        int textWidth = 0;

        TableCellRenderer tableCellRenderer;
        Component comp;
        JTextComponent jtextComp;
        JLabel jLabelComp;
        FontMetrics fontMetrics;

        final TableColumn column = aTable.getColumnModel().getColumn(columnNo);

        if (includeColumnHeaderWidth) {
            final TableCellRenderer headerRenderer = column.getHeaderRenderer();
            if (headerRenderer != null) {
                comp = headerRenderer.getTableCellRendererComponent(aTable, column.getHeaderValue(), false, false, 0, columnNo);

                if (comp instanceof JTextComponent) {
                    jtextComp = (JTextComponent) comp;

                    if (!StringUtils.isEmpty(jtextComp.getText())) {
                        fontMetrics = jtextComp.getFontMetrics(jtextComp.getFont());

                        textWidth = getHeaderWidth(fontMetrics, jtextComp.getText());
                    }
                } else {
                    textWidth = comp.getPreferredSize().width;
                }
            } else {
                try {
                    final String text = (String) column.getHeaderValue();

                    if (!StringUtils.isEmpty(text)) {
                        final JTableHeader tableHeader = aTable.getTableHeader();

                        fontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());

                        textWidth = getHeaderWidth(fontMetrics, text);
                    }
                } catch (ClassCastException ce) {
                    // Can't work out the header column width.
                    textWidth = 0;
                }
            }
            maxWidth = textWidth;
        }

        int cellWidth;

        final int size = aTable.getRowCount();

        final int step;
        final int start;

        // only few values are evaluated for performance reasons:
        if (size > 100) {
            if (size > 5000) {
                step = size / 25;
                start = 13;
            } else {
                step = size / 10;
                start = 3;
            }
        } else {
            step = 1;
            start = 0;
        }

        if (DEBUG && _logger.isDebugEnabled()) {
            _logger.debug("getMaxColumnWidth : table rowCount : " + size + " - step : " + step + " - start : " + start);
        }

        // cache cell renderer :
        tableCellRenderer = null;
        // cache fontMetrics :
        fontMetrics = null;

        Object cellValue;
        String text;

        // skip first cell :
        for (int i = start; i < size; i += step) {
            if (tableCellRenderer == null) {
                tableCellRenderer = aTable.getCellRenderer(i, columnNo);
            }

            cellValue = aTable.getValueAt(i, columnNo);
            if (cellValue != null) {

                comp = tableCellRenderer.getTableCellRendererComponent(aTable, cellValue, false, false, i, columnNo);

                if (comp instanceof DefaultTableCellRenderer) {
                    jLabelComp = ((DefaultTableCellRenderer) comp);

                    if (fontMetrics == null) {
                        fontMetrics = jLabelComp.getFontMetrics(jLabelComp.getFont());
                    }

                    text = (useRendererText) ? jLabelComp.getText() : cellValue.toString();

                    if (useRendererText && text.charAt(0) == '<' && text.startsWith("<html>")) {
                        text = cellValue.toString();
                    }

                    // Hack for double values (truncated):
                    textWidth = SwingUtilities.computeStringWidth(fontMetrics, text);

                    maxWidth = Math.max(maxWidth, textWidth);

                } else if (comp instanceof JTextComponent) {
                    jtextComp = (JTextComponent) comp;

                    if (fontMetrics == null) {
                        fontMetrics = jtextComp.getFontMetrics(jtextComp.getFont());
                    }

                    text = (useRendererText) ? jtextComp.getText() : cellValue.toString();

                    if (useRendererText && text.charAt(0) == '<' && text.startsWith("<html>")) {
                        text = cellValue.toString();
                    }

                    textWidth = SwingUtilities.computeStringWidth(fontMetrics, text);

                    maxWidth = Math.max(maxWidth, textWidth);

                } else {
                    cellWidth = comp.getPreferredSize().width;

                    maxWidth = Math.max(maxWidth, cellWidth);
                }
            }
        }

        maxWidth += columnPadding;

        return maxWidth;
    }

    /**
     * Computes header width (takes care of HTML and multi line text) but limited to MAX_WIDTH_HEADER
     * @param fontMetrics font in use
     * @param text content to analyze
     * @return largest line width
     */
    private static int getHeaderWidth(final FontMetrics fontMetrics, final String text) {
        // note: text must not contain html code as it will be interpreted as text not code:
        int maxWidth = SwingUtilities.computeStringWidth(fontMetrics, text);

        maxWidth += DEFAULT_COLUMN_PADDING_HEADER;
        if (maxWidth > MAX_WIDTH_HEADER) {
            maxWidth = MAX_WIDTH_HEADER;
        }
        return maxWidth;
    }
}
