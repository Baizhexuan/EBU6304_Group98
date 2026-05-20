import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A reusable search toolbar that allows users to filter table data by
 * multiple named fields.
 *
 * <p>Fields are registered via {@link #addField(String, JTextField)} and
 * linked to external {@code JTextField} instances that drive
 * {@code RowFilter} logic in the parent panel. When the user selects a
 * field from the dropdown and types a query, the toolbar writes the value
 * into the bound field and triggers the supplied {@code refreshAction}.</p>
 */
public class FilterToolbar extends JPanel {
    private final JComboBox<String> fieldSelector;
    private final JTextField searchField;
    private final JButton searchButton;
    private final Map<String, JTextField> mappedFields = new LinkedHashMap<String, JTextField>();

    /**
     * Constructs a {@code FilterToolbar} with a single free-text search field.
     *
     * @param placeholder  placeholder text shown in the search input
     * @param refreshAction action run whenever the user submits or clears a search
     */
    public FilterToolbar(String placeholder, Runnable refreshAction) {
        super(new BorderLayout(8, 8));
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 220, 224)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel label = new JLabel("Search");
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        fieldSelector = new JComboBox<String>();
        fieldSelector.setPreferredSize(new Dimension(150, 34));
        searchField = new JTextField(placeholder, 24);
        searchField.setPreferredSize(new Dimension(260, 34));
        searchButton = new JButton("Search");
        BaseDashboard.applyButtonStyle(searchButton, new Color(214, 234, 239), new Color(33, 76, 95));
        JButton clearButton = new JButton("Clear");
        BaseDashboard.applyButtonStyle(clearButton, new Color(225, 234, 238), new Color(33, 76, 95));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        controls.add(label);
        controls.add(fieldSelector);
        controls.add(searchField);
        controls.add(searchButton);
        controls.add(clearButton);
        add(controls, BorderLayout.WEST);

        fieldSelector.addActionListener(e -> pullSelectedFieldValue());
        searchField.addActionListener(e -> applySearch(refreshAction));
        searchButton.addActionListener(e -> applySearch(refreshAction));
        clearButton.addActionListener(e -> {
            for (JTextField field : mappedFields.values()) {
                if (field != null) {
                    field.setText("");
                }
            }
            searchField.setText("");
            refreshAction.run();
        });
    }

    /**
     * Registers a named field and binds it to an external {@code JTextField}.
     *
     * <p>The label is added to the field-selector dropdown. When selected,
     * the toolbar synchronises the search input with {@code targetField} so
     * the parent panel's {@code RowFilter} can apply the query.</p>
     *
     * @param label       display name for the field in the dropdown
     * @param targetField the external text field driven by this toolbar entry
     */
    public void addField(String label, JTextField targetField) {
        mappedFields.put(label, targetField);
        fieldSelector.addItem(label);
        if (fieldSelector.getItemCount() == 1) {
            fieldSelector.setSelectedIndex(0);
            pullSelectedFieldValue();
        }
    }

    private void pullSelectedFieldValue() {
        JTextField target = mappedFields.get(String.valueOf(fieldSelector.getSelectedItem()));
        searchField.setText(target == null ? "" : target.getText());
    }

    private void applySearch(Runnable refreshAction) {
        JTextField target = mappedFields.get(String.valueOf(fieldSelector.getSelectedItem()));
        if (target != null) {
            target.setText(searchField.getText());
        }
        refreshAction.run();
    }
}
