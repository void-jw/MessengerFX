package src;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class Login extends javafx.scene.Scene {
    public static Socket clientSocket;
    public static ArrayList<String> nameList = new ArrayList<>();
    public static String name;

    Client m;
    Button Connect;
    Button RandName;
    TextField IpInput;
    TextField NameInput;
    TextField PortInput;
    TextArea Status;

    public Login(javafx.scene.Parent root, Client main) {
        super(root);
        this.m = main;
        this.m.root = ((Pane) lookup("#pane"));
        this.IpInput = ((TextField) lookup("#IpInput"));
        this.NameInput = ((TextField) lookup("#NameInput"));
        this.PortInput = ((TextField) lookup("#PortInput"));
        this.Status = ((TextArea) lookup("#Status"));

        Random ran = new Random();
        String[] randomNames = {
                "Alexa",
                "Siri",
                "Jarvis",
        };

        this.RandName = ((Button) lookup("#RandName"));
        this.RandName.setOnMouseClicked(new EventHandler<Event>() {
            public void handle(Event event) {
                int R = ran.nextInt(18);
                NameInput.setText(randomNames[R]);
            } // handle()
        }); // RandName.setOnMouseClicked()

        this.m.root = ((Pane) lookup("#pane"));
        this.m.root.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                login();
            }
            else if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                exit();
            }
        });
    }

    public boolean start() throws Exception {
        clientSocket = new Socket("127.0.0.1", 6666);
        return setName(NameInput);
    }

    public void login() {
        if (NameInput.getText().equals(""))
            Status.setText("User name cannot be empty!");
        else {
            // Check duplicates
            if (IpInput.getText().equals("127.0.0.1") && PortInput.getText().equals("6666")) {
                name = NameInput.getText();
                nameList.add(name);
                try {
                    if (start()) {
                        Scene chat = new src.Chat(FXMLLoader.load(getClass().getResource("Chat.fxml")), this.m);
                        Login.this.m.stage.setScene(chat);
                    }
                    else
                        Status.setText("This name has been occupied, please choose another name: ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else
                Status.clear();
        }
    }

    public void exit() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public boolean setName(TextField NameInput) throws Exception {
        String name = NameInput.getText();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
        BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));

        while (true) {
            System.out.println("Please enter your name: ");
            if (name.trim().equals(""))
                System.out.println("User name cannot be empty!");
            else {
                pw.println(name);
                String pass = br.readLine();
                if (pass != null && (pass.equals("FAIL"))) {
                    System.out.println("This name has been occupied, please choose another name: ");
                    return false;
                }
                else {
                    System.out.println("User " + name + " is now accepted!");
                    return true;
                }
            }
        }
    }
}