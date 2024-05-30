package org.kku.jdiskusage.main;

import java.util.Locale;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.kku.jdiskusage.util.preferences.AppPreferences.AppPreference;
import javafx.application.Application;
import javafx.stage.Stage;

public class Test
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    AppPreference<Locale> p;

    p = AppPreferences.localePreference;
    System.out.println(p.getName() + " = " + p.get());
    p.set(Locale.CANADA);
    System.out.println(p.getName() + " = " + p.get());
    p.set(Locale.CHINA);
    System.out.println(p.getName() + " = " + p.get());
    p.set(new Locale("nl"));
    System.out.println(p.getName() + " = " + p.get());

    System.exit(-1);
  }

  public static void main(String[] args)
  {
    launch();
  }
}
