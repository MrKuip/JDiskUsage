package org.kku.jdiskusage.main;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

public class TestSwing
{
  public TestSwing()
  {
  }

  public void start()
  {
    JPanel pane;
    MigLayout layout;
    JFrame frame;
    JButton button1;
    JButton button2;

    layout = new MigLayout("debug", "[][]", "[top]");
    pane = new JPanel();
    pane.setLayout(layout);

    button1 = new JButton("Button 1");
    button1.setFont(new Font("Arial", Font.PLAIN, 10));
    button2 = new JButton("Button 2");
    button2.setFont(new Font("Arial", Font.PLAIN, 20));

    pane.add(button1);
    pane.add(button2);

    frame = new JFrame("Test");
    frame.setContentPane(pane);
    frame.setSize(new Dimension(600, 400));
    frame.setVisible(true);
  }

  public static void main(String[] args)
  {
    new TestSwing().start();
  }
}