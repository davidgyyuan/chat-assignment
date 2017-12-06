package assignment7;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class PacketInfo {
    DatagramSocket receiver;
    public int port;
    public InetAddress ip;
    public char function;
    public String info;
    public String tail;

    public PacketInfo(DatagramSocket receiver, int port, InetAddress ip, String tail) {
        this.receiver = receiver;
        this.port = port;
        this.ip = ip;
        this.function = tail.charAt(0);
        this.info = tail.substring(1);
        this.tail = tail;
    }

    public PacketInfo(DatagramSocket receiver, int port, InetAddress ip, char function, String info) {
        this.receiver = receiver;
        this.port = port;
        this.ip = ip;
        this.function = function;
        this.info = info;
        this.tail = function + info;
    }

    // TODO: Remove varargs they don't actually do anything anymore
    public void sendPacket(Object... o) throws IOException {
        byte buffer[] = this.tail.getBytes();
        DatagramPacket packet
                = new DatagramPacket(
                        buffer, buffer.length, this.ip, this.port);
        receiver.send(packet);
    }

    public static PacketInfo getNewData(DatagramSocket receiver) {
        byte buffer[] = new byte[ChatConsts.size];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            receiver.receive(packet);
        } catch (SocketTimeoutException e){
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new PacketInfo(receiver, packet.getPort(), packet.getAddress(), removeNull(new String(buffer)));
    }

    private static String removeNull(String s) {
        int nullChar = s.indexOf('\u0000');
        if (nullChar == -1) {
            return s;
        }
        return s.substring(0, nullChar);
    }

    public void updateDestination(User u) {
        this.port = u.port;
        this.ip = u.ip;
    }
}
