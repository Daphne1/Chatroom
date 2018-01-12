package Client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Senfo on 10.01.2018.
 */ // ein Thread dient dem Empfangen
class empfangenThread extends Thread {

    // dazu muss der Socket und der BufferedReader übergeben werden
	BufferedReader bis;
	Socket server;
	Client client;

	empfangenThread(BufferedReader bis, Socket server, Client client) {
		this.bis = bis;
		this.server = server;
		this.client = client;
	}

	// Nachrichten koennen vom Server.Server entgegengenommen werden
	// falls sie nicht angenommen werden kann, wird eine Fehlermeldung mit Fehlerursache ausgegeben
	private String annehmen() {
		try {

			return bis.readLine();

		} catch (SocketException e) {

			System.out.println("Die Verbindung wurde unterbrochen.");
			return null;

		} catch (IOException e) {
			System.out.println("Eine Nachricht konnte vom Server.Server nicht angenommen werden.");
			e.printStackTrace();
			return null;
		}
	}

	public void run(){
		// empfängt die Nachrichten vom Server.Server über die Methode annehmen()
		// gibt diese aus, solange der Server.Server läuft
		while(true) {
			String ankommendeNachricht = annehmen();
			if (ankommendeNachricht != null) {

				//System.out.println(ankommendeNachricht);

				//switch types
				JSONObject json = null;
				String type = "";


				try {
					json = new JSONObject(ankommendeNachricht);

					type = json.optString("type","");
				} catch (JSONException e) {
					//malformed data couldnt parse JSON
				}

				if (json != null) {
					//TODO switch case
					if (type.equals("message")) {

						if (client.isLoginConfirmed()) {
							String nachricht = json.optString("message", "");

							if (!nachricht.equals(""))
							    client.appendMessage(nachricht);
						} else {
							//no accepted message til logged in
						}

					} else if (type.equals("raeume")) {

						client.addRooms(json.optJSONArray("message"));

					} else if (type.equals("nutzer")) {

						client.addUsers(json.optJSONArray("message"));

					} else if (type.equals("login")) {

						if (json.optString("status","ok").equals("ok")) {
							client.confirmLogin();
							String nachricht = json.optString("message","");
                            nachricht = nachricht.equals("") ? "Du bist nun eingeloggt" : nachricht;
							client.appendMessage( nachricht );
						} else {
							client.resetLogin(json.optString("message","Ungültiger Login"));
						}

					}

				}

			} else {
				//connection broke
				System.exit(0);
			}
		}
	}
}
