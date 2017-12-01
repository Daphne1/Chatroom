import java.net.*;
import java.io.*;

public class Client {
	public static void main(String args[]) {
        try {
            Socket server = new Socket("localhost", 2345);

			InputStream inputStream = server.getInputStream();
			BufferedReader bufferedReaderInputStream = new BufferedReader(new InputStreamReader(inputStream));

			OutputStream outputStream = server.getOutputStream();
			PrintWriter printWriterOutputStream = new PrintWriter(outputStream, true);

			sendThread sendet = new sendThread(printWriterOutputStream); 
			sendet.start();

			receiveThread empfaengt = new receiveThread(bufferedReaderInputStream);
			empfaengt.start();
		} catch(UnknownHostException e) {
			System.out.println("Can't find host.");
		} catch (IOException e) {
			System.out.println("Error connecting to host.");
		}
	}	
}

class sendThread extends Thread {
	private PrintWriter pout;
	
	sendThread(PrintWriter pout) {
		this.pout = pout;
	}

	private void send(String message, PrintWriter printWriterOutputStream) {
		printWriterOutputStream.println(message);
		printWriterOutputStream.flush();
	}
	
	public void run(){
		try {
			while(true) {
				InputStreamReader tastatur = new InputStreamReader(System.in);
				BufferedReader bufTastatur = new BufferedReader(tastatur);

				String tastatureingabe = bufTastatur.readLine();

				if (tastatureingabe.equals("/abmelden")) {
					System.exit(0);
				} else {
                    send(tastatureingabe, pout);
                }
			}			
		} catch (IOException e) {
		    System.out.println("Der sendThread funktioniert nicht mehr.");
		}
	}
}

class receiveThread extends Thread {
	private BufferedReader bis;

	receiveThread(BufferedReader bis) {
		this.bis = bis;
	}

	private String receive(BufferedReader bufferedReaderInputStream) {
		try {
			return bufferedReaderInputStream.readLine();
		} catch (IOException e) {
			System.out.println("Eine Nachricht konnte vom Server nicht angenommen werden.");
			e.printStackTrace();
			return null;
		}
	}
	
	public void run(){
		while(true) {
			String ankommendeNachricht = receive(bis);
			if (ankommendeNachricht != null) {
				System.out.println(ankommendeNachricht);	
			} else {
				System.exit(0);
			}
		}			
	}
}