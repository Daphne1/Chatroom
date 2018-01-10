import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class SwingApplication {
	private static String labelPrefix = "Number of button clicks: ";
	private int numClicks = 0;
	
	public Component createComponents() {
		// erzeuge Basiskomponente label vom Typ JLabel
		final JLabel label = new JLabel (labelPrefix + "0");
		// erzeuge Basiskomponente button vom Typ JButton
		JButton button = new JButton("I'm a Swing button!");
		button.setMnemonic(KeyEvent.VK_I);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				numClicks++;
				label.setText(labelPrefix + numClicks);
			}
		});
		// erzeuge Container pane vom Typ JPanel
		JPanel pane = new JPanel();
		// erzeuge Layoutmanager für Container
		pane.setLayout(new GridLayout(0, 1));
		pane.add(label);
		pane.add(button);
		return pane;
	}
	
	public static void main (String[] args) {
		// top-level-container
		JFrame frame = new JFrame("SwingApplication");
		SwingApplication app = new SwingApplication();
		
		Component contents = app.createComponents();
		frame.getContentPane().add(contents, BorderLayout.EAST);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.pack();
		frame.setVisible(true);
	}
}

// public class MainFrame extends javax.swing.JFrame {
	
// }