import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerInput extends Thread {
	private Server2 server;
	
	public ServerInput(Server2 server){
		this.server = server;
	}
	public void run() {
		try {
			while(true) {
				// f�ngt den Stream der Tastatur ab
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
