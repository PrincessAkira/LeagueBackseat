package meow.sarah;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FileHelper {
    // @formatter:off
    public static String
            prefix       = "",
            authKey      = "",
            clientId     = "",
            clientSecret = "",
            refreshToken = "",
            channel      = "",
            owner        = "",
            obsScene     = "";
    // @formatter:on

    static Thread thread = new Thread(() -> {
        obshelper.isRunning = true;
        // toggle scene visibility to false
        // wait for 3seconds
        try {
            obshelper.toggleSceneVisibility(false);
            Thread.sleep(2000);
            // toggle scene visibility to true
            obshelper.toggleSceneVisibility(true);
        } catch (final InterruptedException exception) {
            exception.printStackTrace();
        }
        obshelper.isRunning = false;
        return;
    });

    private static int fileCount = 1;
    private static int backseatCount = -1;
    private static int sessionCount = 1;

    // @formatter:off
    public static @NotNull CompletableFuture<Void>
    loadFile(final String currentPath) { return loadFile(currentPath, throwable -> {
        throwable.printStackTrace();
        System.exit(-1);
    }); }
    // @formatter:on

    public static CompletableFuture<Void> loadFile(
            final String currentPath,
            final Consumer<Throwable> onError
    ) {
        return CompletableFuture.runAsync(() -> {
            try {
                Logger.log("Loading config file...");
                final JSONObject jsonObject = readJsonObject(String.format("%s%cconfig.json", currentPath, File.separatorChar));
                // @formatter:off
                if (Stream.of(
                                prefix       = jsonObject.getString("prefix"      ),
                                authKey      = jsonObject.getString("token"       ),
                                channel      = jsonObject.getString("channel"     ),
                                owner        = jsonObject.getString("admin"       ),
                                clientId     = jsonObject.getString("clientid"    ),
                                clientSecret = jsonObject.getString("secret"      ),
                                obsScene     = jsonObject.getString("obsscene"    ),
                                refreshToken = jsonObject.getString("refreshtoken")
                        ).filter(Objects::isNull)
                        .findAny()
                        .orElse("__not_found__") != "__not_found__") {
                    final String fileNotFound = "Config file not found!";
                    Logger.log(fileNotFound);
                    onError.accept(new FileNotFoundException(fileNotFound));
                }
                // @formatter:on
                Logger.log("Loaded config file!");
            } catch (final Throwable throwable) {
                onError.accept(throwable);
            }
        });
    }

    public static @NotNull CompletableFuture<@Nullable File> getFile(final @NotNull String currentPath) {
        return CompletableFuture.supplyAsync(() -> {
            File folder = new File(currentPath, String.format("output%c%s", File.separatorChar, Main.date));
            Logger.log(String.format("Looking for file in %s", folder.getAbsolutePath()));
            File[] files = folder.listFiles(file -> file.getName().endsWith(".json") && file.getName().equals(String.format("Session-%d.json", fileCount)));
            return (files != null && files.length != 0) ? files[0] : null;
        });
    }


    public static CompletableFuture<String> listAll() {
        return listAll(null);
    }

    public static CompletableFuture<String> listAll(final @Nullable Consumer<@NotNull Throwable> onError) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final File file = getFile(Paths.get("").toAbsolutePath().toString()).join();
                if (file == null) return "No file found!";

                final String fileContent = Files.readString(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
                if (fileContent.isEmpty()) return "Nothing was added to the current session yet!";

                final int messageCount = new JSONObject(fileContent).keySet().stream().filter(key -> !key.startsWith("backseat")).toList().size();
                return "There are" + (messageCount == 0 ? "no" : messageCount) + " backseat messages in the current session!";
            } catch (final Throwable throwable) {
                if (onError != null) onError.accept(throwable);
                return "No file found! (with errors)";
            }
        });
    }

    public static CompletableFuture<String> listUserInputSize(
            final @NotNull String user,
            final @Nullable Consumer<Throwable> onError
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final File file = getFile(Paths.get("").toAbsolutePath().toString()).join();
                if (file == null) return "No file found!";

                final String fileContent = Files.readString(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
                if (fileContent.isEmpty()) return "Nothing was added to the current session yet!";

                final String trimmedUser = user.trim();
                final JSONObject fileContentJsonObj = new JSONObject(fileContent);

                final int messageCount = fileContentJsonObj.keySet().stream().filter(key -> key.startsWith("backseat") &&
                        Objects.equals(fileContentJsonObj.getJSONObject(key).getString("user"), trimmedUser)).toList().size();
                return "User " + user + " has " + (messageCount == 0 ? "no" : messageCount) + " messages in the current session!";
            } catch (final Throwable throwable) {
                if (onError != null) onError.accept(throwable);
                return "No file found! (with errors)";
            }
        });
    }

    public static CompletableFuture<String> searchAllInput(final @NotNull String search) {
        return searchAllInput(search, null);
    }

    public static CompletableFuture<String> searchAllInput(
            final @NotNull String search,
            final @Nullable Consumer<Throwable> onError
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final File file = getFile(Paths.get("").toAbsolutePath().toString()).join();
                if (file == null) return "No file found!";

                final String fileContent = Files.readString(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
                if (fileContent.isEmpty()) return "Nothing was added to the current session yet!";

                final String regex = String.format(".*\\b%s\\b.*", search);
                final JSONObject fileContentJsonObj = new JSONObject(fileContent);
                final Map<String, Integer> contentToMsgCountMap = new ConcurrentHashMap<>();
                int messageCount = 0;

                Logger.log(String.format("Searching for \"%s\" in the current session...", search));

                for (final String key : fileContentJsonObj.keySet()) {
                    if (!key.startsWith("backseat")) continue;
                    final String content = fileContentJsonObj.getJSONObject(key).getString("content");
                    if (!content.matches(regex)) continue;
                    contentToMsgCountMap.put(content, ++messageCount);
                    Logger.log(String.format("Found \"%s\" in message %d = \"%s\"", search, messageCount, content));
                }

                if (messageCount == 0)
                    return String.format("There are no messages containing \"%s\" in the current session!", search);
                final StringBuilder resultBuilder = new StringBuilder();
                contentToMsgCountMap.forEach((key, value) -> resultBuilder
                        .append("Result ")
                        .append(value)
                        .append(" = \"")
                        .append(key)
                        .append("\"")
                        .append(System.lineSeparator())
                        .append("  "));
                return resultBuilder.toString();
            } catch (final Throwable throwable) {
                if (onError != null) onError.accept(throwable);
                return "No file found! (with errors)";
            }
        });
    }

    // @formatter:off
    public static CompletableFuture<String> searchUserInput(
            final @NotNull String user,
            final @NotNull String search
    ) { return searchUserInput(user, search, null); }
    // @formatter:on

    public static CompletableFuture<String> searchUserInput(
            final @NotNull String user,
            final @NotNull String search,
            final @Nullable Consumer<Throwable> onError
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final File file = getFile(Paths.get("").toAbsolutePath().toString()).join();
                if (file == null) return "No file found!";

                final String fileContent = Files.readString(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8);
                if (fileContent.isEmpty()) return "Nothing was added to the current session yet!";

                final String regex = String.format(".*\\b%s\\b.*", search);
                final JSONObject fileContentJsonObj = new JSONObject(fileContent);
                final Map<String, Integer> contentToMsgCountMap = new ConcurrentHashMap<>();
                int messageCount = 0;

                Logger.log(String.format("Searching for \"%s\" in the current session...", search));

                for (final String key : fileContentJsonObj.keySet()) {
                    if (!key.startsWith("backseat")) continue;
                    final JSONObject backseatJson = fileContentJsonObj.getJSONObject(key);
                    final String content = backseatJson.getString("content");
                    if (!content.matches(regex)) continue;
                    if (!Objects.equals(backseatJson.getString("user"), user.trim())) continue;
                    contentToMsgCountMap.put(content, ++messageCount);
                    Logger.log(String.format("Found \"%s\" in message %d = \"%s\" for user \"%s\"!", search, messageCount, content, user));
                }

                if (messageCount == 0)
                    return String.format("User %s has no messages containing \"%s\" in the current session!", user, search);
                final StringBuilder resultBuilder = new StringBuilder();
                for (Map.Entry<String, Integer> entry : contentToMsgCountMap.entrySet()) {
                    String content = entry.getKey();
                    int count = entry.getValue();
                    resultBuilder
                            .append("Result ")
                            .append(count)
                            .append(" = \"")
                            .append(content)
                            .append("\"")
                            .append(System.lineSeparator())
                            .append("  ");
                }
                return resultBuilder.toString();
            } catch (final Throwable throwable) {
                if (onError != null) onError.accept(throwable);
                return "No file found! (with errors)";
            }
        });
    }

    public static CompletableFuture<String> getInput(final int id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                final File file = getFile(Paths.get("").toAbsolutePath().toString()).join();
                if (file == null) return "No input found!";
                String json = Files.readString(Paths.get(file.getAbsolutePath()));
                if (json.isEmpty())
                    return "Nothing was added to the current session yet!";
                JSONObject jsonObject = new JSONObject(json);
                JSONObject backseat = jsonObject.getJSONObject("backseat" + id);
                String time = backseat.getString("time");
                String user = backseat.getString("user");
                String content = backseat.getString("content");
                return user + " said this at " + time + ":\n" + content;
            } catch (IOException e) {
                Logger.log("Failed to read JSON file!");
            }
            return "No input found!";
        });
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

        //obshelper.updateItemText("LeagueInput", obshelper.item("LeagueInput"), content2);
        writeOBSFile(user.trim() + " - " + content2);

        try {

            File jsonFile = new File(folder, "Session-" + fileCount + ".json");

            if (jsonFile.exists()) {
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

    public static void writeOBSFile(String text) throws IOException {
        // create file if it doesn't exist
        File textfile = new File("obs.txt");
        // remove old text from file and replace with new text
        FileWriter fileWriter = new FileWriter(textfile, false);
        fileWriter.write(text.replaceAll("\\s+", " ").trim() + " ");
        // add new line on text that is too long
        if (text.length() > 50) {
            fileWriter.write("\n");
        }
        fileWriter.flush();
        fileWriter.close();
        // run thread if thread before finished
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
