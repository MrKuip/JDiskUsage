package org.kku.jdiskusage.ui;

import java.io.File;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.jdiskusage.util.DiskUsageProperties;
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
  private Stage m_stage;
  private Pane m_content;

  @Override
  public void start(Stage stage)
  {
    VBox root;
    Scene scene;

    m_stage = stage;

    root = new VBox(createMenuBar());
    m_content = new Pane();
    root.getChildren().add(m_content);
    scene = new Scene(root, 300, 250);

    stage.setTitle("JDiskUsage");
    stage.setScene(scene);
    stage.show();
  }

  private MenuBar createMenuBar()
  {
    MenuBar menuBar;
    Menu menu;

    menuBar = new MenuBar();

    menu = new Menu("File");
    menu.getItems().addAll(createScanFileTreeMenuItem(), createRecentFilesMenu());
    menuBar.getMenus().add(menu);

    return menuBar;
  }

  private MenuItem createScanFileTreeMenuItem()
  {
    MenuItem menuItem;

    menuItem = new MenuItem("Scan file tree");
    menuItem.setOnAction(e ->
    {
      DirNode dirNode;
      FileTreeView fileTreeView;

      dirNode = new ScanFileTreeUI().execute(m_stage);
      fileTreeView = new FileTreeView(dirNode);
      m_content.getChildren().add(fileTreeView.getComponent());

      DiskUsageProperties.RECENT_FILES.setFileList(
          Stream.concat(Stream.of(new File(dirNode.getName())), DiskUsageProperties.RECENT_FILES.getFileList().stream())
              .distinct().limit(10).collect(Collectors.toList()));
    });

    return menuItem;
  }

  private Menu createRecentFilesMenu()
  {
    Menu menu;

    menu = new Menu("Recent files");
    menu.setOnShowing((e) ->
    {
      System.out.println("on showing");
      ((Menu) e.getSource()).getItems().addAll(DiskUsageProperties.RECENT_FILES.getFileList().stream()
          .map(File::getPath).map(MenuItem::new).collect(Collectors.toList()));
    });
    menu.setOnAction((e) ->
    {
      System.out.println(e);
    });

    return menu;
  }

  public static void main(String[] args)
  {
    launch();
  }
}
