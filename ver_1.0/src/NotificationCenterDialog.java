import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/**
 * Shared bell-centre dialog for reminders, workflow notifications, and TA-MO messages.
 */
public class NotificationCenterDialog extends JDialog {
    private static final Color PAGE_BG = new Color(244, 247, 249);
    private static final Color PANEL_BG = Color.WHITE;
    private static final Color LIST_SELECTED = new Color(225, 236, 249);
    private static final Color SOFT_LINE = new Color(219, 227, 235);
    private static final Color BUBBLE_IN = new Color(247, 249, 252);
    private static final Color BUBBLE_OUT = new Color(225, 239, 255);
    private static final int NOTIFICATION_PAGE_SIZE = 100;

    private final User currentUser;
    private final Runnable afterChange;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private JButton notificationsNavButton;
    private JButton messagesNavButton;

    private DefaultListModel<Notification> notificationListModel;
    private JList<Notification> notificationList;
    private JLabel notificationTitleLabel;
    private JLabel notificationMetaLabel;
    private JTextArea notificationMessageArea;
    private JTextArea notificationActionArea;

    private DefaultListModel<ContactItem> contactListModel;
    private JList<ContactItem> contactList;
    private JLabel chatTitleLabel;
    private JLabel chatMetaLabel;
    private JPanel chatStreamPanel;
    private JScrollPane chatScrollPane;
    private JTextArea messageArea;
    private JButton sendButton;
    private JButton approveButton;
    private JLabel statusLabel;

