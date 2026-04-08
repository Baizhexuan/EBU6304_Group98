/**
 * User: Account model for the TA Recruitment System.
 * <p>
 * Version2 update — L1 password hashing: the {@code password} field is
 * replaced by {@code passwordHash} and {@code salt}. Plaintext passwords
 * are never stored in CSV. Legacy constructors are kept for backward
 * compatibility during migration.
 *
 * @version 2.0
 * @since 2026-04-08
 */
public class User {
    public int id;
    public String username;
    /** @deprecated Version1 field — kept only for migration. Use passwordHash + salt instead. */
    @Deprecated
    public String password;
    /** Version2: SHA-256 hash of (salt + plaintext password), Base64-encoded. */
    public String passwordHash;
    /** Version2: Random salt used for hashing, Base64-encoded. */
    public String salt;
    public String role; // TA, MO, ADMIN

    public User() {}

    /**
     * Version1 legacy constructor (plaintext password).
     * Retained for backward-compatible code paths; prefer the Version2 constructor.
     */
    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * Version2 constructor — stores hashed password with salt.
     *
     * @param id           unique user id
     * @param username     login name
     * @param passwordHash SHA-256 hash (Base64)
     * @param salt         random salt (Base64)
     * @param role         one of TA, MO, ADMIN
     */
    public User(int id, String username, String passwordHash, String salt, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
    }

    /**
     * Verify a plaintext password against this user's stored hash.
     * Version2: delegates to {@link PasswordUtil#verifyPassword}.
     *
     * @param plaintext the password entered by the user
     * @return true if the password matches
     */
    public boolean checkPassword(String plaintext) {
        if (passwordHash != null && salt != null) {
            return PasswordUtil.verifyPassword(plaintext, salt, passwordHash);
        }
        // Fallback for any legacy data that still has plaintext
        return plaintext.equals(password);
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
