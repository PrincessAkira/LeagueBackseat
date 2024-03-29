package meow.sarah;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static meow.sarah.FileHelper.*;

/**
 * @author https://github.com/PrincessAkira (Sarah)
 * Today is the 5/12/2023 @7:08 PM
 * This project is named Default (Template) Project
 * @description
 */
public class Main {

    static boolean firstRun = false;
    static String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    static File outputfile;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Starting...");
        String currentPath = new java.io.File(".").getCanonicalPath();
        outputfile = new File("output-" + date + ".txt");
        try {
            loadFile(currentPath).join();
        } catch (Exception e) {
            Logger.log("Failed to load config file!");
            System.exit(0);
        }
        OAuth2Credential credential = new OAuth2Credential("twitch", authKey);
        TwitchClient twitchClient;
        twitchClient = TwitchClientBuilder.builder()
                .withEnableHelix(true)
                .withChatAccount(credential)
                .withClientId(clientId)
                .withClientSecret(clientSecret)
                .withEnableChat(true)
                .build();
        try {
            twitchClient.getChat().joinChannel(channel);
            twitchClient.getChat().sendMessage(channel, "Bot connected!");
            Logger.log("Bot connected!");
        } catch (Exception e) {
            Logger.log("Failed to join channel!");
            System.exit(0);
        }
        try {
            obshelper.leagueController.connect();
        } catch (Exception e) {
            Logger.log("Failed to connect to OBS!");
            System.exit(0);
        }
        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            try {
                Helper.messageEvent(event, twitchClient, currentPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}