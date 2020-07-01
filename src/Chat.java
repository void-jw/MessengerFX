package src;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Chat extends javafx.scene.Scene {
//    Login login;
    Client m;
    TextField input;
    TextArea userList;
    TextArea chatter;
    TextArea chatterName;
    ExecutorService exec;
    Pane logoutPane;
    Text name;

    public Chat(javafx.scene.Parent root, Client main) {
        super(root);
        this.m = main;
        this.m.root = ((Pane) lookup("#pane"));
        this.logoutPane = ((Pane) lookup("#logout_pane"));
        this.input = ((TextField) lookup("#Input"));
        this.userList = ((TextArea) lookup("#Userlist"));
        this.chatter = ((TextArea) lookup("#Chatter"));
        this.chatterName = ((TextArea) lookup("#ChatterName"));
        this.name = ((Text) lookup("#name_txt"));
        this.exec = null;

        this.input.requestFocus();
        this.name.setText(Login.name);

        // Send Message
        this.input.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                send();
            }
        });

        // Exit MessengerFX
        this.logoutPane.setOnMouseClicked(mouseEvent -> {
            try {
                logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.m.root.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
                try {
                    logout();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void send() {
        if (!input.getText().isEmpty()) {
            String str = input.getText();
            try {
                // Set output stream, which sends messages to server
                PrintWriter out = new PrintWriter(new OutputStreamWriter(Login.clientSocket.getOutputStream(), StandardCharsets.UTF_8), true);
                out.println(str);
                if (exec == null) {
                    // Active threads sent by server
                    exec = Executors.newCachedThreadPool();
                    exec.execute(new ListenerServer());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            input.clear();
        }
    }

    public void logout() throws IOException {
        try {
            Login.clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.m.stage.setScene(this.m.login);
        this.chatter = new TextArea();
//        System.exit(0);
    }

    // Read and send messages from server to client
    class ListenerServer implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(Login.clientSocket.getInputStream(), StandardCharsets.UTF_8));
                String msg;
                while ((msg = br.readLine()) != null) {
                    if (msg.startsWith("/strName")) {
                        String str1 = msg.replace("/strName", "");
                        String str2 = str1.replace("#", "\n");
                        userList.setText(str2);
                    }
                    else {
                        System.out.println(msg);
                        chatter.appendText(msg + "\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}