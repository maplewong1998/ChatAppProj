package com.cc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MessagePane extends JPanel implements MsgListener{

    private final Client client;
    private final String username;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();

    public MessagePane(Client client, String username) {
        this.client = client;
        this.username = username;

        client.addMsgListener(this);

        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String inputMsg = inputField.getText();
                    client.sendMsg(username, inputMsg);
                    listModel.addElement("You: " + inputMsg);
                    inputField.setText("");

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onMsg(String fromUser, String msgBody) {
        String reply = fromUser + ": " + msgBody;
        listModel.addElement(reply);
    }
}
