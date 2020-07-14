package com.cs;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread {
    private final int port;
    private ArrayList<ServerWorker> workers = new ArrayList<>();

    public Server(int port) {
        this.port = port;
    }

    public List<ServerWorker> getWorkers() {
        return workers;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("About to accept client connection...");
            while(true){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from" + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workers.add(worker);
                worker.start();
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker serverWorker) {
        workers.remove(serverWorker);
    }
}
