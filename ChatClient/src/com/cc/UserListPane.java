package com.cc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class UserListPane extends JPanel implements UserStatusListener {


    private final Client client;
    private  JList<String> userListUI;
    private DefaultListModel<String> userListModel;

    public UserListPane(Client client) {
        this.client = client;
        this.client.addUserStatusListener(this);

        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);

        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    String username = userListUI.getSelectedValue();
                    MessagePane messagePane = new MessagePane(client, username);

                    JFrame messageFrame = new JFrame("Message: " + username);
                    messageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    messageFrame.setSize(500, 500);
                    messageFrame.getContentPane().add(messagePane, BorderLayout.CENTER);
                    messageFrame.setVisible(true);
                }
            }
        });
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 8818);


        UserListPane userListPane = new UserListPane(client);
        JFrame frame = new JFrame("User List");
        frame.setDefaultCloseOperation((JFrame.EXIT_ON_CLOSE));
        frame.setSize(500, 750);

        frame.getContentPane().add(new JScrollPane(userListPane), BorderLayout.CENTER);
        frame.setVisible(true);

        if (client.connect()) {
            client.login("admin", "admin");
        }
    }

    @Override
    public void online(String username) {
        userListModel.addElement(username);
    }

    @Override
    public void offline(String username) {
        userListModel.removeElement(username);
    }
}
