package Client;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


//SINGLETON
public class Client {

    private JPanel mainPanel;
    JTextField inputField;
    private JLabel RoomLabel;
    private JButton sendButton;
    private JTabbedPane privateChats;
    private JList<String> userlist;
    private JList<String> roomlist;
    private JTextArea clientLog;
    private JList privatelist;
    private JPanel Benutzer;
    private JPanel Raeume;

    private JPopupMenu popupMenuRoom = new JPopupMenu();
    private JPopupMenu popupMenuUser = new JPopupMenu();
    private JMenuItem switchRoom;
    private JMenuItem openDialog;

    private Socket server;
    private PrintWriter printWriterOutputStream;
    BufferedReader bufferedReaderInputStream;

    private boolean loginConfirmed = false;

    private DefaultListModel listUser = new DefaultListModel();
    private DefaultListModel listRooms = new DefaultListModel();
    private DefaultListModel listPrivateChats = new DefaultListModel();

/*

    private LinkedList<Gespräch> dialogs = new LinkedList<>();
*/


    //ROOMNAME, STRING
    private HashMap<String, String> RoomTexts;
    private String user;
    private String pw;
    //    private boolean started;
    private boolean enteredUser = false;
    private boolean enteredPassword = false;



    protected Client() {

        try {
            server = new Socket("localhost", 3456);
            appendMessage("erfolgreich zu 'localhost' Port: 3456 verbunden");

            // in
            InputStream inputStream = server.getInputStream();
            bufferedReaderInputStream = new BufferedReader(new InputStreamReader(inputStream));

            // out
            OutputStream outputStream = server.getOutputStream();
            printWriterOutputStream = new PrintWriter(outputStream, true);

        } catch(UnknownHostException e) {
            appendMessage("Can't find host.");
        } catch (IOException e) {
            appendMessage("Error connecting to host.");
        }

        sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {

                eingabe();

            }
		});
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                eingabe();
            }
        });
        roomlist.addMouseListener(new MouseAdapter() {
                                       public void mouseClicked(MouseEvent me) {                                             //kontextmenue
                                           // if right mouse button clicked (or me.isPopupTrigger())
                                           if (SwingUtilities.isRightMouseButton(me)
                                                   && !roomlist.isSelectionEmpty()
                                                   && roomlist.locationToIndex(me.getPoint())
                                                   == roomlist.getSelectedIndex()) {
                                               popupMenuRoom.show(roomlist, me.getX(), me.getY());

                                           }
                                       }
                                   }
        );
        popupMenuRoom.add(switchRoom = new JMenuItem("Raum wechseln"));
        switchRoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                switchRoom();
            }
        });

        /////////////////////////////////

        privatelist.addMouseListener(new MouseAdapter() {
                                      public void mouseClicked(MouseEvent me) {                                             //kontextmenue
                                          // if right mouse button clicked (or me.isPopupTrigger())
                                          if (SwingUtilities.isRightMouseButton(me)
                                                  && !privatelist.isSelectionEmpty()
                                                  && privatelist.locationToIndex(me.getPoint())
                                                  == privatelist.getSelectedIndex()) {
                                              popupMenuUser.show(privatelist, me.getX(), me.getY());

                                          }
                                      }
                                  }
        );
        popupMenuUser.add(openDialog = new JMenuItem("privaten Dialog öffnen"));
        openDialog.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // TODO aus EmpfangenTread Dialog starten
                openDialog();
            }
        });
	}

    private void eingabe() {
        String message = inputField.getText();
        appendMessage(message);
        JSONObject request = new JSONObject();
        request
                .put("type", "message")
                .put("message", message);

        senden(request.toString());
        inputField.setText("");
    }

    private void GUI_start() {
        JFrame clientFrame = new JFrame("Client Fenster");
        clientFrame.setLocation(new Point(150, 150));
        clientFrame.setContentPane(mainPanel);
        clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clientFrame.pack();
        clientFrame.setVisible(true);
    }

    protected void confirmLogin() {
	    loginConfirmed = true;
    }

    protected void resetLogin(String error) {
	    loginConfirmed = false;
	    enteredPassword = false;
	    enteredUser = false;

	    appendMessage(error);
    }

    void updateUser(JSONArray array) {

        if (array != null) {

			listUser.clear();

        	for (int i = 0; i < array.length(); i++) {
                listUser.addElement(array.optString(i,""));
            }

            userlist.setModel(listUser);
        }

    }

    void updateRooms(JSONArray array) {

        if (array != null) {

			listRooms.clear();

            for (int i = 0; i < array.length(); i++) {
                listRooms.addElement(array.optString(i,""));
            }
        }

        roomlist.setModel(listRooms);

    }

    void updateAllUser(JSONArray array) {

        if (array != null) {

            listPrivateChats.clear();

            for (int i = 0; i < array.length(); i++) {
                listPrivateChats.addElement(array.optString(i, ""));
            }
        }

        privatelist.setModel(listPrivateChats);

    }

    protected void senden(String message) {

		printWriterOutputStream.println(message);
		printWriterOutputStream.flush();

	}


    public static void main(String args[]) {
		Client C = new Client();
		C.startClient();
	}

	private boolean startClient() {

        GUI_start();

        empfangenThread empfaengt = new empfangenThread(bufferedReaderInputStream, server, this);
        empfaengt.start();


        return true;

	}

	protected void appendMessage (String message) {
		clientLog.append("\n");
		clientLog.append(message);
		System.out.println(message + "\n");
		clientLog.setCaretPosition(clientLog.getDocument().getLength());
	}


    private void switchRoom(){
        JSONObject request = new JSONObject();
        String message = roomlist.getSelectedValue();
        request
                .put("type", "switchRoom")
                .put("message", message);

        senden(request.toString());
    }

    private void openDialog(){
        JSONObject request = new JSONObject();
        String partner = (String) privatelist.getSelectedValue(); // warum ist der Cast nötig?
        request
                .put("type", "privateChat")
//                .put("sender", user)
                .put("online", true)
                .put("privateChat", partner);

        senden(request.toString());
    }

    private void alternativeLogin() {

        if (!enteredUser) {
            user = inputField.getText();
            enteredUser = true;
        } else if (!enteredPassword) {
            pw = inputField.getText();
            enteredPassword = true;

            JSONObject loginrequest = new JSONObject()
                    .put("type","login")
                    .put("user",user)
                    .put("password",pw);

            senden(loginrequest.toString());

        } else if (loginConfirmed) {

            String message = inputField.getText();
            appendMessage(message);

            JSONObject request = new JSONObject();

            if (message.startsWith("/")) {

                //command
                if (message.startsWith("/abmelden")) {

                    request
                            .put("type", "logout")
                            .put("message", "");

                } else if (message.startsWith("/changeroom")) {
                    int indexfirstspace = message.indexOf(' ');
                    String param = message.substring(indexfirstspace + 1);

                    request
                            .put("type", "changeroom")
                            .put("message", param);

                }

            } else {

                //normal message
                request
                        .put("type", "message")
                        .put("message", message);
            }
            senden(request.toString());
            inputField.setText("");
        }
    }


    boolean isLoginConfirmed() {
        return loginConfirmed;
    }

    void openPartnerDialog(JSONObject json) {// Dialog myDialog) {

        JSONObject request = new JSONObject();

        request
                .put("type", "privateChat")
                .put("privateChat", json.optString("sender", ""))
                .put("online", true)
                .put("sender", json.optString("privateChat", ""))
                .put("message", json.optString("message", ""));

        senden(request.toString());

    }

    public String getUser() {
        return user;
    }

}