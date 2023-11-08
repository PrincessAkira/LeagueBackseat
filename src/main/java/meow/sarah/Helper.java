package meow.sarah;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

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
            handleAddCommand(event, twitchClient, currentPath, arguments, mention, prefix);
        } else if (message.startsWith(prefix + "info")) {
            twitchClient.getChat().sendMessage(channel, mention + "This allows you to backseat by typing a timestamp and what you want to backseat with !add <timestamp> <message>");
        } else if (message.startsWith(prefix + "next") && FileHelper.owner.equals(event.getUser().getId())) {
            handleNextCommand(event, twitchClient, mention, fileHelper);
        } else if (message.startsWith(prefix + "get")) {
            Logger.log(Arrays.toString(arguments));
            handleGetCommand(event, twitchClient, arguments, mention);
        } else if (message.startsWith(prefix + "searchuser")) {
            handleSearchUserCommand(twitchClient, arguments, mention, event);
        } else if (message.startsWith(prefix + "listuser")) {
            handleListUserCommand(twitchClient, arguments, mention);
        } else if (message.startsWith(prefix + "list")) {
            handleListAllCommand(twitchClient);
        } else if (message.startsWith(prefix + "searchall")) {
            handleSearchAllCommand(twitchClient, arguments, mention);
        } else if (message.startsWith(prefix)) {
            twitchClient.getChat().sendMessage(channel, mention + "Unknown command!");
        }
    }

    private static void handleAddCommand(ChannelMessageEvent event, TwitchClient twitchClient, String currentPath, String[] arguments, String mention, String prefix) throws IOException {
        // create a string builder, so we can append the lines
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
                stringBuilder.insert(0, mention + " noticed on " + time + " : \n");
            } else if (!line.startsWith(prefix + "add")) {
                stringBuilder.append(line).append(" ");
            }
        }
        if (stringBuilder.substring(stringBuilder.indexOf("\n") + 1).trim().length() < 1 || !timeFound) {
            twitchClient.getChat().sendMessage(channel, mention + "Try again with a message! (e.g 2:15 - message)");
            return;
        }
        try {
            FileHelper.writeFile(stringBuilder.toString(), currentPath, time, mention);
            if (obshelper.isRunning) {
                FileHelper.thread.start();
                Logger.log(FileHelper.thread.isAlive() + " " + FileHelper.thread.getState());
            }
        } catch (IOException e) {
            e.printStackTrace();
            // stop the thread
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
        CompletableFuture<String> messageFuture = FileHelper.getInput(Integer.parseInt(arguments[1]));
        messageFuture.thenApply(message -> twitchClient.getChat().sendMessage(channel, message));
    }

    private static void handleSearchUserCommand(TwitchClient twitchClient, String[] arguments, String mention, ChannelMessageEvent event) {
        CompletableFuture<String> messageFuture = FileHelper.searchUserInput(event.getMessage().split(" ")[1].toLowerCase(), arguments[2]);
        messageFuture.thenApply(message -> twitchClient.getChat().sendMessage(channel, message));
    }

    private static void handleListUserCommand(TwitchClient twitchClient, String[] arguments, String mention) {
        CompletableFuture<String> messageFuture = FileHelper.listUserInputSize(mention, Throwable::printStackTrace);
        messageFuture.thenApply(message -> twitchClient.getChat().sendMessage(channel, message));
    }


    private static void handleListAllCommand(TwitchClient twitchClient) {
        CompletableFuture<String> messageFuture = FileHelper.listAll(Throwable::printStackTrace);
        messageFuture.thenApply(message -> twitchClient.getChat().sendMessage(channel, message));
    }


    private static void handleSearchAllCommand(TwitchClient twitchClient, String[] arguments, String mention) {
        CompletableFuture<String> messageFuture = FileHelper.searchAllInput(arguments[1], Throwable::printStackTrace);
        messageFuture.thenApply(message -> twitchClient.getChat().sendMessage(channel, message));
    }

}
