package org.kku.jdiskusage.ui;

import java.io.FileInputStream;
import java.io.IOException;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.util.IconUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ImageViewExample
  extends Application
{
  @Override
  public void start(Stage stage) throws IOException
  {
    FlowPane root = new FlowPane();
    Font font;

    /*
    GlyphFontRegistry.register("materialdesign",
        ImageViewExample.class.getResourceAsStream("/font/materialdesignicons-webfont.ttf.ttf"), 16);
    
    font = GlyphFontRegistry.font("materialdesign");
    
    char[] text = Character.toChars(Integer.parseInt("F0C7C", 16));
    System.out.println(text);
    root.getChildren().add(new Button("", new Glyph("materialdesign", text)));
    */

    font = Font.loadFont(new FileInputStream(
        "/projecten/own/materialdesignicons/materialdesignicons/src/main/resources/font/materialdesignicons-webfont.ttf"),
        48);
    System.out.println(font);

    Label label = new Label();
    label.setFont(font);
    label.setText(new String(Character.toChars(Integer.parseInt("F0C7C", 16))));

    root.getChildren().add(label);
    root.getChildren().add(IconUtil.createImageNode("file-search", IconSize.VERY_LARGE));

    // Setting the Scene object
    Scene scene = new Scene(root, 595, 370);
    stage.setTitle("Displaying Image");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String args[])
  {
    launch(args);
  }
}
