/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package Server;

/**
*
* @authoationDemo {r do26zeq
*/
import java.awt.*;
public class FrameApplicationDemo{
    static Frame myFrame;
    public static void test(){
        myFrame = new Frame("A simple Frame");
        myFrame.setSize( 300, 200 );
        myFrame.setVisible(true);
        myFrame.add("North", new Button("I like Java"));
    }
}