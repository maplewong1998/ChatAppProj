package com.cs;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread {
    private final Socket clientSocket;
    private final Server server;
    private String username = null;
    private OutputStream outputStream;
    private HashSet<String> chatRooms = new HashSet<>();

    public ServerWorker (Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            ClientSocketHandler();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private void ClientSocketHandler() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("quit".equalsIgnoreCase(cmd) || "logout".equalsIgnoreCase(cmd)) {
                    LogoutHandler();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    LoginHandler(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] msgTokens = StringUtils.split(line, null, 3);
                    MessageHandler(msgTokens);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    JoinHandler(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    LeaveHandler(tokens);
                }else {
                    String msg = "unknown command " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
    }

    public boolean isChatRoom(String room) {
        return room.charAt(0) == '#';
    }

    public boolean isMemberOfChatRoom(String room) {
        return chatRooms.contains(room);
    }

    private void LeaveHandler(String[] tokens) {
        if (tokens.length > 1) {
            String room = tokens[1];
            if(isChatRoom(room)) {
                chatRooms.remove(room);
            }
        }
    }

    private void JoinHandler(String[] tokens) {
        if (tokens.length > 1) {
            String room = tokens[1];
            if(isChatRoom(room)) {
                chatRooms.add(room);
            }
        }
    }

    // direct chat: "msg" "username" msgBody
    // room chat: "msg" "#chatRoom" msgBody
    private void MessageHandler(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String msgBody = tokens[2];

        List<ServerWorker> workers = server.getWorkers();
        for(ServerWorker worker : workers) {
            if (isChatRoom(sendTo)) {
                if (worker.isMemberOfChatRoom(sendTo)) {
                    String outMsg = "msg " + sendTo + " " + username + " " + msgBody + "\n";
                    worker.send(outMsg);
                }
            } else {
                if (sendTo.equalsIgnoreCase(worker.getUsername())) {
                    String outMsg = "msg " + username + " " + msgBody + "\n";
                    worker.send(outMsg);
                }
            }
        }
    }

    public String getUsername() {
        return username;
    }

    private void LoginHandler(OutputStream outputStream, String[] tokens) throws IOException{
        if (tokens.length == 3) {
            String username = tokens[1];
            String password = tokens[2];

            if ((username.equals("admin") && password.equals("admin")) || (username.equals("guest") && password.equals("guest"))) {
                String msg = "Login successful\n";
                outputStream.write(msg.getBytes());
                this.username = username;
                System.out.println(username + " logged in successfully\n");

                //send current user all other online logins
                List<ServerWorker> workers = server.getWorkers();
                for(ServerWorker worker : workers) {
                    if (worker.getUsername() != null) {
                        if (!username.equals(worker.getUsername())) {
                            String BroadcastMsg2 = "online " + worker.getUsername() + "\n";
                            send(BroadcastMsg2);
                        }
                    }
                }
                //notify all other online users new login
                String BroadcastMsg = "online " + getUsername() + "\n";
                for(ServerWorker worker : workers) {
                    if (!username.equals(worker.getUsername())) {
                        worker.send(BroadcastMsg);
                    }
                }
            } else {
                String msg = "Login failed\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for " + username);
            }
        }
    }

    private void LogoutHandler() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workers = server.getWorkers();
        String BroadcastMsg = "offline " + getUsername() + "\n";
        for(ServerWorker worker : workers) {
            worker.send(BroadcastMsg);
        }
        System.out.println("Connection ended from " + clientSocket);
        clientSocket.close();
    }

    private void send(String msg) throws IOException {
        if (username != null) {
            outputStream.write(msg.getBytes());
        }
    }
}
