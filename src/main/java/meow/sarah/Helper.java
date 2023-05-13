package meow.sarah;

import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import java.io.IOException;

import static meow.sarah.Main.outputfile;

public class Helper {

    public static void messageEvent(ChannelMessageEvent event, TwitchClient twitchClient, String currentPath) {
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
                // if string contains \d\D\d{2}|\d{2}\D\d{2}|\d{2}\D\d{1}d
                // those 30h a week of regex paid off.
                if (line.matches("\\d\\D\\d{2}|\\d{2}\\D\\d{2}|\\d{2}\\D\\d{1}") && !timeFound) {
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
            if (!timeFound) {
                twitchClient.getChat().sendMessage(event.getChannel().getName(), mention + "Try again with a timestamp! (2:15 - message)");
                return;
            }
            try {
                FileHelper.writeFile(outputfile, stringBuilder.toString(), currentPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            twitchClient.getChat().sendMessage(event.getChannel().getName(), mention + "backseat added to List!");
            Logger.log(event.getUser().getName() + " added a backseat to the list!");
            Logger.log(mention);
        } else if (message.startsWith(prefix + "info")) {
            // Logger.log(event.getUser().getName() + " requested info!");
            twitchClient.getChat().sendMessage(event.getChannel().getName(), mention + "This allows you to backseat by typing a timestamp and what u want to backseat with !add <timestamp> <message>");
        } else if (message.startsWith(prefix + "next") && FileHelper.owner.equals(event.getUser().getId())) {
            fileHelper.setFileCount(fileHelper.getFileCount() + 1);
            twitchClient.getChat().sendMessage(event.getChannel().getName(), mention + "Starting new Session File!");
        }
        // if starts with prefix and not add or info
        else if (message.startsWith(prefix) && !message.startsWith(prefix + "add") && !message.startsWith(prefix + "info")) {
            twitchClient.getChat().sendMessage(event.getChannel().getName(), mention + "Unknown command!");
        }
    }
}
