package meow.sarah;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static meow.sarah.FileHelper.getInputAsync;

public class Helper {

    public static String channel = FileHelper.channel;

    public static void messageEvent(ChannelMessageEvent event, TwitchClient twitchClient, String currentPath) throws IOException {
        if (obshelper.isRunning) {
            // Wait until OBS is connected before processing messages
            return;
        }

        FileHelper fileHelper = new FileHelper();
        Thread fileHelperThread = new Thread(new FileHelperWorker(fileHelper));
        fileHelperThread.start();

        String message = event.getMessage();
        String[] arguments = message.split(" ");
        String mention = messageResponse.mentionUser(event.getUser().getName());
        String prefix = FileHelper.prefix;

        if (message.startsWith(prefix + "add")) {
            Logger.log("Add command received!");
            handleAddCommand(event, twitchClient, currentPath, arguments, mention, prefix);
        } else if (message.startsWith(prefix + "info")) {
            twitchClient.getChat().sendMessage(channel, mention + "This allows you to backseat by typing a timestamp and what you want to backseat with !add <timestamp> <message>");
        } else if (message.startsWith(prefix + "next") && FileHelper.owner.equals(event.getUser().getId())) {
            handleNextCommand(event, twitchClient, mention, fileHelper);
        } else if (message.startsWith(prefix + "get")) {
            handleGetCommand(event, twitchClient, arguments, mention);
        } else if (message.startsWith(prefix + "searchuser")) {
            handleSearchUserCommand(twitchClient, arguments, mention);
        } else if (message.startsWith(prefix + "listuser")) {
            handleListUserCommand(twitchClient, arguments, mention);
        } else if (message.startsWith(prefix + "list")) {
            handleListAllCommand(twitchClient, mention);
        } else if (message.startsWith(prefix + "searchall")) {
            handleSearchAllCommand(twitchClient, arguments, mention);
        } else if (message.startsWith(prefix)) {
            twitchClient.getChat().sendMessage(channel, mention + "Unknown command!");
        }
    }

    private static void handleAddCommand(ChannelMessageEvent event, TwitchClient twitchClient, String currentPath,
                                         String[] arguments, String mention, String prefix) throws IOException {
        // create a string builder, so we can append the lines
        Logger.log("Add command received!");
        StringBuilder stringBuilder = new StringBuilder();
        String time = "";
        boolean timeFound = false;
        for (String line : arguments) {
            // if string contains \d:\d{2}|\d{2}:\d{2}|\d{2}:\d{1}
            // those 30h a week of regex paid off.
            if (line.matches("\\d:\\d{2}|\\d{2}:\\d{2}|\\d{2}:\\d{1}") && !timeFound) {
                time = line;
                timeFound = true;

                // split time on : and check if second number is between 0 and 60 and not longer than 2 digits
                String[] timeSplit = time.split(":");
                if (Integer.parseInt(timeSplit[1]) > 60 || timeSplit[1].length() > 2) {
                    twitchClient.getChat().sendMessage(channel, mention + "Try again with a valid timestamp! (e.g 2:15 - message)");
                    return;
                }

                //Logger.log("Found time " + time);
                stringBuilder.insert(0, mention + " noticed on " + time + " : \n");
            } else if (line.matches(prefix + "add")) {
                continue;
            } else {
                stringBuilder.append(line).append(" ");
            }
        }
        if (stringBuilder.substring(stringBuilder.indexOf("\n") + 1).trim().length() < 1) {
            twitchClient.getChat().sendMessage(channel, mention + "Try again with a message! (e.g 2:15 - message)");
            return;
        }
        if (!timeFound) {
            twitchClient.getChat().sendMessage(channel, mention + "Try again with a timestamp! (e.g 2:15 - message)");
            return;
        }
        try {
            FileHelper.writeFile(stringBuilder.toString(), currentPath, time, mention);
            FileHelper.thread.start();
        } catch (IOException e) {
            e.printStackTrace();
            // stop the thread
            FileHelper.thread.interrupt();
        }
        twitchClient.getChat().sendMessage(channel, mention + "backseat added to List!");
    }

    private static void handleNextCommand(ChannelMessageEvent event, TwitchClient twitchClient, String mention, FileHelper fileHelper) {
        Thread nextCommandThread = new Thread(() -> {
            fileHelper.setFileCount(fileHelper.getFileCount() + 1);
            fileHelper.setSessionCount(fileHelper.getSessionCount() + 1);
            FileHelper.setBackseatCount(0);
            twitchClient.getChat().sendMessage(channel, mention + "Starting a new Session File!");
        });

        nextCommandThread.start();

        try {
            nextCommandThread.join(); // Wait for the thread to finish
        } catch (InterruptedException e) {
            // Handle InterruptedException if necessary
        }
    }

    public static void handleGetCommand(ChannelMessageEvent event, TwitchClient twitchClient, String[] arguments, String mention) {
        CompletableFuture<String> future = getInputAsync(Integer.parseInt(arguments[1]));

        future.thenAccept(result -> {
            // Send the result to the chat
            twitchClient.getChat().sendMessage(channel, result);
        });
    }

    private static void handleSearchUserCommand(TwitchClient twitchClient, String[] arguments, String mention) {
        // Your 'searchuser' command logic
    }

    private static void handleListUserCommand(TwitchClient twitchClient, String[] arguments, String mention) {
        // Your 'listuser' command logic
    }

    private static void handleListAllCommand(TwitchClient twitchClient, String mention) {
        // Your 'list' command logic
    }

    private static void handleSearchAllCommand(TwitchClient twitchClient, String[] arguments, String mention) {
        // Your 'searchall' command logic
    }

}
