package Server;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

	    //read passwords
	    loadUserData();

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

	    //save passwords
	    saveUserData();
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
		nutzerListe.remove(name);
    }

    private void saveUserData() {

		FileOutputStream out = null;

		try {

			File users = new File("users.txt");
			users.createNewFile(); // if file already exists will do nothing
			FileOutputStream stream = new FileOutputStream(users, false);

			JSONObject allData = new JSONObject();
			JSONArray allUsers = new JSONArray();

			for (Map.Entry<String,String> _x : passwords.entrySet()) {

				JSONObject user = new JSONObject()
						.put(
								_x.getKey(),
								_x.getValue()
						);

				allUsers.put(user);

			}

			allData.put("users",allUsers);
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

									if (!username.equals("") && !password.equals("")) {
										passwords.put(username,password);
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