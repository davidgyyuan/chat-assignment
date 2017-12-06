package assignment7;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class User {
    public InetAddress ip;
    public int port;
    public String name;
    public Queue<PacketInfo> backlog;
    public HashSet<Chat> participantChats;

    public User(InetAddress ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
        backlog = new LinkedList<>();
        participantChats = new HashSet<>();
    }

    public boolean equals(Object o) {
        if (o instanceof User && ((User)o).name.equals(name)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public void clearBacklog() {
        backlog.clear();
    }
}
