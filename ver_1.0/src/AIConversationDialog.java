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
import javax.swing.SwingWorker;

public class AIConversationDialog extends JDialog {
    private final JTextArea questionArea;
    private final JTextArea answerArea;
    private final JLabel statusLabel;
    private final JButton askButton;
    private final String context;

    public AIConversationDialog(JFrame owner, String context) {
        super(owner, "AI Recruitment Assistant", false);
        this.context = context;
        setMinimumSize(new Dimension(760, 560));
        setSize(820, 620);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BaseDashboard.APP_BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout(6, 6));
        header.setOpaque(false);
        JLabel title = new JLabel("Ask AI about matching, workload, or applicant risk");
        title.setFont(BaseDashboard.UI_TITLE_FONT);
        statusLabel = new JLabel(AIConversationService.buildStatusText());
        statusLabel.setForeground(new Color(82, 91, 96));
        header.add(title, BorderLayout.NORTH);
        header.add(statusLabel, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        questionArea = new JTextArea(5, 52);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setText("Which TA should be considered as a safer replacement, and why?");
        answerArea = new JTextArea();
        answerArea.setEditable(false);
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        answerArea.setBackground(BaseDashboard.SURFACE_COLOR);
        answerArea.setText("Ask a question to generate model-backed recruitment guidance. If OPENAI_API_KEY is not set, the dialog will use a local explainable fallback.");

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(new JScrollPane(questionArea), BorderLayout.NORTH);
        center.add(new JScrollPane(answerArea), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        askButton = new JButton("Ask AI");
        JButton closeButton = new JButton("Close");
        BaseDashboard.applyButtonStyle(askButton, BaseDashboard.ACCENT_COLOR, Color.WHITE);
        BaseDashboard.applyButtonStyle(closeButton, new Color(225, 234, 238), BaseDashboard.ACCENT_COLOR);
        actions.add(askButton);
        actions.add(closeButton);
        root.add(actions, BorderLayout.SOUTH);

        askButton.addActionListener(e -> askModelAsync());
        closeButton.addActionListener(e -> dispose());
        add(root);
    }

    private void askModelAsync() {
        final String question = questionArea.getText();
        askButton.setEnabled(false);
        statusLabel.setText("Requesting model response...");
        answerArea.setText("Thinking with the current recruitment context...");

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return AIConversationService.ask(question, context);
            }

            @Override
            protected void done() {
                try {
                    answerArea.setText(get());
                } catch (Exception ex) {
                    answerArea.setText("AI request failed: " + ex.getMessage());
                }
                answerArea.setCaretPosition(0);
                statusLabel.setText(AIConversationService.buildStatusText());
                askButton.setEnabled(true);
            }
        };
        worker.execute();
    }
}
