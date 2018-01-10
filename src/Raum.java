import java.util.LinkedList;

public class Raum {
	private String name;
	private LinkedList<ClientThread> nutzerThreads = new LinkedList<>();
	
	Raum (String raumName) {
		this.name = raumName;
	}
	
	public int getNumberOfPersons (LinkedList<ClientThread> nutzerThreads) {
		return nutzerThreads.size();
	}
	public void removeUser(ClientThread userThread) {
		nutzerThreads.remove(userThread);
	}
	public void addUser(ClientThread userThread) {
		nutzerThreads.add(userThread);
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public LinkedList<ClientThread> getNutzerThreads() {
		return nutzerThreads;
	}
	public void setNutzerThreads(LinkedList<ClientThread> nutzerThreads) {
		this.nutzerThreads = nutzerThreads;
	}
}
