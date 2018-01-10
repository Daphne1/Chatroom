package Server;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;

//SINGLETON
public class Server {

	private static final int PORT = 3456;
    private static Server INSTANCE = new Server();
    public static synchronized Server getInstance() {
        return INSTANCE;
    }

    private ServerSocket socket;

    //username - clientthread
	private static HashMap<String, ClientThread> nutzerListe = new HashMap<>();

	//roomname - room
	private static HashMap<String, Raum> raumListe = new HashMap<>();

	//User - Password
	private static HashMap<String, String> passwords = new HashMap<String, String>();
		
	private Server() {
        try {

            System.out.println("Server wird gestartet!");
            this.socket = new ServerSocket(PORT);
            System.out.println("Server hat gestartet \nZum Beenden '/stop' eingeben.");

            Raum lobby = new Raum("Lobby");
            raumListe.put(lobby.getName(), lobby);

            // Benutzer benutzer = new Benutzer(null, null, lobby, null, null, null);
            System.out.println("Vorhandene Räume: " + raumListe.size());

            AcceptorThread acceptor = new AcceptorThread(this, socket);
            acceptor.start();

        } catch ( IOException e ) {

            System.out.println("Could not bind Socket!");


        }
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

	    //init the server
	    Server.getInstance();

	}
	
	public void end() {
		try {
			socket.close();
		} catch (IOException e) {}
	}
}