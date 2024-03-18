package org.kku.jdiskusage.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DiskUsageMain
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    Menu menu;
    MenuBar menuBar;
    MenuItem menuItem;
    VBox root;
    Scene scene;

    menuBar = new MenuBar();

    menu = new Menu("File");
    menuBar.getMenus().add(menu);

    menuItem = new MenuItem("Scan file tree");
    menuItem.setOnAction(e -> new ScanFileTreeUI().execute(stage));
    menu.getItems().add(menuItem);

    root = new VBox(menuBar);

    scene = new Scene(root, 300, 250);

    stage.setTitle("JDiskUsage");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}
