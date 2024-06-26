package org.kku.jdiskusage.main;

import org.tbee.javafx.scene.layout.MigPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class TestFx
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    MigPane pane;
    Scene scene;

    pane = new MigPane();
    Button b = new Button("hello");
    pane.add(new Button("hello"), "sizegroup test");
    pane.add(new Button("Long text"), "sizegroup test");

    scene = new Scene(pane);
    stage.setScene(scene);

    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}