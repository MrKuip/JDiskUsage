package org.kku.jdiskusage.main;

import java.nio.file.Files;
import java.nio.file.Path;
import org.kku.jdiskusage.ui.DiskUsageView;
import org.kku.jdiskusage.util.AppProperties;
import org.kku.jdiskusage.util.AppSettings.AppSetting;
import org.kku.jdiskusage.util.PathList;
import org.kku.jdiskusage.util.Translator;
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

    m_rootStage = stage;

    Translator.getInstance().changeLocale(AppPreferences.localePreference.get());

    diskUsageView = new DiskUsageView(stage);
    m_rootNode = diskUsageView.getContent();

    scene = new Scene(m_rootNode);
    scene.getStylesheets().add("jdiskusage.css");

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
    stage.show();

    getParameters().getRaw().stream().map(Path::of).filter(path -> Files.exists(path) && Files.isDirectory(path))
        .map(PathList::of).findFirst().ifPresent(diskUsageView::scanDirectory);
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

  private AppSetting<Double> getWidthProperty()
  {
    return AppProperties.WIDTH.forSubject(this);
  }

  private AppSetting<Double> getHeightProperty()
  {
    return AppProperties.HEIGHT.forSubject(this);
  }

  private AppSetting<Double> getXProperty()
  {
    return AppProperties.X.forSubject(this);
  }

  private AppSetting<Double> getYProperty()
  {
    return AppProperties.Y.forSubject(this);
  }
}
