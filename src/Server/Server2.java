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
public class Server2 {

	private static final int PORT = 3456;
	private static Server2 INSTANCE = new Server2();

	public static synchronized Server2 getInstance() {
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


	private Server2() {


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
			this.socket = new ServerSocket(PORT);
			log("Server hat gestartet.");

			newRoom("Lobby");
			newRoom("Füllerfeder");

			updateAllLists();

			AcceptorThread acceptor = new AcceptorThread(this, socket);
			acceptor.start();


		} catch (IOException e) {

			log("Could not bind Socket!");

		}
	}


	public HashMap<String, Map.Entry<String, Boolean>> getPasswords() {
		return passwords;
	}

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
		updateAllLists();
		GUI.setName("Lobby");
	}

	public void removeNutzer(ClientThread name) {
		nutzerListe.remove(name);
		updateAllLists();
	}

	private void saveUserData() {

//		FileOutputStream out = null;

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
				allUsers.put("\n");

			}

			allData.put("users", allUsers);
			stream.write(allData.toString().getBytes());

			updateAllLists();

		} catch (FileNotFoundException e) {
			System.out.println("Couldn't create file.");
		} catch (IOException e) {
			System.out.println("Couldn't open/create file.");
		}
	}

	private void loadUserData() {

		System.out.println("1");

		File users = new File("users.txt");
		if (users.isFile() && users.canRead()) {
			System.out.println("2");
			try {
//				System.out.println("3");
				FileInputStream in = new FileInputStream(users);
				try {
					String content = "";
					int c;
					while ((c = in.read()) != -1) {
						System.out.println("3");
						content += (char)c;
					}

					try {
						JSONObject json = new JSONObject(content);

						JSONArray usersarray = json.optJSONArray("users");
						System.out.println("Array: " + usersarray);

						if (usersarray != null) {
							System.out.println("4");

							for (int i = 0; i < usersarray.length(); i++) {
								System.out.println("5");
								JSONArray user = usersarray.optJSONArray(i);
								System.out.println("User: " + user);

								if (user != null) {
									System.out.println("6");

									String username = user.optString(2, "");
									String password = user.optString(0,"");
                                    Boolean banned = user.optBoolean(1,false);

									if (!username.equals("") && !password.equals("")) {
										System.out.println("7");
										passwords.put(
										        username,
                                                new AbstractMap.SimpleEntry<String,Boolean>(password,banned)
                                        );
									}

//									System.out.println("Daten: " + username + "\t" + password + "\t" + banned);

								}
							}

						} else {
							System.out.println("Array didn't exist.");
						}

					} catch (JSONException e) {
						//malformed data
					}

				} finally {
					in.close();
				}
			} catch (IOException ex) {
				System.out.println("Couldn't open file.");
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
	    Server2.getInstance();


	}

	protected void log(String message) {
		GUI.appendLog(message);
		System.out.println(message);
	}

	protected void newRoom (String name) {
		// TODO ein bestehender Name darf nicht gewählt werden
		raumListe.put(name, new Raum(name));
		updateAllLists();
	}

	protected void editRoom (Raum room, String newName) {
		// TODO ein bestehender Name darf nicht gewählt werden
		// TODO Lobby darf nicht umbenannt werden
		room.setName(newName);
		updateAllLists();
	}

	protected void deleteRoom (Raum room) {
		// TODO Lobby permanent
		raumListe.remove(room);
		updateAllLists();
	}

	protected void updateAllLists () {
		GUI.updateLists(nutzerListe, raumListe);

		for (String s : nutzerListe.keySet()) {
			nutzerListe.get(s).updateLists();
		}
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



	void warnUser(ClientThread ct) {
		ct.send("Bitte keine Dummheiten mehr.");
	}

}