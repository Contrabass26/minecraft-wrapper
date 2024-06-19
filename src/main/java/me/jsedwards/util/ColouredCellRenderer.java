package me.jsedwards.util;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ColouredCellRenderer extends JLabel implements TableCellRenderer {

    public ColouredCellRenderer() {
        super();
        this.setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setBackground((Color) value);
        if (row == table.getSelectedRow()) {
            this.setBorder(new LineBorder(Color.BLACK, 1));
        } else {
            this.setBorder(new LineBorder(Color.BLACK, 0));
        }
        return this;
    }
}
