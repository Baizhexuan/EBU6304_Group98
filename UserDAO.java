package dao;

import models.User;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class UserDAO {
    // 
    private static final String CSV_FILE_PATH = "C:/path/to/your/project/data/users.csv";

    // 模拟登录验证：遍历 CSV 查找匹配的用户名和密码
    public User authenticate(String username, String password) {
        String line = "";
        String cvsSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            // 跳过 CSV 表头 (ID,Username,Password,Role)
            br.readLine(); 

            while ((line = br.readLine()) != null) {
                String[] userData = line.split(cvsSplitBy);
                // userData[1] 是用户名, userData[2] 是密码
                if (userData[1].equals(username) && userData[2].equals(password)) {
                    return new User(userData[0], userData[1], userData[2], userData[3]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // 登录失败
    }
}
