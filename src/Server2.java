import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;


public class Server2 {
	ServerSocket socket;
	int port;

	private static HashMap<String, ClientThread> nutzerListe = new HashMap<>();
	private static HashMap<String, Raum> raumListe = new HashMap<>();
	private static HashMap<String, String> passwords = new HashMap<String, String>();
		
	private Server2(int port) {
		this.port = port;
	}

	public static HashMap<String, String> getPasswords() {
		return passwords;
	}

	public static HashMap<String, ClientThread> getNutzerListe() {
		return nutzerListe;
	}

	public static HashMap<String, Raum> getRaumListe() {
		return raumListe;
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Server hat gestartet \nZum Beenden '/stop' eingeben.");
		Server2 server = new Server2(3456);
		server.socket = new ServerSocket(server.port);

		Raum lobby = new Raum("Lobby");
		raumListe.put(lobby.getName(), lobby);
		
		while (true)  {
			// Benutzer benutzer = new Benutzer(null, null, lobby, null, null, null);
			System.out.println("Vorhandene Räume: " + raumListe.size());
			
			ServerInput eingabe = new ServerInput(server);
			eingabe.start();
			
			ClientThread clientThread = new ClientThread(server.socket.accept());
			clientThread.start();

		}
	}
	
	public void end() {
		try {
			socket.close();
		} catch (IOException e) {}
	}
}