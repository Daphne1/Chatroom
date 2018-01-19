package Client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;


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

	private String annehmen() {
		try {

			return bis.readLine();

		} catch (SocketException e) {

			System.out.println("Die Verbindung wurde unterbrochen.");
			return null;

		} catch (IOException e) {
			System.out.println("Eine Nachricht konnte vom Server2.Server2 nicht angenommen werden.");
			e.printStackTrace();
			return null;
		}
	}

	public void run(){

		while(true) {
			String ankommendeNachricht = annehmen();
			if (ankommendeNachricht != null) {

				JSONObject json = null;
				String type = "";


				try {
					json = new JSONObject(ankommendeNachricht);

					type = json.optString("type","");
				} catch (JSONException e) {

				}

				if (json != null) {

					if (type.equals("message")) {

							String nachricht = json.optString("message", "");

							if (!nachricht.equals(""))
							    client.appendMessage(nachricht);

					} else if (type.equals("raeume")) {

						client.updateRooms(json.optJSONArray("message"));

					} else if (type.equals("nutzer")) {

						client.updateUser(json.optJSONArray("message"));

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
					else {
						client.appendMessage(ankommendeNachricht);
					}
				}

			} else {
				//connection broke
				client.appendMessage("Verbindung verloren! Beende das Programm.");
				System.exit(0);
			}
		}
	}
}
