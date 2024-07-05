package org.kku.jdiskusage.main;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.IconFont;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TestFx2
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    FlowPane pane;
    Scene scene;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Glyph g;
    Font font;
    Label label;
    Text text;
    double size;

    size = 30.0;

    font = IconFont.MATERIAL_DESIGN.getIconFont(size);
    label = new Label("󱃥");
    label.setFont(font);

    text = new Text("󱃥");
    text.setFont(font);

    g = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_LEFT);
    g.setFontSize(30);

    pane = new FlowPane();
    button1 = new Button("", new FxIcon("filter-menu").size(size).fillColor(Color.BLACK).getImageView());
    button2 = new Button("", g);
    button3 = new Button("", label);
    button4 = new Button("", text);

    pane.getChildren().addAll(button1, button4, button2, button3);

    scene = new Scene(pane);
    stage.setScene(scene);

    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}