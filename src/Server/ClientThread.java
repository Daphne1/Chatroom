package Server;

import org.json.*;
import javax.swing.*;
import java.io.*;
import java.net.Socket;

class ClientThread extends Thread { 

	private Socket client;
	private Server2 server2;
		
	private String name;
	boolean valid = true;

	private Raum raum;
	private JLabel RoomLabel;
	private JTextArea TextArea1;
	private JTextField TextField1;
	private JPanel mainPanel;
	private JButton sendButton;
	private JTabbedPane tabbedPane1;
	private JList list1;
	private JList list2;
	private BufferedReader input;
    PrintWriter pWriterOutputStream;

    ClientThread(Server2 server2, Socket client) {
		this.client = client;
		this.server2 = server2;

		try {
			DataOutputStream output = new DataOutputStream(client.getOutputStream());
			pWriterOutputStream = new PrintWriter(output, true);
			InputStream inputStream = client.getInputStream();
			OutputStream outputStream = client.getOutputStream();
			input = new BufferedReader(new InputStreamReader(inputStream));
		} catch (IOException e) {
			server2.log("Fehler im Konstruktor des ClientThreads von " + server2.getNutzerListeHashMap().get(this));
			e.printStackTrace();
		}
	}

    protected String getUserName() {
        return name;
    }

    protected void switchRoom (Raum neuerRaum) {
        server2.log(name + " wechselt vom Raum " + raum + " zu " + neuerRaum);
        sendToRoom(name + " hat zum Raum '" + neuerRaum.getName() + "' gewechselt.");
        raum.removeUser(name);
        raum = neuerRaum;
        raum.addUser(name);
        sendToRoom(name + " ist dem Raum beigetreten.");
    }

	void changeRoom (Raum neuerRaum) {
		raum = neuerRaum;
	}


	public boolean checkPassword (String passwort) {
		return server2.checkUserPassword(name,passwort);
	}

	void send(String message) {

		pWriterOutputStream.println(message);
		pWriterOutputStream.flush();
	}
	
	void sendToRoom (String message) {

	    JSONObject nachricht = new JSONObject()
                .put("type","message")
                .put("message", message)
                .put("status", "ok");

	    String toSend = nachricht.toString();

	    for (String _x : raum.getNutzerList()) {

	        if (!_x.equals(this.getUserName())) {
                server2.sendToUser(_x, toSend);
            }
		}
	}

