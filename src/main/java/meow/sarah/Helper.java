package meow.sarah;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import java.io.IOException;

public class Helper {

    public static String channel = FileHelper.channel;

    public static void messageEvent(ChannelMessageEvent event, TwitchClient twitchClient, String currentPath) throws IOException {
        while (obshelper.isRunning) {
            Logger.log("Waiting for OBS to be ready...");
        }
        //Logger.log(event.getUser().getName() + ": " + event.getMessage());
        FileHelper fileHelper = new FileHelper();
        String message = event.getMessage();
        String[] arguments = message.split(" ");
        var mention = messageResponse.mentionUser(event.getUser().getName());
        var prefix = FileHelper.prefix;
        if (message.startsWith(prefix + "add")) {
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
                FileHelper.thread.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
            twitchClient.getChat().sendMessage(channel, mention + "backseat added to List!");
            //Logger.log(channel + " added a backseat to the list!");
            //Logger.log(mention);
        } else if (message.startsWith(prefix + "info")) {
            // Logger.log(event.getUser().getName() + " requested info!");
            twitchClient.getChat().sendMessage(channel, mention + "This allows you to backseat by typing a timestamp and what u want to backseat with !add <timestamp> <message>");
        } else if (message.startsWith(prefix + "next") && FileHelper.owner.equals(event.getUser().getId())) {
            fileHelper.setFileCount(fileHelper.getFileCount() + 1);
            fileHelper.setSessionCount(fileHelper.getSessionCount() + 1);
            FileHelper.setBackseatCount(0);
            twitchClient.getChat().sendMessage(channel, mention + "Starting new Session File!");
        } else if (message.startsWith(prefix + "get")) {
            twitchClient.getChat().sendMessage(channel, FileHelper.getInput(Integer.parseInt(arguments[1])));
            //Logger.log(event.getUser().getName() + " searched for " + arguments[1].toLowerCase());
        } else if (message.startsWith(prefix + "searchuser")) {
            //Logger.log(String.valueOf(twitchClient.getChat().sendMessage(channel, FileHelper.searchUserInput(event.getMessage().split(" ")[1].toLowerCase(), arguments[2]))));
            twitchClient.getChat().sendMessage(channel, FileHelper.searchUserInput(event.getMessage().split(" ")[1].toLowerCase(), arguments[2]));
        } else if (message.startsWith(prefix + "listuser")) {
            twitchClient.getChat().sendMessage(channel, FileHelper.listUserInputSize(arguments[1].toLowerCase()));
        } else if (message.startsWith(prefix + "list")) {
            twitchClient.getChat().sendMessage(channel, FileHelper.listAll());
        } else if (message.startsWith(prefix + "searchall")) {
            twitchClient.getChat().sendMessage(channel, FileHelper.searchAllInput(arguments[1]));
        }
        // if starts with prefix and not add or info
        else if (message.startsWith(prefix) && !message.startsWith(prefix + "add") && !message.startsWith(prefix + "info") && !message.startsWith(prefix + "next") && !message.startsWith(prefix + "get") && !message.startsWith(prefix + "searchuser") && !message.startsWith(prefix + "listuser") && !message.startsWith(prefix + "list") && !message.startsWith(prefix + "searchall")) {
            twitchClient.getChat().sendMessage(channel, mention + "Unknown command!");
        }
    }
}
