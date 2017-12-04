package assignment7;

import java.net.InetAddress;

public class User {
    public InetAddress ip;
    public String name;

    public User(InetAddress ip, String name) {
        this.ip = ip;
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
