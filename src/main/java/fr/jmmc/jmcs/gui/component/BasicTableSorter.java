package fr.jmmc.jmcs.gui.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TableSorter is a decorator for TableModels; adding sorting
 * functionality to a supplied TableModel. TableSorter does
 * not store or copy the data in its TableModel; instead it maintains
 * a map from the row indexes of the view to the row indexes of the
 * model. As requests are made of the sorter (like getValueAt(row, col))
 * they are passed to the underlying model after the row numbers
 * have been translated via the internal mapping array. This way,
 * the TableSorter appears to hold another copy of the table
 * with the rows in a different order.
 * TableSorter registers itself as a listener to the underlying model,
 * just as the JTable itself would. Events recieved from the model
 * are examined, sometimes manipulated (typically widened), and then
 * passed on to the TableSorter's listeners (typically the JTable).
 * If a change to the model has invalidated the order of TableSorter's
 * rows, a note of this is made and the sorter will resort the
 * rows the next time a value is requested.
 * When the tableHeader property is set, either by using the
 * setTableHeader() method or the two argument constructor, the
 * table header may be used as a complete UI for TableSorter.
 * The default renderer of the tableHeader is decorated with a renderer
 * that indicates the sorting status of each column. In addition,
 * a mouse listener is installed with the following behavior:
 * <ul>
 * <li>
 * Mouse-click: Clears the sorting status of all other columns
 * and advances the sorting status of that column through three
 * values: {NOT_SORTED, ASCENDING, DESCENDING} (then back to
 * NOT_SORTED again).
 * <li>
 * SHIFT-mouse-click: Clears the sorting status of all other columns
 * and cycles the sorting status of the column through the same
 * three values, in the opposite order: {NOT_SORTED, DESCENDING, ASCENDING}.
 * <li>
 * CONTROL-mouse-click and CONTROL-SHIFT-mouse-click: as above except
 * that the changes to the column do not cancel the statuses of columns
 * that are already sorting - giving a way to initiate a compound
 * sort.
 * </ul>
 * This is a long overdue rewrite of a class of the same name that
 * first appeared in the swing table demos in 1997.
 *
 * @author Philip Milne
 * @author Brendon McLean
 * @author Dan van Enckevort
 * @author Parwinder Sekhon
 * @version 2.0 02/27/04
 */
public final class BasicTableSorter extends AbstractTableModel {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(BasicTableSorter.class.getName());
    /** Ascending Sort */
    public static final int ASCENDING = 1;
    /** Not Sorted */
    public static final int NOT_SORTED = 0;
    /** Descending Sort */
    public static final int DESCENDING = -1;
    /** empty sort directive*/
    private static Directive EMPTY_DIRECTIVE = new Directive(-1, NOT_SORTED, null, null);
    /**
     * comparison based on Comparable interface
     */
    public static final Comparator<Object> COMPARABLE_COMPARATOR = new Comparator<Object>() {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(final Object o1, final Object o2) {
            return ((Comparable) o1).compareTo(o2);
        }
    };
    /**
     * Lexical comparison ie compare String values ignoring case.
     */
    public static final Comparator<Object> LEXICAL_COMPARATOR = new Comparator<Object>() {
        @Override
        public int compare(final Object o1, final Object o2) {
            return o1.toString().compareToIgnoreCase(o2.toString());
        }
    };

    /* members */
    private TableModel tableModel;
    private Row[] viewToModel;
    private int[] modelToView;
    private JTableHeader tableHeader;
    private MouseListener mouseListener;
    TableModelListener tableModelListener;
    private final List<Directive> sortingColumns = new ArrayList<Directive>(4);
    /**
     * Indirection array.
     *
     * Contains the model column for any given displayed column.
     * modelColumn = _viewIndex[viewColumn];
     */
    private int[] _viewIndex;