    public NotificationCenterDialog(JFrame owner, User currentUser, Runnable afterChange) {
        super(owner, "Bell Centre", true);
        this.currentUser = currentUser;
        this.afterChange = afterChange;
        setSize(1060, 680);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PAGE_BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(BaseDashboard.TEXT_MUTED);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 18, 10, 18));
        add(statusLabel, BorderLayout.SOUTH);

        refreshAll();
        chooseInitialView();
        applyCurrentLanguage();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(BaseDashboard.HEADER_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(13, 18, 13, 18));

        JLabel title = new JLabel("Bell Centre");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 17));

        JLabel subtitle = new JLabel("Notifications and TA-MO conversations");
        subtitle.setForeground(new Color(225, 237, 250));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(subtitle);

        JButton closeButton = new JButton("Close");
        BaseDashboard.applyButtonStyle(closeButton, new Color(236, 244, 252), BaseDashboard.HEADER_COLOR);
        closeButton.addActionListener(e -> dispose());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(I18n.createLanguageSwitcher(this::applyCurrentLanguage));
        right.add(closeButton);

        header.add(text, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void applyCurrentLanguage() {
        setTitle(I18n.t("Bell Centre"));
        I18n.applyTo(this);
        renderSelectedConversation();
    }

    private JPanel buildMain() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE_BG);
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel rail = new JPanel(new GridLayout(2, 1, 0, 8));
        rail.setBackground(PAGE_BG);
        rail.setPreferredSize(new Dimension(150, 0));

        notificationsNavButton = new JButton("Notifications");
        messagesNavButton = new JButton("Messages");
        notificationsNavButton.addActionListener(e -> showNotificationsView());
        messagesNavButton.addActionListener(e -> showMessagesView());
        showNotificationsView();
        rail.add(notificationsNavButton);
        rail.add(messagesNavButton);

        contentCards.setBackground(PAGE_BG);
        contentCards.add(buildNotificationsView(), "notifications");
        contentCards.add(buildMessagesView(), "messages");

        root.add(rail, BorderLayout.WEST);
        root.add(contentCards, BorderLayout.CENTER);
        return root;
    }

    private void showNotificationsView() {
        cardLayout.show(contentCards, "notifications");
        BaseDashboard.applyButtonStyle(notificationsNavButton, BaseDashboard.ACCENT_COLOR, Color.WHITE);
        BaseDashboard.applyButtonStyle(messagesNavButton, BaseDashboard.SECONDARY_SURFACE, BaseDashboard.ACCENT_COLOR);
        if (statusLabel != null && notificationListModel != null && notificationListModel.isEmpty()) {
            statusLabel.setText(I18n.t("No notifications are available. Open Messages to view TA-MO conversations."));
        }
    }

    private void showMessagesView() {
        cardLayout.show(contentCards, "messages");
        BaseDashboard.applyButtonStyle(messagesNavButton, BaseDashboard.ACCENT_COLOR, Color.WHITE);
        BaseDashboard.applyButtonStyle(notificationsNavButton, BaseDashboard.SECONDARY_SURFACE, BaseDashboard.ACCENT_COLOR);
        if (statusLabel != null && contactListModel != null && contactListModel.isEmpty()) {
            statusLabel.setText(I18n.t("No TA-MO conversations are available for this account yet."));
        }
    }

    private void chooseInitialView() {
        boolean hasNotifications = notificationListModel != null && !notificationListModel.isEmpty();
        boolean hasContacts = contactListModel != null && !contactListModel.isEmpty();
        if (!hasNotifications && hasContacts) {
            showMessagesView();
            statusLabel.setText(I18n.t("No notifications are available, so Messages is shown first."));
        } else {
            showNotificationsView();
        }
    }

    private JPanel buildNotificationsView() {
        JPanel root = new JPanel(new BorderLayout(10, 0));
        root.setBackground(PAGE_BG);
        root.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        notificationListModel = new DefaultListModel<Notification>();
        notificationList = new JList<Notification>(notificationListModel);
        notificationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationList.setCellRenderer(new NotificationListRenderer());
        notificationList.setFixedCellHeight(76);
        notificationList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedNotification();
            }
        });

        JScrollPane listScroll = new JScrollPane(notificationList);
        listScroll.setBorder(BorderFactory.createLineBorder(SOFT_LINE));
        listScroll.setPreferredSize(new Dimension(330, 0));

        JPanel detail = new JPanel(new BorderLayout(10, 10));
        detail.setBackground(PANEL_BG);
        detail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SOFT_LINE),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        notificationTitleLabel = new JLabel("Select a notification");
        notificationTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        notificationTitleLabel.setForeground(new Color(28, 47, 70));
        notificationMetaLabel = new JLabel(" ");
        notificationMetaLabel.setFont(BaseDashboard.UI_BODY_FONT);
        notificationMetaLabel.setForeground(BaseDashboard.TEXT_MUTED);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(notificationTitleLabel);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(notificationMetaLabel);
        detail.add(titleBlock, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(2, 1, 0, 10));
        body.setOpaque(false);
        notificationMessageArea = buildReadOnlyTextArea();
        notificationActionArea = buildReadOnlyTextArea();
        body.add(wrapCard("Message", notificationMessageArea));
        body.add(wrapCard("Suggested action", notificationActionArea));
        detail.add(body, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton refreshButton = new JButton("Refresh");
        JButton markReadButton = new JButton("Mark Read");
        JButton markAllButton = new JButton("Mark All Read");
        BaseDashboard.applyButtonStyle(refreshButton, BaseDashboard.SECONDARY_SURFACE, BaseDashboard.ACCENT_COLOR);
        BaseDashboard.applyButtonStyle(markReadButton, BaseDashboard.ACCENT_COLOR, Color.WHITE);
        BaseDashboard.applyButtonStyle(markAllButton, BaseDashboard.WARNING_SURFACE, new Color(101, 73, 30));
        actions.add(refreshButton);
        actions.add(markReadButton);
        actions.add(markAllButton);
        detail.add(actions, BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> refreshAll());
        markReadButton.addActionListener(e -> markSelectedNotificationRead());
        markAllButton.addActionListener(e -> {
            NotificationService.markAllAsRead(currentUser.id);
            refreshAll();
            statusLabel.setText(I18n.t("All notifications marked as read."));
        });

        root.add(listScroll, BorderLayout.WEST);
        root.add(detail, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildMessagesView() {
        JPanel root = new JPanel(new BorderLayout(10, 0));
        root.setBackground(PAGE_BG);
        root.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

        contactListModel = new DefaultListModel<ContactItem>();
        contactList = new JList<ContactItem>(contactListModel);
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setCellRenderer(new ContactListRenderer());
        contactList.setFixedCellHeight(74);
        contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                renderSelectedConversation();
            }
        });

        JScrollPane contactScroll = new JScrollPane(contactList);
        contactScroll.setBorder(BorderFactory.createLineBorder(SOFT_LINE));
        contactScroll.setPreferredSize(new Dimension(310, 0));

        JPanel chat = new JPanel(new BorderLayout());
        chat.setBackground(PANEL_BG);
        chat.setBorder(BorderFactory.createLineBorder(SOFT_LINE));

        JPanel chatHeader = new JPanel(new BorderLayout(8, 0));
        chatHeader.setBackground(PANEL_BG);
        chatHeader.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        chatTitleLabel = new JLabel("Select a conversation");
        chatTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        chatTitleLabel.setForeground(new Color(28, 47, 70));
        chatMetaLabel = new JLabel(" ");
        chatMetaLabel.setFont(BaseDashboard.UI_BODY_FONT);
        chatMetaLabel.setForeground(BaseDashboard.TEXT_MUTED);
        JPanel chatTitleBlock = new JPanel();
        chatTitleBlock.setOpaque(false);
        chatTitleBlock.setLayout(new BoxLayout(chatTitleBlock, BoxLayout.Y_AXIS));
        chatTitleBlock.add(chatTitleLabel);
        chatTitleBlock.add(Box.createVerticalStrut(3));
        chatTitleBlock.add(chatMetaLabel);
        chatHeader.add(chatTitleBlock, BorderLayout.WEST);

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);
        approveButton = new JButton("Approve");
        JButton markReadButton = new JButton("Read Incoming");
        BaseDashboard.applyButtonStyle(approveButton, BaseDashboard.SUCCESS_SURFACE, new Color(35, 82, 55));
        BaseDashboard.applyButtonStyle(markReadButton, BaseDashboard.SECONDARY_SURFACE, BaseDashboard.ACCENT_COLOR);
        headerActions.add(markReadButton);
        headerActions.add(approveButton);
        chatHeader.add(headerActions, BorderLayout.EAST);
        chat.add(chatHeader, BorderLayout.NORTH);

        chatStreamPanel = new JPanel();
        chatStreamPanel.setLayout(new BoxLayout(chatStreamPanel, BoxLayout.Y_AXIS));
        chatStreamPanel.setBackground(PANEL_BG);
        chatScrollPane = new JScrollPane(chatStreamPanel);
        chatScrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, SOFT_LINE));
        chat.add(chatScrollPane, BorderLayout.CENTER);

        JPanel composer = new JPanel(new BorderLayout(8, 8));
        composer.setBackground(PANEL_BG);
        composer.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        messageArea = new JTextArea(3, 54);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(198, 211, 225)),
                BorderFactory.createEmptyBorder(7, 8, 7, 8)));
        composer.add(messageArea, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        BaseDashboard.applyButtonStyle(sendButton, BaseDashboard.ACCENT_COLOR, Color.WHITE);
        composer.add(sendButton, BorderLayout.EAST);
        chat.add(composer, BorderLayout.SOUTH);

        approveButton.addActionListener(e -> approveSelectedConversation());
        markReadButton.addActionListener(e -> {
            MessageService.markMessagesReadForUser(currentUser.id);
            refreshAll();
            statusLabel.setText(I18n.t("Incoming messages marked as read."));
        });
        sendButton.addActionListener(e -> sendMessage());

        root.add(contactScroll, BorderLayout.WEST);
        root.add(chat, BorderLayout.CENTER);
        return root;
    }

    private JTextArea buildReadOnlyTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("SansSerif", Font.PLAIN, 13));
        area.setForeground(new Color(38, 55, 72));
        area.setBackground(PANEL_BG);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return area;
    }

    private JPanel wrapCard(String title, Component child) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SOFT_LINE),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JLabel label = new JLabel(title);
        label.setForeground(BaseDashboard.TEXT_MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(label, BorderLayout.NORTH);
        panel.add(child, BorderLayout.CENTER);
        return panel;
    }

    private void refreshAll() {
        refreshNotifications();
        refreshContacts();
        renderSelectedConversation();
        if (afterChange != null) {
            afterChange.run();
        }
    }

    private void refreshNotifications() {
        Notification selected = notificationList == null ? null : notificationList.getSelectedValue();
        notificationListModel.clear();
        List<Notification> notifications = NotificationService.getNotificationsForUser(currentUser.id);
        int start = Math.max(0, notifications.size() - NOTIFICATION_PAGE_SIZE);
        for (int i = notifications.size() - 1; i >= start; i--) {
            Notification notification = notifications.get(i);
            notificationListModel.addElement(notification);
        }
        if (statusLabel != null && notifications.size() > NOTIFICATION_PAGE_SIZE) {
            statusLabel.setText(I18n.isChinese()
                    ? "显示最新 " + NOTIFICATION_PAGE_SIZE + " 条通知，共 " + notifications.size() + " 条。"
                    : "Showing latest " + NOTIFICATION_PAGE_SIZE + " of " + notifications.size()
                            + " notifications.");
        }
        restoreNotificationSelection(selected);
        showSelectedNotification();
    }

    private void restoreNotificationSelection(Notification previous) {
        if (notificationListModel.isEmpty()) {
            return;
        }
        if (previous != null) {
            for (int i = 0; i < notificationListModel.size(); i++) {
                if (notificationListModel.get(i).id == previous.id) {
                    notificationList.setSelectedIndex(i);
                    return;
                }
            }
        }
        notificationList.setSelectedIndex(0);
    }

    private void showSelectedNotification() {
        Notification notification = notificationList == null ? null : notificationList.getSelectedValue();
        if (notification == null) {
            notificationTitleLabel.setText(I18n.t("No notifications yet"));
            notificationMetaLabel.setText(I18n.t("Workflow updates will appear here."));
            notificationMessageArea.setText(I18n.t("You are all caught up."));
            notificationActionArea.setText(I18n.t("No action required."));
            return;
        }
        notificationTitleLabel.setText(I18n.t(notification.title));
        notificationMetaLabel.setText(notification.createdAt + "  |  " + I18n.t(notification.status));
        notificationMessageArea.setText(I18n.t(notification.message));
        notificationActionArea.setText(ValidationUtils.isBlank(notification.actionHint)
                ? I18n.t("No suggested action.")
                : I18n.t(notification.actionHint));
        notificationMessageArea.setCaretPosition(0);
        notificationActionArea.setCaretPosition(0);
    }

    private void refreshContacts() {
        ContactItem selected = contactList == null ? null : contactList.getSelectedValue();
        contactListModel.clear();
        Set<String> seen = new HashSet<String>();
        List<Job> jobs = FileStorage.loadJobs();
        List<Application> applications = FileStorage.loadApplications();
        if ("TA".equalsIgnoreCase(currentUser.role)) {
            for (Application app : applications) {
                if (app.taId != currentUser.id) {
                    continue;
                }
                Job job = findJob(jobs, app.jobId);
                User mo = job == null ? null : FileStorage.findUserById(job.moId);
                addContactIfNew(seen, mo, job);
            }
        } else if ("MO".equalsIgnoreCase(currentUser.role)) {
            for (Job job : jobs) {
                if (job.moId != currentUser.id) {
                    continue;
                }
                for (Application app : applications) {
                    if (app.jobId == job.id) {
                        addContactIfNew(seen, FileStorage.findUserById(app.taId), job);
                    }
                }
            }
        }
        if (contactListModel.isEmpty()) {
            addDemoFallbackContacts(seen, jobs);
        }
        restoreContactSelection(selected);
    }

    private void addDemoFallbackContacts(Set<String> seen, List<Job> jobs) {
        if ("TA".equalsIgnoreCase(currentUser.role)) {
            for (Job job : jobs) {
                if (!job.isOpen()) {
                    continue;
                }
                addContactIfNew(seen, FileStorage.findUserById(job.moId), job);
            }
            if (!contactListModel.isEmpty() && statusLabel != null) {
                statusLabel.setText(I18n.t("No application-linked conversations were found, so open MO job contacts are shown for demo use."));
            }
            return;
        }
        if ("MO".equalsIgnoreCase(currentUser.role)) {
            for (Job job : jobs) {
                if (job.moId != currentUser.id) {
                    continue;
                }
                for (User user : FileStorage.loadUsers()) {
                    if ("TA".equalsIgnoreCase(user.role)) {
                        addContactIfNew(seen, user, job);
                    }
                }
            }
            if (!contactListModel.isEmpty() && statusLabel != null) {
                statusLabel.setText(I18n.t("No applicant-linked conversations were found, so TA contacts are shown for demo use."));
            }
        }
    }

    private void restoreContactSelection(ContactItem previous) {
        if (contactListModel.isEmpty()) {
            return;
        }
        if (previous != null) {
            for (int i = 0; i < contactListModel.size(); i++) {
                ContactItem item = contactListModel.get(i);
                if (item.otherUserId == previous.otherUserId && item.jobId == previous.jobId) {
                    contactList.setSelectedIndex(i);
                    return;
                }
            }
        }
        contactList.setSelectedIndex(0);
    }

    private void addContactIfNew(Set<String> seen, User otherUser, Job job) {
        if (otherUser == null || job == null) {
            return;
        }
        String key = otherUser.id + ":" + job.id;
        if (seen.add(key)) {
            contactListModel.addElement(new ContactItem(otherUser.id, job.id, otherUser.getSafeDisplayName(),
                    job.title, job.module, countUnreadForContact(otherUser.id, job.id), latestMessagePreview(otherUser.id, job.id)));
        }
    }

    private int countUnreadForContact(int otherUserId, int jobId) {
        int unread = 0;
        for (MessageRecord message : FileStorage.loadMessages()) {
            if (message.fromUserId == otherUserId && message.toUserId == currentUser.id
                    && message.jobId == jobId && message.isUnreadFor(currentUser.id)) {
                unread++;
            }
        }
        return unread;
    }

    private String latestMessagePreview(int otherUserId, int jobId) {
        String preview = "No messages yet";
        for (MessageRecord message : FileStorage.loadMessages()) {
            boolean sameConversation = message.jobId == jobId
                    && ((message.fromUserId == currentUser.id && message.toUserId == otherUserId)
                    || (message.fromUserId == otherUserId && message.toUserId == currentUser.id));
            if (sameConversation) {
                preview = message.body;
            }
        }
        return preview;
    }

    private void renderSelectedConversation() {
        ContactItem contact = contactList == null ? null : contactList.getSelectedValue();
        chatStreamPanel.removeAll();
        if (contact == null) {
            chatTitleLabel.setText(I18n.t("No conversation selected"));
            chatMetaLabel.setText(I18n.t("TA-MO conversations become available after an application connects both sides."));
            approveButton.setEnabled(false);
            approveButton.setVisible("MO".equalsIgnoreCase(currentUser.role));
            sendButton.setEnabled(false);
            if (contactListModel == null || contactListModel.isEmpty()) {
                statusLabel.setText(I18n.t("No TA-MO conversations are available for this account yet."));
            }
            addEmptyConversation();
            refreshChatPanel();
            return;
        }

        boolean approved = MessageService.hasApprovedConsent(currentUser.id, contact.otherUserId, contact.jobId);
        int remaining = MessageService.getRemainingMessagesBeforeConsent(currentUser.id, contact.otherUserId, contact.jobId);
        boolean canApprove = MessageService.canApproveConversation(currentUser, contact.otherUserId, contact.jobId);
        chatTitleLabel.setText(contact.otherName);
        chatMetaLabel.setText(contact.jobTitle + " / " + contact.module + "  |  "
                + buildConversationStatus(approved, remaining, canApprove));
        approveButton.setVisible(canApprove || "MO".equalsIgnoreCase(currentUser.role));
        approveButton.setEnabled(!approved && canApprove);
        approveButton.setToolTipText(canApprove ? I18n.t("MO can approve this conversation.")
                : I18n.t("Only the MO for this job can approve the conversation."));
        sendButton.setEnabled(true);

        List<MessageRecord> messages = conversationMessages(contact);
        if (messages.isEmpty()) {
            addEmptyConversation();
        } else {
            for (MessageRecord message : messages) {
                addMessageBubble(message, message.fromUserId == currentUser.id);
            }
        }
        refreshChatPanel();
    }

    private List<MessageRecord> conversationMessages(ContactItem contact) {
        List<MessageRecord> messages = new ArrayList<MessageRecord>();
        for (MessageRecord message : FileStorage.loadMessages()) {
            boolean sameConversation = message.jobId == contact.jobId
                    && ((message.fromUserId == currentUser.id && message.toUserId == contact.otherUserId)
                    || (message.fromUserId == contact.otherUserId && message.toUserId == currentUser.id));
            if (sameConversation) {
                messages.add(message);
            }
        }
        return messages;
    }

    private void addEmptyConversation() {
        JPanel empty = new JPanel(new BorderLayout());
        empty.setOpaque(false);
        empty.setBorder(BorderFactory.createEmptyBorder(80, 20, 20, 20));
        JLabel label = new JLabel("<html><div style='text-align:center;width:360px;'>"
                + I18n.t("No messages yet.") + "<br>"
                + I18n.t("Send a concise question or approve the conversation when the other side needs a longer discussion.")
                + "</div></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(BaseDashboard.TEXT_MUTED);
        empty.add(label, BorderLayout.CENTER);
        chatStreamPanel.add(empty);
    }

    private void addMessageBubble(MessageRecord message, boolean mine) {
        JPanel row = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        row.setOpaque(false);

        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(mine ? BUBBLE_OUT : BUBBLE_IN);
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(mine ? new Color(183, 215, 247) : SOFT_LINE),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        bubble.setMaximumSize(new Dimension(420, 120));

        JLabel text = new JLabel("<html><div style='width:360px;'>" + escapeHtml(I18n.t(message.body)) + "</div></html>");
        text.setForeground(new Color(30, 45, 63));
        text.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JLabel meta = new JLabel(I18n.t(mine ? "You" : "Them") + "  " + message.createdAt);
        meta.setFont(new Font("SansSerif", Font.PLAIN, 10));
        meta.setForeground(BaseDashboard.TEXT_MUTED);
        bubble.add(text);
        bubble.add(Box.createVerticalStrut(4));
        bubble.add(meta);
        row.add(bubble);
        chatStreamPanel.add(row);
    }

    private void refreshChatPanel() {
        chatStreamPanel.revalidate();
        chatStreamPanel.repaint();
        javax.swing.SwingUtilities.invokeLater(() -> chatScrollPane.getVerticalScrollBar()
                .setValue(chatScrollPane.getVerticalScrollBar().getMaximum()));
    }

    private void markSelectedNotificationRead() {
        Notification notification = notificationList.getSelectedValue();
        if (notification == null) {
            JOptionPane.showMessageDialog(this, I18n.t("Please select a notification first."), I18n.t("Info"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        NotificationService.markAsRead(notification.id);
        statusLabel.setText(I18n.t("Notification marked as read."));
        refreshAll();
    }

    private void sendMessage() {
        ContactItem contact = contactList.getSelectedValue();
        if (contact == null) {
            JOptionPane.showMessageDialog(this, I18n.t("No TA-MO contact is available for this user."), I18n.t("No Contact"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        MessageSendResult result = MessageService.sendMessage(currentUser, contact.otherUserId, contact.jobId,
                messageArea.getText());
        statusLabel.setText(I18n.t(result.message));
        if (result.success) {
            messageArea.setText("");
        }
        refreshAll();
    }

    private void approveSelectedConversation() {
        ContactItem contact = contactList.getSelectedValue();
        if (contact == null) {
            return;
        }
        if (!MessageService.canApproveConversation(currentUser, contact.otherUserId, contact.jobId)) {
            statusLabel.setText(I18n.t("Only the MO for this job can approve the conversation."));
            return;
        }
        boolean approved = MessageService.approveConversation(currentUser.id, contact.otherUserId, contact.jobId);
        statusLabel.setText(I18n.t(approved
                ? "Conversation approved. The three-message limit is now lifted for this contact."
                : "Only the MO for this job can approve the conversation."));
        refreshAll();
    }

    private String buildConversationStatus(boolean approved, int remaining, boolean canApprove) {
        if (approved) {
            return I18n.t("Conversation approved");
        }
        if (canApprove) {
            return I18n.t("TA is waiting for MO approval. Pre-approval messages left: ") + remaining;
        }
        return I18n.t("Waiting for MO approval. Pre-approval messages left: ") + remaining;
    }

    private Job findJob(List<Job> jobs, int jobId) {
        for (Job job : jobs) {
            if (job.id == jobId) {
                return job;
            }
        }
        return null;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static final class NotificationListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            Notification notification = (Notification) value;
            JPanel panel = new JPanel(new BorderLayout(6, 2));
            panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            panel.setBackground(isSelected ? LIST_SELECTED : PANEL_BG);

            JLabel title = new JLabel(I18n.t(notification.title));
            title.setFont(new Font("SansSerif", notification.isUnread() ? Font.BOLD : Font.PLAIN, 13));
            title.setForeground(new Color(30, 45, 63));
            JLabel meta = new JLabel(notification.createdAt + "  |  " + I18n.t(notification.status));
            meta.setFont(new Font("SansSerif", Font.PLAIN, 11));
            meta.setForeground(BaseDashboard.TEXT_MUTED);
            JLabel preview = new JLabel(shorten(I18n.t(notification.message), 52));
            preview.setFont(new Font("SansSerif", Font.PLAIN, 11));
            preview.setForeground(BaseDashboard.TEXT_MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(title);
            text.add(meta);
            text.add(preview);
            panel.add(text, BorderLayout.CENTER);

            JLabel dot = new JLabel(notification.isUnread() ? I18n.t("NEW") : " ");
            dot.setForeground(BaseDashboard.ACCENT_COLOR);
            dot.setFont(new Font("SansSerif", Font.BOLD, 10));
            panel.add(dot, BorderLayout.EAST);
            return panel;
        }
    }

    private static final class ContactListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            ContactItem contact = (ContactItem) value;
            JPanel panel = new JPanel(new BorderLayout(6, 2));
            panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            panel.setBackground(isSelected ? LIST_SELECTED : PANEL_BG);

            JLabel name = new JLabel(contact.otherName);
            name.setFont(new Font("SansSerif", contact.unread > 0 ? Font.BOLD : Font.PLAIN, 13));
            name.setForeground(new Color(30, 45, 63));
            JLabel job = new JLabel(contact.jobTitle + " / " + contact.module);
            job.setFont(new Font("SansSerif", Font.PLAIN, 11));
            job.setForeground(BaseDashboard.TEXT_MUTED);
            JLabel preview = new JLabel(shorten(I18n.t(contact.preview), 42));
            preview.setFont(new Font("SansSerif", Font.PLAIN, 11));
            preview.setForeground(BaseDashboard.TEXT_MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(name);
            text.add(job);
            text.add(preview);
            panel.add(text, BorderLayout.CENTER);

            if (contact.unread > 0) {
                JLabel badge = new JLabel(String.valueOf(contact.unread));
                badge.setOpaque(true);
                badge.setBackground(new Color(220, 53, 69));
                badge.setForeground(Color.WHITE);
                badge.setHorizontalAlignment(SwingConstants.CENTER);
                badge.setFont(new Font("SansSerif", Font.BOLD, 10));
                badge.setPreferredSize(new Dimension(22, 22));
                panel.add(badge, BorderLayout.EAST);
            }
            return panel;
        }
    }

    private static String shorten(String value, int max) {
        if (value == null) {
            return "";
        }
        String compact = value.replace('\n', ' ').trim();
        return compact.length() <= max ? compact : compact.substring(0, max - 3) + "...";
    }

    private static final class ContactItem {
        private final int otherUserId;
        private final int jobId;
        private final String otherName;
        private final String jobTitle;
        private final String module;
        private final int unread;
        private final String preview;

        private ContactItem(int otherUserId, int jobId, String otherName, String jobTitle, String module, int unread,
                String preview) {
            this.otherUserId = otherUserId;
            this.jobId = jobId;
            this.otherName = otherName;
            this.jobTitle = jobTitle;
            this.module = module;
            this.unread = unread;
            this.preview = preview;
        }
    }
}
