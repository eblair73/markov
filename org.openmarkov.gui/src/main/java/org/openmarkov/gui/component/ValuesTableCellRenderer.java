/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
/**
 *
 */

package org.openmarkov.gui.component;

import org.openmarkov.gui.configuration.LocalPreferences;
import org.openmarkov.gui.loader.element.IconBind;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * This class is used for painting and coloring the table and the headers
 *
 * @author jlgozalo
 * @version 1.0 15/08/2009
 */
public class ValuesTableCellRenderer extends DefaultTableCellRenderer {
    /**
     * first color to use in header rows
     */
    protected static final Color TABLE_HEADER_TEXT_COLOR_1 = LocalPreferences.TABLE_HEADER_TEXT_COLOR_1.get();
    /**
     * second color to use in header rows
     */
    protected static final Color TABLE_HEADER_TEXT_COLOR_2 = LocalPreferences.TABLE_HEADER_TEXT_COLOR_2.get();
    /**
     * third color to use in header rows
     */
    protected static final Color TABLE_HEADER_TEXT_COLOR_3 = LocalPreferences.TABLE_HEADER_TEXT_COLOR_3.get();
    /**
     * color to use in the background of header rows
     */
    protected static final Color TABLE_HEADER_BACKGROUND_COLOR = new Color(220, 220, 220);
    /**
     * default serial ID
     */
    private static final long serialVersionUID = 1L;
    private static final DecimalFormat formatter = new DecimalFormat("0.######", new DecimalFormatSymbols(Locale.US));
    /**
     * to define the first editable row of the table
     */
    protected int firstEditableRow;
    private final boolean[] uncertaintyInColumns;
    private JLabel jUncertaintyIcon;
    
    /**
     * constructor for the renderer
     *
     * @param firstEditableRow     value of the first editable row
     * @param uncertaintyInColumns boolean array with the columns with (1)/without (0) mark. The
     *                             array only has to contain indexes for the editables columns
     */
    public ValuesTableCellRenderer(int firstEditableRow, boolean[] uncertaintyInColumns) {
        this.uncertaintyInColumns = uncertaintyInColumns;
        this.firstEditableRow = firstEditableRow;
    }
    
    public ValuesTableCellRenderer(int firstEditableRow) {
        this(firstEditableRow, null);
    }
    
