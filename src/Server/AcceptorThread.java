package Server;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class AcceptorThread extends Thread {


	/*
	* startet neue ClientThreads
	* */

	private Server server;
	private ServerSocket socket;

	AcceptorThread(Server server, ServerSocket socket) {

		this.server = server;
		this.socket = socket;

	}


	public void run(){

		ServerInput eingabe = new ServerInput(server);
		eingabe.start();

		//Client.Client acceptor
		while (true) {

			try {
				ClientThread clientThread = new ClientThread(server, socket.accept());
				clientThread.start();
			} catch ( IOException e ) {
				System.out.println("Failed to accept connection of a client.");
			}
		}

	}
}