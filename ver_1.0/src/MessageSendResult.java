/**
 * Small result object returned by the message sending rule engine.
 */
public class MessageSendResult {
    public final boolean success;
    public final String message;

    public MessageSendResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
