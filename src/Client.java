import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.HashMap;


public class Client {
	Socket server;
	private JPanel mainPanel;
	private JTextField TextField1;
	private JLabel RoomLabel;
	private JButton sendButton;
	private JTabbedPane tabbedPane1;
	private JList list2;
	private JList list1;
	private JTextArea TextArea1;

	private PrintWriter printWriterOutputStream;


	private boolean enteredUser = false;
	private boolean enteredPassword = false;
	private String user;
	private String pw;


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

	// der Client kann Nachrichten über den printWriterOutputStream senden
	// dieser muss jedoch durch flush() sofort geleert werden, damit nicht erst eine große
	// Nachrichtenansammlung geschickt wird
	public static void senden(String message, PrintWriter printWriterOutputStream) {
		printWriterOutputStream.println(message);
		printWriterOutputStream.flush();
	}
	
	// Nachrichten k�nnen vom Server entgegengenommen werden
	// falls sie nicht angenommen werden kann, wird eine Fehlermeldung mit Fehlerursache ausgegeben
	static String annehmen(BufferedReader bufferedReaderInputStream) {
		try {
			return bufferedReaderInputStream.readLine(); 
		} catch (IOException e) {
			System.out.println("Eine Nachricht konnte vom Server nicht angenommen werden.");
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


			JFrame clientFrame = new JFrame("Client Fenster");
			clientFrame.setContentPane(mainPanel);
			clientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			clientFrame.pack();
			clientFrame.setVisible(true);

			// damit gleichzeitig gesendet und empfangen werden kann hat jeder Client zwei eigene Threads
			// startet sendenThread und empfangenThread
			//sendenThread sendet = new sendenThread(printWriterOutputStream);
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

// ein Thread dient dem Senden
class sendenThread extends Thread {
	// der printWriterOutputStream muss übergeben werden
	PrintWriter pout;
	
	sendenThread(PrintWriter pout) {
		this.pout = pout;
	}
	
	public void run(){
		try {
			while(true) {
				// fängt den Stream der Tastatur ab
				InputStreamReader tastatur = new InputStreamReader(System.in);
				BufferedReader bufTastatur = new BufferedReader(tastatur);
		
				// speichert Daten in der Variable tastatureingabe und sendet diese
				String tastatureingabe = bufTastatur.readLine();
				
				// überprüft vor Senden der Nachricht an den Server, ob 'abmelden' gewünscht war
				// falls ja, terminiere den Client
				if (tastatureingabe.equals("/abmelden")) {
					System.exit(0);
				}
				
				// ansonsten wird die eingegebene Nachricht über den printWriterOutputStream
				// über die Methode senden() an den Server gesendet
				Client.senden(tastatureingabe, pout);
			}			
		} catch (IOException e) { System.out.println("Der sendenThread funktioniert nicht mehr."); }		
	}
}

// ein Thread dient dem Empfangen
class empfangenThread extends Thread {
	// dazu muss der Socket und der BufferedReader übergeben werden
	BufferedReader bis;
	Socket server;
	Client client;
	
	empfangenThread(BufferedReader bis, Socket server, Client client) {
		this.bis = bis;
		this.server = server;
		this.client = client;
	}
	
	public void run(){
		// empfängt die Nachrichten vom Server über die Methode annehmen()
		// gibt diese aus, solange der Server läuft
		while(true) {
			String ankommendeNachricht = Client.annehmen(bis);
			if (ankommendeNachricht != null) {
				System.out.println(ankommendeNachricht);
				client.appendMessage(ankommendeNachricht);
			} else {
				System.exit(0);
			}
		}			
	}
}