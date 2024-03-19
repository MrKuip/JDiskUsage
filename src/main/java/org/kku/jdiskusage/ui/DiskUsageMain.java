package org.kku.jdiskusage.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.jdiskusage.util.DiskUsageProperties;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.NodeIF;
import org.kku.util.SizeUtil;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DiskUsageMain
  extends Application
{
  private Stage m_stage;
  private BorderPane m_treePane;
  private TabPane m_tabPane;
  private Tab m_sizeTab;
  private Tab m_top50Tab;
  private Tab m_sizeDistTab;
  private Tab m_modifiedTab;
  private Tab m_typesTab;
  private RecentFilesMenu m_recentFiles = new RecentFilesMenu();

  @Override
  public void start(Stage stage)
  {
    BorderPane rootPane;
    VBox toolBars;
    SplitPane splitPane;
    Scene scene;

    m_stage = stage;
    rootPane = new BorderPane();

    toolBars = new VBox(createMenuBar());
    splitPane = new SplitPane();

    m_treePane = new BorderPane();
    m_tabPane = new TabPane();
    m_sizeTab = new Tab();
    m_sizeTab.setClosable(false);
    m_sizeTab.setText("Size");
    m_top50Tab = new Tab();
    m_top50Tab.setText("Top 50");
    m_top50Tab.setClosable(false);
    m_sizeDistTab = new Tab();
    m_sizeDistTab.setText("Size Dist");
    m_sizeDistTab.setClosable(false);
    m_modifiedTab = new Tab();
    m_modifiedTab.setText("Modified");
    m_modifiedTab.setClosable(false);
    m_typesTab = new Tab();
    m_typesTab.setText("Types");
    m_typesTab.setClosable(false);
    m_tabPane.getTabs().addAll(m_sizeTab, m_top50Tab, m_sizeDistTab, m_modifiedTab, m_typesTab);

    splitPane.getItems().addAll(m_treePane, m_tabPane);

    rootPane.setTop(toolBars);
    rootPane.setCenter(splitPane);

    scene = new Scene(rootPane, 300, 250);

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

      dirNode = new ScanFileTreeUI().chooseDirectory(m_stage);
      if (dirNode != null)
      {
        m_treePane.setCenter(createTreeTableView(dirNode));
        m_recentFiles.addFile(new File(dirNode.getName()));
      }
    });

    return menuItem;
  }

  private TreeTableView<NodeIF> createTreeTableView(DirNode dirNode)
  {
    FileTreeView fileTreeView;
    TreeTableView<NodeIF> treeTableView;

    fileTreeView = new FileTreeView(dirNode);
    treeTableView = fileTreeView.createComponent();
    treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
    {
      Tab selectedTab;
      TreeItem<NodeIF> selectedItem;

      selectedItem = treeTableView.getSelectionModel().getSelectedItem();
      selectedTab = m_tabPane.getSelectionModel().getSelectedItem();
      if (selectedTab == m_sizeTab)
      {
        fillSizeTab(selectedItem);
      }
    });

    return treeTableView;
  }

  private void fillSizeTab(TreeItem<NodeIF> treeItem)
  {
    if (!treeItem.getChildren().isEmpty())
    {
      PieChart chart;
      double sum;
      double totalSize;
      double minimumDataSize;

      totalSize = treeItem.getValue().getSize();
      minimumDataSize = totalSize * 0.05;

      chart = new PieChart();
      chart.setStartAngle(90.0);
      chart.getData().addAll(treeItem.getChildren().stream().filter(item ->
      {
        return item.getValue().getSize() > minimumDataSize;
      }).limit(10).map(item ->
      {
        return new PieChart.Data(item.getValue().getName(), item.getValue().getSize());
      }).collect(Collectors.toList()));

      if (chart.getData().size() != treeItem.getChildren().size())
      {
        sum = chart.getData().stream().map(t -> t.getPieValue()).reduce(0.0d, Double::sum);
        chart.getData().add(new PieChart.Data("Remainder", treeItem.getValue().getSize() - sum));
      }

      chart.getData().forEach(data -> data.nameProperty()
          .bind(Bindings.concat(data.getName(), "\n", SizeUtil.getFileSize(data.getPieValue()))));

      m_sizeTab.setContent(chart);
    }
  }

  private Menu createRecentFilesMenu()
  {
    return m_recentFiles.createMenu();
  }

  private class RecentFilesMenu
  {
    private List<Menu> m_listenerList = new ArrayList<>();

    public Menu createMenu()
    {
      Menu menu;

      menu = new Menu("Recent files");
      update(menu);
      m_listenerList.add(menu);

      return menu;
    }

    public void addFile(File file)
    {
      DiskUsageProperties.RECENT_FILES
          .setFileList(Stream.concat(Stream.of(file), DiskUsageProperties.RECENT_FILES.getFileList().stream())
              .distinct().limit(10).collect(Collectors.toList()));
      update();
    }

    private void update(Menu menu)
    {
      menu.getItems().clear();
      menu.getItems().addAll(getItems());
    }

    private void update()
    {
      m_listenerList.forEach(this::update);
    }

    private List<MenuItem> getItems()
    {
      return DiskUsageProperties.RECENT_FILES.getFileList().stream().map(File::getPath).map(this::createMenuItem)
          .collect(Collectors.toList());
    }

    private MenuItem createMenuItem(String path)
    {
      MenuItem menuItem;

      menuItem = new MenuItem(path);
      menuItem.setOnAction(e ->
      {
        DirNode dirNode;
        FileTreeView fileTreeView;

        dirNode = new ScanFileTreeUI().scanDirectory(new File(path));
        if (dirNode != null)
        {
          m_treePane.setCenter(createTreeTableView(dirNode));
          m_recentFiles.addFile(new File(dirNode.getName()));
        }
      });

      return menuItem;
    }
  }

  public static void main(String[] args)
  {
    launch();
  }
}
