import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

public abstract class BaseDashboard extends JFrame {
    protected static final Color APP_BACKGROUND = new Color(245, 242, 235);
    protected static final Color SURFACE_COLOR = new Color(255, 252, 247);
    protected static final Color ACCENT_COLOR = new Color(33, 76, 95);
    protected static final Color SOFT_ACCENT = new Color(210, 230, 236);

    protected final User currentUser;
    protected final JTabbedPane tabs;

    protected BaseDashboard(User currentUser, String roleTitle, int width, int height) {
        this.currentUser = currentUser;
        setTitle(roleTitle + " - " + currentUser.getSafeDisplayName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);
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
        bar.add(new JLabel("  " + DemoMetadata.VERSION_LABEL + "  "));
        setJMenuBar(bar);

        tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.setBackground(SURFACE_COLOR);
        add(tabs, BorderLayout.CENTER);
    }

    protected void addTab(String title, java.awt.Component component) {
        tabs.addTab(title, component);
    }

    protected void installRefreshOnTabSwitch(Runnable refreshAction) {
        tabs.addChangeListener(e -> refreshAction.run());
    }

    protected void logout() {
        dispose();
        new LoginFrame();
    }
}
