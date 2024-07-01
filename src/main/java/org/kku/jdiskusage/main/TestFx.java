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

    Button b = new Button("Press");
    scene = new Scene(getMigPane());

    b.setOnAction((ae) -> scene.setRoot(getMigPane()));

    stage.setScene(scene);
    stage.show();
  }

  public MigPane getMigPane()
  {
    MigPane pane;

    pane = new MigPane();
    Button b = new Button("hello");
    pane.add(new Button("hello"), "sizegroup test");
    pane.add(new Button("Long text"), "sizegroup test");

    return pane;
  }

  public static void main(String[] args)
  {
    launch();
  }
}