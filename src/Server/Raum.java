package Server;

import java.util.LinkedList;

public class Raum {

	private String name;
	private LinkedList<String> nutzer = new LinkedList<>();
	
	Raum (String raumName) {
		this.name = raumName;
	}


	protected int getNumberOfPersons () {
		return nutzer.size();
	}

	protected void addUser(String name) {
		nutzer.add(name);
	}

	protected void removeUser(String name) {
		nutzer.remove(name);
	}

	protected String getName() {
		return name;
	}

	protected LinkedList<String> getNutzerList() {
		return nutzer;
	}
	protected void setNutzerList(LinkedList<String> nutzer) {
		this.nutzer = nutzer;
	}

	@Override
	public String toString() {
		return name + " (" + getNumberOfPersons() + " Benutzer)";
	}
}
