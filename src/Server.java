import java.io.*; 
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
	private int port; // in diesem Fall 2345
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
			ReadingServerInput eingabe = new ReadingServerInput();
			eingabe.start();
			
			clientThread clientThread = new clientThread(server.serverSocket.accept(), verwaltung.threads, verwaltung.hmap);
			clientThread.start();
		}
	}
}

class clientThread extends Thread { 
	private Socket client;
	private ArrayList<clientThread> threads;
	private HashMap<String, String> hmap;
	private String name;

    clientThread(Socket client, ArrayList<clientThread> threads, HashMap<String, String> hmap) {
		this.client = client; 
		this.threads = threads;
		this.hmap = hmap;
	}

	private void send(String message) {
		try {
			DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
			PrintWriter printWriterOutputStream = new PrintWriter(dataOutputStream, true);
			
			printWriterOutputStream.println(message);
			printWriterOutputStream.flush();
		} catch (IOException e) {
		    System.out.println("Fehler beim Senden");
		}
	}

	private String receive(BufferedReader bufferedReaderInputStream) {
		try {
			return bufferedReaderInputStream.readLine();
		} catch (IOException e) {
			System.out.println("Fehler beim Empfangen der Nachricht.");
			e.printStackTrace();
			return null;
		}
	}

	public void run(){
		try {
			InputStream inputStream = client.getInputStream();
			
			BufferedReader bufferedReaderInputStream = new BufferedReader(new InputStreamReader(inputStream));
						
			while(true) {
				send("Name: ");
                name = receive(bufferedReaderInputStream);
				send("Passwort: ");
                String passwort = receive(bufferedReaderInputStream);
				
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
			StringBuilder onlineListe = new StringBuilder();
            for (clientThread thread1 : threads) {
                onlineListe.append(thread1.name).append("\n");
            }
			send(onlineListe.toString());

            for (clientThread thread : threads) {
                if (!thread.name.equals(name)) {
                    thread.send(name + " hat sich eingeloggt");
                }
            }
			
			while(true) {
				String aString = receive(bufferedReaderInputStream);
				if (aString != null) {
					System.out.println(name + ": \t" + aString);
                    for (clientThread thread : threads) {
                        if (thread.name.equals(name)) {
                            thread.send(name + ": \t" + aString);
                        }
                    }
				} else {
					System.out.println(name + " hat seine Verbindung abgebrochen");
                    for (clientThread thread : threads) {
                        if (thread.name.equals(name)) {
                            thread.send("Zu " + name + " besteht keine Verbindung mehr.");
                        }
                    }
					break;
				}
				
			}
		} catch ( IOException e ) {
            System.out.println("Fehler bei Ein- und Ausgabe");
        }
		finally { 
			if ( client != null ) try { 
				client.close(); 
				threads.remove(this);
			} catch ( IOException e ) {
                System.out.println("Fehler bei Ein- und Ausgabe");
            }
		}
	}
}

class ReadingServerInput extends Thread {
    ReadingServerInput(){}

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
	    } catch (IOException e) {
		    System.out.println("Fail");
		}
	}
}
	
class Verwaltung extends Thread {
    ArrayList<clientThread> threads = new ArrayList<>(); // angemeldete Nutzer
    HashMap<String, String> hmap = new HashMap<>(); // Namen und PWer

	Verwaltung(){}
}