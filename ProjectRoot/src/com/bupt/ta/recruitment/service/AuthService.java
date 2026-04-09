package com.bupt.ta.recruitment.service;

import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

public class AuthService {

    private final CsvStorage<User> userStorage;

    public AuthService(CsvStorage<User> userStorage) {
        this.userStorage = userStorage;
    }

    /**
     * Authenticates a user based on username and password.
     * @param username The username.
     * @param password The plain text password.
     * @return An Optional containing the User if authentication is successful, otherwise empty.
     */
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userStorage.readAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.verifyPassword(password, user.getHashedPassword(), user.getSalt())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /**
     * Registers a new user.
     * @param username The username.
     * @param password The plain text password.
     * @param role The user's role.
     * @return true if registration is successful, false if the username already exists.
     */
    public boolean register(String username, String password, String role) {
        List<User> users = userStorage.readAll();
        boolean usernameExists = users.stream().anyMatch(u -> u.getUsername().equals(username));

        if (usernameExists) {
            return false; // Username already taken
        }

        String salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(password, salt);
        
        // Find the max current ID to generate a new one
        String newId = users.stream()
                .map(User::getId)
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0) + 1 + "";

        User newUser = new User(newId, username, hashedPassword, salt, role);
        userStorage.create(newUser);
        return true;
    }

    public Optional<User> findUserByUsername(String username) {
        return userStorage.readAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }
}