    /**
     * Creates a new TableSorter object.
     *
     * @param tableModel
     * @param tableHeader
     */
    public BasicTableSorter(TableModel tableModel, JTableHeader tableHeader) {
        this.mouseListener = new MouseHandler();
        this.tableModelListener = new TableModelHandler();
        
        tableModel.addTableModelListener(tableModelListener);
        setTableHeader(tableHeader);
        setTableModel(tableModel);
        
        computeColumnsIndirectionArray();
    }
    
    private void clearSortingState() {
        viewToModel = null;
        modelToView = null;
    }
    
    public TableModel getTableModel() {
        return tableModel;
    }
    
    public void setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;

        // Multiple table model listeners => ordering side effects !!!
        clearSortingState();
        fireTableStructureChanged();
    }
    
    public JTableHeader getTableHeader() {
        return tableHeader;
    }
    
    public void setTableHeader(JTableHeader tableHeader) {
        if (this.tableHeader != null) {
            this.tableHeader.removeMouseListener(mouseListener);
            
            TableCellRenderer defaultRenderer = this.tableHeader.getDefaultRenderer();
            
            if (defaultRenderer instanceof SortableHeaderRenderer) {
                this.tableHeader.setDefaultRenderer(((SortableHeaderRenderer) defaultRenderer).tableCellRenderer);
            }
        }
        
        this.tableHeader = tableHeader;
        
        if (this.tableHeader != null) {
            this.tableHeader.addMouseListener(mouseListener);
            this.tableHeader.setDefaultRenderer(new SortableHeaderRenderer(this.tableHeader.getDefaultRenderer()));
        }
    }
    
    public boolean isSorting() {
        return !sortingColumns.isEmpty();
    }
    
    private Directive getDirective(final int column) {
        for (Directive directive : sortingColumns) {
            if (directive.column == column) {
                return directive;
            }
        }
        
        return EMPTY_DIRECTIVE;
    }
    
    public int getSortingStatus(int column) {
        return getDirective(_viewIndex[column]).direction;
    }
    
    private void sortingStatusChanged() {
        clearSortingState();
        fireTableDataChanged();
        
        if (tableHeader != null) {
            tableHeader.repaint();
        }
    }
    
    public void setSortingStatus(final int column, final int status) {
        final int realColumn = _viewIndex[column];
        
        final Directive directive = getDirective(realColumn);
        
        if (directive != EMPTY_DIRECTIVE) {
            sortingColumns.remove(directive);
        }
        
        if (status != NOT_SORTED) {
            sortingColumns.add(createDirective(realColumn, status));
        }
        
        sortingStatusChanged();
    }
    
    Icon getHeaderRendererIcon(final int column, final int size) {
        final Directive directive = getDirective(_viewIndex[column]);
        
        if (directive == EMPTY_DIRECTIVE) {
            return null;
        }
        
        return new Arrow(directive.direction == DESCENDING, size, sortingColumns.indexOf(directive));
    }
    
    private void cancelSorting() {
        sortingColumns.clear();
        sortingStatusChanged();
    }
    
    private void checkSortingState() {
        for (ListIterator<Directive> it = sortingColumns.listIterator(); it.hasNext();) {
            Directive directive = it.next();

            // Get the current column index given its name:
            int columnId = -1;
            for (int i = 0, len = tableModel.getColumnCount(); i < len; i++) {
                if (directive.colName.equals(tableModel.getColumnName(i))) {
                    columnId = i;
                    break;
                }
            }

            // check column indexes:
            if (columnId != directive.column) {
                if (columnId != -1) {
                    // update directive:
                    directive = createDirective(columnId, directive.direction);
                    _logger.debug("update sorting directive: {}", directive);
                    it.set(directive);
                } else {
                    // missing column:
                    _logger.debug("remove sorting directive: {}", directive);
                    it.remove();
                }
            }
        }
        clearSortingState();
    }
    
    private Directive createDirective(final int realColumn, final int direction) {
        // note: may return a virtual column name (AA, AB, etc.) if realColumn > column count
        // see AbstractTableModel.getColumnName(int)
        final String colName = tableModel.getColumnName(realColumn);
        
        return new Directive(realColumn, direction, getComparator(realColumn), colName);
    }
    
    private Comparator<Object> getComparator(final int realColumn) {
        final Class<?> columnType = tableModel.getColumnClass(realColumn);
        
        if (String.class == columnType) {
            return LEXICAL_COMPARATOR;
        }
        
        if (Comparable.class.isAssignableFrom(columnType)) {
            return COMPARABLE_COMPARATOR;
        }
        
        return LEXICAL_COMPARATOR;
    }
    
    private Row[] getViewToModel() {
        if (viewToModel == null) {
            
            final int tableModelRowCount = tableModel.getRowCount();
            final Row[] newModel = new Row[tableModelRowCount];
            
            for (int row = 0; row < tableModelRowCount; row++) {
                newModel[row] = new Row(row);
            }
            
            if (isSorting()) {
                final long start = System.nanoTime();
                
                Arrays.sort(newModel);
                
                if (_logger.isDebugEnabled()) {
                    _logger.debug("sort ({} stars) processed in {} ms.", tableModelRowCount, 1e-6d * (System.nanoTime() - start));
                }
            }

            // update model once:
            viewToModel = newModel;
        }
        
        return viewToModel;
    }
    
    public int modelIndex(final int viewIndex) {
        return getViewToModel()[viewIndex].modelIndex;
    }
    
    private int[] getModelToView() {
        if (modelToView == null) {
            final Row[] viewModel = getViewToModel();
            final int len = viewModel.length;
            
            modelToView = new int[len];
            
            for (int i = 0; i < len; i++) {
                modelToView[viewModel[i].modelIndex] = i;
            }
        }
        
        return modelToView;
    }
    
    public int viewIndex(final int modelIndex) {
        return getModelToView()[modelIndex];
    }

    /**
     * Find the column index in the view
     * @param modelColumn model column index
     * @return view index of this column or -1 if not found
     */
    public int findColumnViewIndex(final int modelColumn) {
        if (modelColumn != -1) {
            for (int i = 0, len = _viewIndex.length; i < len; i++) {
                if (_viewIndex[i] == modelColumn) {
                    return i;
                }
            }
        }
        return -1;
    }

    // TableModel interface methods
    @Override
    public int getRowCount() {
        return (tableModel == null) ? 0 : tableModel.getRowCount();
    }
    
    @Override
    public int getColumnCount() {
        int nbOfColumns = 0;
        
        if (tableModel != null) {
            // If the table is empty, should show NO columns at all.
            nbOfColumns = Math.min(tableModel.getColumnCount(), _viewIndex.length);
        }
        
        return nbOfColumns;
    }
    
    @Override
    public String getColumnName(final int column) {
        return tableModel.getColumnName(_viewIndex[column]);
    }
    
    @Override
    public Class<?> getColumnClass(final int column) {
        return tableModel.getColumnClass(_viewIndex[column]);
    }
    
    @Override
    public boolean isCellEditable(final int row, final int column) {
        return tableModel.isCellEditable(modelIndex(row), _viewIndex[column]);
    }
    
    @Override
    public Object getValueAt(final int row, final int column) {
        return tableModel.getValueAt(modelIndex(row), _viewIndex[column]);
    }
    
    @Override
    public void setValueAt(final Object aValue, final int row, final int column) {
        tableModel.setValueAt(aValue, modelIndex(row), _viewIndex[column]);
    }

    /**
     * Automatically called whenever the observed model changed
     */
    public void computeColumnsIndirectionArray() {
        // Get column count in the model:
        final int nbOfModelColumns = tableModel.getColumnCount();

        // Full view, with all columns
        // allocate corresponding memory for the indirection array
        _viewIndex = new int[nbOfModelColumns];

        // Generate a 'one to one' indirection array to show every single column
        for (int i = 0; i < nbOfModelColumns; i++) {
            _viewIndex[i] = i;
        }
    }

    // Helper classes
    private final class Row implements Comparable<Row> {
        
        final int modelIndex;
        
        Row(final int index) {
            this.modelIndex = index;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public int compareTo(final Row other) {
            final int row1 = modelIndex;
            final int row2 = other.modelIndex;
            
            int column, comparison;
            Object o1, o2;
            Directive directive;
            
            for (int i = 0, len = sortingColumns.size(); i < len; i++) {
                directive = sortingColumns.get(i);
                column = directive.column;
                o1 = tableModel.getValueAt(row1, column);
                o2 = tableModel.getValueAt(row2, column);

                // Define null less than everything, except null.
                if ((o1 == null) && (o2 == null)) {
                    comparison = 0;
                } else if (o1 == null) {
                    comparison = -1;
                } else if (o2 == null) {
                    comparison = 1;
                } else {
                    comparison = directive.comparator.compare(o1, o2);
                }
                
                if (comparison != 0) {
                    return (directive.direction == DESCENDING) ? -comparison : comparison;
                }
            }
            
            return 0;
        }
    }
    
    private final class TableModelHandler implements TableModelListener {
        
        @Override
        public void tableChanged(TableModelEvent e) {
            
            computeColumnsIndirectionArray();

            // If we're not sorting by anything, just pass the event along.
            if (!isSorting()) {
                clearSortingState();
                fireTableChanged(e);
                
                return;
            }

            // If the table structure has changed, cancel the sorting; the
            // sorting columns may have been either moved or deleted from
            // the model.
            if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                // 2017: maintain sorting enabled when the user enable/disable filters or dynamic columns are updated (vis2 / dist)
                checkSortingState();
                fireTableChanged(e);
                
                return;
            }

            // We can map a cell event through to the view without widening
            // when the following conditions apply:
            //
            // a) all the changes are on one row (e.getFirstRow() == e.getLastRow()) and,
            // b) all the changes are in one column (column != TableModelEvent.ALL_COLUMNS) and,
            // c) we are not sorting on that column (getSortingStatus(column) == NOT_SORTED) and,
            // d) a reverse lookup will not trigger a sort (modelToView != null)
            //
            // Note: INSERT and DELETE events fail this test as they have column == ALL_COLUMNS.
            //
            // The last check, for (modelToView != null) is to see if modelToView
            // is already allocated. If we don't do this check; sorting can become
            // a performance bottleneck for applications where cells
            // change rapidly in different parts of the table. If cells
            // change alternately in the sorting column and then outside of
            // it this class can end up re-sorting on alternate cell updates -
            // which can be a performance problem for large tables. The last
            // clause avoids this problem.
            int column = e.getColumn();
            
            if ((e.getFirstRow() == e.getLastRow())
                    && (column != TableModelEvent.ALL_COLUMNS)
                    && (getSortingStatus(column) == NOT_SORTED)
                    && (modelToView != null)) {
                int viewIndex = getModelToView()[e.getFirstRow()];
                fireTableChanged(new TableModelEvent(BasicTableSorter.this,
                        viewIndex, viewIndex, column, e.getType()));
                
                return;
            }

            // Something has happened to the data that may have invalidated the row order.
            clearSortingState();
            fireTableDataChanged();
        }
    }
    
    private final class MouseHandler extends MouseAdapter {
        
        @Override
        public void mouseClicked(MouseEvent e) {
            JTableHeader h = (JTableHeader) e.getSource();
            TableColumnModel columnModel = h.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = columnModel.getColumn(viewColumn).getModelIndex();
            
            if (column != -1) {
                int status = getSortingStatus(column);
                
                if (!e.isControlDown()) {
                    cancelSorting();
                }

                // Cycle the sorting states through {NOT_SORTED, ASCENDING, DESCENDING} or
                // {NOT_SORTED, DESCENDING, ASCENDING} depending on whether shift is pressed.
                status += (e.isShiftDown() ? (-1) : 1);
                status = ((status + 4) % 3) - 1; // signed mod, returning {-1, 0, 1}
                setSortingStatus(column, status);
            }
        }
    }
    
    private final static class Arrow implements Icon {
        
        private final boolean descending;
        private final int size;
        private final int priority;
        
        Arrow(final boolean descending, final int size, final int priority) {
            this.descending = descending;
            this.size = size;
            this.priority = priority;
        }
        
        @Override
        public void paintIcon(final Component c, final Graphics g, final int x, int y) {
            final Color color = (c == null) ? Color.red : c.getBackground();

            // In a compound sort, make each succesive triangle 20%
            // smaller than the previous one.
            final int dx = (int) (size / 2d * Math.pow(0.8d, priority));
            final int dy = descending ? dx : (-dx);

            // Align icon (roughly) with font baseline.
            final int bl = y + ((5 * size) / 6) + (descending ? (-dy) : 0);
            
            final int shift = descending ? 1 : (-1);
            g.translate(x, bl);

            // Right diagonal.
            g.setColor(color.darker());
            g.drawLine(dx / 2, dy, 0, 0);
            g.drawLine(dx / 2, dy + shift, 0, shift);

            // Left diagonal.
            g.setColor(color.brighter());
            g.drawLine(dx / 2, dy, dx, 0);
            g.drawLine(dx / 2, dy + shift, dx, shift);

            // Horizontal line.
            if (descending) {
                g.setColor(color.darker().darker());
            } else {
                g.setColor(color.brighter().brighter());
            }
            
            g.drawLine(dx, 0, 0, 0);
            
            g.setColor(color);
            g.translate(-x, -bl);
        }
        
        @Override
        public int getIconWidth() {
            return size;
        }
        
        @Override
        public int getIconHeight() {
            return size;
        }
    }
    
    private final class SortableHeaderRenderer implements TableCellRenderer {

        /* members */
        /** parent table cell header renderer */
        final TableCellRenderer tableCellRenderer;
        /** internal string buffer */
        final StringBuilder _buffer = new StringBuilder(128);

        /**
         * Protected constructor
         * @param tableCellRenderer parent  table cell header renderer
         */
        SortableHeaderRenderer(final TableCellRenderer tableCellRenderer) {
            this.tableCellRenderer = tableCellRenderer;
        }
        
        @Override
        public Component getTableCellRendererComponent(final JTable table, final Object value,
                                                       final boolean isSelected, final boolean hasFocus,
                                                       final int row, final int column) {
            
            final Component c = tableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (c instanceof JLabel) {
                final JLabel label = (JLabel) c;
                label.setHorizontalTextPosition(JLabel.LEFT);
                
                final int colIndex = table.convertColumnIndexToModel(column);
                
                if (colIndex != -1) {
                    label.setIcon(getHeaderRendererIcon(colIndex, label.getFont().getSize()));
                    
                    if (false) {
                        // Set the column header tooltip (with unit if any)
                        final int viewColumn = _viewIndex[colIndex];

                        // TODO: use a column metadata provider to get such information
                        String tooltip = "";
                        final String unit = "";

                        // If a unit was found
                        if (unit.length() != 0) {
                            final StringBuilder sb = _buffer;

                            // If a description was found
                            if (tooltip.length() != 0) {
                                // Add a space separator between description and unit
                                sb.append(tooltip).append(' ');
                            }

                            // Append the unit
                            tooltip = sb.append('(').append(unit).append(')').toString();
                            
                            sb.setLength(0); // recycle buffer
                        }
                        label.setToolTipText(tooltip);
                    }
                }
            }

            // Return the component
            return c;
        }
    }
    
    private final static class Directive {
        
        final int column;
        /* column name used to ensure consistency */
        final String colName;
        final int direction;
        final Comparator<Object> comparator;
        
        Directive(final int column, final int direction, final Comparator<Object> comparator, final String colName) {
            this.column = column;
            this.colName = colName;
            this.direction = direction;
            this.comparator = comparator;
        }
        
        @Override
        public String toString() {
            return "Directive{" + "column=" + column + ", colName=" + colName + ", direction=" + direction + '}';
        }
    }
}
