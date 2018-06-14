package Client;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class Dialog {

    JFrame dialog;
    //    Client client;
    private JPanel dialogpanel;
    private JButton button1;
    private JTextField textField;
    private JTextArea textArea;

    private String partner;

    String userName;
    empfangenThread et;


    Dialog(empfangenThread et) {

        this.et = et;

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                eingabe();

            }
        });
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                eingabe();

            }
        });

    }

    private void eingabe() {
        String message = textField.getText();
        appendMessage("ich: \t" + message);
        // TODO sendet nicht oder erstellt nicht
        JSONObject request = new JSONObject();
        request
                .put("type", "privateChat")
                .put("privateChat", partner)
                .put("online", true)
//                .put("sender", userName)
                .put("message", message);

//        textArea.setText(request.optString("message", "feehler"));
//        System.out.println("client: " + et.client.getUser());
//        System.out.println("userName: " + userName);
        System.out.println("PartnerDialog: " + partner);
        et.client.senden(request.toString());
//        textField.setText("Bla");
        textField.setText("");
    }

    protected void appendMessage (String message) {
        if (!message.matches((".*Die Unterhaltung wurde geschlossen."))) {
            dialog.setVisible(true);
        }
        textArea.append(message + "\n");
        System.out.println(message + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    void Dialog_start() {
        dialog = new JFrame();
        dialog.setContentPane(dialogpanel);
        Random rn = new Random();
        int i = Math.abs(rn.nextInt());
        int j = Math.abs(rn.nextInt());
        dialog.setLocation(i%800, j%1700);
//        dialog.setLocation(new Point(500, 500));
//        dialog.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {

                JSONObject request = new JSONObject();
                request
                        .put("type", "privateChat")
                        .put("privateChat", partner)
                        .put("online", true)
                        .put("message", "Die Unterhaltung wurde geschlossen.");

                System.out.println("request WindowClosing: " + request);
                System.out.println("PartnerDialog: " + partner);
                et.client.senden(request.toString());
                System.out.println("request WindowClosing: " + request);

//                dialog.dispose();
                dialog.setVisible(false);
                et.getPrivateChatList().remove(this);
            }
        });

        dialog.pack();
        dialog.setVisible(true);
        dialog.setTitle(partner);
        et.addDialog(this);
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }
}
