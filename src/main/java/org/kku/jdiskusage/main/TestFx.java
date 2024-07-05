package org.kku.jdiskusage.main;

import org.tbee.javafx.scene.layout.MigPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TestFx
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    Scene scene;

    Button b = new Button("Press");
    scene = new Scene(getMigPane());
    scene.getStylesheets().add("jdiskusage.css");

    b.setOnAction((ae) -> scene.setRoot(getMigPane()));

    stage.setScene(scene);
    stage.show();
  }

  public MigPane getMigPane()
  {
    MigPane pane;

    pane = new MigPane("");
    pane.getStyleClass().add("haha");
    Button b = new Button("hello");
    pane.add(new Button("north"), "dock north");
    pane.add(new Button("west"), "dock west");
    pane.add(new Button("hello"), "sizegroup test");
    pane.add(new Button("Long text"), "sizegroup test");

    return pane;
  }

  public Pane getPane()
  {
    FlowPane pane;

    pane = new FlowPane();
    pane.getStyleClass().add("haha");
    pane.getChildren().add(new Button("north"));
    pane.getChildren().add(new Button("west"));
    pane.getChildren().add(new Button("hello"));
    pane.getChildren().add(new Button("Long text"));

    return pane;
  }

  public static void main(String[] args)
  {
    launch();
  }
}