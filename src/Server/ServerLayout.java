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
    private JTextField textField1;
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

    ServerLayout(Server server) {

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                response.setText("");
                System.out.println(chooseAction.getSelectedItem());
                switch ((String) chooseAction.getSelectedItem()) {

                    case "Raum umbenennen":
                        response.setText(roomList.getSelectedValue().toString() + "wurde in " + textField1.getText() + " umbenannt.");//TODO zum testen die einzelen sachen in einzele zeilen schreiebn
                        server.editRoom((Raum) roomList.getSelectedValue(), textField1.getText());
                        updateLists(server.getNutzerListeHashMap(), server.getRaumListeHashMap());
                        break;
                    case "Raum löschen":
                        response.setText("Raum " + roomList.getSelectedValue().toString() + " wurde gelöscht.");
                        server.deleteRoom((Raum) roomList.getSelectedValue());
                        updateLists(server.getNutzerListeHashMap(), server.getRaumListeHashMap());
                        break;
                    case "Raum erstellen":
                        server.newRoom(textField1.getText());
                        response.setText("Raum " + textField1.getText() + "wurde erstellt.");
                        updateLists(server.getNutzerListeHashMap(), server.getRaumListeHashMap());
                        break;
                    case "Benutzer verwarnen":
                        server.warnUser((ClientThread) userList.getSelectedValue());
                        response.setText("Benutzer " + userList.getSelectedValue() + " wurde verwarnt.");
                        break;
                    case "Benutzer kicken":
                        response.setText("Benutzer " + userList.getSelectedValue() + " wurde gekickt.");
                        server.kickUser((ClientThread) userList.getSelectedValue());
                        updateLists(server.getNutzerListeHashMap(), server.getRaumListeHashMap());
                        break;
                    case "Benutzer ausschließen":
                        response.setText("Benutzer " + userList.getSelectedValue() + " wurde gebannt und ist ab sofort von dem Server ausgeschlossen.");
                        updateLists(server.getNutzerListeHashMap(), server.getRaumListeHashMap());
//                        TODO s.bannUser((ClientThread) userList.getSelectedValue());
                        break;
                    case "Server umbennen":
                        server.editServername(textField1.getText());
                        response.setText("Der Server " + server.serverName + " wurde in " + textField1.getText() + "umbenannt.");
                        serverlogInfo.setText("Server '" + textField1.getText() + "' ist online.");
                        break;
                    case "Passwortdatei lesen":
//                        TODO s.readPasswordDatei();
                        response.setText("Die Passwortdatei wurde ausgelesen.");
                        break;
                }
            }
        });
    }

    public void start_gui() {
        JFrame frame = new JFrame("ServerLayout");
        frame.setContentPane(this.ROOT);
/*
        bar = new JMenuBar();
        Server = new JMenu("Server");
        Benutzer = new JMenu("Benutzer");
        Raum = new JMenu("Server.Raum");
        Operationen = new JMenu("Operationen");

        fileNew = new JMenuItem("Server umbenennen");

        //frame.add(bar, new BorderLayout().PAGE_START);
        bar.add(Server);
        bar.add(Benutzer);
        bar.add(Raum);
        bar.add(Operationen);

        Server.Server.add(fileNew);
*/

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

    public void setName(String s) {
        //name.setText(s);      //TODO das label scheint nicht zu existieren
    }

    public void setServerlogInfo(String serverName) {
        serverlogInfo.setText("Server '" + serverName + "' ist online.");
    }
}