package assignment7;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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

    public void sendPacket(boolean sendToServer) throws IOException {
        DatagramSocket sender = new DatagramSocket();
        byte buffer[] = this.tail.getBytes();
        DatagramPacket packet
                = new DatagramPacket(
                        buffer, buffer.length, this.ip, sendToServer ? ChatConsts.serverPort : ChatConsts.clientPort);
        sender.send(packet);
    }

    public static PacketInfo getNewData(DatagramSocket receiver) {
        byte buffer[] = new byte[ChatConsts.size];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            receiver.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new PacketInfo(packet.getAddress(), removeNull(new String(buffer)));
    }

    private static String removeNull(String s) {
        int nullChar = s.indexOf('\u0000');
        if (nullChar == -1) {
            return s;
        }
        return s.substring(0, nullChar);
    }
}
