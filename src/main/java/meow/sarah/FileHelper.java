package meow.sarah;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FileHelper {

    public static String prefix;
    public static String authkey;
    public static String channel;
    public static String owner;

    private static int fileCount = 1;

    private static int backseatCount = -1;
    private static int sessionCount = 1;

    public static void loadFile(String currentPath) throws IOException {
        JSONObject jsonObject = readJsonObject(currentPath + "/config.json");
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

    public static File getFile(String currentPath) {
        File folder = new File(currentPath + "/output/" + Main.date);
        File[] files = folder.listFiles(file -> file.getName().endsWith(".json") && file.getName().equals("Session-" + fileCount + ".json"));
        return (files != null && files.length > 0) ? files[0] : null;
    }

    public static String listUserInputSize(String user) throws IOException {
        File file = getFile(new java.io.File(".").getCanonicalPath());
        int messageCount = 0;

        if (file != null) {
            String fileContent = Files.readString(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            JSONObject jsonData = new JSONObject(fileContent);
            for (String key : jsonData.keySet()) {
                if (key.startsWith("backseat")) {
                    JSONObject backseat = jsonData.getJSONObject(key);
                    String author = backseat.getString("user");
                    if (Objects.equals(author, user.trim())) {
                        messageCount++;
                    }
                }
            }
            if (messageCount == 0) {
                return "User " + user + " has no messages in the current session!";
            }
            return "User " + user + " has " + messageCount + " messages in the current session!";
        }
        return "No file found!";
    }


    public static String searchUserInput(String user, String search) throws IOException {
        File file = getFile(new java.io.File(".").getCanonicalPath());
        int messageCount = 0;

        if (file != null) {
            String fileContent = Files.readString(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
            JSONObject jsonData = new JSONObject(fileContent);
            HashMap<String, Integer> map = new HashMap<>();

            for (String key : jsonData.keySet()) {
                if (key.startsWith("backseat")) {
                    JSONObject backseat = jsonData.getJSONObject(key);
                    String content = backseat.getString("content");
                    if (content.matches(".*\\b" + search + "\\b.*")) {
                        String author = backseat.getString("user");
                        if (Objects.equals(author, user.trim())) {
                            messageCount++;
                            map.put(content, messageCount);
                        }
                    }
                }
            }
            if (messageCount == 0) {
                return "User " + user + " has no messages in the current session!";
            }

            StringBuilder resultBuilder = new StringBuilder();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                String content = entry.getKey();
                int count = entry.getValue();
                resultBuilder.append("Result ").append(count).append(" = \"").append(content).append("\"").append(System.lineSeparator()).append("  ");
            }
            return resultBuilder.toString();
        }
        return "No file found!";
    }


    public static String getInput(int id) throws IOException {
        // getFile returns a File object, which is then passed to readString
        File file = getFile(new java.io.File(".").getCanonicalPath());
        if (file != null) {
            try {
                String json = Files.readString(Paths.get(file.getAbsolutePath()));
                JSONObject jsonObject = new JSONObject(json);
                JSONObject backseat = jsonObject.getJSONObject("backseat" + id);
                String time = backseat.getString("time");
                String user = backseat.getString("user");
                String content = backseat.getString("content");
                Logger.log("Time: " + time + " User: " + user + " Content: " + content);
                return user + " said this at " + time + ":\n" + content;
            } catch (Exception e) {
                Logger.log("Failed to read JSON file!");
            }
        }
        return "No input found!";
    }

    private static JSONObject readJsonObject(String filePath) throws IOException {
        String jsonContent = Files.readString(Paths.get(filePath));
        return new JSONObject(jsonContent);
    }

    public static void writeFile(String content, String currentPath, String time, String user) throws IOException {
        File folder = new File(currentPath + "/output/" + Main.date);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles(file -> !file.getName().endsWith(".json"));
        if (files != null && files.length > 0 && !Main.firstRun) {
            fileCount += files.length;
        }
        Main.firstRun = true;
        Logger.log(fileCount + " : Count");

        File file = new File(folder, "Session-" + fileCount + ".txt");

        String content2 = content;

        JSONObject data = new JSONObject();
        JSONObject backseatKey = new JSONObject();
        backseatKey.put("time", time.trim());
        backseatKey.put("user", user.trim());
        backseatKey.put("session", sessionCount);
        backseatKey.put("content", content2 = content.substring(content.indexOf("\n") + 1).trim());
        backseatCount++;
        data.put("backseat" + backseatCount, backseatKey);

        try {
            File jsonFile = new File(folder, "Session-" + fileCount + ".json");
            if (!jsonFile.exists()) {
                jsonFile.createNewFile();
            } else {
                String existingData = Files.readString(Paths.get(jsonFile.getAbsolutePath()));
                if (!existingData.isEmpty()) {
                    JSONObject existingJson = new JSONObject(existingData);
                    existingJson.put("backseat" + getBackseatCount(), backseatKey);
                    data = existingJson;
                }
            }
            FileWriter jsonWriter = new FileWriter(jsonFile);
            jsonWriter.write(data.toString());
            jsonWriter.flush();
            jsonWriter.close();
        } catch (Exception e) {
            Logger.log("Failed to write to JSON file!");
        }

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(content + "\n \n");
            fileWriter.close();
        } catch (Exception e) {
            Logger.log("Failed to write to file!");
        }
    }

    public static int getBackseatCount() {
        return backseatCount;
    }

    public static void setBackseatCount(int backseatCount) {
        FileHelper.backseatCount = backseatCount;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        FileHelper.fileCount = fileCount;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        FileHelper.sessionCount = sessionCount;
    }
}
