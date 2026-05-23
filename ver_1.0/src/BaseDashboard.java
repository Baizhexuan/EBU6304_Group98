import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JButton;
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
import javax.swing.UIManager;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.plaf.basic.BasicButtonUI;

public abstract class BaseDashboard extends JFrame {
    /** Bold font used for section headings throughout the UI. */
    protected static final Font UI_TITLE_FONT = new Font("SansSerif", Font.BOLD, 15);
    /** Regular font used for body text and table cells. */
    protected static final Font UI_BODY_FONT = new Font("SansSerif", Font.PLAIN, 12);
    /** Background colour for the main window content area. */
    protected static final Color APP_BACKGROUND = new Color(244, 247, 249);
    /** Background colour for cards and panels placed on {@link #APP_BACKGROUND}. */
    protected static final Color SURFACE_COLOR = Color.WHITE;
    /** Primary accent colour used for headings and active elements. */
    protected static final Color ACCENT_COLOR = new Color(19, 61, 111);
    /** Lighter tint of {@link #ACCENT_COLOR} used for hover states and highlights. */
    protected static final Color SOFT_ACCENT = new Color(229, 237, 249);
    /** Colour used for table grid lines and card borders. */
    protected static final Color BORDER_COLOR = new Color(219, 227, 235);
    /** Muted foreground colour used for secondary labels. */
    protected static final Color TEXT_MUTED = new Color(86, 97, 109);
    /** Dark top-navigation colour taken from the prototype. */
    protected static final Color HEADER_COLOR = new Color(17, 58, 106);
    /** Neutral button and filter surface. */
    protected static final Color SECONDARY_SURFACE = new Color(240, 245, 250);
    /** Gentle success surface for recommendation and selected states. */
    protected static final Color SUCCESS_SURFACE = new Color(231, 244, 233);
    /** Gentle warning surface for manual review actions. */
    protected static final Color WARNING_SURFACE = new Color(251, 244, 229);

    /** The currently authenticated user whose data populates this dashboard. */
    protected final User currentUser;
    /** The tabbed pane that hosts each functional section of the dashboard. */
    protected final JTabbedPane tabs;
    /** Bell entry point for notifications and TA-MO messages. */
    private JButton bellButton;
    /** English role title used as the stable translation key for the window title. */
    private final String roleTitle;
    /** Header context label refreshed when the user switches language. */
    private JLabel headerContextLabel;

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
        this.roleTitle = roleTitle;
        setTitle(I18n.t(roleTitle + " - " + currentUser.getSafeDisplayName()));
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
                I18n.t(DemoMetadata.buildAboutMessage()),
                I18n.t(DemoMetadata.AI_MATCH_HELP_TITLE),
                JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        bar.add(helpMenu);
        bar.add(new JLabel("  " + DemoMetadata.VERSION_LABEL + "  "));
        setJMenuBar(bar);

        tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 12));
        tabs.setBackground(SURFACE_COLOR);
        tabs.setForeground(new Color(34, 50, 67));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));

        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(APP_BACKGROUND);
        workspace.add(buildDashboardHeader(roleTitle + " | " + currentUser.getSafeDisplayName()), BorderLayout.NORTH);
        workspace.add(tabs, BorderLayout.CENTER);
        add(workspace, BorderLayout.CENTER);
        refreshBellBadge();
    }

    /**
     * Installs the compact prototype-aligned defaults used by all Swing
     * screens before the first frame is created.
     */
    public static void installPrototypeTheme() {
        Font controlFont = UI_BODY_FONT;
        Font strongFont = new Font("SansSerif", Font.BOLD, 12);
        UIManager.put("Label.font", controlFont);
        UIManager.put("Button.font", strongFont);
        UIManager.put("TextField.font", controlFont);
        UIManager.put("PasswordField.font", controlFont);
        UIManager.put("TextArea.font", controlFont);
        UIManager.put("ComboBox.font", controlFont);
        UIManager.put("Table.font", controlFont);
        UIManager.put("TableHeader.font", strongFont);
        UIManager.put("Menu.font", strongFont);
        UIManager.put("MenuItem.font", controlFont);
        UIManager.put("TabbedPane.font", strongFont);
        UIManager.put("Panel.background", APP_BACKGROUND);
        UIManager.put("OptionPane.background", APP_BACKGROUND);
        UIManager.put("Table.selectionBackground", SOFT_ACCENT);
        UIManager.put("Table.selectionForeground", new Color(25, 43, 62));
        UIManager.put("TextField.background", SURFACE_COLOR);
        UIManager.put("PasswordField.background", SURFACE_COLOR);
        UIManager.put("TextArea.background", SURFACE_COLOR);
        UIManager.put("TextField.border", inputBorder());
        UIManager.put("PasswordField.border", inputBorder());
        UIManager.put("TextArea.border", inputBorder());
        UIManager.put("ComboBox.background", SURFACE_COLOR);
        UIManager.put("ComboBox.border", inputBorder());
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(BORDER_COLOR));
        UIManager.put("TabbedPane.selected", SURFACE_COLOR);
        UIManager.put("TabbedPane.background", APP_BACKGROUND);
    }

    /**
     * Builds the navy portal header shared by entry pages and dashboards.
     *
     * @param contextText right-aligned context shown inside the top bar
     * @return compact header panel aligned with the PDF prototype
     */
    public static JPanel buildPortalHeader(String contextText) {
        return buildPortalHeader(contextText, null);
    }

    public static JPanel buildPortalHeader(String contextText, Runnable languageChangeAction) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(HEADER_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));

        JLabel brand = new JLabel("BUPT International School");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel context = new JLabel(contextText);
        context.setForeground(new Color(229, 238, 249));
        context.setFont(new Font("SansSerif", Font.BOLD, 11));
        context.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(context);
        if (languageChangeAction != null) {
            right.add(I18n.createLanguageSwitcher(languageChangeAction));
        }

        header.add(brand, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildDashboardHeader(String contextText) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(HEADER_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));

        JLabel brand = new JLabel("BUPT International School");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 12));

        headerContextLabel = new JLabel(contextText);
        headerContextLabel.setForeground(new Color(229, 238, 249));
        headerContextLabel.setFont(new Font("SansSerif", Font.BOLD, 11));

        bellButton = new JButton("\uD83D\uDD14");
        bellButton.setToolTipText("Open notifications and messages");
        bellButton.setFocusPainted(false);
        bellButton.setBackground(new Color(236, 244, 252));
        bellButton.setForeground(HEADER_COLOR);
        bellButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        bellButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(204, 222, 239)),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        bellButton.addActionListener(e -> openBellCentre());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(headerContextLabel);
        right.add(I18n.createLanguageSwitcher(this::applyCurrentLanguage));
        right.add(bellButton);

        header.add(brand, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    protected void refreshBellBadge() {
        if (bellButton == null || currentUser == null) {
            return;
        }
        int unread = NotificationService.countUnreadForUser(currentUser.id)
                + MessageService.countUnreadMessagesForUser(currentUser.id);
        bellButton.setText(unread > 0 ? "\uD83D\uDD14 " + unread : "\uD83D\uDD14");
    }

    protected void applyCurrentLanguage() {
        setTitle(I18n.t(roleTitle + " - " + currentUser.getSafeDisplayName()));
        if (headerContextLabel != null) {
            headerContextLabel.setText(I18n.t(roleTitle + " | " + currentUser.getSafeDisplayName()));
        }
        I18n.applyTo(this);
        refreshBellBadge();
    }

    private void openBellCentre() {
        NotificationCenterDialog dialog = new NotificationCenterDialog(this, currentUser, this::refreshBellBadge);
        dialog.setVisible(true);
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
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel heading = new JLabel(title);
        heading.setFont(UI_TITLE_FONT);
        heading.setForeground(new Color(28, 47, 70));
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
        button.setMargin(new Insets(4, 10, 4, 10));
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        resizeButtonToFitText(button);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(foreground.equals(Color.WHITE) ? new Color(13, 48, 89) : BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
    }

    public static void resizeButtonToFitText(javax.swing.AbstractButton button) {
        if (button == null) {
            return;
        }
        FontMetrics metrics = button.getFontMetrics(button.getFont());
        int textWidth = button.getText() == null ? 0 : metrics.stringWidth(button.getText());
        Insets margin = button.getMargin() == null ? new Insets(0, 0, 0, 0) : button.getMargin();
        int width = Math.max(96, textWidth + margin.left + margin.right + 34);
        int height = Math.max(31, metrics.getHeight() + 14);
        Dimension size = new Dimension(width, height);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
    }

    /**
     * Applies the standard appearance to a {@link JTable} used in all dashboards.
     *
     * @param table the table to style
     */
    protected void styleTable(JTable table) {
        TableCellRenderer existingRenderer = table.getDefaultRenderer(Object.class);
        if (existingRenderer == null || existingRenderer.getClass().getName().startsWith("javax.swing")) {
            table.setDefaultRenderer(Object.class, new WrappingTableCellRenderer());
        }
        table.setRowHeight(34);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(233, 238, 243));
        table.setFillsViewportHeight(true);
        table.setFont(UI_BODY_FONT);
        table.setSelectionBackground(SOFT_ACCENT);
        table.setSelectionForeground(new Color(25, 43, 62));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(243, 247, 251));
        table.getTableHeader().setForeground(new Color(34, 50, 67));
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));
    }

    protected static class WrappingTableCellRenderer extends JTextArea implements TableCellRenderer {
        protected WrappingTableCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(false);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            setText(value == null ? "" : I18n.t(String.valueOf(value)));
            setFont(table.getFont());
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            int width = table.getColumnModel().getColumn(column).getWidth();
            setSize(new Dimension(width, Short.MAX_VALUE));
            int preferredHeight = Math.max(34, getPreferredSize().height + 2);
            if (table.getRowHeight(row) < preferredHeight) {
                table.setRowHeight(row, preferredHeight);
            }
            return this;
        }
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
        applyCurrentLanguage();
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

    private static javax.swing.border.Border inputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(198, 211, 225)),
                BorderFactory.createEmptyBorder(4, 7, 4, 7));
    }
}
