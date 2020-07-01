package src;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    ArrayList<String> strStoreInfo = new ArrayList<>();
    private ServerSocket serversocket;
    private ExecutorService exec;

    // The set of all the print writers for all the clients, used for broadcast.
    private Map<String, PrintWriter> storeInfo;

    public Server() {
        try {
            serversocket = new ServerSocket(6666);
            exec = Executors.newCachedThreadPool();
            storeInfo = new HashMap<String, PrintWriter>();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    private void putIn(String key, PrintWriter value) {
        synchronized (this) {
            storeInfo.put(key, value);
        }
    }

    private synchronized void remove(String key) {
        storeInfo.remove(key);
        strStoreInfo.remove(key);
        System.out.println("Online users: " + storeInfo.size());
    } // remove()

    private synchronized void broadcast(String message) {
        for (PrintWriter out : storeInfo.values()) {
            out.println(message);
        }
    }

    public void start() {
        try {
            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = serversocket.accept();
                InetAddress address = socket.getInetAddress();
                System.out.println("Client: " + address.getHostAddress() + " has connected!");
                exec.execute(new Handler(socket));
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The client handler task.
     */
    class Handler implements Runnable {
        private final Socket socket;
        private String name;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String getName() throws Exception {
            // Read client user name from buffer
            BufferedReader bReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            // Send output to client
            PrintWriter ipw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            while (true) {
                String name = bReader.readLine();
                if ((name.trim().length() == 0) || storeInfo.containsKey(name)) {
                    ipw.println("FAIL");
                } else {
                    ipw.println("OK");
                    return name;
                } 
            } 
        }

        @Override
        public void run() {
            try {
                // Check if user name is valid
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                name = getName();
                putIn(name, pw);
                strStoreInfo.add(name);
                System.out.println("[System Broadcast] " + '"' + name + "” has joined.");
                broadcast("[System Broadcast] " + '"' + name + "” has joined.");
                BufferedReader bReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); // 接收聊天訊息
                String msg = null;
                System.out.println("Online users: " + storeInfo.size());
                StringBuilder strNameList = new StringBuilder("/strName");
                for (String s : strStoreInfo) strNameList.append(s).append("#");
                TimeUnit.SECONDS.sleep(2);
                broadcast(strNameList.toString());

                while ((msg = bReader.readLine()) != null) {
                    if (msg.startsWith("/msg")) {
                        /*
                         * Private chat mode
                         * Usage:
                         *      /msg [User Name]: message
                         */
                        int index = msg.indexOf(": ");
                        if (index != 0) {
                            String target = msg.substring(5, index);
                            String context = msg.substring(index + 1);
                            String info = name + ": " + context;
                            PrintWriter ppw = storeInfo.get(target);
                            PrintWriter ipw = storeInfo.get(name);
                            if (storeInfo.get(target) == null) {
                                System.out.println("ERR: User does not exist.");
                                ppw = storeInfo.get(name);
                                ppw.println("[System Broadcast] This user is not online yet.");
                            } 
                            else {
                                if (ppw != null)
                                    ppw.println("[DM] " + info);
                                ipw.println("[DM] " + info);
                                System.out.println(info + " [DM to] " + target);
                            } 
                        } 
                    } 
                    else {
                        System.out.println("[GROUP] " + name + ": " + msg);
                        broadcast("[GROUP] " + name + ": " + msg);
                    } 
                } 
            } catch (Exception ignored) {

            } finally {
                remove(name);
                // Offline broadcast
                StringBuilder strNameList = new StringBuilder("/strName");
                for (String s : strStoreInfo) strNameList.append(s).append(" ");
                broadcast(strNameList.toString());
                System.out.println("[System Broadcast] " + '"' + name + '"' + " has left.");
                broadcast("[System Broadcast] " + '"' + name + '"' + " has left.");
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}