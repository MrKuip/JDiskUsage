package org.kku.jdiskusage.main;

import java.io.File;
import java.util.Locale;
import org.kku.jdiskusage.ui.DiskUsageView;
import org.kku.jdiskusage.util.ApplicationPropertyExtensionIF;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main
  extends Application
    implements ApplicationPropertyExtensionIF
{
  @Override
  public void start(Stage stage)
  {
    DiskUsageView diskUsageView;
    Scene scene;

    Locale.setDefault(AppPreferences.localePreference.get());

    diskUsageView = new DiskUsageView(stage);
    scene = new Scene(diskUsageView);
    scene.getStylesheets().add("jdiskusage.css");

    stage.setHeight(getProps().getDouble(Property.HEIGHT, 400));
    stage.setWidth(getProps().getDouble(Property.WIDTH, 600));
    stage.setX(getProps().getDouble(Property.X, 0));
    stage.setY(getProps().getDouble(Property.Y, 0));

    stage.heightProperty().addListener(getProps().getChangeListener(Property.HEIGHT));
    stage.widthProperty().addListener(getProps().getChangeListener(Property.WIDTH));
    stage.xProperty().addListener(getProps().getChangeListener(Property.X));
    stage.yProperty().addListener(getProps().getChangeListener(Property.Y));

    stage.setTitle("JDiskUsage");
    stage.setScene(scene);
    stage.show();

    getParameters().getRaw().stream().filter(p -> {
      File file = new File(p);
      return file.exists() && file.isDirectory();
    }).findFirst().ifPresent(diskUsageView::scanDirectory);
  }

  public static void main(String[] args)
  {
    launch(args);
  }
}
