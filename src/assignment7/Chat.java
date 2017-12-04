package assignment7;

import java.util.ArrayList;
import java.util.HashSet;

public class Chat {
    private String chatID;
    private HashSet<String> users = new HashSet<>();
    private ArrayList<Message> messages = new ArrayList<>();

    public Chat(String chatID, String initUser) {
        this.chatID = chatID;
        users.add(initUser);
        // TODO: update Clients??
    }

    public void addMessage(Message m) {
        messages.add(m);
        // TODO: update Clients
    }

    public void addUser(String s) {
        users.add(s);
        // TODO: update clinets
    }
}
