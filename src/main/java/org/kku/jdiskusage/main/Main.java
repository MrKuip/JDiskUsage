package org.kku.jdiskusage.main;

import java.util.Locale;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.DiskUsageView;
import org.kku.jdiskusage.ui.util.IconUtil;
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
    Scene scene;

    Locale.setDefault(AppPreferences.localePreference.get());

    scene = new Scene(new DiskUsageView(stage));
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
    stage.getIcons().add(IconUtil.createImage("file-search", IconSize.SMALL));
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}
