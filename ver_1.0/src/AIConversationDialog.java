import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class AIConversationDialog extends JDialog {
    private final JTextArea questionArea;
    private final JTextArea answerArea;
    private final String context;

    public AIConversationDialog(JFrame owner, String context) {
        super(owner, "AI Recruitment Assistant", false);
        this.context = context;
        setMinimumSize(new Dimension(720, 520));
        setSize(780, 580);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BaseDashboard.APP_BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Ask AI about matching, workload, or applicant risk");
        title.setFont(BaseDashboard.UI_TITLE_FONT);
        root.add(title, BorderLayout.NORTH);

        questionArea = new JTextArea(5, 52);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setText("Which TA should be considered as a safer replacement, and why?");
        answerArea = new JTextArea();
        answerArea.setEditable(false);
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        answerArea.setBackground(BaseDashboard.SURFACE_COLOR);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(new JScrollPane(questionArea), BorderLayout.NORTH);
        center.add(new JScrollPane(answerArea), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton askButton = new JButton("Ask AI");
        JButton closeButton = new JButton("Close");
        BaseDashboard.applyButtonStyle(askButton, BaseDashboard.ACCENT_COLOR, Color.WHITE);
        BaseDashboard.applyButtonStyle(closeButton, new Color(225, 234, 238), BaseDashboard.ACCENT_COLOR);
        actions.add(askButton);
        actions.add(closeButton);
        root.add(actions, BorderLayout.SOUTH);

        askButton.addActionListener(e -> askModel());
        closeButton.addActionListener(e -> dispose());
        add(root);
    }

    private void askModel() {
        answerArea.setText("Thinking with the current recruitment context...");
        String answer = AIConversationService.ask(questionArea.getText(), context);
        answerArea.setText(answer);
        answerArea.setCaretPosition(0);
    }
}
