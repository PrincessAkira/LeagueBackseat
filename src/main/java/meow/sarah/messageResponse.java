package meow.sarah;

public class messageResponse {

    public static String mentionUser(String event) {
        // this somehow returns the right mention but mentions the bot instead of the user
        return "@" + event + " ";
    }

}
