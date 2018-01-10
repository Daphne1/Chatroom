package Server;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

//SINGLETON
public class Server {

	private static final int PORT = 3456;
    private static Server INSTANCE = new Server();
    public static synchronized Server getInstance() {
        return INSTANCE;
    }

    private ServerSocket socket;

    //username - clientthread
	private HashMap<String, ClientThread> nutzerListe;

	//roomname - room
	private HashMap<String, Raum> raumListe;

	//User - Password
	private HashMap<String, String> passwords;
		
	private Server() {

	    //init lookups
        this.raumListe = new HashMap<>();
	    this.nutzerListe = new HashMap<>();
	    this.passwords = new HashMap<>();

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

	/*
	public static HashMap<String, String> getPasswords() {
		return passwords;
	}
	*/

	//passwords need a lock
	public boolean checkUserPassword(String user, String password) {
	    if (passwords.containsKey(user)) {
	        return passwords.get(user).equals(password);
        } else {
	        return false;
        }
    }

    public boolean userExists(String user) {
	    return passwords.containsKey(user);
    }

    public void createUser(String user, String password) {
	    passwords.put(user,password);
    }

    //raumliste needs a lock
	public Set<String> getRaumListe() {
		return raumListe.keySet();
	}

	public Raum getRaum (String name) {
	    return raumListe.containsKey(name) ? null : raumListe.get(name);
    }

	//nutzerliste needs a lock
    public Set<String> getNutzerListe() {
        return nutzerListe.keySet();
    }

	public void insertNutzer(String name, ClientThread thread) {
	    nutzerListe.put(name, thread);
    }

    public void removeNutzer(String name) {

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