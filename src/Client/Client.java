package Client;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.HashMap;


//SINGLETON
public class Client {

    private static Client INSTANCE = new Client();

    public static synchronized Client getInstance() {
        return INSTANCE;
    }

	private JPanel mainPanel;
	private JTextField inputField;
	private JLabel RoomLabel;
	private JButton sendButton;
	private JTabbedPane tabbedPane1;
	private JList<String> userlist;
	private JList<String> roomlist;
    private JPanel Benutzer;
    private JPanel Raeume;
    private JTextArea clientLog;

	private Socket server;
	private PrintWriter printWriterOutputStream;

	private boolean enteredUser = false;
	private boolean enteredPassword = false;
	private boolean loginConfirmed = false;
	private String user;
	private String pw;

    //ROOMNAME, STRING
	private HashMap<String, String> RoomTexts;
    private DefaultListModel listUser = new DefaultListModel();
    private DefaultListModel listRooms = new DefaultListModel();


	public Client() {

        boolean started = startClient();

        if (!started) {
            System.exit(0);
        }

        appendMessage("Enter Username, then password!");
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

				} else if (loginConfirmed) {

				    String message = inputField.getText();
                    appendMessage(message);

                    JSONObject request = new JSONObject();

				    if (message.startsWith("/")) {

				        //command
                        if (message.startsWith("/abmelden")) {

                            request
                                    .put("type","logout")
                                    .put("message","");

                        } else if (message.startsWith("/changeroom")) {
                            int indexfirstspace = message.indexOf(' ');
                            String param = message.substring(indexfirstspace + 1);

                            request
                                    .put("type","changeroom")
                                    .put("message",param);

                        }

                    } else {

				        //normal message
				        request
                            .put("type", "message")
                            .put("message", message);
                    }
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

    public void confirmLogin() {
	    loginConfirmed = true;
    }

    public void resetLogin(String error) {
	    loginConfirmed = false;
	    enteredPassword = false;
	    enteredUser = false;

	    appendMessage(error);
    }

    public boolean isLoginConfirmed() {
	    return loginConfirmed;
    }

    public void addRooms (JSONArray array) {

        if (array != null) {

			listUser.clear();

        	for (int i = 0; i < array.length(); i++) {
                listUser.addElement(array.optString(i,""));
            }
        }

    }

    public void addUsers (JSONArray array) {

        if (array != null) {

			listRooms.clear();

            for (int i = 0; i < array.length(); i++) {
                listRooms.addElement(array.optString(i,""));
            }
        }

    }

	// der Client.Client kann Nachrichten über den printWriterOutputStream senden
	// dieser muss jedoch durch flush() sofort geleert werden, damit nicht erst eine große
	// Nachrichtenansammlung geschickt wird
	public void senden(String message, PrintWriter printWriterOutputStream) {
		printWriterOutputStream.println(message);
		printWriterOutputStream.flush();
	}

	/*
	// Nachrichten koennen vom Server.Server entgegengenommen werden
	// falls sie nicht angenommen werden kann, wird eine Fehlermeldung mit Fehlerursache ausgegeben
	static String annehmen(BufferedReader bufferedReaderInputStream) {
		try {
			return bufferedReaderInputStream.readLine(); 
		} catch (IOException e) {
			appendMessage("Eine Nachricht konnte vom Server.Server nicht angenommen werden.");
			e.printStackTrace();
			return null;
		}
	}
	*/
	
	
	public static void main(String args[]) {
		Client.getInstance();
	}

	private boolean startClient() {
		try {
		    //??
            GUI_start();

			Socket server = new Socket("localhost", 3456);

			// in
			InputStream inputStream = server.getInputStream();
			BufferedReader bufferedReaderInputStream = new BufferedReader(new InputStreamReader(inputStream));

			// out
			OutputStream outputStream = server.getOutputStream();
			printWriterOutputStream = new PrintWriter(outputStream, true);


			JFrame clientFrame = new JFrame("Client Fenster");
			clientFrame.setContentPane(mainPanel);
			clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			clientFrame.pack();
			clientFrame.setVisible(true);

			// damit gleichzeitig gesendet und empfangen werden kann hat jeder Client.Client zwei eigene Threads
			// startet Client.sendenThread und Client.empfangenThread
			//Client.sendenThread sendet = new Client.sendenThread(printWriterOutputStream);
			//sendet.start();
            empfangenThread empfaengt = new empfangenThread(bufferedReaderInputStream, server, this);
            empfaengt.start();

            return true;

		} catch(UnknownHostException e) {
			appendMessage("Can't find host.");
		} catch (IOException e) {
			appendMessage("Error connecting to host.");
		}

		return false;
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