package Server;

import Server.*;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

class ClientThread extends Thread { 

	private Socket client;
		
	private String name;
	private Raum raum;
	private JLabel RoomLabel;
	private JTextArea TextArea1;
	private JTextField TextField1;
	private JPanel mainPanel;
	private JButton sendButton;
	private JTabbedPane tabbedPane1;
	private JList list1;
	private JList list2;

	ClientThread(Socket client) {
		this.client = client;
	}

	public void changeRoom (Raum neuerRaum) {
		raum = neuerRaum;
	}

/*
	public boolean checkPassword (passwort) {
		if (this.passwort.equals(passwort))
	}
*/
	void send(String message) {
		try {
			DataOutputStream output = new DataOutputStream(client.getOutputStream());
			PrintWriter pWriterOutputStream = new PrintWriter(output, true);
			
			pWriterOutputStream.println(message);
			pWriterOutputStream.flush();
		} catch (IOException e) { System.out.println("Fehler beim Senden der Nachricht des Clients."); }
	}
	
	void sendToRoom (String message) {
		for (int p=0; p < raum.getNutzerThreads().size(); p++) {
			raum.getNutzerThreads().get(p).send(message);
		}
	}

	String accept(BufferedReader inputStream) {
		try { 
			String input = inputStream.readLine();
			return input; 
		} catch (IOException e) {
			System.out.println("Ankommende Nachrichten werden nicht akzeptiert.");
			e.printStackTrace();
			return null;
		}
	}
	
	public void run(){ 
		// Bearbeitung einer aufgebauten Verbindung
		try {
			System.out.println("Server.ClientThread läuft");
			InputStream inputStream = client.getInputStream();
			OutputStream outputStream = client.getOutputStream();
			BufferedReader input= new BufferedReader(new InputStreamReader(inputStream));
			String name, passwort;
			while(true) {
				send("Name: ");
				name = accept(input);
				send("Passwort: ");
				passwort = accept(input);

				if (Server.getPasswords().containsKey(name)) {
					if (Server.getPasswords().get(name).equals(passwort)) {
						raum = Server.getRaumListe().get("Lobby");
						Server.getNutzerListe().put(name, this);
						send("Du bist eingeloggt.\nZum Ausloggen schreibe '/abmelden'.");
						System.out.println("zweites if");
						break;
					} else {
						send("Dein Passwort wird nicht angenommen. Bitte versuche es noch einmal.");
						System.out.println("Else");
					}
				} else {
					raum = Server.getRaumListe().get("Lobby");
					send("Du hast einen neuen Account erstellt.");
					System.out.println("Neuer Account erstellt: \t" + name);
					send("Du bist eingeloggt.\nZum Ausloggen schreibe '/abmelden'.");
					break;
				}
			}

			// send("\nAktuelle Nutzer:");
			String onlineListe = "";
			for (int p = 0; p < Server.getNutzerListe().size(); p++) {
				onlineListe = onlineListe + Server.getNutzerListe().get(p).name + "\n";
			}
			send(onlineListe);
			sendToRoom(name + " hat sich eingeloggt.");
				
			
			while(true) {
				String in = accept(input);
				if (in.equals("/ChangeRoom")) {
					send("Neuer Chat: ");
                    String neuerRaum = in;
                } else if (in != null) {
					System.out.println(name + ": \t" + in);
					sendToRoom(name + ":\t" + in);
				} else {
					System.out.println(name + " hat seine Verbindung abgebrochen");
					sendToRoom("Zu " + name + " besteht keine Verbindung mehr.");
					break;
				}
			}
		} catch ( IOException e ) {

        } finally {
			if ( client != null ) try {
				client.close();
				Server.getNutzerListe().remove(name);
                Server.getRaumListe().remove(name);
			} catch ( IOException e ) { }
		}
	}
}