public class Notification {
    public int id;
    public int userId;
    public String title;
    public String message;
    public String status;
    public String createdAt;
    public String actionHint;

    public boolean isUnread() {
        return !"READ".equalsIgnoreCase(status);
    }
}
