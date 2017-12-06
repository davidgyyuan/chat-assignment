package assignment7;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientMain extends Application{

    static final boolean inDebug = false;

    static String name;
    static InetAddress serverIP;
    static DatagramSocket receiver;
    static ArrayList<Chat> chats = new ArrayList<>();

    int chatIn = -1;
    boolean updateScanning = false;
    boolean serverUp = false;

    ListView<String> chatList;
    ListView<String> chatHistory;
    String currentChatID = "";

    public static void main(String... args) {
        int i = 0;
        while (true) {
            try {
                receiver = new DatagramSocket(ChatConsts.clientPort + i);
                if (! inDebug) {
                    receiver.setSoTimeout(200);
                }
                break;
            } catch (SocketException e) {
                i++;
            }
        }
        launch(args);
    }

    public void start(Stage primaryStage) {
        Timeline socketChecker = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!updateScanning) {
                    return;
                }
                try {
                    new PacketInfo(receiver, ChatConsts.serverPort, serverIP, 'r', name).sendPacket();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PacketInfo response = PacketInfo.getNewData(receiver);
                if (response == null) {
                    if (serverUp) {
                        serverUp = false;
                        error("Server is shut down.");
                    }
                    return;
                }
                /*if (! serverUp) {
                    popup("Server is up.");
                }*/
                serverUp = true;
                int updates = Integer.parseInt(response.info);
                //System.out.println(updates);
                for (int i = 0; i < updates; i++) {
                    processPacket(PacketInfo.getNewData(receiver));
                }
            }
        }));
        socketChecker.setCycleCount(Timeline.INDEFINITE);
        socketChecker.play();

        primaryStage.setTitle("Login");
        GridPane loginGrid = new GridPane();
        Scene loginScene = new Scene(loginGrid);

        GridPane mainGrid = new GridPane();
        Scene mainScene = new Scene(mainGrid);

        GridPane chatGrid = new GridPane();
        Scene chatScene = new Scene(chatGrid);

        Button backButton = new Button("Back");
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.setScene(mainScene);
                chatIn = -1;

                ObservableList<String> chatsInfo = FXCollections.observableArrayList(toSummaryChatInfo());
                chatList.setItems(chatsInfo);
            }
        });
        Label chatIDLabel = new Label();
        chatHistory = new ListView<>();
        TextField chatInputField = new TextField();
        Button sendButton = new Button("Send Message");
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    new PacketInfo(receiver, ChatConsts.serverPort, serverIP, 'm',
                            currentChatID + ":" + name + ":" + chatInputField.getText())
                            .sendPacket();
                    chatInputField.setText("");
                } catch (IOException e) {
                    error("Could not send message");
                }
            }
        });
        TextField addUserField = new TextField();
        Button addUserButton = new Button("Add User");
        addUserButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String newUser = addUserField.getText();
                addUserField.setText("");
                try {
                    new PacketInfo(receiver, ChatConsts.serverPort, serverIP, 'a',
                            String.format("%s:%s", currentChatID, newUser)).sendPacket();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PacketInfo p = PacketInfo.getNewData(receiver);
                if (p == null) {
                    error("Server is down.");
                }
                else if (p.function == 'y') {
                    popup(String.format("User %s added to chat!", newUser));
                } else {
                    if (p.info.equals("e")) {
                        error(String.format("User %s does not exist.", newUser));
                    }
                    else {
                        error(String.format("User already in chat.", newUser));
                    }
                }
            }
        });
        Button viewUsersButton = new Button("View Users");
        viewUsersButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    new PacketInfo(receiver, ChatConsts.serverPort, serverIP, 'l', currentChatID).sendPacket();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PacketInfo p = PacketInfo.getNewData(receiver);
                if (p == null) {
                    error("Server is down.");
                } else {
                    popup(p.info);
                }
            }
        });

        chatGrid.add(backButton, 0, 0);
        chatGrid.add(chatIDLabel, 1, 0);
        chatGrid.add(chatHistory, 1, 1);
        chatGrid.add(chatInputField, 1, 2);
        chatGrid.add(sendButton, 1, 3);
        chatGrid.add(addUserField, 0, 2);
        chatGrid.add(addUserButton, 0, 3);
        chatGrid.add(viewUsersButton, 0, 1);

        Label nameLabel = new Label("Username:");
        TextField nameField = new TextField();
        Label ipLabel = new Label("Server IP:");
        TextField ipField = new TextField();
        Button login = new Button("Login");
        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (sendUserRegister(nameField.getText(), ipField.getText())) {
                    primaryStage.setTitle(
                            String.format("Username: %s, Server IP: %s", name, serverIP.getHostAddress()));
                    primaryStage.setScene(mainScene);
                    primaryStage.sizeToScene();
                    updateScanning = true;
                    serverUp = true;
                    ObservableList<String> chatsInfo = FXCollections.observableArrayList(toSummaryChatInfo());
                    chatList.setItems(chatsInfo);
                }
            }
        });

        loginGrid.add(nameLabel, col(true), row(true));
        loginGrid.add(nameField, col(false), row(false));
        loginGrid.add(ipLabel, col(true), row(true));
        loginGrid.add(ipField, col(false), row(false));
        loginGrid.add(login, col(true), row(true));

        resetRowCount();

        chatList = new ListView<>();
        ObservableList<String> chatsInfo = FXCollections.observableArrayList(toSummaryChatInfo());
        chatList.setItems(chatsInfo);
        Button newChatButton = new Button("New Chat");
        newChatButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newChat();
            }
        });
        Button openChatButton = new Button("Open Chat");
        openChatButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int selected = chatList.getSelectionModel().getSelectedIndex();
                if (selected > -1) {
                    chatIn = selected;
                    ObservableList<String> chatsObservableList =
                            FXCollections.observableArrayList(toSummaryChatHistory(selected));
                    chatHistory.setItems(chatsObservableList);
                    currentChatID = chats.get(selected).getChatID();
                    chatIDLabel.setText("Chat ID: " + currentChatID);
                    primaryStage.setScene(chatScene);
                }
            }
        });
        Button quit = new Button("Quit");
        quit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.exit(0);
            }
        });

        mainGrid.add(newChatButton, col(true), row(true));
        mainGrid.add(chatList, col(true), row(true));
        mainGrid.add(openChatButton, col(true), row(true));
        mainGrid.add(quit, col(true), row(true));

        primaryStage.setScene(loginScene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private void processPacket(PacketInfo newData) {
        if (newData == null) {
            return;
        }
        if (newData.function == 'x') {
            String[] data = enhancedSplit(newData.info, 1);
            String chatID = data[0];
            String user = data[1];
            Chat c = new Chat(chatID, user);
            chats.add(c);
            ObservableList<String> chatsInfo = FXCollections.observableArrayList(toSummaryChatInfo());
            chatList.setItems(chatsInfo);
        } else if (newData.function == 'p') {
            String[] data = enhancedSplit(newData.info, 2);
            String chatID = data[0];
            String user = data[1];
            String message = data[2];
            Message m = new Message(chatID, message, user);
            Chat c = findChat(chatID);
            c.addMessageClient(m);
            if (chatIn >= 0 && chats.get(chatIn).getChatID().equals(chatID)) {
                ObservableList<String> chatsObservableList =
                        FXCollections.observableArrayList(toSummaryChatHistory(chatIn));
                chatHistory.setItems(chatsObservableList);
            }
            ObservableList<String> chatsInfo = FXCollections.observableArrayList(toSummaryChatInfo());
            // Hack to make list update...
            chatsInfo.add(" ");
            chatList.setItems(chatsInfo);
            chatsInfo.remove(chatsInfo.size() - 1);
            chatList.setItems(chatsInfo);
        }
    }

    private ArrayList<String> toSummaryChatHistory(int selected) {
        Chat c = chats.get(selected);
        ArrayList<String> entries = new ArrayList<>();
        for (Message m : c.getMessages()) {
            String s = String.format("%s said: %n %s", m.name, m.message);
            entries.add(s);
        }
        return entries;
    }

    private ArrayList<String> toSummaryChatInfo() {
        ArrayList<String> info = new ArrayList<>();
        for (Chat c : chats) {
            String s = "Chat ID: " + c.getChatID() + System.lineSeparator() + c.getLastMessage();
            info.add(s);
        }
        return info;
    }

    private void newChat() {
        try {
            new PacketInfo(receiver, ChatConsts.serverPort, serverIP, 'c', name).sendPacket(true, receiver.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
            //return null;
        }
    }

    private Chat findChat (String id) {
        for (Chat c : chats) {
            if (c.getChatID().equals(id)) {
                return c;
            }
        }
        return null;
    }

    private boolean sendUserRegister(String user, String ipFieldText) {
        try {
            new PacketInfo(receiver, ChatConsts.serverPort, InetAddress.getByName(ipFieldText), 'u', user).sendPacket();
        } catch (IOException e) {
            e.printStackTrace();
            error("Problem connecting to server, are you sure the IP address is accurate?");
            return false;
        }
        PacketInfo response = PacketInfo.getNewData(receiver);
        if (response == null) {
            error("No server found");
            return false;
        }
        if (response.function == 'y') {
            name = user;
            serverIP = response.ip;
            int existingChats = Integer.parseInt(response.info);
            for (int i = 0; i < existingChats; i++) {
                try {
                    new PacketInfo(receiver, ChatConsts.serverPort, serverIP, 'k', " ").sendPacket();
                    PacketInfo newResponse = PacketInfo.getNewData(receiver);
                    String[] data = enhancedSplit(newResponse.info, 2);
                    String chatID = data[0];
                    String chatUser = data[1];
                    String chatText = data[2];
                    Chat existingChat = new Chat(chatID);
                    if (! chatUser.equals("null")) {
                        Message m = new Message(chatID, chatText, chatUser);
                        existingChat.addMessageClient(m);
                    }
                    chats.add(existingChat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        } else {
            return false;
        }
    }


    // Grid Management
    private static int r = -1;

    private static int c = 0;
    private void resetRowCount() {
        r = -1;
    }

    private static int row(boolean i) {
        r += i ? 1 : 0;
        return r;
    }

    private static int col(boolean r) {
        c = r ? 0 : (c + 1);
        return c;
    }

    // Alerts

    public static void error(String cmd) {
        //System.out.printf("error processing: %s%n", cmd);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Command Error");
        alert.setHeaderText("An error occurred with your requested command.");
        alert.setContentText(cmd);

        alert.showAndWait();
    }

    public static void popup (String stats) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Stats");
        alert.setContentText(stats);
        alert.showAndWait();
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
}
