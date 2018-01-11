package Server;

import Server.*;
import org.json.*;
import javax.swing.*;
import java.io.*;
import java.net.Socket;

class ClientThread extends Thread { 

	private Socket client;
	private Server server;
		
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

	ClientThread(Server server, Socket client) {
		this.client = client;
		this.server = server;
	}

	protected void switchRoom (Raum neuerRaum) {
		server.log(name + " wechselt vom Raum " + raum + " zu " + neuerRaum);
		sendToRoom(name + " hat zum Raum '" + neuerRaum.getName() + "' gewechselt.");
		raum.removeUser(this);
		raum = neuerRaum;
		raum.addUser(this);
		sendToRoom(name + " ist dem Raum beigetreten.");
	}


	protected boolean checkPassword (String passwort) {
		return server.checkUserPassword(name,passwort);
	}

	void send(String message) {
		try {
			DataOutputStream output = new DataOutputStream(client.getOutputStream());
			PrintWriter pWriterOutputStream = new PrintWriter(output, true);
			
			pWriterOutputStream.println(message);
			pWriterOutputStream.flush();
		} catch (IOException e) { server.log("Fehler beim Senden der Nachricht des Clients."); }
	}
	
	void sendToRoom (String message) {
		/*for (int p=0; p < raum.getNumberOfPersons(); p++) {
			raum.getNutzerThreads().get(p).send(message);
		}*/
		for (ClientThread ct : raum.getNutzerThreads()) {
			ct.send(message);
		}
	}

	String accept(BufferedReader inputStream) {
		try { 
			String input = inputStream.readLine();
			server.log(input);
			return input; 
		} catch (IOException e) {
			server.log("Ankommende Nachrichten werden nicht akzeptiert.");
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		return "[" + raum.getName() + "] /t" + name;
	}



	public void run(){
		// Bearbeitung einer aufgebauten Verbindung
		try {
			server.log("Server.ClientThread läuft");
			InputStream inputStream = client.getInputStream();
			OutputStream outputStream = client.getOutputStream();
			BufferedReader input= new BufferedReader(new InputStreamReader(inputStream));
			String name = "", passwort = "";

			raum = (Raum) server.getNutzerListeHashMap().get("Lobby");
			switchRoom(raum);

			String startupMessage = accept(input);



			try {
                JSONObject credentials = new JSONObject(startupMessage);

                name = credentials.optString("user", "");
                passwort = credentials.optString("password", "");

            } catch (JSONException e) {
			    //couldnt read / malformed syntax
            }

            System.out.println("Hallihallo: :) :) ):");
			while(true) {

				if (server.userExists(name)) {
					if (server.checkUserPassword(name, passwort)) {
						raum = server.getRaum("Lobby");
						server.insertNutzer(name, this);

						JSONObject answer = new JSONObject()
                                .put("type","login")
                                .put("status","ok")
                                .put("message","\"Du bist eingeloggt.\\nZum Ausloggen schreibe '/abmelden'.\"");

						send(answer.toString());

						//System.out.println("zweites if");
						break;

					} else {

					    JSONObject answer = new JSONObject()
                                .put("type","login")
                                .put("status","bad")
                                .put("message","Dein Passwort wird nicht angenommen. Bitte versuche es noch einmal.");

					    send(answer.toString());

					}
				} else {

				    server.createUser(name,passwort);
				    raum = server.getRaum("Lobby");
					server.log("Neuer Account erstellt: \t" + name);

					JSONObject answer = new JSONObject()
                            .put("type","login")
                            .put("status","ok")
                            .put("message",
                                    "Du hast einen neuen Account erstellt. \nDu bist eingeloggt.\nZum Ausloggen schreibe '/abmelden'.");

					send(answer.toString());

					break;

				}
			}

			// send("\nAktuelle Nutzer:");
			JSONArray onlineListe = new JSONArray();
			for (String _x : server.getNutzerListe()) {
				onlineListe.put(_x);
			}

			JSONObject nutzer = new JSONObject()
                    .put("type","nutzer")
                    .put("message",onlineListe)
                    .put("status","ok");

			send(nutzer.toString());

            JSONArray raumListe = new JSONArray();
            for (String _x : server.getRaumListe()) {
                raumListe.put(_x);
            }

            JSONObject raeume = new JSONObject()
                    .put("type","raeume")
                    .put("message",raumListe)
                    .put("status","ok");

            send(raeume.toString());

			JSONObject loginAnnouncement = new JSONObject()
                    .put("type","message")
                    .put("message",name + " hat sich eingeloggt.")
                    .put("status","ok");
			sendToRoom(loginAnnouncement.toString());

			while(true) {
				System.out.println("Zweite Schleife gestartet. \n");

				String in = accept(input);

				JSONObject message = null;
				String type = "";

				try {
				    message = new JSONObject(in);
                    type = message.optString("type","");

                } catch (JSONException e) {
				    //malformed data
                }

				if (message == null) {
				    //handle malformed message
                } else {

				    //TODO switch each type

                    if (in.equals("/ChangeRoom")) {
                        send("Neuer Chat: ");
                        String neuerRaum = in;
                    } else if (in != null) {
                        server.log(name + ": \t" + in);
                        sendToRoom(name + ":\t" + in);
                    } else {
                        server.log(name + " hat seine Verbindung abgebrochen");
                        sendToRoom("Zu " + name + " besteht keine Verbindung mehr.");
                        break;
                    }

				}
			}
		} catch ( IOException e ) {

        } finally {
			if ( client != null ) try {
				client.close();
				server.removeNutzer(this);
			} catch ( IOException e ) { }
		}
	}

}