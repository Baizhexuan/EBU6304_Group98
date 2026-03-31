package models;

public class User {
    private String id;
    private String username;
    private String password;
    private String role; // TA, MO, or ADMIN

    // 构造函数
    public User(String id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters 和 Setters (篇幅原因省略，请自行生成)
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}
