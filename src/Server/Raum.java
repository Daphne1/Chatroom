package Server;

import java.util.LinkedList;

public class Raum {
	private String name;
	private LinkedList<String> nutzer = new LinkedList<>();
	
	Raum (String raumName) {
		this.name = raumName;
	}
	
	public int getNumberOfPersons () {
		return nutzer.size();
	}
	public void removeUser(String name) {
		nutzer.remove(name);
	}
	public void addUser(String name) {
		nutzer.add(name);
	}

	protected String getName() {
		return name;
	}
	protected void setName(String name) {
		this.name = name;
	}

	public LinkedList<String> getNutzerList() {
		return nutzer;
	}

	@Override
	public String toString() {
		return name + " (" + getNumberOfPersons() + " Benutzer)";
	}
}
