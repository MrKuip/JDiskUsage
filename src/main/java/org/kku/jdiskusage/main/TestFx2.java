package org.kku.jdiskusage.main;

import java.io.InputStream;
import javafx.application.Application;
import javafx.stage.Stage;

public class TestFx2
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    String resourceName;

    resourceName = "META-INF/resources/webjars/flag-icon-css/4.1.7/flags/1x1/ad.svg";

    try (InputStream stream = getClass().getResourceAsStream(resourceName))
    {

    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void main(String[] args)
  {
    launch();
  }
}