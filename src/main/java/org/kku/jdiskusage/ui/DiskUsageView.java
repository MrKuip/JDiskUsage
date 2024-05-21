package org.kku.jdiskusage.ui;

import javafx.scene.paint.Color;


import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.SegmentedButton;
import org.kku.fonticons.ui.FxIcon.IconBuilder;
import org.kku.fonticons.ui.FxIcon.IconColor;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.util.FormatterFactory;
import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.fonticons.ui.FxIcon.IconAlignment;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.ApplicationPropertyExtensionIF;
import org.kku.jdiskusage.util.CommonUtil;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.FileTree.FileNodeWithPath;
import org.kku.jdiskusage.util.FileTree.FilterIF;
import org.kku.jdiskusage.util.SizeUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DiskUsageView
  extends BorderPane
    implements ApplicationPropertyExtensionIF
{
  private DiskUsageMainData m_diskUsageMainData = new DiskUsageMainData();

  private class DiskUsageMainData
  {
    private Stage mi_stage;
    private TreePaneData mi_treePaneData;
    private TabPaneData mi_tabPaneData;
    private SizePane mi_sizeTab;
    private Top50Pane mi_top50Tab;
    private SizeDistributionPane mi_sizeDistributionTab;
    private LastModifiedDistributionPane mi_modifiedDistributionTab;
    private TypesPane mi_typesTab;
    private RecentFilesMenu mi_recentFiles;
    private FilterPane mi_filterPane;

    private DiskUsageMainData()
    {
    }

    public void init()
    {
      mi_treePaneData = new TreePaneData();
      mi_tabPaneData = new TabPaneData();
      mi_sizeTab = new SizePane();
      mi_top50Tab = new Top50Pane();
      mi_sizeDistributionTab = new SizeDistributionPane();
      mi_modifiedDistributionTab = new LastModifiedDistributionPane();
      mi_typesTab = new TypesPane();
      mi_recentFiles = new RecentFilesMenu();
      mi_filterPane = new FilterPane();
    }

    TreeItem<FileNodeIF> getSelectedTreeItem()
    {
      if (mi_treePaneData == null || mi_treePaneData.mi_treeTableView == null)
      {
        return null;
      }
      return mi_treePaneData.mi_treeTableView.getSelectionModel().getSelectedItem();
    }
  }

  {
    m_diskUsageMainData.init();
  }

  public DiskUsageView(Stage stage)
  {
    VBox toolBars;
    SplitPane splitPane;

    getProps().getDouble(Property.HEIGHT, 0);

    m_diskUsageMainData.mi_stage = stage;

    toolBars = new VBox(createMenuBar(), createFilterPane());
    splitPane = new SplitPane();

    Stream.of(TabPaneData.TabData.values()).forEach(td -> m_diskUsageMainData.mi_tabPaneData.createTab(td));

    splitPane.getItems().addAll(m_diskUsageMainData.mi_treePaneData.getNode(),
        m_diskUsageMainData.mi_tabPaneData.getNode());
    splitPane.getDividers().get(0).positionProperty()
        .addListener(getProps().getChangeListener(Property.SPLIT_PANE_POSITION));
    SplitPane.setResizableWithParent(m_diskUsageMainData.mi_treePaneData.getNode(), false);
    SplitPane.setResizableWithParent(m_diskUsageMainData.mi_tabPaneData.getNode(), false);
    splitPane.getDividers().get(0).setPosition(getProps().getDouble(Property.SPLIT_PANE_POSITION, 25.0));

    setTop(toolBars);
    setCenter(splitPane);

    m_diskUsageMainData.mi_tabPaneData.getNode().setTop(m_diskUsageMainData.mi_treePaneData.createBreadCrumbBar());
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

      dirNode = new ScanFileTreeDialog().chooseDirectory(m_diskUsageMainData.mi_stage);
      if (dirNode != null)
      {
        m_diskUsageMainData.mi_treePaneData.createTreeTableView(dirNode);
        m_diskUsageMainData.mi_recentFiles.addFile(new File(dirNode.getName()));
      }
    });

    return menuItem;
  }

  private Menu createRecentFilesMenu()
  {
    return m_diskUsageMainData.mi_recentFiles.createMenu();
  }

  private FilterPane createFilterPane()
  {
    return m_diskUsageMainData.mi_filterPane;
  }

  private class FilterPane
    extends BorderPane
  {
    private final Map<String, Set<Filter>> mi_filterByOwnerMap = new HashMap<>();
    private final HBox mi_filterActivationPane = new HBox();
    private final HBox mi_filterPane = new HBox();
    private Button mi_activateFilterButton;
    private Button mi_cancelFilterButton;

    private FilterPane()
    {
      mi_filterActivationPane.setPadding(new Insets(2, 10, 2, 2));
      mi_filterActivationPane.setSpacing(2);

      mi_filterPane.setPadding(new Insets(2, 0, 2, 0));
      setVisible(false);
      setManaged(false);

      setLeft(mi_filterActivationPane);
      setCenter(mi_filterPane);

      updateFilterActivationPane();
    }

    public void addFilter(Filter filter)
    {
      if (mi_filterByOwnerMap.computeIfAbsent(filter.getOwnerName(), (k) -> new HashSet<>()).add(filter))
      {
        Button button;

        IconBuilder builder = IconUtil.buildImage("close").size(IconSize.SMALLER).color(IconColor.RED);
        Node node = IconUtil.createImageNode(builder);
        button = new Button(filter.getFilterText(), IconUtil.createImageNode("close", IconSize.SMALLER));
        button.setContentDisplay(ContentDisplay.RIGHT);
        button.setUserData(filter);
        button.setDisable(true);
        button.setOnAction((a) ->
        {
          removeFilter((Button) a.getSource());
        });

        mi_filterPane.getChildren().add(button);

        updateFilterActivationPane();
      }
    }

    public void removeFilter(Node node)
    {
      mi_filterPane.getChildren().remove(node);
      mi_filterByOwnerMap.remove(node.getUserData());

      updateFilterActivationPane();
    }

    public boolean accept(FileNodeIF fileNode)
    {
      Set<Entry<String, Set<Filter>>> entries;

      entries = mi_filterByOwnerMap.entrySet();
      for (Entry<String, Set<Filter>> entry : entries)
      {
        if (entry.getValue().stream().filter(filter -> filter.accept(fileNode)).findFirst().isPresent())
        {
          continue;
        }

        return false;
      }

      return true;
    }

    private void updateFilterActivationPane()
    {
      if (!mi_filterPane.getChildren().isEmpty())
      {
        boolean disabled;

        if (mi_filterActivationPane.getChildren().isEmpty())
        {
          mi_activateFilterButton = new Button("Activate filter",
              IconUtil.createImageNode("filter-plus", IconSize.SMALLER));
          mi_activateFilterButton.setOnAction((ae) -> mi_filterPane.getChildren().forEach(child ->
          {
            child.setDisable(false);
            m_diskUsageMainData.mi_treePaneData.setFilter((fn) -> accept(fn));
            updateFilterActivationPane();
          }));
          mi_filterActivationPane.getChildren().add(mi_activateFilterButton);

          mi_cancelFilterButton = new Button("Cancel filter",
              IconUtil.createImageNode("filter-minus", IconSize.SMALLER));
          mi_cancelFilterButton.setOnAction((ae) ->
          {
            new ArrayList<>(mi_filterPane.getChildren()).forEach(child ->
            {
              if (child.isDisabled())
              {
                removeFilter(child);
              }
            });
            updateFilterActivationPane();
          });
          mi_filterActivationPane.getChildren().add(mi_cancelFilterButton);
        }

        disabled = mi_filterPane.getChildren().stream().filter(node -> node.isDisabled()).findFirst().isPresent();
        mi_activateFilterButton.setDisable(!disabled);
        mi_cancelFilterButton.setDisable(!disabled);
        setVisible(true);
        setManaged(true);
      }
      else
      {
        if (!mi_filterActivationPane.getChildren().isEmpty())
        {
          mi_filterActivationPane.getChildren().clear();
          mi_activateFilterButton = null;
          mi_cancelFilterButton = null;
          setVisible(false);
          setManaged(false);
        }
      }
    }
  }

  public class Filter
      implements FilterIF
  {
    private final String mi_ownerName;
    private final String mi_filterText;
    private final Predicate<FileNodeIF> mi_fileNodePredicate;

    public Filter(String ownerName, String filterText, Predicate<FileNodeIF> fileNodePredicate)
    {
      mi_ownerName = ownerName;
      mi_filterText = filterText;
      mi_fileNodePredicate = fileNodePredicate;
    }

    @Override
    public boolean accept(FileNodeIF fileNode)
    {
      return getPredicate().test(fileNode);
    }

    public String getOwnerName()
    {
      return mi_ownerName;
    }

    public String getFilterText()
    {
      return mi_filterText;
    }

    public Predicate<FileNodeIF> getPredicate()
    {
      return mi_fileNodePredicate;
    }

    @Override
    public int hashCode()
    {
      return getFilterText().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
      if (obj == null || !(obj instanceof Filter))
      {
        return false;
      }

      return ((Filter) obj).getFilterText().equals(getFilterText());
    }
  }

  private class TreePaneData
  {
    private FileTreeView mi_fileTreeView;
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

    public void setFilter(FilterIF filter)
    {
      mi_fileTreeView.setFilter(filter);
    }

    public void createTreeTableView(DirNode dirNode)
    {
      mi_fileTreeView = new FileTreeView(dirNode);
      mi_treeTableView = mi_fileTreeView.createComponent();
      mi_treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
      {
        m_diskUsageMainData.mi_tabPaneData.itemSelected();
      });
      mi_breadCrumbBar.selectedCrumbProperty().bind(mi_treeTableView.getSelectionModel().selectedItemProperty());
      mi_breadCrumbBar.setAutoNavigationEnabled(false);
      mi_breadCrumbBar.setOnCrumbAction((e) ->
      {
        select(e.getSelectedCrumb());
      });

      Platform.runLater(() ->
      {
        mi_treeTableView.getSelectionModel().select(0);
        mi_treeTableView.getSelectionModel().getSelectedItem().setExpanded(true);
      });

      mi_treePane.setCenter(mi_treeTableView);
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
      TOP50("Top 50 (size)", "trophy", TabPaneData::fillTop50Tab),
      DISTRIBUTION_SIZE("Distribution size", "chart-bell-curve", TabPaneData::fillSizeDistributionTab),
      DISTRIBUTION_MODIFIED("Distribution last modified", "sort-calendar-ascending",
          TabPaneData::fillModifiedDistributionTab),
      DISTRIBUTION_TYPES("Distribution types", "chart-pie", TabPaneData::fillTypeDistributionTab);

      private final String m_name;
      private final String m_iconName;
      private final Function<DiskUsageMainData, Node> m_fillContent;

      private TabData(String name, String iconName, Function<DiskUsageMainData, Node> fillContent)
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

      Node fillContent(DiskUsageMainData mainData)
      {
        return m_fillContent.apply(mainData);
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
        fillContent(newTab);
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

    public void fillContent(Tab tab)
    {
      Optional<Entry<TabData, Tab>> tabEntry;

      tabEntry = mi_tabByTabId.entrySet().stream().filter(entry -> tab.equals(entry.getValue())).findFirst();
      if (tabEntry.isPresent() && m_diskUsageMainData.getSelectedTreeItem() != null)
      {
        TabData tabData;

        tabData = tabEntry.get().getKey();
        tab.setContent(
            mi_contentByTabId.computeIfAbsent(tabData, td -> tabEntry.get().getKey().fillContent(m_diskUsageMainData)));
      }
      else
      {
        tab.setContent(new Label(""));
      }
    }

    public void itemSelected()
    {
      Tab selectedTab;

      mi_contentByTabId.clear();

      selectedTab = mi_tabPane.getSelectionModel().getSelectedItem();
      m_diskUsageMainData.mi_tabPaneData.fillContent(selectedTab);
    }

    private static Node fillSizeTab(DiskUsageMainData mainData)
    {
      return mainData.mi_sizeTab.getNode(mainData.mi_treePaneData);
    }

    private static Node fillTop50Tab(DiskUsageMainData mainData)
    {
      return mainData.mi_top50Tab.getNode(mainData.mi_treePaneData);
    }

    private static Node fillSizeDistributionTab(DiskUsageMainData mainData)
    {
      return mainData.mi_sizeDistributionTab.getNode(mainData.mi_treePaneData);
    }

    private static Node fillModifiedDistributionTab(DiskUsageMainData mainData)
    {
      return mainData.mi_modifiedDistributionTab.getNode(mainData.mi_treePaneData);
    }

    private static Node fillTypeDistributionTab(DiskUsageMainData mainData)
    {
      return mainData.mi_typesTab.getNode(mainData.mi_treePaneData);
    }

  }

  private abstract class AbstractTabContentPane
  {
    private Map<String, PaneType> m_paneTypeByIdMap = new LinkedHashMap<>();
    private final BorderPane mi_node = new BorderPane();
    private final SegmentedButton mi_segmentedButton = new SegmentedButton();
    private PaneType mi_currentPaneType;
    private Map<PaneType, Node> mi_nodeByPaneTypeMap = new HashMap<>();
    private TreePaneData mi_currentTreePaneData;
    private TreeItem<FileNodeIF> mi_currentTreeItem;

    private record PaneType(String description, String iconName, Supplier<Node> node) {
    };

    private AbstractTabContentPane()
    {
    }

    protected void init()
    {
      m_paneTypeByIdMap.values().stream().map(paneType ->
      {
        ToggleButton button;

        button = new ToggleButton();
        button.setTooltip(new Tooltip(paneType.description()));
        button.setGraphic(IconUtil.createImageNode(paneType.iconName(), IconSize.SMALLER));
        button.setUserData(paneType);
        button.setOnAction((ae) ->
        {
          setCurrentPaneType((PaneType) ((Node) ae.getSource()).getUserData());
        });
        if (mi_currentPaneType == paneType)
        {
          button.setSelected(true);
        }

        return button;
      }).forEach(button ->
      {
        mi_segmentedButton.getButtons().add(button);
      });

      BorderPane.setMargin(mi_segmentedButton, new Insets(2, 2, 2, 2));
      mi_node.setBottom(mi_segmentedButton);
    }

    protected PaneType createPaneType(String paneTypeId, String description, String iconName, Supplier<Node> node)
    {
      return createPaneType(paneTypeId, description, iconName, node, false);
    }

    protected PaneType createPaneType(String paneTypeId, String description, String iconName, Supplier<Node> node,
        boolean current)
    {
      PaneType paneType;

      paneType = m_paneTypeByIdMap.computeIfAbsent(paneTypeId,
          (panelTypeId) -> new PaneType(description, iconName, node));
      if (mi_currentPaneType == null || current)
      {
        mi_currentPaneType = paneType;
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
        mi_node.setCenter(mi_nodeByPaneTypeMap.computeIfAbsent(mi_currentPaneType, type ->
        {
          return type.node().get();
        }));
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

    Node getNode(TreePaneData treePaneData)
    {
      if (mi_currentTreePaneData != treePaneData || getCurrentTreeItem() != m_diskUsageMainData.getSelectedTreeItem())
      {
        mi_currentTreePaneData = treePaneData;
        mi_nodeByPaneTypeMap.clear();
      }

      initCurrentNode();
      return mi_node;
    }
  }

  private class SizePane
    extends AbstractTabContentPane
  {
    private SizePane()
    {
      createPaneType("PIECHART", "Show details table", "chart-pie", this::getPieChartNode, true);
      createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode);
      createPaneType("TABLE", "Show details table", "table", this::getTableNode);

      init();
    }

    Node getPieChartNode()
    {
      TreeItem<FileNodeIF> treeItem;

      treeItem = m_diskUsageMainData.getSelectedTreeItem();
      if (!treeItem.getChildren().isEmpty())
      {
        PieChart chart;
        double sum;
        double totalSize;
        double minimumDataSize;

        totalSize = treeItem.getValue().getSize();
        minimumDataSize = totalSize * 0.02;

        record Data(PieChart.Data pieChartData, TreeItem<FileNodeIF> treeItem) {
        }

        chart = FxUtil.createPieChart();
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
          tuple.pieChartData.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, (me) ->
          {
            m_diskUsageMainData.mi_treePaneData.select(tuple.treeItem);
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

    Node getBarChartNode()
    {
      return new Label("Bar chart");
    }

    Node getTableNode()
    {
      return new Label("Table chart");
    }
  }

  private class Top50Pane
    extends AbstractTabContentPane
  {
    private Top50Pane()
    {
      createPaneType("PIECHART", "Show details table", "chart-pie", this::getPieChartNode);
      createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode);
      createPaneType("TABLE", "Show details table", "table", this::getTableNode, true);

      init();
    }

    Node getPieChartNode()
    {
      return new Label("PieChart");
    }

    Node getBarChartNode()
    {
      return new Label("BarChart");
    }

    Node getTableNode()
    {
      TreeItem<FileNodeIF> treeItem;

      treeItem = m_diskUsageMainData.getSelectedTreeItem();
      if (!treeItem.getChildren().isEmpty())
      {
        FileNodeIF node;
        ObservableList<ObjectWithIndex<FileNodeWithPath>> list;
        MyTableView<ObjectWithIndex<FileNodeWithPath>> table;
        MyTableColumn<ObjectWithIndex<FileNodeWithPath>, Integer> rankColumn;
        MyTableColumn<ObjectWithIndex<FileNodeWithPath>, String> nameColumn;
        MyTableColumn<ObjectWithIndex<FileNodeWithPath>, Long> fileSizeColumn;
        MyTableColumn<ObjectWithIndex<FileNodeWithPath>, Date> lastModifiedColumn;
        MyTableColumn<ObjectWithIndex<FileNodeWithPath>, Integer> numberOfLinksColumn;
        MyTableColumn<ObjectWithIndex<FileNodeWithPath>, String> pathColumn;
        ObjectWithIndexFactory<FileNodeWithPath> objectWithIndexFactory;

        objectWithIndexFactory = new ObjectWithIndexFactory<>();

        node = treeItem.getValue();
        list = node.streamNodeWithPath("").filter(FileNodeWithPath::isFile).sorted(FileNodeIF.getSizeComparator())
            .limit(50).map(objectWithIndexFactory::create)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

        table = new MyTableView<>("Top50");
        table.setEditable(false);

        rankColumn = table.addColumn("Rank");
        rankColumn.initPersistentPrefWidth(100.0);
        rankColumn.setCellValueGetter(ObjectWithIndex<FileNodeWithPath>::getIndex);
        rankColumn.setCellValueAlignment(Pos.CENTER_RIGHT);

        nameColumn = table.addColumn("Name");
        nameColumn.initPersistentPrefWidth(200.0);
        nameColumn.setCellValueGetter((owi) -> owi.getObject().getName());

        fileSizeColumn = table.addColumn("File size");
        fileSizeColumn.initPersistentPrefWidth(100.0);
        fileSizeColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
        fileSizeColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
        fileSizeColumn.setCellValueGetter((owi) -> owi.getObject().getSize());

        lastModifiedColumn = table.addColumn("Last modified");
        lastModifiedColumn.initPersistentPrefWidth(200.0);
        lastModifiedColumn.setCellValueGetter((owi) -> new Date(owi.getObject().getLastModifiedTime()));
        lastModifiedColumn.setCellValueFormatter(FormatterFactory.createSimpleDateFormatter("dd/MM/yyyy HH:mm:ss"));
        lastModifiedColumn.setCellValueAlignment(Pos.CENTER_RIGHT);

        numberOfLinksColumn = table.addColumn("Number links\n to file");
        numberOfLinksColumn.initPersistentPrefWidth(100.0);
        numberOfLinksColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
        numberOfLinksColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
        numberOfLinksColumn.setCellValueGetter((owi) -> owi.getObject().getNumberOfLinks());

        pathColumn = table.addColumn("Path");
        pathColumn.setCellValueGetter((owi) -> owi.getObject().getParentPath());

        table.setItems(list);

        return table;
      }

      return new Label("No data");
    }
  }

  private class SizeDistributionPane
    extends AbstractTabContentPane
  {
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

    private SizeDistributionPane()
    {
      createPaneType("PIECHART", "Show details table", "chart-pie", this::getPieChartNode);
      createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode, true);
      createPaneType("TABLE", "Show details table", "table", this::getTableNode);

      init();
    }

    Node getPieChartNode()
    {
      return new Label("Pie chart");
    }

    Node getBarChartNode()
    {
      TreeItem<FileNodeIF> treeItem;

      treeItem = m_diskUsageMainData.getSelectedTreeItem();
      if (!treeItem.getChildren().isEmpty())
      {
        GridPane pane;
        NumberAxis xAxis;
        CategoryAxis yAxis;
        BarChart<Number, String> barChart;
        XYChart.Series<Number, String> series1;
        XYChart.Series<Number, String> series2;
        FileNodeIF node;
        record Data(Long numberOfFiles, Long sizeOfFiles) {
        }
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
        Stream.of(SizeDistributionBucket.values()).forEach(bucket ->
        {
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
        Stream.of(SizeDistributionBucket.values()).forEach(bucket ->
        {
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

    Node getTableNode()
    {
      return new Label("Table");
    }
  }

  private class LastModifiedDistributionPane
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

    private LastModifiedDistributionPane()
    {
      createPaneType("PIECHART", "Show details table", "chart-pie", this::getPieChartNode);
      createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode);
      createPaneType("TABLE", "Show details table", "table", this::getTableNode);

      init();
    }

    Node getTableNode()
    {
      TreeItem<FileNodeIF> treeItem;

      treeItem = m_diskUsageMainData.getSelectedTreeItem();
      if (treeItem != null && !treeItem.getChildren().isEmpty())
      {
        GridPane pane;
        FileNodeIF node;
        record Data(Long numberOfFiles, Long sizeOfFiles) {
        }
        Map<LastModifiedDistributionBucket, Data> map;
        long todayMidnight;
        ObservableList<Entry<LastModifiedDistributionBucket, Data>> list;
        MyTableView<Entry<LastModifiedDistributionBucket, Data>> table;
        MyTableColumn<Entry<LastModifiedDistributionBucket, Data>, String> timeIntervalColumn;
        MyTableColumn<Entry<LastModifiedDistributionBucket, Data>, Long> sumOfFileSizesColumn;
        MyTableColumn<Entry<LastModifiedDistributionBucket, Data>, Long> numberOfFilesColumn;
        Data dataDefault = new Data(0l, 0l);

        pane = new GridPane();
        todayMidnight = CommonUtil.getMidnight();

        node = treeItem.getValue();
        map = node.streamNode().filter(FileNodeIF::isFile)
            .collect(Collectors.groupingBy(
                fileNode -> LastModifiedDistributionBucket.findBucket(todayMidnight, fileNode.getLastModifiedTime()),
                Collectors.teeing(Collectors.counting(),
                    Collectors.summingLong(fileNode -> fileNode.getSize() / 1000000),
                    (numberOfFiles, sizeOfFiles) -> new Data(numberOfFiles, sizeOfFiles))));

        list = map.entrySet().stream().collect(Collectors.toCollection(FXCollections::observableArrayList));

        table = new MyTableView<>("LastModifiedDistribution");
        table.setEditable(false);

        timeIntervalColumn = table.addColumn("Time interval");
        timeIntervalColumn.initPersistentPrefWidth(300.0);
        timeIntervalColumn.setCellValueGetter((o) -> o.getKey().getText());
        sumOfFileSizesColumn = table.addColumn("Sum of file sizes");
        timeIntervalColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
        sumOfFileSizesColumn.initPersistentPrefWidth(100.0);
        sumOfFileSizesColumn.setCellValueGetter((o) -> o.getValue().sizeOfFiles());
        numberOfFilesColumn = table.addColumn("Sum of file sizes");
        timeIntervalColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
        numberOfFilesColumn.initPersistentPrefWidth(100.0);
        numberOfFilesColumn.setCellValueGetter((o) -> o.getValue().numberOfFiles());

        table.setItems(list);

        pane.add(table, 0, 1);
        GridPane.setHgrow(table, Priority.ALWAYS);
        GridPane.setVgrow(table, Priority.ALWAYS);

        return pane;
      }

      return new Label("No data");
    }

    Node getBarChartNode()
    {
      return new Label("Bar chart");
    }

    Node getPieChartNode()
    {
      TreeItem<FileNodeIF> treeItem;

      treeItem = m_diskUsageMainData.getSelectedTreeItem();
      if (treeItem != null && !treeItem.getChildren().isEmpty())
      {
        GridPane pane;
        NumberAxis xAxis;
        CategoryAxis yAxis;
        BarChart<Number, String> barChart;
        XYChart.Series<Number, String> series1;
        XYChart.Series<Number, String> series2;
        FileNodeIF node;
        record Data(Long numberOfFiles, Long sizeOfFiles) {
        }
        Map<LastModifiedDistributionBucket, Data> map;
        Data dataDefault = new Data(0l, 0l);
        long todayMidnight;

        pane = new GridPane();
        todayMidnight = CommonUtil.getMidnight();

        node = treeItem.getValue();
        map = node.streamNode().filter(FileNodeIF::isFile)
            .collect(Collectors.groupingBy(
                fileNode -> LastModifiedDistributionBucket.findBucket(todayMidnight, fileNode.getLastModifiedTime()),
                LinkedHashMap::new,
                Collectors.teeing(Collectors.counting(),
                    Collectors.summingLong(fileNode -> fileNode.getSize() / 1000000),
                    (numberOfFiles, sizeOfFiles) -> new Data(numberOfFiles, sizeOfFiles))));

        xAxis = new NumberAxis();
        yAxis = new CategoryAxis();
        barChart = FxUtil.createBarChart(xAxis, yAxis);
        barChart.setTitle("Distribution of last modified dates in " + treeItem.getValue().getName());
        xAxis.setLabel("Number of files");
        yAxis.setLabel("Last modified date");

        series1 = new XYChart.Series<>();
        Stream.of(LastModifiedDistributionBucket.values()).forEach(bucket ->
        {
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
        yAxis.setLabel("Last modified date");

        series2 = new XYChart.Series<>();
        Stream.of(LastModifiedDistributionBucket.values()).forEach(bucket ->
        {
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
  }

  private class TypesPane
    extends AbstractTabContentPane
  {
    private final String OTHER = "<Other>";
    private final String NONE = "<None>";

    private TypesPane()
    {
      createPaneType("PIECHART", "Show details table", "chart-pie", this::getPieChartNode);
      createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode);
      createPaneType("TABLE", "Show details table", "table", this::getTableNode);

      init();
    }

    Node getPieChartNode()
    {
      TreeItem<FileNodeIF> treeItem;

      treeItem = m_diskUsageMainData.getSelectedTreeItem();
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
            .collect(Collectors.groupingBy(TypesPane.this::getFileType, Collectors.counting()));
        totalCount = fullMap.values().stream().reduce(0l, Long::sum);
        minimumCount = totalCount * 0.01; // Only types with a count larger than a percentage are shown

        reducedMap = fullMap.entrySet().stream()
            .sorted(Comparator.comparing(Entry<String, Long>::getValue, Comparator.reverseOrder()))
            .filter(e -> e.getValue() > minimumCount).limit(10)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        otherCount = totalCount - reducedMap.values().stream().reduce(0l, Long::sum);
        if (otherCount != 0)
        {
          reducedMap.put(OTHER, otherCount);
        }

        reducedMap.entrySet().forEach(entry ->
        {
          PieChart.Data data;
          String name;

          name = entry.getKey() + "\n" + entry.getValue() + " files";
          data = new PieChart.Data(name, entry.getValue());
          chart.getData().add(data);
          data.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, (e) ->
          {
            m_diskUsageMainData.mi_filterPane.addFilter(new Filter(getClass().getSimpleName(),
                "Distribution type == " + entry.getKey(), (fileNode) -> fileNode.getName().endsWith(entry.getKey())));
          });
        });

        return chart;
      }

      return new Label("No data");
    }

    Node getBarChartNode()
    {
      return new Label("Bar chart");
    }

    Node getTableNode()
    {
      return new Label("Table");
    }

    private String getFileType(String fileName)
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

      return NONE;
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
      getProps().setFileList(Property.RECENT_FILES,
          Stream.concat(Stream.of(file), getProps().getFileList(Property.RECENT_FILES).stream()).distinct().limit(10)
              .collect(Collectors.toList()));
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
      return getProps().getFileList(Property.RECENT_FILES).stream().map(File::getPath).map(this::createMenuItem)
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
          m_diskUsageMainData.mi_treePaneData.createTreeTableView(dirNode);
          m_diskUsageMainData.mi_recentFiles.addFile(new File(dirNode.getName()));
        }
      });

      return menuItem;
    }
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
}
