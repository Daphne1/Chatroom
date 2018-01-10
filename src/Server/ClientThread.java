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

	public void changeRoom (Raum neuerRaum) {
		raum = neuerRaum;
	}


	public boolean checkPassword (String passwort) {
		return server.checkUserPassword(name,passwort);
	}

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
			String name = "", passwort = "";

			String startupMessage = accept(input);

			try {
                JSONObject credentials = new JSONObject(startupMessage);

                name = credentials.optString("user", "");
                passwort = credentials.optString("password", "");

            } catch (JSONException e) {
			    //couldnt read / malformed syntax
            }

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

						//System.out.println("Else");


					}
				} else {

				    server.createUser(name,passwort);
				    raum = server.getRaum("Lobby");
					System.out.println("Neuer Account erstellt: \t" + name);

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
                        System.out.println(name + ": \t" + in);
                        sendToRoom(name + ":\t" + in);
                    } else {
                        System.out.println(name + " hat seine Verbindung abgebrochen");
                        sendToRoom("Zu " + name + " besteht keine Verbindung mehr.");
                        break;
                    }

				}
			}
		} catch ( IOException e ) {

        } finally {
			if ( client != null ) try {
				client.close();
				server.removeNutzer(name);
                //Server.getRaumListe().remove(name); wtf's this supposed to do?!
			} catch ( IOException e ) { }
		}
	}
}