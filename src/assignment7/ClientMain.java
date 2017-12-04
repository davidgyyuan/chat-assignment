package assignment7;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientMain extends Application{

    static String name;
    static InetAddress serverIP;
    static DatagramSocket receiver;
    static ArrayList<Chat> chats = new ArrayList<>();

    public static void main(String... args) {
        int i = 0;
        while (true) {
            try {
                receiver = new DatagramSocket(ChatConsts.clientPort + i);
                break;
            } catch (SocketException e) {
                i++;
            }
        }
        launch(args);
    }

    public void start(Stage primaryStage) {
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
            }
        });
        Label chatIDLabel = new Label();
        ListView<String> chatHistory = new ListView<>();
        TextField chatInputField = new TextField();
        Button sendButton = new Button("Send Message");
        sendButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    new PacketInfo(receiver, ChatConsts.serverPort, serverIP, 'm',
                            chatIDLabel.getText() + ":" + chatInputField.getText())
                            .sendPacket(true, receiver.getLocalPort());
                    chatInputField.setText("");
                } catch (IOException e) {
                    error("Could not send message");
                }
            }
        });

        chatGrid.add(backButton, col(true), row(true));
        chatGrid.add(chatHistory, col(false), row(true));
        chatGrid.add(chatInputField, 1, row(true));
        chatGrid.add(sendButton, 1, row(true));

        resetRowCount();

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
                }
            }
        });

        loginGrid.add(nameLabel, col(true), row(true));
        loginGrid.add(nameField, col(false), row(false));
        loginGrid.add(ipLabel, col(true), row(true));
        loginGrid.add(ipField, col(false), row(false));
        loginGrid.add(login, col(true), row(true));

        resetRowCount();

        ListView<String> chatList = new ListView<>();
        ObservableList<String> chatsInfo = FXCollections.observableArrayList(toSummaryChatInfo());
        chatList.setItems(chatsInfo);
        Button newChatButton = new Button("New Chat");
        newChatButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Chat c = newChat();
                if (c != null) {
                    chats.add(c);
                    ObservableList<String> chatsInfo = FXCollections.observableArrayList(toSummaryChatInfo());
                    chatList.setItems(chatsInfo);
                }
            }
        });
        Button openChatButton = new Button("Open Chat");
        openChatButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int selected = chatList.getSelectionModel().getSelectedIndex();
                if (selected > -1) {
                    ObservableList<String> chatsObservableList =
                            FXCollections.observableArrayList(toSummaryChatHistory(selected));
                    chatHistory.setItems(chatsObservableList);
                    chatIDLabel.setText(chats.get(selected).getChatID());
                    primaryStage.setScene(chatScene);
                }
            }
        });

        mainGrid.add(newChatButton, col(true), row(true));
        mainGrid.add(chatList, col(true), row(true));
        mainGrid.add(openChatButton, col(true), row(true));

        primaryStage.setScene(loginScene);
        primaryStage.sizeToScene();
        primaryStage.show();
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

    private Chat newChat() {
        try {
            new PacketInfo(receiver, ChatConsts.serverPort, serverIP, 'c', name).sendPacket(true, receiver.getLocalPort());
        } catch (IOException e) {
            return null;
        }

        PacketInfo response = PacketInfo.getNewData(receiver);
        return new Chat(response.info, name);
    }

    private boolean sendUserRegister(String user, String ipFieldText) {
        try {
            new PacketInfo(receiver, ChatConsts.serverPort, InetAddress.getByName(ipFieldText), 'u', user).sendPacket(true, receiver.getLocalPort());
        } catch (IOException e) {
            e.printStackTrace();
            error("Problem connecting to server, are you sure the IP address is accurate?");
            return false;
        }
        // TODO Concurrency, blocking method
        PacketInfo response = PacketInfo.getNewData(receiver);
        if (response.function == 'y') {
            name = user;
            serverIP = response.ip;
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
}
