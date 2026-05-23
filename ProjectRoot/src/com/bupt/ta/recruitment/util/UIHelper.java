package com.bupt.ta.recruitment.util;

import java.awt.Color;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class UIHelper {
    public static final Color SUCCESS = new Color(214, 245, 214);
    public static final Color WARNING = new Color(255, 242, 204);
    public static final Color DANGER = new Color(255, 221, 221);
    public static final Color INFO = new Color(226, 239, 255);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private UIHelper() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidGpa(String gpaText) {
        try {
            double value = Double.parseDouble(gpaText.trim());
            return value >= 0.0 && value <= 4.0;
        } catch (Exception e) {
            return false;
        }
    }

    public static TableRowSorter<TableModel> installSorter(JTable table, int defaultSortColumn) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        if (defaultSortColumn >= 0 && defaultSortColumn < table.getColumnCount()) {
            sorter.setSortKeys(java.util.Collections.singletonList(new RowSorter.SortKey(defaultSortColumn, SortOrder.ASCENDING)));
        }
        return sorter;
    }
}
