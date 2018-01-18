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

	    String toSend = nachricht.toString();

	    for (String _x : raum.getNutzerList()) {

	        server2.sendToUser(_x, toSend);

		}
	}

	String accept() {
		try { 
			String input = this.input.readLine();
			server2.log(input);
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
			server2.log(name+ " ist jetzt da");//Johannes DEBUG

			request.put("message", "Passwort: ");
			send(request.toString());

			// send("Passwort: ");
			nachricht = accept();

			json = new JSONObject(nachricht);
//			String type = json.optString("type","");
			String passwort = json.optString("message", "");

//            String passwort = accept();
			server2.log("passwort: "+ passwort);//Johannes DEBUG

			if (!server2.checkUserPassword(name, passwort)) {          //TODO umbennenen
				server2.createUser(name, passwort);
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
			server2.log("ClientThread läuft");
            //TODO in den Construktor verschieben
            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            pWriterOutputStream = new PrintWriter(output, true);
			InputStream inputStream = client.getInputStream();
			OutputStream outputStream = client.getOutputStream();
			input= new BufferedReader(new InputStreamReader(inputStream));

			/*
			raum = (Raum) server2.getNutzerListeHashMap().get("Lobby");
			switchRoom(raum);

			String startupMessage = accept(input);

			 */
			//erwarte korrekte userdaten

            login();


            raum = server2.getRaum("Lobby");
            raum.addUser(name);
            server2.insertNutzer(name, this);

//TODO eigene methoden

            updateLists();

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
													.put("message","Raum existiert nicht")
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
								case "":break;
								default:
									server2.log(getUserName() + " hat einen unbekannten befehl gesendet");
									break;

					}

				}
			}
		} catch ( IOException e ) {
            System.out.println("IO fuckkerino");
        } finally {

			if ( client != null ) {
                try {
                    server2.removeNutzer(this);
                    raum.removeUser(name);
                    client.close();
                    //Server2.getRaumListe().remove(name); wtf's this supposed to do?!
                } catch (IOException e) {

                }
            }

		}
	}

	protected void updateLists() {
        ////////////////////////////////////
        //Sende nutzerliste zum nutzer
        //-> Funktion

        // send("\nAktuelle Nutzer:");
        JSONArray onlineListe = new JSONArray();
        for (String _x : raum.getNutzerList()) {
            onlineListe.put(_x);
        }

            /*TODO wenn du deinen alten eigenen code benutzen möchtest:
            *anstatt der while schleife die Methode login() verwenden
            * (habe es nur aus dem alten code in eine eigene methode kopiert)
            * */


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
