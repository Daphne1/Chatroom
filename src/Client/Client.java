package Client;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.HashMap;


//SINGLETON
public class Client {

//    private static Client INSTANCE = new Client();

/*
    public static synchronized Client getInstance() {
        return INSTANCE;
    }
*/

	private JPanel mainPanel;
	private JTextField inputField;
	private JLabel RoomLabel;
	private JButton sendButton;
	private JTabbedPane tabbedPane1;
	private JList userlist;
	private JList roomlist;
    private JPanel Benutzer;
    private JPanel Raeume;
    private JTextArea clientLog;

	private Socket server;
	private PrintWriter printWriterOutputStream;

	private boolean enteredUser = false;
	private boolean enteredPassword = false;
	private String user;
	private String pw;
	private BufferedReader bis;

    //ROOMNAME, STRING
	private HashMap<String, String> RoomTexts;

    private DefaultListModel listUser = new DefaultListModel();
    private DefaultListModel listRooms = new DefaultListModel();



	public Client() {

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {

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

                    senden(loginrequest.toString(), printWriterOutputStream);

				} else {

				    String message = inputField.getText();
                    appendMessage(message);

                    JSONObject request = new JSONObject()
                            .put("type", "message")
                            .put("message", message);

                    senden(request.toString(), printWriterOutputStream);

                }

				inputField.setText("");
			}
		});
	}

	private void GUI_start() {
        JFrame clientFrame = new JFrame("Client Fenster");
        clientFrame.setContentPane(mainPanel);
        clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        clientFrame.pack();
        clientFrame.setVisible(true);
    }

	public void senden(String message, PrintWriter printWriterOutputStream) {
		printWriterOutputStream.println(message);
		printWriterOutputStream.flush();
	}

	String annehmen() {
		try {
			return bis.readLine();
		} catch (IOException e) {
            appendMessage("Eine Nachricht konnte vom Server.Server nicht angenommen werden.");
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static void main(String args[]) {
		new Client().startClient();
	}

	public void startClient() {
		try {
            GUI_start();

			Socket server = new Socket("localhost", 3456);

			// in
			InputStream inputStream = server.getInputStream();
			this.bis = new BufferedReader(new InputStreamReader(inputStream));

			// out
			OutputStream outputStream = server.getOutputStream();
			printWriterOutputStream = new PrintWriter(outputStream, true);




            appendMessage("Enter Username, then password!");
            loop();
            /*empfangenThread empfaengt = new empfangenThread(bis, server, this);
            empfaengt.start();*/

		} catch(UnknownHostException e) {
			appendMessage("Can't find host.");
		} catch (IOException e) {
			appendMessage("Error connecting to host.");
		}
	}

    private void loop() {
        while(true) {
            String ankommendeNachricht = annehmen();
            if (ankommendeNachricht != null) {
                appendMessage(ankommendeNachricht);
            } else {
                appendMessage("Client geschlossen. Keine weiteren Informationen. \n");
                System.exit(0);
            }
        }
    }

	protected void appendMessage (String message) {
		clientLog.append("\n");
		clientLog.append(message);
		System.out.println(message + "\n");
		clientLog.setCaretPosition(clientLog.getDocument().getLength());
	}

    /*protected void updateLists (HashMap<String, ClientThread> userlist, HashMap<String, Raum> roomlist) {
        listUser.clear();
        listRooms.clear();
        for(String key : userlist.keySet()) {
            listUser.addElement(userlist.get(key));
        }
        for(String key : roomlist.keySet()) {
            listRooms.addElement(roomlist.get(key));
        }
        userlist.setModel(listUser);
        roomlist.setModel(listRooms);

        // TODO
    }*/
}