package org.kku.jdiskusage.main;

import org.kku.jdiskusage.ui.util.ColorPalette;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TestFx
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    Scene scene;

    scene = new Scene(getColorPane());

    stage.setTitle("TextFx");
    stage.setScene(scene);
    stage.setWidth(800.0);
    stage.setHeight(400.0);
    stage.setOnCloseRequest((cr) -> { Platform.exit(); System.exit(1); });
    stage.show();
  }

  private Pane getColorPane()
  {
    MigPane pane;
    double maxDepth;

    maxDepth = 20;

    pane = new MigPane("wrap " + ColorPalette.getColorList().size() + ", gap 0, fill", "", "");
    for (int depth = 0; depth < 20; depth++)
    {
      double brightness;

      brightness = (depth / maxDepth);
      ColorPalette.getColorList().stream().forEach(color -> {
        Label label;
        label = new Label("");
        label.setStyle(color.getBackgroundCss(brightness));

        pane.add(label, "grow");
      });
    }

    return pane;
  }

  public static void main(String[] args)
  {
    launch();
  }
}