package org.kku.jdiskusage.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.kku.jdiskusage.ui.DiskUsageView;
import org.kku.jdiskusage.util.AppProperties;
import org.kku.jdiskusage.util.AppSettings.AppSetting;
import org.kku.jdiskusage.util.DirectoryChooser.PathList;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import javafx.application.Application;
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

  @Override
  public void start(Stage stage)
  {
    DiskUsageView diskUsageView;
    Scene scene;
    Rectangle2D defaultScreenBounds;
    AppSetting<Double> xProperty;
    AppSetting<Double> yProperty;
    AppSetting<Double> widthProperty;
    AppSetting<Double> heightProperty;

    m_rootStage = stage;

    Locale.setDefault(AppPreferences.localePreference.get());

    diskUsageView = new DiskUsageView(stage);
    m_rootNode = diskUsageView.getContent();

    scene = new Scene(m_rootNode);
    scene.getStylesheets().add("jdiskusage.css");

    defaultScreenBounds = getDefaultScreenBounds();

    xProperty = AppProperties.X.forSubject(Main.this);
    yProperty = AppProperties.Y.forSubject(Main.this);
    widthProperty = AppProperties.WIDTH.forSubject(Main.this);
    heightProperty = AppProperties.HEIGHT.forSubject(Main.this);

    stage.setX(xProperty.get(defaultScreenBounds.getMinX()));
    stage.setY(AppProperties.Y.forSubject(Main.this).get(defaultScreenBounds.getMinY()));
    stage.setWidth(AppProperties.WIDTH.forSubject(Main.this).get(defaultScreenBounds.getWidth()));
    stage.setHeight(AppProperties.HEIGHT.forSubject(Main.this).get(defaultScreenBounds.getHeight()));

    stage.xProperty().addListener(xProperty.getChangeListener());
    stage.yProperty().addListener(yProperty.getChangeListener());
    stage.widthProperty().addListener(widthProperty.getChangeListener());
    stage.heightProperty().addListener(heightProperty.getChangeListener());

    //stage.xProperty().addListener((a, b, c) -> { System.out.println("changed 1!"); });
    //stage.xProperty().addListener((a, b, c) -> { System.out.println("changed 3!"); });
    //stage.xProperty().asObject().addListener((a, b, c) -> { System.out.println("changed 2!"); });

    stage.setTitle("JDiskUsage");
    stage.setScene(scene);
    stage.show();

    getParameters().getRaw().stream().map(Path::of).filter(path -> {
      return Files.exists(path) && Files.isDirectory(path);
    }).map(PathList::of).findFirst().ifPresent(diskUsageView::scanDirectory);
  }

  static public Node getRootNode()
  {
    return m_rootNode;
  }

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
    launch(args);
  }
}
