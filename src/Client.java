package src;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Client extends Application {
    Stage stage;
    Pane root;
    Scene scene;
    Scene login;
    Scene chat;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        root = new Pane();
        root.setStyle("-fx-background-color:#FF00FF");

        double width = 370, height = 580;
        scene = new Scene(root, width, height);

        this.login = new src.Login(FXMLLoader.load(getClass().getResource("Login.fxml")), this);
        this.chat = new src.Chat(FXMLLoader.load(getClass().getResource("Chat.fxml")), this);

        primaryStage.setScene(login);
        primaryStage.setTitle("MessengerFX");
        primaryStage.show();
    }

    public Scene getLogin() {
        return login;
    }

    public Scene getChat() {
        return chat;
    }
}