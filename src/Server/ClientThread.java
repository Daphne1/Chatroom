package Server;

import org.json.*;
import javax.swing.*;
import java.io.*;
import java.net.Socket;

class ClientThread extends Thread { 

	private Socket client;
	private Server server;
		
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

    ClientThread(Server server, Socket client) {
		this.client = client;
		this.server = server;
	}

    protected void switchRoom (Raum neuerRaum) {
        server.log(name + " wechselt vom Raum " + raum + " zu " + neuerRaum);
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
		return server.checkUserPassword(name,passwort);
	}

	void send(String message) {
	    System.out.println("ich sende:"+message); //Johannes DEBUG
        //TODO jason object erstellen bzw wo die funktion benutz wird durch die methoden die json object erstellen ersetzen (auser in den methoden)
			pWriterOutputStream.println(message);
			pWriterOutputStream.flush();
	}
	
	void sendToRoom (String message) {

	    JSONObject nachricht = new JSONObject()
                .put("type","message")
                .put("message", message)
                .put("status", "ok");

	    String toSend = nachricht.getString("message");

	    for (String _x : raum.getNutzerList()) {

	        server.sendToUser(_x, toSend);

		}
	}

	String accept() {
		try { 
			String input = this.input.readLine();
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
		return "[" + raum.getName() + "] " + name;
	}


	public String getUserName() {
		return name;
	}

	void kick() {
	    try {

            server.removeNutzer(this);
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
			JSONObject request = new JSONObject();
			request
					.put("type", "message")
					.put("message", "Name: ");

			send(request.toString());

//            send("Name: "); // null sendet an den Client
            String nachricht = accept();

			JSONObject json = new JSONObject(nachricht);
//			String type = json.optString("type","");
			String name = json.optString("message", "");

			this.name = name;
            server.log(name+ " ist jetzt da");//Johannes DEBUG

			request.put("message", "Passwort: ");
			send(request.toString());

            // send("Passwort: ");
			nachricht = accept();

			json = new JSONObject(nachricht);
//			String type = json.optString("type","");
			String passwort = json.optString("message", "");

//            String passwort = accept();
            server.log("passwort: "+ passwort);//Johannes DEBUG

            if (!server.checkUserPassword(name, passwort)) {          //TODO umbennenen
                server.createUser(name, passwort);
                send("Du hast einen neuen Account erstellt.");
                System.out.println("Neuer Account registriert: " + name);
            }
            if (checkPassword(passwort)) {// TODO oder die checkPassword benutzen
                send("Du bist eingeloggt.\nZum Ausloggen schreibe '/abmelden'.");
                break;
            } else {
                send("Dein Passwort wird nicht angenommen. Bitte versuche es noch einmal.");
            }
        }
    }
    public void run(){
		// Bearbeitung einer aufgebauten Verbindung
		try {
			server.log("ClientThread läuft");
            //TODO in den Construktor verschieben
            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            pWriterOutputStream = new PrintWriter(output, true);
			InputStream inputStream = client.getInputStream();
			OutputStream outputStream = client.getOutputStream();
			input= new BufferedReader(new InputStreamReader(inputStream));

			/*
			raum = (Raum) server.getNutzerListeHashMap().get("Lobby");
			switchRoom(raum);

			String startupMessage = accept(input);

			 */
			//erwarte korrekte userdaten

            login();
            /*TODO wenn du deinen alten eigenen code benutzen möchtest:
            *anstatt der while schleife die Methode login() verwenden
            * (habe es nur aus dem alten code in eine eigene methode kopiert)
            * */
/*

            while(true) {

				//erwarte login request
				String startupMessage = accept();

				try {
					JSONObject credentials = new JSONObject(startupMessage);

					name = credentials.optString("user", "");
					passwort = credentials.optString("password", "");

				} catch (JSONException e) {
					//couldnt read / malformed syntax
				}

				if (server.userExists(name)) {
					if (server.checkUserPassword(name, passwort)) {

					    if (!server.isBanned( name )) {
                            server.insertNutzer(name, this);

                            JSONObject answer = new JSONObject()
                                    .put("type", "login")
                                    .put("status", "ok")
                                    .put("message", "Du bist eingeloggt.\nZum Ausloggen schreibe '/abmelden'.");

                            send(answer.toString());

                            //System.out.println("zweites if");
                            break;
                        } else {

					        JSONObject answer = new JSONObject()
                                    .put("type", "login")
                                    .put("status", "bad")
                                    .put("message", "Du bist gebannt.");

                            send(answer.toString());
                        }

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
				    // raum = server.getRaum("Lobby");
					// server.log("Neuer Account erstellt: \t" + name);

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
*/



            raum = server.getRaum("Lobby");
            raum.addUser(name);
            server.insertNutzer(name, this);//Johannes

//TODO eigene methoden

            ////////////////////////////////////
            //Sende nutzerliste zum nutzer
            //-> Funktion

			// send("\nAktuelle Nutzer:");
			JSONArray onlineListe = new JSONArray();
			for (String _x : raum.getNutzerList()) {
				onlineListe.put(_x);
			}

			JSONObject nutzer = new JSONObject()
                    .put("type","nutzer")
                    .put("message",onlineListe)
                    .put("status","ok");

			send(nutzer.toString());
            ////////////////////////////////////


            ////////////////////////////////////
			//sende Raumlist zum User
            //-> Funktion
            JSONArray raumListe = new JSONArray();
            for (String _x : server.getRaumListe()) {
                raumListe.put(_x);
            }

            JSONObject raeume = new JSONObject()
                    .put("type","raeume")
                    .put("message",raumListe)
                    .put("status","ok");

            send(raeume.toString());
            ////////////////////////////////////

//TODO name vorher setzen
			sendToRoom(name + " hat sich eingeloggt.");

			while(valid) {
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
				    //handle malformed message
                } else {

				    //TODO switch each type

					//switch each type
					if (type.equals("message")) {

					    String nachricht = message.optString("message","");

					    if (!nachricht.equals("")) {
                            sendToRoom(name + ":\t" + nachricht);
                        }

					} else if (type.equals("changeroom")) {


					    String raumName = message.optString("message", "");

					    if (!raumName.equals("")) {
                            Raum neuerRaum = server.getRaum(raumName);

                            if (neuerRaum != null) {
                                send(
                                        new JSONObject()
                                                .put("type","message")
                                                .put("message","Raum zu " + raumName + " gewechselt!")
                                                .put("status","ok")
                                                .toString()
                                );
                                raum.removeUser(name);
                                raum = neuerRaum;
                                raum.addUser(name);
                            } else {
                                send(
                                        new JSONObject()
                                                .put("type","message")
                                                .put("message","Raum existiert nicht")
                                                .put("status","ok")
                                                .toString());
                            }

                        }

					} else if (type.equals("logout")) {

                        System.out.println(name + " hat seine Verbindung abgebrochen");
                        sendToRoom("Zu " + name + " besteht keine Verbindung mehr.");

                        if ( client != null ) {
                            try {
                                raum.removeUser(name);
                                server.removeNutzer(this);
                                client.close();
                                //Server.getRaumListe().remove(name); wtf's this supposed to do?!
                            } catch (IOException e) {

                            }
                        }

                        break;

					} else {
						//unknown type
					}

				}
			}
		} catch ( IOException e ) {
            System.out.println("IO fuckkerino");
        } finally {

			if ( client != null ) {
                try {
                    server.removeNutzer(this);
                    raum.removeUser(name);
                    client.close();
                    //Server.getRaumListe().remove(name); wtf's this supposed to do?!
                } catch (IOException e) {

                }
            }

		}
	}
}