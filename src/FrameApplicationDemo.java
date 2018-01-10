/**
 * 
 */

/**
 * @author Daphy_2
 *
 */

import java.awt.*;

public class FrameApplicationDemo{
	static Frame myFrame;
	static public void main(String argv[]){
		// Erzeugen eines Frames
		myFrame = new Frame("A simple Frame");
		myFrame.setSize( 300, 200 );
		// Anzeigen des Frames
		myFrame.setVisible(true);
		myFrame.add( "West", new Button(" I like Java "));
	}
}