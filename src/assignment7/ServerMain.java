package assignment7;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Random;

/*
    Format:
    [x][info]

    x - function
        c - new chat -> chatID
            original user
        a - add user to chat
            chatID:added user
        m - message
            chatID:message
        u - user registration
            username

        y - successful response
            some info
        n - unsuccessful response
            reason

*/

public class ServerMain {

    static Random randomGenerator = new Random();
    static DatagramSocket receiver;
    // Name to User
    static HashMap<String, User> userMap = new HashMap<>();
    // Chat ID to Chat
    static HashMap<String, Chat> chatMap = new HashMap<>();

    public static void main(String... args) throws IOException {
        System.out.println("Server Starting...");
        System.out.print("Server IP: ");
        System.out.println(Inet4Address.getLocalHost().getHostAddress());
        receiver = new DatagramSocket(ChatConsts.serverPort);

        while (true) {
            PacketInfo receivedData = PacketInfo.getNewData(receiver);
            if (receivedData == null) {
                continue;
            }
            if (receivedData.function == 'u') {
                /*if (userMap.containsKey(receivedData.info)) {
                    sendPacket(new PacketInfo(receivedData.ip, 'n', "User exists"));
                }*/
                userMap.put(receivedData.info, new User(receivedData.ip, receivedData.port, receivedData.info));
                new PacketInfo(receiver, receivedData.port, receivedData.ip, 'y', " ").sendPacket(false, receiver.getLocalPort());
            } else if (receivedData.function == 'c') {
                String user = receivedData.info;
                String id = generateID();
                chatMap.put(id, new Chat(id, user));
                new PacketInfo(receiver, receivedData.port, receivedData.ip, 'y', id).sendPacket(false, receiver.getLocalPort());
            } else if (receivedData.function == 'a') {
                String[] data = enhancedSplit(receivedData.info, 1);
                String id = data[0];
                String user = data[1];
                if (userMap.containsKey(user)) {
                    Chat c = chatMap.get(id);
                    c.addUser(user);
                    new PacketInfo(receiver, receivedData.port, receivedData.ip, 'y', " ").sendPacket(false, receiver.getLocalPort());
                } else {
                    new PacketInfo(receiver, receivedData.port, receivedData.ip, 'n', "User does not exist").sendPacket(false, receiver.getLocalPort());
                }
            } else if (receivedData.function == 'm') {
                String[] data = enhancedSplit(receivedData.info, 1);
                String id = data[0];
                String message = data[1];
                Chat c = chatMap.get(id);
                c.addMessage(new Message(id, message, ipToName(receivedData.ip)));
            }
        }
    }

    private static String ipToName(InetAddress ip) {
        for (User u : userMap.values()) {
            if (u.ip.equals(ip)) {
                return u.name;
            }
        }
        throw new RuntimeException("IP doesn't exist");
    }

    private static String[] enhancedSplit(String info, int i) {
        String[] data = new String[i + 1];
        String[] split = info.split(":");
        int j = 0;
        for (; j < i; j++) {
            data[j] = split[j];
        }
        StringBuilder tail = new StringBuilder();
        for (int k = j; k < split.length; k++) {
            tail.append(split[k]);
        }
        data[j] = tail.toString();
        return data;
    }

    private static String generateID() {
        StringBuilder potentialID = new StringBuilder();
        do {
            for (int i = 0; i < 5; i++) {
                char nextChar = (char) (randomGenerator.nextInt(126 - 33) + 33);
                potentialID.append(nextChar);
            }
        } while (chatMap.containsKey(potentialID.toString()));
        return potentialID.toString();
    }
}
