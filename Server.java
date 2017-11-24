import java.io.*; 
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;


public class Server {
	// der Port ist in diesem Fall 2345
	private int port;
	// ArrayList zur Speicherung der aktuell angemeldeten Nutzer
	private ArrayList<clientThread> threads = new ArrayList<clientThread>();
	// HashMap zur Speicherung der angelegten Nutzernamen und dazugehörigen Passwörter
	private HashMap<String, String> hmap = new HashMap<String, String>();
	// Anlegen des Serversockets server
	private ServerSocket serverSocket;
	
	private Server(int port) {
		this.port = port;
	}
	public static void main(String[] args) throws IOException {
		System.out.println("Server hat gestartet \nZum Beenden '/stop' eingeben.");
		Server server = new Server(2345);
		server.serverSocket = new ServerSocket(server.port);
		Verwaltung verwaltung = new Verwaltung();
		
		while (true)  {
			ReadingServerInput eingabe = new ReadingServerInput(server);
			eingabe.start();
			
			clientThread clientThread = new clientThread(server.serverSocket.accept(), verwaltung.threads, verwaltung.hmap);
			clientThread.start();
		}
	}
	
	public void end() {
		try {
			serverSocket.close();
		} catch (IOException e) {}
	}
}

class clientThread extends Thread { 
	private Socket client;
	private ArrayList<clientThread> threads;
	private HashMap<String, String> hmap;
	private String name;
	private String passwort;
	
	clientThread(Socket client, ArrayList<clientThread> threads, HashMap<String, String> hmap) { 
		this.client = client; 
		this.threads = threads;
		this.hmap = hmap;
	}
	
	private  void send(String message) {
		try {
			DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
			PrintWriter printWriterOutputStream = new PrintWriter(dataOutputStream, true);
			
			printWriterOutputStream.println(message);
			printWriterOutputStream.flush();
		} catch (IOException e) { System.out.println("Fehler beim Senden"); }
	}
	
	
	private String accept(BufferedReader bufferedReaderInputStream) {
		try { 
			String x = bufferedReaderInputStream.readLine();
			// System.out.println("Ich habe empfangen: " + x);
			return x; 
		} catch (IOException e) {
			System.out.println("Error connectng to host.");
			e.printStackTrace();
			return null;
		}
	}
	
	
	public void run(){ 
		// Bearbeitung einer aufgebauten Verbindung
		try {
			InputStream inputStream = client.getInputStream();
			OutputStream outputStream = client.getOutputStream();
			
			BufferedReader bufferedReaderInputStream = new BufferedReader(new InputStreamReader(inputStream));
						
			while(true) {
				send("Name: "); // null sendet an den Client
				String name = accept(bufferedReaderInputStream);
				this.name = name;
				send("Passwort: ");
				String passwort = accept(bufferedReaderInputStream);
				this.passwort = passwort;
				
				if (!hmap.containsKey(name)) {
					hmap.put(name, passwort);
					send("Du hast einen neuen Account erstellt.");
					System.out.println("Neuer Account registriert: " + name);
				}
				if (hmap.get(name).equals(passwort)) {
					
					send("Du bist eingeloggt.\nZum Ausloggen schreibe '/abmelden'.");
					break;
				} else {
					send("Dein Passwort wird nicht angenommen. Bitte versuche es noch einmal.");
				}
			}
			threads.add(this);
			
			send("\nAktuelle Nutzer:");
			String onlineListe = "";
			for (int p = 0; p < threads.size(); p++) {
				onlineListe = onlineListe + threads.get(p).name + "\n";
			}
			send(onlineListe);
			
			for (int z = 0; z < threads.size(); z++) {
				if (!threads.get(z).name.equals(name)) {
					threads.get(z).send(name + " hat sich eingeloggt");
				}
			}			
			
			while(true) {
				String aString = accept(bufferedReaderInputStream);
				if (aString != null) {
					System.out.println(name + ": \t" + aString);
					for(int i1 = 0; i1< threads.size(); i1++) {
						if(threads.get(i1).name != name) {
							threads.get(i1).send(name + ": \t" + aString);
						}
					}
				} else {
					System.out.println(name + " hat seine Verbindung abgebrochen");
					for(int i1 = 0; i1< threads.size(); i1++) {
						if(threads.get(i1).name != name) {
							threads.get(i1).send("Zu " + name + " besteht keine Verbindung mehr.");
						}
					}
					break;
				}
				
			}
		} catch ( IOException e ) {} // Fehler bei Ein- und Ausgabe
		finally { 
			if ( client != null ) try { 
				client.close(); 
				threads.remove(this);
			} catch ( IOException e ) { } } 
	}
}

class ReadingServerInput extends Thread {
	
	private Server server;
	
	public ReadingServerInput(Server server){
		this.server = server;
	}
	public void run() {
		try {
		while(true) {
			// fängt den Stream der Tastatur ab
			InputStreamReader serverEingabe = new InputStreamReader(System.in);
			BufferedReader bufTastatur = new BufferedReader(serverEingabe);
					
			// speichert Daten in der Variable tastatureingabe und sendet diese
			String tastatureingabe = bufTastatur.readLine();
			if (tastatureingabe.equals("/stop")) {
				System.exit(0);
			}
		}
	} catch (IOException e) { System.out.println("fail"); }
	}
}
	
class Verwaltung extends Thread {
	// ArrayList zur Speicherung der aktuell angemeldeten Nutzer
	protected ArrayList<clientThread> threads = new ArrayList<clientThread>();
	// HashMap zur Speicherung der angelegten Nutzernamen und dazugehörigen Passwörter
	protected HashMap<String, String> hmap = new HashMap<String, String>();

	Verwaltung(){ }
}