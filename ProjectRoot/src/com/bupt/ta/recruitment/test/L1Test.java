package com.bupt.ta.recruitment.test;

import com.bupt.ta.recruitment.model.*;
import com.bupt.ta.recruitment.util.CsvStorage;
import java.util.*;

public class L1Test {
    public static void main(String[] args) {
        // 1. 初始化 User 存储 (传入文件路径和映射函数)
        CsvStorage<User> userStorage = new CsvStorage<>("data/users.csv", User::fromCsvRow);

        // 2. 创建一个测试用户
        User testUser = new User("u1", "alice", "hashed_pwd", "salt123", User.UserRole.TA);
        
        // 3. 保存
        userStorage.saveAll(Collections.singletonList(testUser));
        System.out.println("User saved successfully!");

        // 4. 读取并验证
        User retrieved = userStorage.findById("u1", User::getId);
        System.out.println("Retrieved user: " + retrieved.getUsername());
    }
}