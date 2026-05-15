import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;

public abstract class BaseDashboard extends JFrame {
    protected static final Font UI_TITLE_FONT = new Font("SansSerif", Font.BOLD, 16);
    protected static final Font UI_BODY_FONT = new Font("SansSerif", Font.PLAIN, 13);
    protected static final Color APP_BACKGROUND = new Color(247, 248, 249);
    protected static final Color SURFACE_COLOR = Color.WHITE;
    protected static final Color ACCENT_COLOR = new Color(32, 78, 92);
    protected static final Color SOFT_ACCENT = new Color(226, 237, 240);
    protected static final Color BORDER_COLOR = new Color(218, 224, 228);
    protected static final Color TEXT_MUTED = new Color(82, 91, 96);

    protected final User currentUser;
    protected final JTabbedPane tabs;

    protected BaseDashboard(User currentUser, String roleTitle, int width, int height) {
        this.currentUser = currentUser;
        setTitle(roleTitle + " - " + currentUser.getSafeDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);
        setMinimumSize(new java.awt.Dimension(960, 680));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(APP_BACKGROUND);

        JMenuBar bar = new JMenuBar();
        bar.setBackground(SURFACE_COLOR);
        JMenu accountMenu = new JMenu("Account");
        accountMenu.setFont(new Font("SansSerif", Font.BOLD, 13));
        JMenuItem logoutItem = new JMenuItem("Logout");
        logoutItem.addActionListener(e -> logout());
        accountMenu.add(logoutItem);
        bar.add(accountMenu);

        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("SansSerif", Font.BOLD, 13));
        JMenuItem aboutItem = new JMenuItem("About This Build");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
                DemoMetadata.buildAboutMessage(),
                "About This Demo",
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        bar.add(helpMenu);
        bar.add(new JLabel("  " + DemoMetadata.VERSION_LABEL + "  "));
        setJMenuBar(bar);

        tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setBackground(SURFACE_COLOR);
        tabs.setForeground(new Color(45, 54, 58));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        add(tabs, BorderLayout.CENTER);
    }

    protected void addTab(String title, java.awt.Component component) {
        tabs.addTab(title, component);
    }

    protected void installRefreshOnTabSwitch(Runnable refreshAction) {
        tabs.addChangeListener(e -> refreshAction.run());
    }

    protected JScrollPane wrapScrollable(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(APP_BACKGROUND);
        return scrollPane;
    }

    protected JPanel buildSectionIntro(String title, String body) {
        JPanel intro = new JPanel();
        intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        intro.setBackground(SURFACE_COLOR);
        intro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 16));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel summary = new JLabel("<html><div style='width:760px;'>" + body + "</div></html>");
        summary.setFont(UI_BODY_FONT);
        summary.setForeground(TEXT_MUTED);
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);

        intro.add(heading);
        intro.add(new JLabel(" "));
        intro.add(summary);
        return intro;
    }

    protected JLabel buildStatusPill(String text, Color background, Color foreground) {
        JLabel label = new JLabel(" " + text + " ");
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }

    protected void styleActionButton(javax.swing.JButton button, Color background, Color foreground) {
        applyButtonStyle(button, background, foreground);
    }

    public static void applyButtonStyle(javax.swing.JButton button, Color background, Color foreground) {
        button.setUI(new BasicButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setMargin(new Insets(6, 12, 6, 12));
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(Math.max(104, button.getPreferredSize().width), 36));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(foreground.equals(Color.WHITE) ? new Color(22, 64, 78) : new Color(178, 191, 198)),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
    }

    protected void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(232, 236, 238));
        table.setFillsViewportHeight(true);
        table.setFont(UI_BODY_FONT);
        table.setSelectionBackground(new Color(216, 233, 239));
        table.setSelectionForeground(new Color(28, 44, 50));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(239, 243, 245));
        table.getTableHeader().setForeground(new Color(45, 54, 58));
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
    }

    protected JPanel buildActionRow() {
        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        return actions;
    }

    protected void logout() {
        dispose();
        new LoginFrame();
    }
}
