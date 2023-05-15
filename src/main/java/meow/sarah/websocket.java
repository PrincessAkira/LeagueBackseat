package meow.sarah;

import okhttp3.internal.ws.WebSocketWriter;
import okio.ByteString;

/**
 * @author https://github.com/PrincessAkira (Sarah)
 * Today is the 5/15/2023 @11:57 AM
 * This project is named LeagueBackseat
 * @description Another day of Insanity
 */
public class websocket {

    public static void send(String message) {
        // send message to obs
        try {
            WebSocketWriter writer = new WebSocketWriter(true, null, null, true, true, 10000);
            writer.writeMessageFrame(8, ByteString.of(message.getBytes()));
            Logger.log("Sent message to OBS!");
        } catch (Exception e) {
            Logger.log("Failed to send message to OBS!");
        }
    }

}
