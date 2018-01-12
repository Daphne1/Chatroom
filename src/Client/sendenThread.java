package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by Senfo on 10.01.2018.
 */ // ein Thread dient dem Senden
class sendenThread extends Thread {
	// der printWriterOutputStream muss übergeben werden
	PrintWriter pout;

	sendenThread(PrintWriter pout) {
		this.pout = pout;
	}

	public void run(){
		try {
			while(true) {
				// fängt den Stream der Tastatur ab
				InputStreamReader tastatur = new InputStreamReader(System.in);
				BufferedReader bufTastatur = new BufferedReader(tastatur);

				// speichert Daten in der Variable tastatureingabe und sendet diese
				String tastatureingabe = bufTastatur.readLine();

				// überprüft vor Senden der Nachricht an den Server.Server, ob 'abmelden' gewünscht war
				// falls ja, terminiere den Client.Client
				if (tastatureingabe.equals("/abmelden")) {
					System.exit(0);
				}

				// ansonsten wird die eingegebene Nachricht über den printWriterOutputStream
				// über die Methode senden() an den Server.Server gesendet
				Client.getInstance().senden(tastatureingabe, pout);
			}
		} catch (IOException e) { System.out.println("Der Client.sendenThread funktioniert nicht mehr."); }
	}
}
