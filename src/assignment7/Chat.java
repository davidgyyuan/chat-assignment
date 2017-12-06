package assignment7;

import java.util.*;

public class Chat implements Observer {
    private String chatID;
    private HashSet<String> users = new HashSet<>();
    private ArrayList<Message> messages = new ArrayList<>();
    private String initUser;
    private ObservableString newMessage = new ObservableString(this);

    public Chat(String chatID) {
        this.chatID = chatID;
    }

    public Chat(String chatID, String initUser) {
        this.chatID = chatID;
        users.add(initUser);
        this.initUser = initUser;
    }

    public void broadcastNewChat(String initUser) {
        User u = ServerMain.userMap.get(initUser);
        PacketInfo p = new PacketInfo(ServerMain.receiver, u.port, u.ip, 'x', chatID + ":" + initUser);
        u.backlog.add(p);
    }

    public void addMessage(Message m) {
        newMessage.setValue(String.format("%s:%s:%s", chatID, m.name, m.message));
        messages.add(m);
    }

    public void addMessageClient(Message m) {
        messages.add(m);
    }

//    public void broadcastLatestMessage() {
//        Message sent = messages.get(messages.size() - 1);
//        for (String s : users) {
//            User u = ServerMain.userMap.get(s);
//            u.backlog.add(new PacketInfo(ServerMain.receiver, u.port, u.ip, 'p',
//                    String.format("%s:%s:%s", chatID, sent.name, sent.message)));
//        }
//    }

    public void addUser(String s) {
        users.add(s);
        // TODO: update clinets
    }

    public void broadcastUserAdd(String s) {
        User u = ServerMain.userMap.get(s);
        PacketInfo p = new PacketInfo(ServerMain.receiver, u.port, u.ip, 'x', String.format("%s:%s", chatID, initUser));
        u.backlog.add(p);
    }

    public String getChatID() {
        return chatID;
    }

    public String getLastMessage() {
        if (messages.isEmpty()) {
            return "No messages here yet...";
        }
        return messages.get(messages.size() - 1).message;
    }

    public Message getLastMessageComplete() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public boolean hasUser(String s) {
        return users.contains(s);
    }

    public Collection<String> getUsers() {
        return users;
    }

    @Override
    public void update(Observable o, Object arg) {
        String info = ((ObservableString) o).getValue();
        for (String s : users) {
            User u = ServerMain.userMap.get(s);
            u.backlog.add(new PacketInfo(ServerMain.receiver, u.port, u.ip, 'p', info));
        }
    }
}
