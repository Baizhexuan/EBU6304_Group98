package com.bupt.ta.recruitment.model;

/**
 * CsvSerializable 接口
 * 
 * 该接口定义了所有需要被 CsvStorage 存储的模型类必须实现的行为。
 * 只要一个类实现了此接口，CsvStorage 就能通过泛型机制统一地将其写入 CSV 文件。
 * 
 * 对应 L1 基础架构层：数据持久化标准
 */
public interface CsvSerializable {

    /**
     * 将当前对象的所有字段转换为一行 CSV 格式的字符串。
     * 
     * 实现类应确保字段顺序与 fromCsvRow 方法中的解析顺序严格一致。
     * 建议在实现时使用 String.join(",", ...) 方式。
     * 
     * @return 一个以逗号分隔的字符串，代表该对象的一条记录。
     */
    String toCsvRow();
}