package org.kku.jdiskusage.main;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class TestSwing
{
  public TestSwing()
  {
    start();
  }

  public void start()
  {
    JPanel pane;
    MigLayout layout;
    JFrame frame;

    layout = new MigLayout();
    pane = new JPanel();
    pane.setLayout(layout);
    pane.add("sizegroup test", new JButton("Text"));
    pane.add("sizegroup test", new JButton("Long text"));

    frame = new JFrame("Test");
    frame.setContentPane(pane);
    frame.setSize(new Dimension(600, 400));
    frame.show();
  }

  public static void main(String[] args)
  {
    new Test();
  }
}