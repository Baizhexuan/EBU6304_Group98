/**
 * Represents a system user account.
 *
 * <p>A user holds authentication credentials and a role that determines which
 * dashboard is shown after login. The three valid roles are {@code TA},
 * {@code MO} (Module Organiser), and {@code ADMIN}.</p>
 */
public class User {
    /** Unique user identifier. */
    public int id;
    /** Login username. */
    public String username;
    /** Password value used by the demo login flow. */
    public String password;
    /** Role determining which dashboard the user can access. */
    public String role;
    /** Human-readable name displayed in the UI. */
    public String displayName;

    /** Constructs an empty {@code User} instance used by the CSV deserialiser. */
    public User() {
    }

    /**
     * Constructs a fully initialised {@code User}.
     *
     * @param id          unique numeric identifier
     * @param username    login username
     * @param password    hashed password string (never plain text)
     * @param role        one of {@code TA}, {@code MO}, or {@code ADMIN}
     * @param displayName human-readable display name shown in the UI
     */
    public User(int id, String username, String password, String role, String displayName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.displayName = displayName;
    }

    /**
     * Returns the display name if set, otherwise falls back to the username.
     *
     * @return a non-null, non-empty string safe for UI rendering
     */
    public String getSafeDisplayName() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName.trim();
        }
        return username;
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