    /**
     * headers rows are displayed in a gray background color with red and blue
     * foreground alternatively non headers rows are displayed in an alternative
     * cyan and light gray background color with black foreground color the
     * first two column are in gray
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        setHorizontalAlignment(SwingConstants.CENTER);
        setCellFonts(table, value, isSelected, hasFocus, row, column);
        setCellColors(table, value, isSelected, hasFocus, row, column);
        setCellBorders(table, value, isSelected, hasFocus, row, column);
        setMinimumSize(table, value, isSelected, hasFocus, row, column);
        if (value instanceof Double) {
            value = formatter.format(value);
        }
        if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) && (row >= firstEditableRow) && uncertaintyInColumns != null
                && uncertaintyInColumns[column - 1]) {
            getUncertaintyIcon().setText(value.toString());
            return getUncertaintyIcon();
        } else {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
    
    private void setMinimumSize(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    }
    
    /**
     * set cell fonts
     *
     * @param table      - table where the cell is located
     * @param value      - the value of the cell in edition
     * @param isSelected - true if the cell is selected by the user
     * @param hasFocus   - true if the cell has the focus by the user
     * @param row        - row of the cell
     * @param column     - column of the cell
     */
    private void setCellFonts(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Font sansboldFont = new Font("SansSerif", Font.BOLD, 30);
        Font sansFont = new Font("SansSerif", Font.PLAIN, 14);
        if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) {
            // PARENTS CELLS
            setFont(sansboldFont);
        }
        if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row >= firstEditableRow)) {
            // NODE STATES CELLS
            setFont(sansboldFont);
        }
        if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) {
            // HEADER CELLS
            setFont(sansboldFont);
        }
        if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) & (row >= firstEditableRow)) {
            // DATA CELLS
            setFont(sansFont);
        }
    }
    
    /**
     * set cell colors
     *
     * @param table      - table where the cell is located
     * @param value      - the value of the cell in edition
     * @param isSelected - true if the cell is selected by the user
     * @param hasFocus   - true if the cell has the focus by the user
     * @param row        - row of the cell
     * @param column     - column of the cell
     */
    protected void setCellColors(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
                                 int column) {
        if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) {
            // PARENTS CELLS set alternate colors
            switch (row % 3) {
                case 0:
                    setBackground(TABLE_HEADER_BACKGROUND_COLOR);
                    setForeground(TABLE_HEADER_TEXT_COLOR_1);
                    break;
                case 1:
                    setBackground(TABLE_HEADER_BACKGROUND_COLOR);
                    setForeground(TABLE_HEADER_TEXT_COLOR_2);
                    break;
                case 2:
                    setBackground(TABLE_HEADER_BACKGROUND_COLOR);
                    setForeground(TABLE_HEADER_TEXT_COLOR_3);
                    break;
                default:
                    break;
            }
        }
        if ((column < ValuesTable.FIRST_EDITABLE_COLUMN) & (row >= firstEditableRow)) {
            // NODE STATES CELLS
            setBackground(TABLE_HEADER_BACKGROUND_COLOR);
            setForeground(Color.BLACK);
        }
        if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) & (row < firstEditableRow)) {
            // HEADER CELLS
            switch (row % 3) {
                case 0, 1, 2:
                    setBackground(TABLE_HEADER_BACKGROUND_COLOR);
                    break;
                default:
                    break;
            }
            switch (row % 2) {
                case 0:
                    if (column % 2 == 0) {
                        setForeground(new Color(128, 0, 64));
                    } else {
                        setForeground(Color.BLUE.darker());
                    }
                    break;
                case 1:
                    if (column % 2 == 0) {
                        setForeground(Color.BLUE.darker());
                    } else {
                        setForeground(new Color(128, 0, 64).darker());
                    }
                    break;
                default:
                    break;
            }
        }
        if ((column >= ValuesTable.FIRST_EDITABLE_COLUMN) && firstEditableRow >= 0 && (row >= firstEditableRow)) {
            
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            if (hasFocus) {
                if (table.isCellEditable(row, column)) {
                    setForeground(Color.BLUE);
                    setBackground(Color.YELLOW);
                }
            }
        }
    }
    
    // ESCA-JAVA0173: not considering unused parameters for the method.
    
    /**
     * set cell borders
     *
     * @param table      - table where the cell is located
     * @param value      - the value of the cell in edition
     * @param isSelected - true if the cell is selected by the user
     * @param hasFocus   - true if the cell has the focus by the user
     * @param row        - row of the cell
     * @param column     - column of the cell
     */
    private void setCellBorders(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBorder(new LineBorder(Color.BLACK, 5));
        if (hasFocus) {
            setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            getUncertaintyIcon().setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        } else {
            getUncertaintyIcon().setBorder(noFocusBorder);
        }
    }
    
    /**
     * @return the firstEditableRow
     */
    public int getFirstEditableRow() {
        return firstEditableRow;
    }
    
    /**
     * @param firstEditableRow the firstEditableRow to set
     */
    public void setFirstEditableRow(int firstEditableRow) {
        this.firstEditableRow = firstEditableRow;
    }
    
    /**
     * @param column index of the column to mark
     */
    public void setMark(int column) {
        if (column < uncertaintyInColumns.length) {
            uncertaintyInColumns[column] = true;
        }
    }
    
    /**
     * @param column index of the column to unmark
     */
    public void unMark(int column) {
        if (column < uncertaintyInColumns.length) {
            uncertaintyInColumns[column] = false;
        }
    }
    
    protected JLabel getUncertaintyIcon() {
        if (jUncertaintyIcon == null) {
            jUncertaintyIcon = new JLabel();
            jUncertaintyIcon.setName("jUncertaintyIcon");
            jUncertaintyIcon.setOpaque(true);
            jUncertaintyIcon.setIcon(IconBind.UNCERTAINTY.icon());
            jUncertaintyIcon.setText("Uncertainty");
            jUncertaintyIcon.setHorizontalAlignment(SwingConstants.RIGHT);
            jUncertaintyIcon.setHorizontalTextPosition(SwingConstants.LEFT);
            jUncertaintyIcon.setIconTextGap(0);
            jUncertaintyIcon.setBackground(Color.WHITE);
        }
        return jUncertaintyIcon;
    }
}
