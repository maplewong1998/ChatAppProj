package com.cc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MsgListener> msgListeners = new ArrayList<>();

    public Client(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String username) {
                System.out.println("ONLINE: " + username);
            }

            @Override
            public void offline(String username) {
                System.out.println("OFFLINE: " + username);
            }
        });

        client.addMsgListener(new MsgListener() {
            @Override
            public void onMsg(String fromUser, String msgBody) {
                System.out.println("You got a message from " + fromUser + " =====> " + msgBody);
            }
        });

        if (!client.connect()) {
            System.err.println("Connection failed");
        } else {
            System.out.println("Connection success");
            if (client.login("admin", "admin")) {
                System.out.println("Login successful");
                client.sendMsg("","");
            } else {
                System.err.println("Login failed");
            }
        }
    }

    public void sendMsg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    public boolean login(String username, String password) throws IOException {
        String cmd = "login " + username + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Server response: " + response);

        if ("Login successful".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }

    public void logout() throws IOException {
        String cmd = "logout\n";
        serverOut.write(cmd.getBytes());
    }

    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        OnlineHandler(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        OfflineHandler(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] msgTokens = StringUtils.split(line, null, 3);
                        MsgHandler(msgTokens);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void MsgHandler(String[] msgTokens) {
        String username = msgTokens[1];
        String msgBody = msgTokens[2];

        for(MsgListener listener : msgListeners) {
            listener.onMsg(username, msgBody);
        }
    }

    private void OfflineHandler(String[] tokens) {
        String username = tokens[1];
        for(UserStatusListener listener : userStatusListeners) {
            listener.offline(username);
        }
    }

    private void OnlineHandler(String[] tokens) {
        String username = tokens[1];
        for(UserStatusListener listener : userStatusListeners) {
            listener.online(username);
        }
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMsgListener(MsgListener listener) {
        msgListeners.add(listener);
    }

    public void removeMsgListener(MsgListener listener) {
        msgListeners.remove(listener);
    }
}
