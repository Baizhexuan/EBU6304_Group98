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
    /** Unique notification identifier. */
    public int id;
    /** Identifier of the user who should receive the notification. */
    public int userId;
    /** Short notification heading. */
    public String title;
    /** Body message shown in the notification panel. */
    public String message;
    /** Read state, normally {@code UNREAD} or {@code READ}. */
    public String status;
    /** Human-readable creation timestamp. */
    public String createdAt;
    /** Suggested follow-up action shown to the user. */
    public String actionHint;

    /**
     * Creates an empty notification record for CSV population or service assembly.
     */
    public Notification() {
    }

    /**
     * Returns {@code true} when the notification has not yet been read by the user.
     *
     * @return {@code true} if status is not {@code READ} (case-insensitive)
     */
    public boolean isUnread() {
        return !"READ".equalsIgnoreCase(status);
    }
}
