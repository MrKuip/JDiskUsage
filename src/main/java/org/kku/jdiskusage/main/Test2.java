package org.kku.jdiskusage.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Test2
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    Scene scene;
    FlowPane pane;
    Button b1, b2;

    b1 = new Button("Native directoryChooser");
    b1.setOnAction((ae) -> {
      new DirectoryChooser().showDialog(stage);
    });

    b2 = new Button("Own directoryChooser");
    b2.setOnAction((ae) -> {
      new org.kku.jdiskusage.util.DirectoryChooser().showDialog(stage);
    });

    pane = new FlowPane();
    pane.getChildren().addAll(b1, b2);
    scene = new Scene(pane);

    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}
