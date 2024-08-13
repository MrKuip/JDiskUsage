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

    scene = new Scene(getNode());
    stage.setTitle("JDiskUsage");
    stage.setScene(scene);
    stage.setHeight(200);
    stage.setWidth(600);
    stage.show();
  }

  private MigPane getNode()
  {
    MigPane pane;

    pane = new MigPane("wrap 4, debug", "[][][]push[align right]", "[][]push[]");

    pane.add(new Button("123"));
    pane.add(new Button("123"));
    pane.add(new Button("123"));
    pane.add(new Button("123"));
    pane.add(new Button("123"));
    pane.add(new Button("123"));
    pane.add(new Button("123"));
    pane.add(new Button("123"));

    return pane;
  }

  public static void main(String[] args)
  {
    launch();
  }
}