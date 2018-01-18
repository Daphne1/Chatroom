package Server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class ServerLayout {
    private JLabel serverlogInfo;
    private JTabbedPane tabbedPane1;
    private JList roomList;
    private JList userList;
    private JComboBox chooseAction;
    private JButton button1;
    private JTextArea LogDisplay;
    private JPanel ROOT;
    private JLabel name;
    private JLabel response;
    private JTextField actionInformation;
    private JMenuBar bar;
    private JMenu Server;
    private JMenu Benutzer;
    private JMenu Raum;
    private JMenu Operationen;

    DefaultListModel user = new DefaultListModel();
    DefaultListModel rooms = new DefaultListModel();

    private static ServerLayout INSTANCE;

    ServerLayout(Server2 server2) {

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                response.setText("");
                System.out.println(chooseAction.getSelectedItem());
                Raum r = (Raum) roomList.getSelectedValue();
                switch ((String) chooseAction.getSelectedItem()) {

                    case "Raum umbenennen":
                        if (r != null) {
                            String newName = actionInformation.getText();
                            if (!newName.equals("Lobby") && !chooseAction.getSelectedItem().equals(server2.getRaumListeHashMap().get("Lobby"))) {
                                server2.editRoom(r, newName);
                                response.setText(r.getName() + "wurde in '" + newName + "' umbenannt.");
                                server2.log("\""+r.getName()+"\" wurde zu \""+ newName + "\" umbenannt");
                            }
                        }
                        break;
                    case "Raum löschen":
                        if (r != null) {
                            if (!r.getName().equals("Lobby")) {
                                server2.deleteRoom(r);
                                response.setText("Raum " + r.getName() + " wurde gelöscht.");
                                server2.log("Raum: \""+ r.getName() + "\" wurde gelöscht");
                            }
                        }
                        break;
                    case "Raum erstellen":
                        String name = actionInformation.getText();
                        if (!name.equals("Lobby")) {
                            server2.newRoom(name);
                            response.setText("Raum '" + actionInformation.getText() + "' wurde erstellt.");
                            server2.log("Raum: \""+ name + "\" wurde erstellt ");
                        }
                        break;
                    case "Benutzer verwarnen":
                        if (userList.getSelectedValue() != null) {
                            server2.warnUser((ClientThread) userList.getSelectedValue());
                            response.setText("Benutzer " + userList.getSelectedValue() + " wurde verwarnt.");
                        }
                        break;
                    case "Benutzer kicken":
                        if (userList.getSelectedValue() != null) {
                            response.setText("Benutzer " + userList.getSelectedValue() + " wurde gekickt.");
                            server2.kickUser(((ClientThread) userList.getSelectedValue()).getUserName());
                        }
                        break;
                    case "Benutzer ausschließen":
                        if (userList.getSelectedValue() != null) {
                            response.setText("Benutzer " + userList.getSelectedValue() + " wurde gebannt und ist ab sofort von dem Server2 ausgeschlossen.");
                            server2.banUser(((ClientThread) userList.getSelectedValue()).getUserName());
                        }
                        break;
                    case "Server umbennen":
                        String newServerName = actionInformation.getText();
                        server2.editServername(newServerName);
                        response.setText("Der Server '" + server2.serverName + "' wurde in " + newServerName + "umbenannt.");
                        break;
                    case "Passwortdatei lesen":
                        server2.loadUserData();
                        response.setText("Die Passwortdatei wurde ausgelesen.");
                        break;
                }
            }
        });
    }

    public void start_gui() {

        JFrame frame = new JFrame("ServerLayout");
        frame.setContentPane(this.ROOT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }


    protected void appendLog (String message) {

        LogDisplay.append(message + "\n");
        LogDisplay.setCaretPosition(LogDisplay.getDocument().getLength());

    }

    protected void updateLists (HashMap<String, ClientThread> userlist, HashMap<String, Raum> roomlist) {

        user.clear();
        rooms.clear();

        for(String key : userlist.keySet()) {
            user.addElement(userlist.get(key));
        }
        for(String key : roomlist.keySet()) {
            rooms.addElement(roomlist.get(key));
        }

        userList.setModel(user);
        roomList.setModel(rooms);

    }

    public void setServerlogInfo(String serverName) {
        serverlogInfo.setText("Server2 '" + serverName + "' ist online.");
    }

}