import java.net.*;
import java.io.*;
import java.util.*;

import java.util.HashMap;


public class Client {
	Socket server;
	
	// der Client kann Nachrichten über den printWriterOutputStream senden
	// dieser muss jedoch durch flush() sofort geleert werden, damit nicht erst eine große
	// Nachrichtenansammlung geschickt wird
	public static void senden(String message, PrintWriter printWriterOutputStream) {
		printWriterOutputStream.println(message);
		printWriterOutputStream.flush();
	}
	
	// Nachrichten können vom Server entgegengenommen werden
	// falls sie nicht angenommen werden kann, wird eine Fehlermeldung mit Fehlerursache ausgegeben
	static String annehmen(BufferedReader bufferedReaderInputStream) {
		try {
			return bufferedReaderInputStream.readLine(); 
		} catch (IOException e) {
			System.out.println("Eine Nachricht konnte vom Server nicht angenommen werden.");
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static void main(String args[]) {
		try {
			Socket server = new Socket("localhost", 2345);
			
			// in
			InputStream inputStream = server.getInputStream();
			BufferedReader bufferedReaderInputStream = new BufferedReader(new InputStreamReader(inputStream));
			
			// out
			OutputStream outputStream = server.getOutputStream();
			PrintWriter printWriterOutputStream = new PrintWriter(outputStream, true);
			
			// damit gleichzeitig gesendet und empfangen werden kann hat jeder Client zwei eigene Threads
			// startet sendenThread und empfangenThread
			sendenThread sendet = new sendenThread(printWriterOutputStream); 
			sendet.start();
			empfangenThread empfaengt = new empfangenThread(bufferedReaderInputStream, server); 
			empfaengt.start();
			
		} catch(UnknownHostException e) {
			System.out.println("Can't find host.");
		} catch (IOException e) {
			System.out.println("Error connecting to host.");
		}
	}	
}

// ein Thread dient dem Senden
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
				
				// überprüft vor Senden der Nachricht an den Server, ob 'abmelden' gewünscht war
				// falls ja, terminiere den Client
				if (tastatureingabe.equals("/abmelden")) {
					System.exit(0);
				}
				
				// ansonsten wird die eingegebene Nachricht über den printWriterOutputStream
				// über die Methode senden() an den Server gesendet
				Client.senden(tastatureingabe, pout);
			}			
		} catch (IOException e) { System.out.println("Der sendenThread funktioniert nicht mehr."); }		
	}
}

// ein Thread dient dem Empfangen
class empfangenThread extends Thread {
	// dazu muss der Socket und der BufferedReader übergeben werden
	BufferedReader bis;
	Socket server;
	
	empfangenThread(BufferedReader bis, Socket server) {
		this.bis = bis;
		this.server = server;
	}
	
	public void run(){
		// empfängt die Nachrichten vom Server über die Methode annehmen()
		// gibt diese aus, solange der Server läuft
		while(true) {
			String ankommendeNachricht = Client.annehmen(bis);
			if (ankommendeNachricht != null) {
				System.out.println(ankommendeNachricht);	
			} else {
				System.exit(0);
			}
		}			
	}
}
