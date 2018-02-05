package Client;

import Server.Server2;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


class empfangenThread extends Thread {

    // dazu muss der Socket und der BufferedReader übergeben werden
	BufferedReader bis;
	Socket server;
	Client client;

	private ArrayList<Dialog> privateChatList = new ArrayList<>();

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

//						client.updateAllUser(json.optJSONArray("message"));
						client.updateAllUser(json.optJSONArray("allUser"));

					} else if (type.equals("login")) {

						if (json.optString("status", "ok").equals("ok")) {
							client.confirmLogin();
							String nachricht = json.optString("message", "");
							nachricht = nachricht.equals("") ? "Du bist nun eingeloggt" : nachricht;
							client.appendMessage(nachricht);
						} else {
							client.resetLogin(json.optString("message", "Ungültiger Login"));
						}

					} else if (type.equals("privateChat")) {

						boolean partner_exists = false;
//						String partnerName = json.optString("privateChat", "");
//						client.appendMessage("partnerName: " + partnerName);
						boolean partner_online = json.optBoolean("online", false);
						Dialog myDialog = null;
						String sender = json.optString("sender", "");
						System.out.println("Sender: " + sender);

						for (int i = 0; i < privateChatList.size(); i++) {
							System.out.println("Sender: " + privateChatList.get(i));
							if (privateChatList.get(i).getPartner().equals(sender)) {
								myDialog = privateChatList.get(i);
								partner_exists = true;
								break;
							}
						}

						if (!partner_exists) {
							myDialog = new Dialog(this);
//							System.out.println("clientET: " + client.getUser());
							myDialog.Dialog_start();
							privateChatList.add(myDialog);
							myDialog.appendMessage("Neuer Chat zu " + sender + " geöffnet.");

							client.openPartnerDialog(json);
						}

						if (partner_online) {
							myDialog.appendMessage(
									json.optString("sender", "") + ":\t" +
									json.optString("message", "")
							);
						}

					} else {

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

	public ArrayList<Dialog> getPrivateChatList() {
		return privateChatList;
	}

	void addDialog(Dialog dia) {
		privateChatList.add(dia);
	}

}

// TODO jeder kann nur privaten Chat mit sich selbst öffnen
// TODO Nachrichten aus dem privaten Dialog werden nicht weitergegeben?
// TODO wenn Dialog geschlossen wird, schließt sich auch das Fenster des Clients, dieser bleibt aber angemeldet, nur die GUI verschwindet.

