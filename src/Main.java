public class Main {
    public static void main(String[] args) {
        System.out.println("--- BUPT TA Recruitment System Initializing ---");

        //模拟创建TA

        TA newTA = new TA("2026001", "Cheng Jiahua", "jh.c@bupt.edu.cn", "Java, Python");
        
        //测试文件存储功能

        FileStorage.saveTA(newTA);
        System.out.println("Progress: TA Profile created and saved to local file.");

        //测试读取

        System.out.println("Current TAs in system: " + FileStorage.loadAllTAs());
    }
}