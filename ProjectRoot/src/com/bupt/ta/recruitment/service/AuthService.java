package com.bupt.ta.recruitment.service;

import com.bupt.ta.recruitment.model.User;
import com.bupt.ta.recruitment.util.CsvStorage;
import com.bupt.ta.recruitment.util.PasswordUtil;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class responsible for user authentication and registration.
 * It keeps credential-related business rules out of the Swing UI layer.
 */
public class AuthService {
    // 持有用户 CSV 存储对象，供登录与注册流程复用。
    private final CsvStorage<User> userStorage;

    public AuthService(CsvStorage<User> userStorage) {
        // 注入具体的用户存储实现，便于在不同环境下复用服务逻辑。
        this.userStorage = userStorage;
    }

    public Optional<User> login(String username, String password) {
        // 遍历全部用户，查找用户名和密码都匹配的记录。
        for (User user : userStorage.loadAll()) {
            // 用户名采用忽略大小写比较，同时对输入密码做盐值校验。
            if (user.getUsername().equalsIgnoreCase(username)
                    && PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash())) {
                // 找到匹配用户后立即返回成功结果。
                return Optional.of(user);
            }
        }
        // 遍历完成仍未找到匹配项时，返回空结果表示登录失败。
        return Optional.empty();
    }

    public boolean register(String username, String password, User.UserRole role) {
        // 先读取现有用户列表，避免重复加载文件。
        List<User> users = userStorage.loadAll();
        // 注册前先校验用户名是否已经被占用。
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                // 发现重名用户时直接返回失败。
                return false;
            }
        }

        // 为新用户生成独立盐值。
        String salt = PasswordUtil.generateSalt();
        // 使用盐值对原始密码做哈希处理。
        String hashedPassword = PasswordUtil.hashPassword(password, salt);
        // 创建新用户对象并追加到用户集合中。
        users.add(new User(UUID.randomUUID().toString(), username, hashedPassword, salt, role));
        // 将更新后的用户集合完整写回 CSV。
        userStorage.saveAll(users);
        // 持久化成功后返回 true。
        return true;
    }

    public Optional<User> findUserByUsername(String username) {
        // 提供单独的用户名查询，供 UI 层区分“用户不存在”和“密码错误”。
        for (User user : userStorage.loadAll()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                // 命中用户名后立即返回对应用户对象。
                return Optional.of(user);
            }
        }
        // 未找到时返回空结果。
        return Optional.empty();
    }
}
