package assignment7;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClientMain extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");
        GridPane grid = new GridPane();

        Label nameLabel = new Label("Username:");
        TextField nameField = new TextField();
        Label ipLabel = new Label("Server IP:");
        TextField ipField = new TextField();
        Button login = new Button("Login");

        grid.add(nameLabel, col(true), row(true));
        grid.add(nameField, col(false), row(false));
        grid.add(ipLabel, col(true), row(true));
        grid.add(ipField, col(false), row(false));
        grid.add(login, col(true), row(true));

        Scene primaryScene = new Scene(grid);
        primaryStage.setScene(primaryScene);
        primaryStage.sizeToScene();

        primaryStage.show();
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
}
