package Server;

import java.util.LinkedList;

public class Raum {
	private String name;
	private LinkedList<ClientThread> nutzerThreads = new LinkedList<>();
	
	Raum (String raumName) {
		this.name = raumName;
	}
	
	protected int getNumberOfPersons() {
		return nutzerThreads.size();
	}
	protected void removeUser(ClientThread userThread) {
		nutzerThreads.remove(userThread);
	}
	protected void addUser(ClientThread userThread) {
		nutzerThreads.add(userThread);
	}

	protected String getName() {
		return name;
	}
	protected void setName(String name) {
		this.name = name;
	}

	protected LinkedList<ClientThread> getNutzerThreads() {
		return nutzerThreads;
	}
	protected void setNutzerThreads() {
		this.nutzerThreads = nutzerThreads;
	}

	@Override
	public String toString() {
		return name + " (" + getNumberOfPersons() + " Benutzer)";
	}
}
