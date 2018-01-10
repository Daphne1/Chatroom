package Client;

import java.io.BufferedReader;
import java.net.Socket;

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

	public void run(){
		// empfängt die Nachrichten vom Server.Server über die Methode annehmen()
		// gibt diese aus, solange der Server.Server läuft
		while(true) {
			String ankommendeNachricht = Client.annehmen(bis);
			if (ankommendeNachricht != null) {
				System.out.println(ankommendeNachricht);
				client.appendMessage(ankommendeNachricht);
			} else {
				System.exit(0);
			}
		}
	}
}
