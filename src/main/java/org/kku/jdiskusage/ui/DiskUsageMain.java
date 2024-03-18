package org.kku.jdiskusage.ui;

import org.kku.jdiskusage.util.FileTree.DirNode;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DiskUsageMain
  extends Application
{
  Pane m_content;

  @Override
  public void start(Stage stage)
  {
    Menu menu;
    MenuBar menuBar;
    MenuItem menuItem;
    VBox root;
    Scene scene;

    menuBar = new MenuBar();
    root = new VBox(menuBar);
    m_content = new Pane();
    root.getChildren().add(m_content);
    scene = new Scene(root, 300, 250);

    menu = new Menu("File");
    menuBar.getMenus().add(menu);

    menuItem = new MenuItem("Scan file tree");
    menu.getItems().add(menuItem);
    menuItem.setOnAction(e ->
    {
      DirNode dirNode;
      FileTreeView fileTreeView;

      dirNode = new ScanFileTreeUI().execute(stage);
      fileTreeView = new FileTreeView(dirNode);

      m_content.getChildren().add(fileTreeView.getComponent());
    });

    stage.setTitle("JDiskUsage");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args)
  {
    launch();
  }
}
