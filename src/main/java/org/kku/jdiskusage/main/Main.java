package org.kku.jdiskusage.main;

import java.nio.file.Files;
import java.nio.file.Path;
import org.kku.common.util.AppProperties.AppProperty;
import org.kku.common.util.Log;
import org.kku.common.util.SuppressFBWarnings;
import org.kku.fx.ui.util.ChartStyleSheet;
import org.kku.fx.ui.util.LogoUtil;
import org.kku.fx.ui.util.RootStage;
import org.kku.fx.util.FxProperty;
import org.kku.jdiskusage.ui.DiskUsageView;
import org.kku.jdiskusage.util.AppSettings;
import org.kku.jdiskusage.util.PathList;
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

  @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
  @Override
  public void start(Stage stage)
  {
    DiskUsageView diskUsageView;
    Scene scene;
    Rectangle2D defaultScreenBounds;

    RootStage.set(stage);
    LogoUtil.init(getClass());

    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
      Log.log.error(throwable, "%s %s", thread, throwable.getMessage());
    });

    diskUsageView = new DiskUsageView(stage);
    m_rootNode = diskUsageView.getContent();

    scene = new Scene(m_rootNode);
    scene.getStylesheets().add("module-resources/jdiskusage.css");
    ChartStyleSheet.getInstance().styleSheetProperty().addListener((_, oldValue, newValue) -> {
      scene.getStylesheets().remove(oldValue);
      scene.getStylesheets().add(newValue);
    });

    defaultScreenBounds = getDefaultScreenBounds();

    stage.setX(getXProperty().get(defaultScreenBounds.getMinX()));
    stage.setY(getYProperty().get(defaultScreenBounds.getMinY()));
    stage.setWidth(getWidthProperty().get(defaultScreenBounds.getWidth()));
    stage.setHeight(getHeightProperty().get(defaultScreenBounds.getHeight()));

    stage.xProperty().addListener(FxProperty.getChangeListener(getXProperty()));
    stage.yProperty().addListener(FxProperty.getChangeListener(getYProperty()));
    stage.widthProperty().addListener(FxProperty.getChangeListener(getWidthProperty()));
    stage.heightProperty().addListener(FxProperty.getChangeListener(getHeightProperty()));

    stage.getIcons().addAll(LogoUtil.getLogoList());
    stage.setTitle("JDiskUsage");
    stage.setScene(scene);
    stage.setOnCloseRequest((_) -> { Platform.exit(); System.exit(1); });
    stage.show();

    getParameters().getRaw().stream().map(Path::of).filter(path -> Files.exists(path) && Files.isDirectory(path))
        .map(PathList::of).findFirst().ifPresent(diskUsageView::scanDirectory);

    ChartStyleSheet.getInstance().refresh();
  }

  @SuppressFBWarnings(value = "MS_EXPOSE_REP")
  static public Node getRootNode()
  {
    return m_rootNode;
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
      Log.log.error(ex, "Main exits with exception");
    }
  }

  private AppProperty<Double> getWidthProperty()
  {
    return AppSettings.WIDTH.forSubject(this);
  }

  private AppProperty<Double> getHeightProperty()
  {
    return AppSettings.HEIGHT.forSubject(this);
  }

  private AppProperty<Double> getXProperty()
  {
    return AppSettings.X.forSubject(this);
  }

  private AppProperty<Double> getYProperty()
  {
    return AppSettings.Y.forSubject(this);
  }
}
