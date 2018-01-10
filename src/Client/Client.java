package Client;

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
	private JTextField TextField1;
	private JLabel RoomLabel;
	private JButton sendButton;
	private JTabbedPane tabbedPane1;
	private JList list2;
	private JList list1;
	private JTextArea TextArea1;

	private Socket server;
	private PrintWriter printWriterOutputStream;

	private boolean enteredUser = false;
	private boolean enteredPassword = false;
	private String user;
	private String pw;

    //ROOMNAME, STRING
	private HashMap<String, String> RoomTexts;

	public Client() {
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {

				if (!enteredUser) {
					user = TextField1.getText();
					enteredUser = true;
				} else if (!enteredPassword) {
					pw = TextField1.getText();
					enteredPassword = true;
				}
				TextArea1.append(TextField1.getText());
				senden(TextField1.getText(), printWriterOutputStream);
				TextField1.setText("");
			}
		});
	}

	// der Client.Client kann Nachrichten über den printWriterOutputStream senden
	// dieser muss jedoch durch flush() sofort geleert werden, damit nicht erst eine große
	// Nachrichtenansammlung geschickt wird
	public static void senden(String message, PrintWriter printWriterOutputStream) {
		printWriterOutputStream.println(message);
		printWriterOutputStream.flush();
	}
	
	// Nachrichten koennen vom Server.Server entgegengenommen werden
	// falls sie nicht angenommen werden kann, wird eine Fehlermeldung mit Fehlerursache ausgegeben
	static String annehmen(BufferedReader bufferedReaderInputStream) {
		try {
			return bufferedReaderInputStream.readLine(); 
		} catch (IOException e) {
			System.out.println("Eine Nachricht konnte vom Server.Server nicht angenommen werden.");
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static void main(String args[]) {
		new Client().start_client();
	}

	public void start_client() {
		try {
			Socket server = new Socket("localhost", 3456);

			// in
			InputStream inputStream = server.getInputStream();
			BufferedReader bufferedReaderInputStream = new BufferedReader(new InputStreamReader(inputStream));

			// out
			OutputStream outputStream = server.getOutputStream();
			printWriterOutputStream = new PrintWriter(outputStream, true);


			JFrame clientFrame = new JFrame("Client.Client Fenster");
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

		} catch(UnknownHostException e) {
			System.out.println("Can't find host.");
		} catch (IOException e) {
			System.out.println("Error connecting to host.");
		}
	}

	protected void appendMessage (String message) {
		TextArea1.append("\n");
		TextArea1.append(message);
	}
}

