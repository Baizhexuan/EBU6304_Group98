import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
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
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Shared Swing frame base for role-specific dashboards.
 *
 * <p>The base frame centralises account menus, visual styling, tab helpers,
 * refresh feedback, and common table formatting used by TA, MO, and Admin
 * views.</p>
 */
public abstract class BaseDashboard extends JFrame {
    /** Bold font used for section headings throughout the UI. */
    protected static final Font UI_TITLE_FONT = new Font("SansSerif", Font.BOLD, 16);
    /** Regular font used for body text and table cells. */
    protected static final Font UI_BODY_FONT = new Font("SansSerif", Font.PLAIN, 13);
    /** Background colour for the main window content area. */
    protected static final Color APP_BACKGROUND = new Color(247, 248, 249);
    /** Background colour for cards and panels placed on {@link #APP_BACKGROUND}. */
    protected static final Color SURFACE_COLOR = Color.WHITE;
    /** Primary accent colour used for headings and active elements. */
    protected static final Color ACCENT_COLOR = new Color(32, 78, 92);
    /** Lighter tint of {@link #ACCENT_COLOR} used for hover states and highlights. */
    protected static final Color SOFT_ACCENT = new Color(226, 237, 240);
    /** Colour used for table grid lines and card borders. */
    protected static final Color BORDER_COLOR = new Color(218, 224, 228);
    /** Muted foreground colour used for secondary labels. */
    protected static final Color TEXT_MUTED = new Color(82, 91, 96);

    /** The currently authenticated user whose data populates this dashboard. */
    protected final User currentUser;
    /** The tabbed pane that hosts each functional section of the dashboard. */
    protected final JTabbedPane tabs;

    /**
     * Constructs the base dashboard window.
     *
     * @param currentUser the authenticated user
     * @param roleTitle   label shown in the window title bar alongside the username
     * @param width       initial window width in pixels
     * @param height      initial window height in pixels
     */
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

    /**
     * Adds a named tab containing the supplied component.
     *
     * @param title     tab label shown in the tabbed pane
     * @param component content panel displayed when the tab is selected
     */
    protected void addTab(String title, java.awt.Component component) {
        tabs.addTab(title, component);
    }

    /**
     * Installs a change listener that calls {@code refreshAction} whenever the
     * user switches to a different tab.
     *
     * @param refreshAction action run on every tab-selection change event
     */
    protected void installRefreshOnTabSwitch(Runnable refreshAction) {
        tabs.addChangeListener(e -> refreshAction.run());
    }

    /**
     * Wraps a component in a styled {@link JScrollPane}.
     *
     * @param component the component to wrap
     * @return a scroll pane ready to be added to a layout
     */
    protected JScrollPane wrapScrollable(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(APP_BACKGROUND);
        return scrollPane;
    }

    /**
     * Builds a styled section header panel containing a bold title and a
     * descriptive body paragraph.
     *
     * @param title section heading text
     * @param body  HTML-safe body text rendered below the heading
     * @return a panel ready to be placed above a table or form
     */
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

    /**
     * Builds a small coloured pill label used to represent status values.
     *
     * @param text       text displayed inside the pill
     * @param background pill background colour
     * @param foreground pill text colour
     * @return a styled label suitable for inline status display
     */
    protected JLabel buildStatusPill(String text, Color background, Color foreground) {
        JLabel label = new JLabel(" " + text + " ");
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        return label;
    }

    /**
     * Applies the standard button appearance to the given button.
     *
     * @param button     button to style
     * @param background fill colour
     * @param foreground text and border colour
     */
    protected void styleActionButton(javax.swing.JButton button, Color background, Color foreground) {
        applyButtonStyle(button, background, foreground);
    }

    /**
     * Static variant of {@link #styleActionButton} so non-subclass code can
     * apply the same styling (e.g. {@link FilterToolbar}).
     *
     * @param button     button to style
     * @param background fill colour
     * @param foreground text and border colour
     */
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

    /**
     * Applies the standard appearance to a {@link JTable} used in all dashboards.
     *
     * @param table the table to style
     */
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

    /**
     * Creates an empty right-aligned action button row.
     *
     * @return panel with {@code FlowLayout.RIGHT} ready for buttons
     */
    protected JPanel buildActionRow() {
        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        return actions;
    }

    /**
     * Prepares the UI for a potentially slow data refresh operation by showing
     * a wait cursor, clearing the table model, and updating status labels.
     *
     * @param statusLabel label to update with {@code message} (may be {@code null})
     * @param model       table model to clear (may be {@code null})
     * @param detailArea  text area to show a waiting message (may be {@code null})
     * @param message     status text to display while loading
     */
    protected void beginRefreshFeedback(JLabel statusLabel, DefaultTableModel model, JTextArea detailArea,
            String message) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (model != null) {
            model.setRowCount(0);
        }
        if (detailArea != null) {
            detailArea.setText("Refreshing...\nPlease wait a moment.");
            paintNow(detailArea);
        }
        if (statusLabel != null) {
            statusLabel.setText(message);
            paintNow(statusLabel);
        }
    }

    /**
     * Restores the default cursor after a refresh operation completes.
     */
    protected void endRefreshFeedback() {
        setCursor(Cursor.getDefaultCursor());
    }

    private void paintNow(Component component) {
        if (component instanceof javax.swing.JComponent && component.isShowing()) {
            javax.swing.JComponent swingComponent = (javax.swing.JComponent) component;
            swingComponent.paintImmediately(0, 0, swingComponent.getWidth(), swingComponent.getHeight());
        }
    }

    /**
     * Logs out the current user by disposing this window and returning to the
     * {@link LoginFrame}.
     */
    protected void logout() {
        dispose();
        new LoginFrame();
    }
}
