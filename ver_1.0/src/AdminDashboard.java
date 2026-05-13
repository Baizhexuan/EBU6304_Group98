import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends BaseDashboard {
    private static final String[] APPLICATION_STATUSES = {"PENDING", "SELECTED", "REJECTED", "WITHDRAWN"};
    private static final String[] JOB_STATUSES = {"OPEN", "CLOSED"};

    private JTable workloadTable;
    private DefaultTableModel workloadModel;
    private JTextField workloadUsernameFilterField;
    private JTextField workloadNameFilterField;
    private JTextField workloadEmailFilterField;
    private JComboBox<String> workloadStatusFilter;
    private JLabel adminSummaryLabel;
    private JLabel aiReadinessLabel;
    private JLabel recommendationTitleLabel;
    private JTextArea recommendationArea;

    private JTable applicationsTable;
    private DefaultTableModel applicationsModel;
    private JTextField applicationTaFilterField;
    private JTextField applicationJobFilterField;
    private JTextField applicationModuleFilterField;
    private JTextField applicationStatusFilterField;
    private boolean applicationsDirty;
    private List<Application> applicationSnapshot = new ArrayList<Application>();

    private JTable jobsTable;
    private DefaultTableModel jobsModel;
    private JTextField jobMoFilterField;
    private JTextField jobTitleFilterField;
    private JTextField jobModuleFilterField;
    private JTextField jobStatusFilterField;
    private boolean jobsDirty;
    private List<Job> jobSnapshot = new ArrayList<Job>();

    public AdminDashboard(User currentUser) {
        super(currentUser, "Admin Dashboard", 1280, 820);
        addTab("Workload Monitor", createWorkloadPanel());
        addTab("Applications Overview", createApplicationsPanel());
        addTab("Jobs Overview", createJobsPanel());
        installRefreshOnTabSwitch(this::refreshVisibleTab);
        installCloseGuard();
        refreshAll();
        setVisible(true);
    }

    @Override
    protected void logout() {
        if (!confirmDiscardIfNeeded("logout")) {
            return;
        }
        super.logout();
    }

    private void installCloseGuard() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmDiscardIfNeeded("close the dashboard")) {
                    dispose();
                    new LoginFrame();
                }
            }
        });
    }

    private JPanel createWorkloadPanel() {
