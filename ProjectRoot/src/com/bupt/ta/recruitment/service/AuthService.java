package com.bupt.ta.recruitment.service;

import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.PasswordUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthService {
    private final CsvStorage<User> userStorage;

    public AuthService(CsvStorage<User> userStorage) {
        this.userStorage = userStorage;
    }

    public Optional<User> login(String username, String password) {
        for (User user : userStorage.loadAll()) {
            if (user.getUsername().equalsIgnoreCase(username)
                    && PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public boolean register(String username, String password, User.UserRole role) {
        List<User> users = userStorage.loadAll();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }

        String salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(password, salt);
        users.add(new User(UUID.randomUUID().toString(), username, hashedPassword, salt, role));
        userStorage.saveAll(users);
        return true;
    }

    public Optional<User> findUserByUsername(String username) {
        for (User user : userStorage.loadAll()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
