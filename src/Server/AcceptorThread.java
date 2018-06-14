package Server;

import java.io.*;
import java.net.ServerSocket;

class AcceptorThread extends Thread {


	/*
	* startet neue ClientThreads
	* */

	private Server2 server2;
	private ServerSocket socket;

	AcceptorThread(Server2 server2, ServerSocket socket) {

		this.server2 = server2;
		this.socket = socket;

	}


	public void run(){

		ServerInput eingabe = new ServerInput(server2);
		eingabe.start();

		//Client.Client acceptor
		while (true) {

			try {
				ClientThread clientThread = new ClientThread(server2, socket.accept());
				clientThread.start();
			} catch ( IOException e ) {
				System.out.println("Failed to accept connection of a client.");
			}
		}

	}
}