package Server;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import javax.swing.*;

//SINGLETON
public class Server {

	private static final int PORT = 3456;
    private static Server INSTANCE = new Server();
    public static synchronized Server getInstance() {
        return INSTANCE;
    }
    String serverName = "PseudoSportProgram";

    private ServerSocket socket;

    //username - clientthread
	private HashMap<String, ClientThread> nutzerListe;

	//roomname - room
	private HashMap<String, Raum> raumListe;

	//User - Password
	private HashMap<String, String> passwords;

	private ServerLayout GUI;

	private Server() {


	    //init lookups
        this.raumListe = new HashMap<>();
	    this.nutzerListe = new HashMap<>();
	    this.passwords = new HashMap<>();

	    try {
			GUI = new ServerLayout(this);
			GUI.start_gui(GUI);
			GUI.setServerlogInfo(serverName);
            log("Server ist gestartet!");
            this.socket = new ServerSocket(PORT);
            log("Server hat gestartet \nZum Beenden '/stop' eingeben.");

            newRoom("Lobby");

            // Benutzer benutzer = new Benutzer(null, null, lobby, null, null, null);
            log("Vorhandene Räume: " + raumListe.size());

            AcceptorThread acceptor = new AcceptorThread(this, socket);
            acceptor.start();

        } catch ( IOException e ) {

            log("Could not bind Socket!");

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

    protected HashMap getNutzerListeHashMap() {
		return nutzerListe;
	}

	public void insertNutzer(String name, ClientThread thread) {
	    nutzerListe.put(name, thread);
	    updateAllLists(nutzerListe, raumListe);
	    GUI.setName("Lobby");
    }

    public void removeNutzer(ClientThread ct) {
		nutzerListe.remove(ct);
		updateAllLists(nutzerListe, raumListe);
	}

	public static void main(String[] args) throws IOException {

	    //init the server
	    Server.getInstance();


	}

	protected void log(String message) {
		GUI.appendLog(message);
		System.out.println(message);
	}

	protected void newRoom (String name) {
		// TODO ein bestehender Name darf nicht gewählt werden
		raumListe.put(name, new Raum(name));
		updateAllLists(nutzerListe, raumListe);
	}

	protected void editRoom (Raum room, String newName) {
		// TODO ein bestehender Name darf nicht gewählt werden
		// TODO Lobby darf nicht umbenannt werden
		room.setName(newName);
		updateAllLists(nutzerListe, raumListe);
	}

	protected void deleteRoom (Raum room) {
		// TODO Lobby permanent
		raumListe.remove(room);
		updateAllLists(nutzerListe, raumListe);
	}

	private void updateAllLists (HashMap nutzerListe, HashMap raumListe) {
		GUI.updateLists(nutzerListe, raumListe);

		ArrayList<String> nutzerlistTemp = iterateHashmap(nutzerListe);
		String[] nutzerliste = new String[nutzerlistTemp.size()];
		for (int i=0; i < nutzerListe.size(); i++) {
			nutzerliste[i] = nutzerlistTemp.get(i);
		}

		ArrayList<String> roomlistTemp = iterateHashmap(raumListe);
		String[] roomliste = new String[roomlistTemp.size()];
		for (int i=0; i < raumListe.size(); i++) {
			roomliste[i] = roomlistTemp.get(i);
		}

		// TODO die userList und roomList an die Clients senden

	}

	private ArrayList<String> iterateHashmap (HashMap<String, Object> map) {
		ArrayList temp = new ArrayList();

		for(String key : map.keySet()) {
			temp.add(key);
		}
		return temp;
	}

	protected void editServername (String newName) {
		serverName = newName;
		GUI.setServerlogInfo(newName);
	}

	public void end() {
		try {
			socket.close();
		} catch (IOException e) {}
	}

	protected void refreshGUI () {

	}

	void warnUser(ClientThread ct) {
		ct.send("Bitte keine Dummheiten mehr.");
	}
	void kickUser(ClientThread ct) {
		ct.send("Das wars.");
		ct.send(null);
	}
	/*void bannUser(ClientThread ct) {
		ct.send(null);
		passwords.get(ct.getName()) = "äölkjhgfddssasaszuiejhj";
	}*/

}