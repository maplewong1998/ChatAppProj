package com.cc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class UserListPane extends JPanel implements UserStatusListener {


    private final Client client;
    private  JList<String> userListUI;
    private DefaultListModel<String> userListModel;
    JButton logoutButton = new JButton("Logout");

    public UserListPane(Client client) {
        this.client = client;
        this.client.addUserStatusListener(this);

        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);
        add(logoutButton, BorderLayout.SOUTH);

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

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogout();
            }
        });
    }

    private void doLogout() {
        try {
            SwingUtilities.getWindowAncestor(this).dispose();
            client.logout();
            LoginWindow loginWindow = new LoginWindow();
            loginWindow.setVisible(true);
        } catch (IOException ex) {
            ex.printStackTrace();
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
