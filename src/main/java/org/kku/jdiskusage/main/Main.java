package org.kku.jdiskusage.main;

import java.nio.file.Files;
import java.nio.file.Path;
import org.kku.jdiskusage.ui.DiskUsageView;
import org.kku.jdiskusage.ui.util.ChartStyleSheet;
import org.kku.jdiskusage.util.AppSettings;
import org.kku.jdiskusage.util.AppProperties.AppSetting;
import org.kku.jdiskusage.util.Log;
import org.kku.jdiskusage.util.PathList;
import org.kku.jdiskusage.util.SuppressFBWarnings;
import org.kku.jdiskusage.util.Translator;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main
  extends Application
{
  static private Pane m_rootNode;
  static private Stage m_rootStage;

  @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
  @Override
  public void start(Stage stage)
  {
    DiskUsageView diskUsageView;
    Scene scene;
    Rectangle2D defaultScreenBounds;

    m_rootStage = stage;

    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
      Log.log.error(throwable, "%s %s", thread, throwable.getMessage());
    });

    Translator.getInstance().changeLocale(AppPreferences.localePreference.get());

    diskUsageView = new DiskUsageView(stage);
    m_rootNode = diskUsageView.getContent();

    scene = new Scene(m_rootNode);
    scene.getStylesheets().add("jdiskusage.css");
    scene.getStylesheets().add(new ChartStyleSheet().getStyleSheet());

    defaultScreenBounds = getDefaultScreenBounds();

    stage.setX(getXProperty().get(defaultScreenBounds.getMinX()));
    stage.setY(getYProperty().get(defaultScreenBounds.getMinY()));
    stage.setWidth(getWidthProperty().get(defaultScreenBounds.getWidth()));
    stage.setHeight(getHeightProperty().get(defaultScreenBounds.getHeight()));

    stage.xProperty().addListener(getXProperty().getChangeListener());
    stage.yProperty().addListener(getYProperty().getChangeListener());
    stage.widthProperty().addListener(getWidthProperty().getChangeListener());
    stage.heightProperty().addListener(getHeightProperty().getChangeListener());

    stage.setTitle("JDiskUsage");
    stage.setScene(scene);
    stage.setOnCloseRequest((cr) -> { Platform.exit(); System.exit(1); });
    stage.show();

    getParameters().getRaw().stream().map(Path::of).filter(path -> Files.exists(path) && Files.isDirectory(path))
        .map(PathList::of).findFirst().ifPresent(diskUsageView::scanDirectory);
  }

  @SuppressFBWarnings(value = "MS_EXPOSE_REP")
  static public Node getRootNode()
  {
    return m_rootNode;
  }

  @SuppressFBWarnings(value = "MS_EXPOSE_REP")
  static public Stage getRootStage()
  {
    return m_rootStage;
  }

  private Rectangle2D getDefaultScreenBounds()
  {
    Rectangle2D screenBounds;

    screenBounds = Screen.getPrimary().getBounds();

    return new Rectangle2D(screenBounds.getMinX() + (screenBounds.getWidth() * 0.1),
        screenBounds.getMinY() + (screenBounds.getHeight() * 0.1), screenBounds.getWidth() * 0.8,
        screenBounds.getHeight() * 0.8);
  }

  public static void main(String[] args)
  {
    try
    {
      launch(args);
    }
    catch (Throwable ex)
    {
      ex.printStackTrace();
    }
  }

  private AppSetting<Double> getWidthProperty()
  {
    return AppSettings.WIDTH.forSubject(this);
  }

  private AppSetting<Double> getHeightProperty()
  {
    return AppSettings.HEIGHT.forSubject(this);
  }

  private AppSetting<Double> getXProperty()
  {
    return AppSettings.X.forSubject(this);
  }

  private AppSetting<Double> getYProperty()
  {
    return AppSettings.Y.forSubject(this);
  }
}
