/**
 * Tracks whether a TA-MO conversation has been approved beyond the three-message limit.
 */
public class MessageConsent {
    public int id;
    public int userAId;
    public int userBId;
    public int jobId;
    public boolean approved;
    public int requestedBy;
    public String updatedAt;
}
