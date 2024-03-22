package org.kku.jdiskusage.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.controlsfx.control.BreadCrumbBar;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.DiskUsageProperties;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.SizeUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class DiskUsageMain
  extends Application
{
  private Stage m_stage;
  private TreePaneData m_treePaneData = new TreePaneData();
  private TabPaneData m_tabPaneData = new TabPaneData();
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

    Stream.of(TabPaneData.TabData.values()).forEach(td -> m_tabPaneData.createTab(td));

    splitPane.getItems().addAll(m_treePaneData.getNode(), m_tabPaneData.getNode());
    splitPane.getDividers().get(0).positionProperty()
        .addListener(DiskUsageProperties.SPLIT_PANE_POSITION.getChangeListener());
    SplitPane.setResizableWithParent(m_treePaneData.getNode(), false);
    SplitPane.setResizableWithParent(m_tabPaneData.getNode(), false);
    splitPane.getDividers().get(0).setPosition(DiskUsageProperties.SPLIT_PANE_POSITION.getDouble(25.0));

    rootPane.setTop(toolBars);
    rootPane.setCenter(splitPane);

    scene = new Scene(rootPane);

    stage.setHeight(DiskUsageProperties.SCENE_HEIGHT.getDouble(400));
    stage.setWidth(DiskUsageProperties.SCENE_WIDTH.getDouble(600));
    stage.setX(DiskUsageProperties.SCENE_LOCATION_X.getDouble(0));
    stage.setY(DiskUsageProperties.SCENE_LOCATION_Y.getDouble(0));

    stage.widthProperty().addListener(DiskUsageProperties.SCENE_WIDTH.getChangeListener());
    stage.heightProperty().addListener(DiskUsageProperties.SCENE_HEIGHT.getChangeListener());
    stage.xProperty().addListener(DiskUsageProperties.SCENE_LOCATION_X.getChangeListener());
    stage.yProperty().addListener(DiskUsageProperties.SCENE_LOCATION_Y.getChangeListener());

    m_tabPaneData.getNode().setTop(m_treePaneData.createBreadCrumbBar());

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
    menuItem.setGraphic(IconUtil.createImageNode("file-search", IconSize.SMALLER));
    menuItem.setOnAction(e ->
    {
      DirNode dirNode;

      dirNode = new ScanFileTreeDialog().chooseDirectory(m_stage);
      if (dirNode != null)
      {
        m_treePaneData.createTreeTableView(dirNode);
        m_recentFiles.addFile(new File(dirNode.getName()));
      }
    });

    return menuItem;
  }

  private Menu createRecentFilesMenu()
  {
    return m_recentFiles.createMenu();
  }

  private class TreePaneData
  {
    private BorderPane mi_treePane;
    private BreadCrumbBar<FileNodeIF> mi_breadCrumbBar;
    private TreeTableView<FileNodeIF> mi_treeTableView;

    public TreePaneData()
    {
      mi_treePane = new BorderPane();
    }

    public BorderPane getNode()
    {
      return mi_treePane;
    }

    public TreeTableView<FileNodeIF> getTreeTableView()
    {
      return mi_treeTableView;
    }

    public void createTreeTableView(DirNode dirNode)
    {
      FileTreeView fileTreeView;

      fileTreeView = new FileTreeView(dirNode);
      mi_treeTableView = fileTreeView.createComponent();
      mi_treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
      {
        m_tabPaneData.setSelected(mi_treeTableView.getSelectionModel().getSelectedItem());
      });
      mi_breadCrumbBar.selectedCrumbProperty().bind(mi_treeTableView.getSelectionModel().selectedItemProperty());
      mi_breadCrumbBar.setAutoNavigationEnabled(false);
      mi_breadCrumbBar.setOnCrumbAction((e) ->
      {
        select(e.getSelectedCrumb());
      });

      Platform.runLater(() -> mi_treeTableView.getSelectionModel().select(0));

      mi_treePane.setCenter(mi_treeTableView);
    }

    public TreeItem<FileNodeIF> getSelectedItem()
    {
      if (mi_treeTableView == null)
      {
        return null;
      }
      return mi_treeTableView.getSelectionModel().getSelectedItem();
    }

    public void select(TreeItem<FileNodeIF> treeItem)
    {
      if (mi_treeTableView != null)
      {
        // Make sure the path to select is expanded, because selection alone doesn't
        // expand the tree
        TreeItem<FileNodeIF> parentTreeItem = treeItem;
        while ((parentTreeItem = parentTreeItem.getParent()) != null)
        {
          parentTreeItem.setExpanded(true);
        }

        mi_treeTableView.getSelectionModel().select(treeItem);

        // Scroll to the selected item to make sure it is visible for the user
        mi_treeTableView.scrollTo(mi_treeTableView.getSelectionModel().getSelectedIndex());
      }
    }

    public BreadCrumbBar<FileNodeIF> createBreadCrumbBar()
    {
      mi_breadCrumbBar = new BreadCrumbBar<>();
      return mi_breadCrumbBar;
    }
  }

  private class TabPaneData
  {
    private enum TabData
    {
      SIZE("Size", "chart-pie", TabPaneData::fillSizeTab),
      TOP50("Top 50", "trophy", TabPaneData::fillTop50Tab),
      SIZE_DISTRIBUTION("Size Dist", "chart-bell-curve", TabPaneData::fillSizeDistributionTab),
      MODIFIED("Modified", "sort-calendar-ascending", TabPaneData::fillSizeTab),
      TYPES("Types", "chart-bell-curve", TabPaneData::fillSizeTab);

      private final String m_name;
      private final String m_iconName;
      private final BiFunction<TreePaneData, TreeItem<FileNodeIF>, Node> m_fillContent;

      private TabData(String name, String iconName, BiFunction<TreePaneData, TreeItem<FileNodeIF>, Node> fillContent)
      {
        m_name = name;
        m_iconName = iconName;
        m_fillContent = fillContent;
      }

      public String getName()
      {
        return m_name;
      }

      public String getIconName()
      {
        return m_iconName;
      }

      public Tab createTab()
      {
        Tab tab;

        tab = new Tab();
        tab.setClosable(false);
        tab.setText(getName());
        if (getIconName() != null)
        {
          tab.setGraphic(IconUtil.createImageNode(getIconName(), IconSize.SMALLER));
        }

        return tab;
      }

      Node fillContent(TreePaneData treePaneData, TreeItem<FileNodeIF> selectedItem)
      {
        return m_fillContent.apply(treePaneData, selectedItem);
      }
    }

    private BorderPane mi_borderPane;
    private TabPane mi_tabPane;
    private Map<TabData, Tab> mi_tabByTabId = new HashMap<>();
    private Map<TabData, Node> mi_contentByTabId = new HashMap<>();

    private TabPaneData()
    {
      mi_tabPane = new TabPane();
      mi_tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) ->
      {
        fillContent(newTab, m_treePaneData.getSelectedItem());
      });
      mi_borderPane = new BorderPane();
      mi_borderPane.setCenter(mi_tabPane);
    }

    public BorderPane getNode()
    {
      return mi_borderPane;
    }

    public Tab createTab(TabData tabData)
    {
      return mi_tabByTabId.computeIfAbsent(tabData, td ->
      {
        Tab tab;
        tab = td.createTab();
        mi_tabPane.getTabs().add(tab);
        return tab;
      });
    }

    public void fillContent(Tab tab, TreeItem<FileNodeIF> selectedItem)
    {
      Optional<Entry<TabData, Tab>> tabEntry;

      tabEntry = mi_tabByTabId.entrySet().stream().filter(entry -> tab.equals(entry.getValue())).findFirst();
      if (tabEntry.isPresent() && selectedItem != null)
      {
        TabData tabData;

        tabData = tabEntry.get().getKey();
        tab.setContent(mi_contentByTabId.computeIfAbsent(tabData,
            td -> tabEntry.get().getKey().fillContent(m_treePaneData, selectedItem)));
      }
      else
      {
        tab.setContent(new Label(""));
      }
    }

    public void setSelected(TreeItem<FileNodeIF> selectedItem)
    {
      Tab selectedTab;

      mi_contentByTabId.clear();

      selectedTab = mi_tabPane.getSelectionModel().getSelectedItem();
      m_tabPaneData.fillContent(selectedTab, selectedItem);
    }

    private static Node fillSizeTab(TreePaneData treePaneData, TreeItem<FileNodeIF> treeItem)
    {
      if (!treeItem.getChildren().isEmpty())
      {
        PieChart chart;
        double sum;
        double totalSize;
        double minimumDataSize;

        totalSize = treeItem.getValue().getSize();
        minimumDataSize = totalSize * 0.05;

        record Data(PieChart.Data pieChartData, TreeItem<FileNodeIF> treeItem) {
        }

        chart = new PieChart();
        chart.setStartAngle(90.0);
        treeItem.getChildren().stream().filter(item ->
        {
          return item.getValue().getSize() > minimumDataSize;
        }).limit(10).map(item ->
        {
          PieChart.Data data;

          data = new PieChart.Data(item.getValue().getName(), item.getValue().getSize());
          data.nameProperty().bind(Bindings.concat(data.getName(), "\n", SizeUtil.getFileSize(data.getPieValue())));

          return new Data(data, item);
        }).forEach(tuple ->
        {
          chart.getData().add(tuple.pieChartData);
          tuple.pieChartData.getNode().setUserData(tuple.treeItem);
          tuple.pieChartData.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED, (me) ->
          {
            treePaneData.select(tuple.treeItem);
            // treePaneData.select(4);
          });
        });

        if (chart.getData().size() != treeItem.getChildren().size())
        {
          sum = chart.getData().stream().map(data -> data.getPieValue()).reduce(0.0d, Double::sum);
          chart.getData().add(new PieChart.Data("Remainder", treeItem.getValue().getSize() - sum));
        }

        return chart;
      }

      return new Label("No data");
    }

    private static Node fillTop50Tab(TreePaneData treePaneData, TreeItem<FileNodeIF> treeItem)
    {
      if (!treeItem.getChildren().isEmpty())
      {
        FileNodeIF node;
        ObservableList<FileNodeIF> list;
        TableView<FileNodeIF> table;
        TableColumn<FileNodeIF, Short> rankColumn;
        TableColumn<FileNodeIF, String> nameColumn;
        TableColumn<FileNodeIF, Long> fileSizeColumn;
        TableColumn<FileNodeIF, Date> modifiedColumn;
        TableColumn<FileNodeIF, String> pathColumn;

        node = treeItem.getValue();
        list = node.streamNode().filter(FileNodeIF::isFile).sorted(FileNodeIF.getSizeComparator()).limit(50)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

        list.forEach(n ->
        {
          System.out.printf("%9d %s%n", n.getSize(), n.getName());
        });

        table = new TableView<>();
        table.setEditable(false);

        rankColumn = new TableColumn<>("No");
        nameColumn = new TableColumn<>("Name");
        fileSizeColumn = new TableColumn<>("File size");
        modifiedColumn = new TableColumn<>("Modified");
        pathColumn = new TableColumn<>("path");

        table.getColumns().add(rankColumn);
        table.getColumns().add(nameColumn);
        table.getColumns().add(fileSizeColumn);
        table.getColumns().add(modifiedColumn);
        table.getColumns().add(pathColumn);

        rankColumn.setCellValueFactory(new Callback<CellDataFeatures<FileNodeIF, Short>, ObservableValue<Short>>()
        {
          @Override
          public ObservableValue<Short> call(CellDataFeatures<FileNodeIF, Short> p)
          {
            return new ReadOnlyObjectWrapper<Short>(Short.valueOf((short) 1));
          }
        });

        nameColumn.setCellValueFactory(new Callback<CellDataFeatures<FileNodeIF, String>, ObservableValue<String>>()
        {
          @Override
          public ObservableValue<String> call(CellDataFeatures<FileNodeIF, String> p)
          {
            return new ReadOnlyObjectWrapper<String>(p.getValue().getName());
          }
        });

        fileSizeColumn.setCellValueFactory(new Callback<CellDataFeatures<FileNodeIF, Long>, ObservableValue<Long>>()
        {
          @Override
          public ObservableValue<Long> call(CellDataFeatures<FileNodeIF, Long> p)
          {
            return new ReadOnlyObjectWrapper<Long>(p.getValue().getSize());
          }
        });
        table.setItems(list);

        return table;
      }

      return new Label("No data");
    }

    private static Node fillSizeDistributionTab(TreePaneData treePaneData, TreeItem<FileNodeIF> treeItem)
    {
      if (!treeItem.getChildren().isEmpty())
      {
        FileNodeIF node;
        Map<SizeDistributionBucket, Long> map;

        node = treeItem.getValue();
        map = node.streamNode().filter(FileNodeIF::isFile).map(FileNodeIF::getSize)
            .collect(Collectors.groupingBy(SizeDistributionBucket::findBucket, Collectors.counting()));

        map.entrySet().forEach(System.out::println);
      }

      return new Label("No data");
    }
  }

  private enum SizeDistributionBucket
  {
    INVALID("Invalid", -Double.MAX_VALUE, 0),
    SIZE_0_KB_TO_1_KB("0 KB - 1 KB", kilo_bytes(0), kilo_bytes(1)),
    SIZE_1_KB_TO_4_KB("1 KB - 4 KB", kilo_bytes(1), kilo_bytes(4)),
    SIZE_4_KB_TO_16_KB("4 KB - 16 KB", kilo_bytes(4), kilo_bytes(16)),
    SIZE_16_KB_TO_64_KB("16 KB - 64 KB", kilo_bytes(16), kilo_bytes(64)),
    SIZE_64_KB_TO_256_KB("64 KB - 256 KB", kilo_bytes(64), kilo_bytes(256)),
    SIZE_256_KB_TO_1_MB("256 KB - 1 MB", kilo_bytes(256), mega_bytes(1)),
    SIZE_1_MB_TO_4_MB("1 MB - 4 MB", mega_bytes(1), mega_bytes(4)),
    SIZE_4_MB_TO_16_MB("4 MB - 16 MB", mega_bytes(4), mega_bytes(16)),
    SIZE_16_MB_TO_64_MB("16 MB - 64 MB", mega_bytes(16), mega_bytes(64)),
    SIZE_64_MB_TO_256_MB("64 MB - 256 MB", mega_bytes(64), mega_bytes(256)),
    SIZE_256_MB_TO_1_GB("256 MB - 1 GB", mega_bytes(256), giga_bytes(1)),
    SIZE_1_GB_TO_4_GB("1 GB - 4 GB", giga_bytes(1), giga_bytes(4)),
    SIZE_4_GB_TO_16_GB("4 GB - 16 GB", giga_bytes(4), giga_bytes(16)),
    SIZE_OVER_16_GB("Over 16 GB", giga_bytes(16), Double.MAX_VALUE);

    private final String mi_text;
    private final double mi_from;
    private final double mi_to;

    SizeDistributionBucket(String text, double from, double to)
    {
      mi_text = text;
      mi_from = from;
      mi_to = to;
    }

    public String getText()
    {
      return mi_text;
    }

    double getFrom()
    {
      return mi_from;
    }

    double getTo()
    {
      return mi_to;
    }

    static public SizeDistributionBucket findBucket(long value)
    {
      return Stream.of(values()).filter(bucket -> value > bucket.getFrom() && value < bucket.getTo()).findFirst()
          .orElse(INVALID);
    }

    static private double kilo_bytes(double value)
    {
      return 1000 * value;
    }

    static private double mega_bytes(double value)
    {
      return kilo_bytes(value) * 1000;
    }

    static private double giga_bytes(double value)
    {
      return mega_bytes(value) * 1000;
    }
  }

  private class RecentFilesMenu
  {
    private List<Menu> m_listenerList = new ArrayList<>();

    public Menu createMenu()
    {
      Menu menu;

      menu = new Menu("Recent scans");
      menu.setGraphic(IconUtil.createImageNode("history", IconSize.SMALLER));
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
      menuItem.setGraphic(IconUtil.createImageNode("folder-outline", IconSize.SMALLER));
      menuItem.setOnAction(e ->
      {
        DirNode dirNode;

        dirNode = new ScanFileTreeDialog().scanDirectory(new File(path));
        if (dirNode != null)
        {
          m_treePaneData.createTreeTableView(dirNode);
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
