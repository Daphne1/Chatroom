package Server;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.util.*;
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


	private ServerLayout GUI;

	//  User - Password
	private HashMap<String, Map.Entry<String, Boolean>> passwords;


	private Server() {


		//init lookups
		this.raumListe = new HashMap<>();
		this.nutzerListe = new HashMap<>();
		this.passwords = new HashMap<>();

		//read passwords
		loadUserData();

		try {
			GUI = new ServerLayout(this);
			GUI.start_gui();
			GUI.setServerlogInfo(serverName);
			log("Server ist gestartet!");
			this.socket = new ServerSocket(PORT);
			log("Server hat gestartet \nZum Beenden '/stop' eingeben.");

			// newRoom("Lobby");
			Raum lobby = new Raum("Lobby");
			raumListe.put(lobby.getName(), lobby);

			Raum lobby2 = new Raum("Lobby2");
			raumListe.put(lobby2.getName(), lobby2);

			// Benutzer benutzer = new Benutzer(null, null, lobby, null, null, null);
			log("Vorhandene Räume: " + raumListe.size());

			AcceptorThread acceptor = new AcceptorThread(this, socket);
			acceptor.start();


		} catch (IOException e) {

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
			//password match
			return passwords.get(user).getKey().equals(password);
		} else {
			return false;
		}
	}

	public boolean isBanned(String user) {
		return (passwords.containsKey(user)) ? passwords.get(user).getValue() : false;
	}

	public boolean userExists(String user) {
		return passwords.containsKey(user);
	}

	public void createUser(String user, String password) {
		passwords.put(user, new AbstractMap.SimpleEntry<String, Boolean>(password, false));

		//save passwords
		saveUserData();
	}

	//raumliste needs a lock
	public Set<String> getRaumListe() {
		return raumListe.keySet();
	}

	protected HashMap getRaumListeHashMap() {
		return nutzerListe;
	}

	public Raum getRaum(String name) {
		return raumListe.containsKey(name) ? raumListe.get(name) : null;
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

	public void removeNutzer(ClientThread name) {
		nutzerListe.remove(name);
		updateAllLists(nutzerListe, raumListe);
	}

	private void saveUserData() {

		FileOutputStream out = null;

		try {

			File users = new File("users.txt");
			users.createNewFile(); // if file already exists will do nothing
			FileOutputStream stream = new FileOutputStream(users, false);

			JSONObject allData = new JSONObject();
			JSONArray allUsers = new JSONArray();

			for (Map.Entry<String, Map.Entry<String, Boolean>> _x : passwords.entrySet()) {

				JSONObject user = new JSONObject()
						.put(
								"user",
								_x.getKey()
						)
						.put(
								"password",
								_x.getValue().getKey()
						)
						.put(
								"banned",
								_x.getValue().getValue()
						);

				allUsers.put(user);

			}

			allData.put("users", allUsers);
			stream.write(allData.toString().getBytes());

		} catch (FileNotFoundException e) {
			//couldnt create file
		} catch (IOException e) {
			//couldnt open/create file
		}
	}

	private void loadUserData() {

		File users = new File("users.txt");
		if (users.isFile() && users.canRead()) {
			try {
				FileInputStream in = new FileInputStream(users);
				try {
					String content = "";
					int c;
					while ((c = in.read()) != -1) {
						content += (char)c;
					}

					try {
						JSONObject json = new JSONObject(content);

						JSONArray usersarray = json.optJSONArray("users");

						if (usersarray != null) {

							for (int i = 0; i < usersarray.length(); i++) {
								JSONObject user = usersarray.optJSONObject(i);

								if (user != null) {
									String username = user.optString("user", "");
									String password = user.optString("password","");
                                    Boolean banned = user.optBoolean("banned",false);

									if (!username.equals("") && !password.equals("")) {
										passwords.put(
										        username,
                                                new AbstractMap.SimpleEntry<String,Boolean>(password,banned)
                                        );
									}

								}
							}

						} else {
							//array didnt exist
						}

					} catch (JSONException e) {
						//malformed data
					}

				} finally {
					in.close();
				}
			} catch (IOException ex) {
				//couldnt open file
			}
		}
	}

	public void sendToUser (String nutzer, String message) {

	    nutzerListe.get(nutzer).send(message);

    }

    public void banUser(String user) {

	    if (passwords.containsKey(user)) {

	        passwords.get(user).setValue(true);

        }

        saveUserData();

	    kickUser(user);
    }

    public void kickUser(String user) {

	    if (nutzerListe.containsKey(user))
	        nutzerListe.get(user).kick();

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