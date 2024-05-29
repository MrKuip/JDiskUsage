package org.kku.jdiskusage.main;

import java.util.Locale;
import org.kku.jdiskusage.util.Translator;
import javafx.application.Application;
import javafx.stage.Stage;

public class Test
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    Locale.setDefault(new Locale("nl"));

    System.out.println(Translator.translatedTextProperty("hallo daar").get());
    System.out.println(Translator.translatedTextProperty("Show file size").get());
    System.out.println(Translator.translatedTextProperty("Show-file-size").get());

    System.exit(-1);
  }

  public static void main(String[] args)
  {
    launch();
  }
}
