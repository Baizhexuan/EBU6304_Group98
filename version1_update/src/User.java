public class User {
    public int id;
    public String username;
    public String password;
    public String role; // TA, MO, ADMIN

    public User() {}

    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
