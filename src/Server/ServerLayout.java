package Server;

import javax.swing.*;

public class ServerLayout {
    private JLabel serverlogInfo;
    private JTabbedPane tabbedPane1;
    private JList roomList;
    private JList userList;
    private JTextField textField1;
    private JComboBox comboBox1;
    private JButton button1;
    private JTextArea textArea1;
    private JPanel ROOT;
    private JMenuBar bar;
    private JMenu Server;
    private JMenu Benutzer;
    private JMenu Raum;
    private JMenu Operationen;

    private static ServerLayout INSTANCE;

    public static void main(String[] args) {
        INSTANCE = new ServerLayout();
        INSTANCE.start_gui();
    }

    public void start_gui() {
        JFrame frame = new JFrame("ServerLayout");
        frame.setContentPane(this.ROOT);

        bar = new JMenuBar();
        Server = new JMenu("Server");
        Benutzer = new JMenu("Benutzer");
        Raum = new JMenu("Server.Raum");
        Operationen = new JMenu("Operationen");

        /*fileNew = new JMenuItem("New File");
        fileOpen = new JMenuItem("Open File");
        fileSave = new JMenuItem("Save File");
        fileExit = new JMenuItem("Exit");
        imgBtn1 = new JMenuItem("Useless Button");
        hlpAbout = new JMenuItem("About this program");*/

        //frame.add(bar, new BorderLayout().PAGE_START);
        bar.add(Server);
        bar.add(Benutzer);
        bar.add(Raum);
        bar.add(Operationen);

        /*Server.Server.add(fileNew);
        Server.Server.add(fileOpen);
        Server.Server.add(fileSave);
        Server.Server.add(fileExit);
        Benutzer.add(imgBtn1);
        Server.Raum.add(hlpAbout);*/

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
