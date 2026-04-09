package com.bupt.ta.recruitment.util;

import java.awt.Color;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public final class UIHelper {
    public static final Color STATUS_PENDING = new Color(255, 242, 204);
    public static final Color STATUS_SELECTED = new Color(217, 234, 211);
    public static final Color STATUS_REJECTED = new Color(244, 204, 204);
    public static final Color STATUS_INFO = new Color(220, 230, 241);

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private UIHelper() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidGpa(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            double gpa = Double.parseDouble(value.trim());
            return gpa >= 0.0 && gpa <= 4.0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public static TableRowSorter<TableModel> installSorter(JTable table, int defaultColumn) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        if (defaultColumn >= 0 && defaultColumn < table.getColumnCount()) {
            sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(defaultColumn, SortOrder.ASCENDING)));
        }
        return sorter;
    }
}
