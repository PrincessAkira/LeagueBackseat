package meow.sarah;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileHelper {

    public static String prefix;
    public static String authkey;
    public static String channel;
    public static String owner;

    private static int fileCount = 0;

    public static void loadFile(String currentPath) throws IOException {
        File file = new File(currentPath + "/config.json");
        JSONObject jsonObject = new JSONObject(new String(new FileInputStream(file).readAllBytes(), StandardCharsets.UTF_8));
        prefix = jsonObject.getString("prefix");
        authkey = jsonObject.getString("token");
        channel = jsonObject.getString("channel");
        owner = jsonObject.getString("admin");
        if (prefix == null || authkey == null || channel == null) {
            Logger.log("Config file not found!");
            System.exit(0);
        }
        Logger.log("Loaded config file!");
    }

    public static void writeFile(File outputfile, String content, String currentPath) throws IOException {
        File folder = new File(currentPath + "/output/" + Main.date);
        if (!folder.exists()) {
            folder.mkdir();
        }
        // count files of folder of current date
        File[] files = folder.listFiles();
        if (files != null && !Main.firstRun) {
            fileCount = files.length;
        }
        Main.firstRun = true;
        Logger.log(fileCount + " : Count");
        File file = new File(folder + "/Session-" + fileCount + ".txt");
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            fileWriter.write(content + "\n \n");
        } catch (Exception e) {
            Logger.log("Failed to write to file!");
        }
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        FileHelper.fileCount = fileCount;
    }
}
