import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.*;
import java.awt.color.*;
 
public class LayoutChatroom {
	
	public static void main(String[] args) {
		JFrame serverFrame = new JFrame("Server");
		serverFrame.setSize(450, 300);
		
		Border bo = new LineBorder(Color.yellow);
		JMenuBar hauptmenue = new JMenuBar();
		hauptmenue.setBorder(bo);
		
		JMenu menu1 = new JMenu("Server");
		hauptmenue.add(menu1);
		
		JMenu menu2 = new JMenu("Benutzer");
		JMenuItem itemBenutzer1 = new JMenuItem("Client1");
		menu2.add(itemBenutzer1);
		hauptmenue.add(menu2);
		
		JMenu menu3 = new JMenu("Room");
		JMenuItem itemRoom1 = new JMenuItem("Room1");
		menu3.add(itemRoom1);
		hauptmenue.add(menu3);
		
		JMenu menu4 = new JMenu("Optionen");
		JMenuItem itemOptionen1 = new JMenuItem("Option1");
		menu4.add(itemOptionen1);
		hauptmenue.add(menu4);
		
		serverFrame.setJMenuBar(hauptmenue);
		
		
		JTabbedPane tabpane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		
		JPanel serverPanel = new JPanel();
		serverPanel.setBackground(Color.magenta);
		tabpane.addTab("Server", serverPanel);
		
		JPanel benutzerPanel = new JPanel();
		benutzerPanel.setBackground(Color.yellow);
		tabpane.addTab("Benutzer", benutzerPanel);
		
		JPanel roomPanel = new JPanel();
		roomPanel.setBackground(Color.BLACK);
		tabpane.addTab("Room", roomPanel);
		
		JPanel optionenPanel = new JPanel();
		optionenPanel.setBackground(Color.CYAN);
		tabpane.addTab("Optionen", optionenPanel);
		
		JPanel serverlog = new JPanel();
		JPanel status = new JPanel();
		
		JLabel serverPanelServerlogLabel = new JLabel("Server 'BlueMoon' ist online.");
		JLabel serverPanelStatusLabel = new JLabel("Hierhin kommt die �bersicht.");
		
		serverlog.add(serverPanelServerlogLabel);
		status.add(serverPanelStatusLabel);
		
		// Aufteilung mit JInternalFrame
		JInternalFrame serverlogFrame = new JInternalFrame("Serverlog", true, true, true, true);
		JInternalFrame statusFrame = new JInternalFrame("Status");
		JInternalFrame informationenFrame = new JInternalFrame("Informationen");
		JInternalFrame aufgabenFrame = new JInternalFrame("Aufgaben");
		
		serverPanel.add(serverlogFrame);
		serverPanel.add(statusFrame);
		serverPanel.add(informationenFrame);
		serverPanel.add(aufgabenFrame);
		
		serverlogFrame.setSize(200, 200);
		serverlogFrame.setLocation(0, 0);
		serverlogFrame.show();
		
		statusFrame.setSize(200, 200);
		statusFrame.setLocation(200, 0);
		statusFrame.show();
		
		informationenFrame.setSize(200, 200);
		informationenFrame.setLocation(0, 0);
		informationenFrame.show();
		
		aufgabenFrame.setSize(200, 200);
		aufgabenFrame.setLocation(0, 0);
		aufgabenFrame.show();
		
		
		JTextArea serverTextVerlauf = new JTextArea(5, 20);
		// Zeilenumbruch
		serverTextVerlauf.setLineWrap(true);
		// Zeilenumbruch nur nach ganzen Worten
		serverTextVerlauf.setWrapStyleWord(true);
		
		// scrollen
		JScrollPane chatScrollen = new JScrollPane(serverTextVerlauf);
		// vorher: JScrollPane chatverlauf = new JScrollPane(serverlog, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		chatScrollen.setBackground(Color.GRAY);
		serverPanel.add(chatScrollen);
		
		
		JTabbedPane statusTabpane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		
		JPanel statusBenutzer = new JPanel();
		statusTabpane.addTab("Benutzer", statusBenutzer);
		JPanel statusRooms = new JPanel();
		statusTabpane.addTab("R�ume", statusRooms);
		
		String benutzerListe[] = {"Client1", "Client2", "Client3"};
		DefaultListModel<String> listenModell1 = new DefaultListModel<String>();
		JList benutzerUebersicht = new JList(benutzerListe);
		for(int i=0; i < benutzerListe.length; i++) {
			listenModell1.addElement(benutzerListe[i]);
		}
		statusBenutzer.add(benutzerUebersicht);
		
		// sp�ter:
		// listenModell1.add(3, "Client 4");
		// listenModell1.removeElement("Client2");
		
		String roomsListe[] = {"Room1", "Room2", "Room3"};
		DefaultListModel<String> listenModell2 = new DefaultListModel<String>();
		JList roomsUebersicht = new JList(roomsListe);
		for(int j=0; j < benutzerListe.length; j++) {
			listenModell2.addElement(benutzerListe[j]);
		}
		statusRooms.add(roomsUebersicht);
		
		
		statusFrame.add(statusTabpane);
	
		
		JLabel aufgabenAuswahlLabel = new JLabel("W�hle eine Operation.");
		aufgabenFrame.add(aufgabenAuswahlLabel);
		
		String comboBoxListe[] = {"Raum erstellen", "Raumnamen �ndern", "folgenden Nutzer verwarnen"};
		JComboBox aufgabenAuswahl = new JComboBox(comboBoxListe);
		aufgabenFrame.add(aufgabenAuswahl);
		
		JTextField aufgabenTextfeld = new JTextField(null, 30);
		aufgabenTextfeld.setForeground(Color.blue);
		aufgabenTextfeld.setBackground(Color.yellow);
		aufgabenFrame.add(aufgabenTextfeld);
		
		JButton buttonOK = new JButton("OK");
		aufgabenFrame.add(buttonOK);
		
		JLabel aufgabenAuswertung = new JLabel();
		aufgabenFrame.add(aufgabenAuswertung);
		
		
		
		// Aufteilungsversuch
		JSplitPane splitpaneServer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitpaneServer.setLeftComponent(serverlog);
		splitpaneServer.setRightComponent(status);
	
		serverPanel.add(splitpaneServer);
		
		serverFrame.add(tabpane);
		
		
		// serverFrame.add(new JLabel("ein JLabel"));
		serverFrame.setVisible(true);
	}
	
	/*
	public void actionPerformed (ActionEvent ae) {
		if (ae.getSource() == this.buttonOK) {
			aufgabenAuswertung.setText("Du hast auf OK gedrückt.");
		}
	}
	*/
}

/*
public class RoomListModel extends DefaultListModel {
	public Object getElementAt(int index) {
		Room raum = (Room) super.getElementAt(index);
		return raum.membersOfRoom();
	}
	public void addRoom(Room raum) {
		if(!this.contains(raum)) {
			int i=0;
			while ( i<this.size() && (Room)this.get(i).getRang() <= ((Room)raum).getRang() ) {
				i++;
			}
			this.add(i, raum);
		}
	}
}
*/