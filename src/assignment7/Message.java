package assignment7;

public class Message {
    String chatID;
    String message;
    String name;

    public Message(String chatID, String message, String user) {
        this.chatID = chatID;
        this.message = message;
        this.name = user;
    }
}
