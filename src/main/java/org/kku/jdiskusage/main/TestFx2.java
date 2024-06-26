package org.kku.jdiskusage.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class TestFx2
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    VBox pane;
    Scene scene;
    TextFlow textFlow;
    Text text;
    Label label;

    pane = new VBox();

    text = new Text("abcdefghijklmnopqrstuvwxyz");
    textFlow = new TextFlow(text);
    label = new Label("abcdefghijklmnopqrstuvwxyz");

    pane.getChildren().addAll(label, textFlow);

    text = new Text("abcdefghijklmnopqrstuvwxyz");
    text.setSmooth(true);
    text.setFontSmoothingType(FontSmoothingType.LCD);
    textFlow = new TextFlow(text);
    pane.getChildren().add(textFlow);

    scene = new Scene(pane);
    stage.setScene(scene);

    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}