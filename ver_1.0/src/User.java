public class User {
    public int id;
    public String username;
    public String password;
    public String role;
    public String displayName;

    public User() {
    }

    public User(int id, String username, String password, String role, String displayName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.displayName = displayName;
    }

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
