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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FilterToolbar extends JPanel {
    private final JComboBox<String> fieldSelector;
    private final JTextField searchField;
    private final Map<String, JTextField> mappedFields = new LinkedHashMap<String, JTextField>();
    private boolean syncing;

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
        JButton clearButton = new JButton("Clear");
        BaseDashboard.applyButtonStyle(clearButton, new Color(225, 234, 238), new Color(33, 76, 95));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        controls.add(label);
        controls.add(fieldSelector);
        controls.add(searchField);
        controls.add(clearButton);
        add(controls, BorderLayout.WEST);

        fieldSelector.addActionListener(e -> pullSelectedFieldValue());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { pushSelectedFieldValue(refreshAction); }
            public void removeUpdate(DocumentEvent e) { pushSelectedFieldValue(refreshAction); }
            public void changedUpdate(DocumentEvent e) { pushSelectedFieldValue(refreshAction); }
        });
        clearButton.addActionListener(e -> {
            syncing = true;
            for (JTextField field : mappedFields.values()) {
                if (field != null) {
                    field.setText("");
                }
            }
            searchField.setText("");
            syncing = false;
            refreshAction.run();
        });
    }

    public void addField(String label, JTextField targetField) {
        mappedFields.put(label, targetField);
        fieldSelector.addItem(label);
    }

    private void pullSelectedFieldValue() {
        JTextField target = mappedFields.get(String.valueOf(fieldSelector.getSelectedItem()));
        syncing = true;
        searchField.setText(target == null ? "" : target.getText());
        syncing = false;
    }

    private void pushSelectedFieldValue(Runnable refreshAction) {
        if (syncing) {
            return;
        }
        JTextField target = mappedFields.get(String.valueOf(fieldSelector.getSelectedItem()));
        if (target != null && !target.getText().equals(searchField.getText())) {
            target.setText(searchField.getText());
        } else {
            refreshAction.run();
        }
    }
}
