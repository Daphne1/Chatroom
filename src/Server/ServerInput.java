package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerInput extends Thread {

	/*
	*
	* liest von der Tastatur ein
	* und falls man in in die nich verwendete Konssole "/stop" schreibt beendet es den Server2
	* */

	private Server2 server2;
	
	public ServerInput(Server2 server2){
		this.server2 = server2;
	}
	public void run() {
		try {
			while(true) {
				// faengt den Stream der Tastatur ab
				InputStreamReader serverEingabe = new InputStreamReader(System.in);
				BufferedReader bufTastatur = new BufferedReader(serverEingabe);
				
				// speichert Daten in der Variable tastatureingabe und sendet diese
				String tastatureingabe = bufTastatur.readLine();
				if (tastatureingabe.equals("/stop")) {
					System.exit(0);
				}
			}
		} catch (IOException var4) { System.out.println("Servereingaben werden nicht mehr gelesen."); }
	}
}
