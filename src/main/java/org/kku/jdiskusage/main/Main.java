package org.kku.jdiskusage.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.kku.jdiskusage.ui.DiskUsageView;
import org.kku.jdiskusage.util.ApplicationPropertyExtensionIF;
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
    implements ApplicationPropertyExtensionIF
{
  static private Pane m_rootNode;

  @Override
  public void start(Stage stage)
  {
    DiskUsageView diskUsageView;
    Scene scene;
    Rectangle2D defaultScreenBounds;

    Locale.setDefault(AppPreferences.localePreference.get());

    diskUsageView = new DiskUsageView(stage);
    m_rootNode = diskUsageView.getContent();

    scene = new Scene(m_rootNode);
    scene.getStylesheets().add("jdiskusage.css");

    defaultScreenBounds = getDefaultScreenBounds();

    stage.setX(getProps().getDouble(Property.X, defaultScreenBounds.getMinX()));
    stage.setY(getProps().getDouble(Property.Y, defaultScreenBounds.getMinY()));
    stage.setWidth(getProps().getDouble(Property.WIDTH, defaultScreenBounds.getWidth()));
    stage.setHeight(getProps().getDouble(Property.HEIGHT, defaultScreenBounds.getHeight()));

    stage.heightProperty().addListener(getProps().getChangeListener(Property.HEIGHT));
    stage.widthProperty().addListener(getProps().getChangeListener(Property.WIDTH));
    stage.xProperty().addListener(getProps().getChangeListener(Property.X));
    stage.yProperty().addListener(getProps().getChangeListener(Property.Y));

    stage.setTitle("JDiskUsage");
    stage.setScene(scene);
    stage.show();

    getParameters().getRaw().stream().map(Path::of).filter(path -> {
      return Files.exists(path) && Files.isDirectory(path);
    }).findFirst().ifPresent(diskUsageView::scanDirectory);
  }

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
    launch(args);
  }
}
