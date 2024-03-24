package org.kku.jdiskusage.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.SegmentedButton;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.CommonUtil;
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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class DiskUsageMain
  extends Application
{
  private Stage m_stage;
  private TreePaneData m_treePaneData = new TreePaneData();
  private TabPaneData m_tabPaneData = new TabPaneData();
  private LastModifiedDistributionPane m_modifiedDistributionTab = new LastModifiedDistributionPane();
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
    stage.getIcons().add(IconUtil.createImage("file-search", IconSize.SMALL));
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
    menuItem.setOnAction(e -> {
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
      mi_treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        m_tabPaneData.setSelected(mi_treeTableView.getSelectionModel().getSelectedItem());
      });
      mi_breadCrumbBar.selectedCrumbProperty().bind(mi_treeTableView.getSelectionModel().selectedItemProperty());
      mi_breadCrumbBar.setAutoNavigationEnabled(false);
      mi_breadCrumbBar.setOnCrumbAction((e) -> {
        select(e.getSelectedCrumb());
      });

      Platform.runLater(() -> {
        mi_treeTableView.getSelectionModel().select(0);
        mi_treeTableView.getSelectionModel().getSelectedItem().setExpanded(true);
      });

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

  private static class TabPaneData
  {
    private enum TabData
    {
      SIZE("Size", "chart-pie", TabPaneData::fillSizeTab),
      TOP50("Top 50", "trophy", TabPaneData::fillTop50Tab),
      DISTRIBUTION_SIZE("Size Dist", "chart-bell-curve", TabPaneData::fillSizeDistributionTab),
      DISTRIBUTION_MODIFIED("Modified", "sort-calendar-ascending", TabPaneData::fillModifiedDistributionTab),
      DISTRIBUTION_TYPES("Types", "chart-pie", TabPaneData::fillTypeDistributionTab);

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
      mi_tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
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
      return mi_tabByTabId.computeIfAbsent(tabData, td -> {
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
        minimumDataSize = totalSize * 0.02;

        record Data(PieChart.Data pieChartData, TreeItem<FileNodeIF> treeItem) {}

        chart = FxUtil.createPieChart();
        treeItem.getChildren().stream().filter(item -> {
          return item.getValue().getSize() > minimumDataSize;
        }).limit(10).map(item -> {
          PieChart.Data data;

          data = new PieChart.Data(item.getValue().getName(), item.getValue().getSize());
          data.nameProperty().bind(Bindings.concat(data.getName(), "\n", SizeUtil.getFileSize(data.getPieValue())));

          return new Data(data, item);
        }).forEach(tuple -> {
          chart.getData().add(tuple.pieChartData);
          tuple.pieChartData.getNode().setUserData(tuple.treeItem);
          tuple.pieChartData.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED, (me) -> {
            treePaneData.select(tuple.treeItem);
            // treePaneData.select(4);
          });
        });

        if (chart.getData().size() != treeItem.getChildren().size())
        {
          sum = chart.getData().stream().map(data -> data.getPieValue()).reduce(0.0d, Double::sum);
          chart.getData().add(new PieChart.Data("<Other>", treeItem.getValue().getSize() - sum));
        }

        return chart;
      }

      return new Label("No data");
    }

    public static class ObjectWithIndexFactory<T>
    {
      private int nextRank = 1;

      public ObjectWithIndex<T> create(T object)
      {
        return new ObjectWithIndex<>(object, nextRank++);
      }
    }

    public static class ObjectWithIndex<T>
    {
      private final T m_object;
      private final int m_index;

      private ObjectWithIndex(T object, int index)
      {
        m_object = object;
        m_index = index;
      }

      public int getIndex()
      {
        return m_index;
      }

      public T getObject()
      {
        return m_object;
      }
    }

    private static Node fillTop50Tab(TreePaneData treePaneData, TreeItem<FileNodeIF> treeItem)
    {
      if (!treeItem.getChildren().isEmpty())
      {
        FileNodeIF node;
        ObservableList<ObjectWithIndex<FileNodeIF>> list;
        TableView<ObjectWithIndex<FileNodeIF>> table;
        TableColumn<ObjectWithIndex<FileNodeIF>, Integer> rankColumn;
        TableColumn<ObjectWithIndex<FileNodeIF>, String> nameColumn;
        TableColumn<ObjectWithIndex<FileNodeIF>, Long> fileSizeColumn;
        TableColumn<ObjectWithIndex<FileNodeIF>, Date> lastModifiedColumn;
        TableColumn<ObjectWithIndex<FileNodeIF>, String> pathColumn;
        ObjectWithIndexFactory<FileNodeIF> objectWithIndexFactory;

        objectWithIndexFactory = new ObjectWithIndexFactory<>();

        node = treeItem.getValue();
        list = node.streamNode().filter(FileNodeIF::isFile).sorted(FileNodeIF.getSizeComparator()).limit(50)
            .map(objectWithIndexFactory::create).collect(Collectors.toCollection(FXCollections::observableArrayList));

        table = new TableView<>();
        table.setEditable(false);

        rankColumn = new TableColumn<>("Rank");
        nameColumn = new TableColumn<>("Name");
        fileSizeColumn = new TableColumn<>("File size");
        lastModifiedColumn = new TableColumn<>("Last modified");
        pathColumn = new TableColumn<>("path");

        table.getColumns().add(rankColumn);
        table.getColumns().add(nameColumn);
        table.getColumns().add(fileSizeColumn);
        table.getColumns().add(lastModifiedColumn);
        table.getColumns().add(pathColumn);

        rankColumn.setCellValueFactory(
            new Callback<CellDataFeatures<ObjectWithIndex<FileNodeIF>, Integer>, ObservableValue<Integer>>()
            {
              @Override
              public ObservableValue<Integer> call(CellDataFeatures<ObjectWithIndex<FileNodeIF>, Integer> p)
              {
                return new ReadOnlyObjectWrapper<Integer>(p.getValue().getIndex());
              }
            });

        nameColumn.setCellValueFactory(
            new Callback<CellDataFeatures<ObjectWithIndex<FileNodeIF>, String>, ObservableValue<String>>()
            {
              @Override
              public ObservableValue<String> call(CellDataFeatures<ObjectWithIndex<FileNodeIF>, String> p)
              {
                return new ReadOnlyObjectWrapper<String>(p.getValue().getObject().getName());
              }
            });

        lastModifiedColumn.setCellValueFactory(
            new Callback<CellDataFeatures<ObjectWithIndex<FileNodeIF>, Date>, ObservableValue<Date>>()
            {
              @Override
              public ObservableValue<Date> call(CellDataFeatures<ObjectWithIndex<FileNodeIF>, Date> p)
              {
                return new ReadOnlyObjectWrapper<Date>(new Date(p.getValue().getObject().getLastModifiedTime()));
              }
            });

        lastModifiedColumn.setCellFactory(column -> {
          TableCell<ObjectWithIndex<FileNodeIF>, Date> cell = new TableCell<>()
          {
            private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            @Override
            protected void updateItem(Date date, boolean empty)
            {
              super.updateItem(date, empty);

              if (empty || date == null)
              {
                setText("");
              }
              else
              {
                setText(format.format(date));
              }
            }
          };

          return cell;
        });
        fileSizeColumn.setCellValueFactory(
            new Callback<CellDataFeatures<ObjectWithIndex<FileNodeIF>, Long>, ObservableValue<Long>>()
            {
              @Override
              public ObservableValue<Long> call(CellDataFeatures<ObjectWithIndex<FileNodeIF>, Long> p)
              {
                return new ReadOnlyObjectWrapper<Long>(p.getValue().getObject().getSize());
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
        GridPane pane;
        NumberAxis xAxis;
        CategoryAxis yAxis;
        BarChart<Number, String> barChart;
        XYChart.Series<Number, String> series1;
        XYChart.Series<Number, String> series2;
        FileNodeIF node;
        record Data(Long numberOfFiles, Long sizeOfFiles) {}
        Map<SizeDistributionBucket, Data> map;
        Data dataDefault = new Data(0l, 0l);

        pane = new GridPane();

        node = treeItem.getValue();
        map = node.streamNode().filter(FileNodeIF::isFile).map(FileNodeIF::getSize)
            .collect(Collectors.groupingBy(SizeDistributionBucket::findBucket,
                Collectors.teeing(Collectors.counting(), Collectors.summingLong(a -> a / 1000000),
                    (numberOfFiles, sizeOfFiles) -> new Data(numberOfFiles, sizeOfFiles))));

        xAxis = new NumberAxis();
        yAxis = new CategoryAxis();
        barChart = FxUtil.createBarChart(xAxis, yAxis);
        barChart.setTitle("Distribution of sizes in " + treeItem.getValue().getName());
        xAxis.setLabel("Number of files");
        yAxis.setLabel("File sizes");

        series1 = new XYChart.Series<>();
        Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
          Data value;
          value = map.getOrDefault(bucket, dataDefault);
          series1.getData().add(new XYChart.Data<Number, String>(value.numberOfFiles(), bucket.getText()));
        });

        barChart.getData().add(series1);
        pane.add(barChart, 0, 0);
        GridPane.setHgrow(barChart, Priority.ALWAYS);
        GridPane.setVgrow(barChart, Priority.ALWAYS);

        xAxis = new NumberAxis();
        yAxis = new CategoryAxis();
        barChart = FxUtil.createBarChart(xAxis, yAxis);
        xAxis.setLabel("Total size of files (in Gb)");
        yAxis.setLabel("File sizes");

        series2 = new XYChart.Series<>();
        Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
          Data value;
          value = map.getOrDefault(bucket, dataDefault);
          series2.getData().add(new XYChart.Data<Number, String>(value.sizeOfFiles(), bucket.getText()));
        });

        barChart.getData().add(series2);
        pane.add(barChart, 0, 1);
        GridPane.setHgrow(barChart, Priority.ALWAYS);
        GridPane.setVgrow(barChart, Priority.ALWAYS);

        return pane;
      }

      return new Label("No data");
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
        return Stream.of(values()).filter(bucket -> value >= bucket.getFrom() && value < bucket.getTo()).findFirst()
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

    private static Node fillModifiedDistributionTab(TreePaneData treePaneData, TreeItem<FileNodeIF> treeItem)
    {
      return new LastModifiedDistributionPane().getNode(treePaneData, treeItem);
    }

    private static Node fillTypeDistributionTab(TreePaneData treePaneData, TreeItem<FileNodeIF> treeItem)
    {
      if (!treeItem.getChildren().isEmpty())
      {
        PieChart chart;
        FileNodeIF node;
        Map<String, Long> fullMap;
        Map<String, Long> reducedMap;
        long totalCount;
        double minimumCount;
        long otherCount;

        chart = FxUtil.createPieChart();

        node = treeItem.getValue();
        fullMap = node.streamNode().filter(FileNodeIF::isFile).map(FileNodeIF::getName)
            .collect(Collectors.groupingBy(TabPaneData::getFileType, Collectors.counting()));
        totalCount = fullMap.values().stream().reduce(0l, Long::sum);
        minimumCount = totalCount * 0.01; // Only types with a count larger than a percentage are shown

        reducedMap = fullMap.entrySet().stream()
            .sorted(Comparator.comparing(Entry<String, Long>::getValue, Comparator.reverseOrder()))
            .filter(e -> e.getValue() > minimumCount).limit(10)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        otherCount = totalCount - reducedMap.values().stream().reduce(0l, Long::sum);
        if (otherCount != 0)
        {
          reducedMap.put("<Other>", otherCount);
        }

        reducedMap.entrySet().stream().map(entry -> {
          PieChart.Data data;
          data = new PieChart.Data(entry.getKey(), entry.getValue());
          data.nameProperty().bind(Bindings.concat(data.getName(), "\n", entry.getValue()));
          return data;
        }).forEach(data -> {
          chart.getData().add(data);
        });

        return chart;
      }

      return new Label("No data");
    }

    private static String getFileType(String fileName)
    {
      int index;

      index = fileName.lastIndexOf(".");
      if (index > 0 && index != -1)
      {
        String type;

        type = fileName.substring(index + 1);
        if (type.chars().allMatch(Character::isLetter))
        {
          return type.toLowerCase();
        }
      }

      return "<none>";
    }
  }

  private abstract static class AbstractTabContentPane
  {
    private Map<String, PaneType> m_paneTypeByIdMap = new LinkedHashMap<>();
    private final BorderPane mi_node = new BorderPane();
    private final SegmentedButton mi_segmentedButton = new SegmentedButton();
    private PaneType mi_currentPaneType;
    private TreePaneData mi_currentTreePaneData;
    private TreeItem<FileNodeIF> mi_currentTreeItem;
    private Map<PaneType, Node> mi_nodeByPaneTypeMap = new HashMap<>();

    private record PaneType(String description, String iconName, Supplier<Node> node) {};

    private AbstractTabContentPane()
    {
    }

    protected void init()
    {
      mi_segmentedButton.getButtons().addAll(m_paneTypeByIdMap.values().stream().map(paneType -> {
        ToggleButton button;

        button = new ToggleButton();
        button.setTooltip(new Tooltip(paneType.description()));
        button.setGraphic(IconUtil.createImageNode(paneType.iconName(), IconSize.SMALLER));
        button.setUserData(paneType);
        button.setOnAction((ae) -> {
          setCurrentPaneType((PaneType) ((Node) ae.getSource()).getUserData());
        });
        if (mi_currentPaneType == paneType)
        {
          button.setSelected(true);
        }

        return button;
      }).collect(Collectors.toList()));

      mi_node.setBottom(mi_segmentedButton);
    }

    protected PaneType createPaneType(String paneTypeId, String description, String iconName, Supplier<Node> node)
    {
      PaneType paneType;

      paneType = m_paneTypeByIdMap.computeIfAbsent(paneTypeId,
          (panelTypeId) -> new PaneType(description, iconName, node));
      if (mi_currentPaneType == null)
      {
        setCurrentPaneType(paneType);
      }

      return paneType;
    }

    private void setCurrentPaneType(PaneType paneType)
    {
      mi_currentPaneType = paneType;
      initCurrentNode();
    }

    private void initCurrentNode()
    {
      if (mi_currentPaneType != null)
      {
        mi_node.setCenter(mi_nodeByPaneTypeMap.computeIfAbsent(mi_currentPaneType, type -> type.node().get()));
      }
      else
      {
        mi_node.setCenter(new Label(""));
      }
    }

    protected TreeItem<FileNodeIF> getCurrentTreeItem()
    {
      return mi_currentTreeItem;
    }

    Node getNode(TreePaneData treePaneData, TreeItem<FileNodeIF> treeItem)
    {
      if (mi_currentTreePaneData != treePaneData && mi_currentTreeItem != treeItem)
      {
        mi_currentTreePaneData = treePaneData;
        mi_currentTreeItem = treeItem;
        mi_nodeByPaneTypeMap.clear();
      }

      initCurrentNode();
      return mi_node;
    }
  }

  private static class LastModifiedDistributionPane
    extends AbstractTabContentPane
  {
    private enum LastModifiedDistributionBucket
    {
      INVALID("Invalid", Long.MAX_VALUE - 1l, Long.MAX_VALUE),
      LAST_MODIFIED_FUTURE("In the future", -Long.MAX_VALUE, 0),
      LAST_MODIFIED_TODAY("Today", days(0), days(1)),
      LAST_MODIFIED_YESTERDAY("Yesterday", days(1), days(2)),
      LAST_MODIFIED_1_DAY_TILL_7_DAYS("2 - 7 days", days(2), days(8)),
      LAST_MODIFIED_7_DAYs_TILL_30_DAYS("7 - 30 days", days(9), days(31)),
      LAST_MODIFIED_30_DAYS_TILL_90_DAYS("30 - 90 days", days(31), days(91)),
      LAST_MODIFIED_90_DAYS_TILL_180_DAYS("90 - 180 days", days(91), days(181)),
      LAST_MODIFIED_180_DAYS_TILL_365_DAYS("180 - 365 days", days(181), years(1)),
      LAST_MODIFIED_1_YEAR_TILL_2_YEAR("1 - 2 years", years(1), years(2)),
      LAST_MODIFIED_2_YEAR_TILL_3_YEAR("2 - 3 years", years(2), years(3)),
      LAST_MODIFIED_3_YEAR_TILL_6_YEAR("3 - 6 years", years(3), years(6)),
      LAST_MODIFIED_6_YEAR_TILL_10_YEAR("6 - 10 years", years(6), years(10)),
      LAST_MODIFIED_OVER_10_YEARS("Over 10 years", years(10), Long.MAX_VALUE);

      private final String mi_text;
      private final long mi_from;
      private final long mi_to;

      LastModifiedDistributionBucket(String text, long from, long to)
      {
        mi_text = text;
        mi_from = from;
        mi_to = to;
      }

      public String getText()
      {
        return mi_text;
      }

      long getFrom()
      {
        return mi_from;
      }

      long getTo()
      {
        return mi_to;
      }

      static public LastModifiedDistributionBucket findBucket(long todayMidnight, long lastModified)
      {
        long ago;

        ago = todayMidnight - lastModified;

        LastModifiedDistributionBucket b = Stream.of(values())
            .filter(bucket -> ago >= bucket.getFrom() && ago < bucket.getTo()).findFirst().orElse(INVALID);

        System.out.println(b + " -> " + new Date(lastModified) + " ago: " + (ago / (24 * 60 * 60 * 1000)) + " dagen");

        return b;
      }

      private static long days(long days)
      {
        return days * 24 * 60 * 60 * 1000;
      }

      private static long years(long years)
      {
        return days(years * 365);
      }
    }

    LastModifiedDistributionPane()
    {
      createPaneType("PIECHART", "Show details table", "table", this::getPieChartNode);
      createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode);
      createPaneType("TABLE", "Show details table", "table", this::getTableNode);

      init();
    }

    Node getTableNode()
    {
      return new Label("Table");
    }

    Node getBarChartNode()
    {
      return new Label("Bar chart");
    }

    Node getPieChartNode()
    {
      TreeItem<FileNodeIF> treeItem;

      treeItem = getCurrentTreeItem();
      if (treeItem != null && !treeItem.getChildren().isEmpty())
      {
        GridPane pane;
        NumberAxis xAxis;
        CategoryAxis yAxis;
        BarChart<Number, String> barChart;
        XYChart.Series<Number, String> series1;
        XYChart.Series<Number, String> series2;
        FileNodeIF node;
        record Data(Long numberOfFiles, Long sizeOfFiles) {}
        Map<LastModifiedDistributionBucket, Data> map;
        Data dataDefault = new Data(0l, 0l);
        long todayMidnight;

        pane = new GridPane();
        todayMidnight = CommonUtil.getMidnight();

        System.out.println("30 dagen = " + LastModifiedDistributionBucket.days(30));

        Stream.of(LastModifiedDistributionBucket.values()).forEach(bucket -> {
          System.out.println(bucket.getText() + " " + bucket.getFrom() + " until " + bucket.getTo());
        });

        node = treeItem.getValue();
        map = node.streamNode().filter(FileNodeIF::isFile)
            .collect(Collectors.groupingBy(
                fileNode -> LastModifiedDistributionBucket.findBucket(todayMidnight, fileNode.getLastModifiedTime()),
                Collectors.teeing(Collectors.counting(),
                    Collectors.summingLong(fileNode -> fileNode.getSize() / 1000000),
                    (numberOfFiles, sizeOfFiles) -> new Data(numberOfFiles, sizeOfFiles))));

        xAxis = new NumberAxis();
        yAxis = new CategoryAxis();
        barChart = FxUtil.createBarChart(xAxis, yAxis);
        barChart.setTitle("Distribution of last modification dates in " + treeItem.getValue().getName());
        xAxis.setLabel("Number of files");
        yAxis.setLabel("File sizes");

        System.out.println("numberOfFiles:");
        series1 = new XYChart.Series<>();
        Stream.of(LastModifiedDistributionBucket.values()).forEach(bucket -> {
          Data value;
          value = map.getOrDefault(bucket, dataDefault);
          series1.getData().add(new XYChart.Data<Number, String>(value.numberOfFiles(), bucket.getText()));
          System.out.println(bucket.getText() + " -> " + value.numberOfFiles());
        });

        barChart.getData().add(series1);
        pane.add(barChart, 0, 0);
        GridPane.setHgrow(barChart, Priority.ALWAYS);
        GridPane.setVgrow(barChart, Priority.ALWAYS);

        xAxis = new NumberAxis();
        yAxis = new CategoryAxis();
        barChart = FxUtil.createBarChart(xAxis, yAxis);
        xAxis.setLabel("Total size of files (in Gb)");
        yAxis.setLabel("File sizes");

        System.out.println("sizeOfFiles:");
        series2 = new XYChart.Series<>();
        Stream.of(LastModifiedDistributionBucket.values()).forEach(bucket -> {
          Data value;
          value = map.getOrDefault(bucket, dataDefault);
          series2.getData().add(new XYChart.Data<Number, String>(value.sizeOfFiles(), bucket.getText()));
          System.out.println(bucket.getText() + " -> " + value.sizeOfFiles());
        });

        barChart.getData().add(series2);
        pane.add(barChart, 0, 1);
        GridPane.setHgrow(barChart, Priority.ALWAYS);
        GridPane.setVgrow(barChart, Priority.ALWAYS);

        return pane;
      }

      return new Label("No data");
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
      menuItem.setOnAction(e -> {
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
