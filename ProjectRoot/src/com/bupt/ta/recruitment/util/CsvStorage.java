package com.bupt.ta.recruitment.util;

import com.bupt.ta.recruitment.model.CsvSerializable;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用 CSV 存储引擎 - L1 核心组件
 * @param <T> 必须实现 CsvSerializable 接口的模型类
 */
public class CsvStorage<T extends CsvSerializable> {
    private final String filePath;
    private final Function<String, T> mapper; // 用于将 CSV 行转换为对象的函数

    /**
     * @param filePath CSV 文件路径 (例如: "data/users.csv")
     * @param mapper   映射函数 (例如: User::fromCsvRow)
     */
    public CsvStorage(String filePath, Function<String, T> mapper) {
        this.filePath = filePath;
        this.mapper = mapper;
        ensureFileExists();
    }

    private void ensureFileExists() {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs(); // 创建 data 文件夹
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- CRUD 操作 ---

    /**
     * 读取所有数据
     */
    public List<T> loadAll() {
        List<T> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                T item = mapper.apply(line);
                if (item != null) list.add(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 保存所有数据 (覆盖写入)
     */
    public void saveAll(List<T> data) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            for (T item : data) {
                pw.println(item.toCsvRow());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据 ID 查找单个对象 (假设模型类都有 getId 方法)
     * 注意：这里需要一个简单的 ID 提取逻辑，或者通过 Model 接口定义 getId
     */
    public T findById(String id, Function<T, String> idExtractor) {
        return loadAll().stream()
                .filter(item -> idExtractor.apply(item).equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * 更新对象
     */
    public void update(T updatedItem, Function<T, String> idExtractor) {
        List<T> all = loadAll();
        for (int i = 0; i < all.size(); i++) {
            if (idExtractor.apply(all.get(i)).equals(idExtractor.apply(updatedItem))) {
                all.set(i, updatedItem);
                break;
            }
        }
        saveAll(all);
    }

    /**
     * 删除对象
     */
    public void delete(String id, Function<T, String> idExtractor) {
        List<T> all = loadAll().stream()
                .filter(item -> !idExtractor.apply(item).equals(id))
                .collect(Collectors.toList());
        saveAll(all);
    }
}