	String accept() {
		try { 
			String input = this.input.readLine();
			if (input == null){
				server2.log("null emfangen");
				closeClientThread();
			}else{
				server2.log(input);
			}
			return input;
		} catch (IOException e) {
			server2.log("<ClientThread> accept funtioniert nicht");
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		return "[" + raum.getName() + "] " + name;
	}


	void kick() {
	    try {

            server2.removeNutzer(this);
            raum.removeUser(name);
            valid = false;

	        if (client != null)
	            client.close();

        } catch (IOException e) {
	        //already disconnected?
        }
    }

	private void login() {
		while(true)
		{
			server2.log("ein neuer Client möchte sich anmelden");
			JSONObject request = new JSONObject();
			request
					.put("type", "message")
					.put("message", "Name: ");

			send(request.toString());

			String nachricht = accept();
			JSONObject json = new JSONObject(nachricht);
			String name = json.optString("message", "");
			this.name = name;
			server2.log(name+ " versucht sich anzumelden ");

			request.put("message", "Passwort: ");
			send(request.toString());
			nachricht = accept();

			json = new JSONObject(nachricht);
			String passwort = json.optString("message", "");



			if (!server2.userExists(name)) {
				server2.createUser(name, passwort);

				request.put("message", "Du hast einen neuen Account erstellt.");
				send(request.toString());

				server2.log("Neuer Account registriert: " + name);
			}
			if (checkPassword(passwort)) {
				// prüfen ob der user schon angemeldet ist
				if (server2.nutzerlisteContainsUser(name)){
					server2.log(name + " versucht sich anzumelden, obwohl er schon angemeldet ist");
					request
							.put("type", "message")
							.put("message", "Der Nutzer ist schon angemeldet");
					send(request.toString());
					login();
				}

				// prüfen ob der user gebannt ist
				if (server2.isbanned(name)){
					server2.log(name + " versucht sich anzumelden, obwohl er gebannt ist");
					request
							.put("type", "message")
							.put("message", "Der Nutzer ist gebannt ");
					send(request.toString());
					login();
				}

				request.put("message", "Du bist eingeloggt.\nZum Ausloggen schreibe '/abmelden'.");
				send(request.toString());

				server2.log(name + " ist jetzt angemeldet");
				break;
			} else {
				request.put("message", "Dein Passwort wird nicht angenommen. Bitte versuche es noch einmal.");
				send(request.toString());
			}
		}
	}

    public void run(){
		// Bearbeitung einer aufgebauten Verbindung
		server2.log("ClientThread läuft");

		login();

		raum = server2.getRaum("Lobby");
		raum.addUser(name);
		server2.insertNutzer(name, this);

		updateLists();

		sendToRoom(name + " hat sich eingeloggt.");

		while(valid) {
            loop();
        }
		server2.log("schleife beendet"); //DEBUG
	}

	private void closeClientThread(){
    	server2.log(name + " hat den Server verlassen");
		server2.removeNutzer(this);
		raum.removeUser(name);
		try {
				client.close();
		} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// TODO in log: alles über append in verlauf.txt einschreiben
	private void loop(){
		String in = accept();

		JSONObject message = null;
		String type = "";

		try {
			message = new JSONObject(in);
			type = message.optString("type","");

		} catch (JSONException e) {
			//malformed data
		}

		if (message == null) {
			closeClientThread();
		} else {

			switch (type){
				case "message":
					String nachricht = message.optString("message","");
					if (!nachricht.equals("")) {
						sendToRoom(name + ":\t" + nachricht);
					}
					break;
				case "switchRoom":
					String raumName = message.optString("message", "");

					if (!raumName.equals("")) {
						Raum neuerRaum = server2.getRaum(raumName);

						if (neuerRaum != null) {
							switchRoom(neuerRaum);
						} else {
							send(
									new JSONObject()
											.put("type","message")
											.put("message","Raum existiert nicht.")
											.put("status","ok")
											.toString());
						}

					}
					break;
				case "logout":
					System.out.println(name + " hat seine Verbindung abgebrochen");
					sendToRoom("Zu " + name + " besteht keine Verbindung mehr.");

					if ( client != null ) {
						try {
							raum.removeUser(name);
							server2.removeNutzer(this);
							client.close();
							//Server2.getRaumListe().remove(name); wtf's this supposed to do?!
						} catch (IOException e) {

						}
					}

					break;
				default:
					server2.log(getUserName() + " hat einen unbekannten befehl gesendet");
					break;

			}

		}
	}

	protected void updateLists() {
        ////////////////////////////////////
        //Sende nutzerliste zum nutzer
        //-> Funktion

        // aktuelle Nutzer
        JSONArray onlineListe = new JSONArray();

        if (raum != null) {
            for (String _x : raum.getNutzerList()) {
                onlineListe.put(_x);
            }

            JSONObject nutzer = new JSONObject()
                    .put("type","nutzer")
                    .put("message", onlineListe)
                    .put("status","ok");

            send(nutzer.toString());
        }
        ////////////////////////////////////


        ////////////////////////////////////
        //sende Raumlist zum User
        //-> Funktion
        JSONArray raumListe = new JSONArray();
        for (String _x : server2.getRaumListe()) {
            raumListe.put(_x);
        }

        JSONObject raeume = new JSONObject()
                .put("type","raeume")
                .put("message",raumListe)
                .put("status","ok");

        send(raeume.toString());
        ////////////////////////////////////
    }
}
