package assignment7;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ClientMain extends Application{

    static String name;
    static InetAddress serverIP;
    static DatagramSocket receiver;

    public static void main(String[] args) throws SocketException {
        receiver = new DatagramSocket(ChatConsts.clientPort);
        launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");
        GridPane loginGrid = new GridPane();
        Scene loginScene = new Scene(loginGrid);
        GridPane mainGrid = new GridPane();
        Scene mainScene = new Scene(mainGrid);

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
                }
            }
        });

        loginGrid.add(nameLabel, col(true), row(true));
        loginGrid.add(nameField, col(false), row(false));
        loginGrid.add(ipLabel, col(true), row(true));
        loginGrid.add(ipField, col(false), row(false));
        loginGrid.add(login, col(true), row(true));

        primaryStage.setScene(loginScene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private boolean sendUserRegister(String user, String ipFieldText) {
        try {
            new PacketInfo(InetAddress.getByName(ipFieldText), 'u', user).sendPacket(true);
        } catch (IOException e) {
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
