import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {
    private static final String TA_FILE = "ta_data.txt";

    //保存TA信息到txt

    public static void saveTA(TA ta) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TA_FILE, true))) {
            writer.write(ta.toCSV());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving TA data: " + e.getMessage());
        }
    }

    //从文件读取所有TA

    public static List<String> loadAllTAs() {
        List<String> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(TA_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                data.add(line);
            }
        } catch (IOException e) {
            System.err.println("No existing data found.");
        }
        return data;
    }
}