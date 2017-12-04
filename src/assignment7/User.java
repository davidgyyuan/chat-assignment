package assignment7;

import java.net.InetAddress;

public class User {
    public InetAddress ip;
    public int port;
    public String name;

    public User(InetAddress ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
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
}
