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
    Scene scene;
    MigPane pane;
    Button button1;
    Button button2;

    button1 = new Button("Button 1");
    button1.setStyle("-fx-font-size: 10");
    button2 = new Button("Button 1");
    button2.setStyle("-fx-font-size: 20");

    pane = new MigPane("debug", "", "top");
    pane.add(button1);
    pane.add(button2);

    scene = new Scene(pane);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}