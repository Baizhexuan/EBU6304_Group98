/**
 * Represents an in-app notification delivered to a specific user.
 *
 * <p>Notifications are generated automatically when key recruitment events
 * occur (e.g. application selected, application rejected, profile incomplete).
 * The {@code status} field is either {@code UNREAD} or {@code READ}.
 * The optional {@code actionHint} field provides a UI label for a suggested
 * follow-up action.</p>
 */
public class Notification {
    public int id;
    public int userId;
    public String title;
    public String message;
    public String status;
    public String createdAt;
    public String actionHint;

    /**
     * Returns {@code true} when the notification has not yet been read by the user.
     *
     * @return {@code true} if status is not {@code READ} (case-insensitive)
     */
    public boolean isUnread() {
        return !"READ".equalsIgnoreCase(status);
    }
}
