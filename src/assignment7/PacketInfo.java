package assignment7;

import java.net.InetAddress;

public class PacketInfo {
    public InetAddress ip;
    public char function;
    public String info;
    public String tail;

    public PacketInfo(InetAddress ip, String tail) {
        this.ip = ip;
        this.function = tail.charAt(0);
        this.info = tail.substring(1);
        this.tail = tail;
    }

    public PacketInfo(InetAddress ip, char function, String info) {
        this.ip = ip;
        this.function = function;
        this.info = info;
        this.tail = function + info;
    }
}
