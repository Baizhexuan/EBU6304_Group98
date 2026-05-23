/**
 * Represents one in-app message between a TA and an MO.
 */
public class MessageRecord {
    public int id;
    public int fromUserId;
    public int toUserId;
    public int jobId;
    public String body;
    public String status;
    public String createdAt;

    public boolean isUnreadFor(int userId) {
        return toUserId == userId && !"READ".equalsIgnoreCase(status);
    }
}
