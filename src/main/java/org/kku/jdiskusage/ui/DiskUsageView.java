package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.FxIcon.IconColor;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.util.FormatterFactory;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.ApplicationPropertyExtensionIF;
import org.kku.jdiskusage.util.CommonUtil;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.FileTree.FileNodeWithPath;
import org.kku.jdiskusage.util.FileTree.FilterIF;
import org.kku.jdiskusage.util.Translator;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.kku.jdiskusage.util.preferences.DisplayMetric;
import org.kku.jdiskusage.util.preferences.Sort;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DiskUsageView
  extends BorderPane
    implements ApplicationPropertyExtensionIF
{
  private DiskUsageMainData m_diskUsageMainData = new DiskUsageMainData();

  private class DiskUsageMainData
  {
    private FullScreen mi_fullScreen;
    private final Navigation mi_navigation = new Navigation();
    private final TreePaneData mi_treePaneData = new TreePaneData();
    private final TabPaneData mi_tabPaneData = new TabPaneData();
    private final SizePane mi_sizeTab = new SizePane();
    private final Top50Pane mi_top50Tab = new Top50Pane();
    private final SizeDistributionPane mi_sizeDistributionTab = new SizeDistributionPane();
    private final LastModifiedDistributionPane mi_modifiedDistributionTab = new LastModifiedDistributionPane();
    private final TypesPane mi_typesTab = new TypesPane();
    private final RecentFilesMenu mi_recentFiles = new RecentFilesMenu();
    private final PreferencesMenu mi_preferences = new PreferencesMenu();
    private final FilterPane mi_filterPane = new FilterPane();

    private DiskUsageMainData()
    {
    }

    TreeItem<FileNodeIF> getSelectedTreeItem()
    {
      if (mi_treePaneData == null || mi_treePaneData.mi_treeTableView == null)
      {
        return null;
      }
      return mi_treePaneData.mi_treeTableView.getSelectionModel().getSelectedItem();
    }

    public void addFilter(Filter filter, boolean activateFilterImmediately)
    {
      mi_filterPane.addFilter(filter, activateFilterImmediately);
    }
  }

  public DiskUsageView(Stage stage)
  {
    VBox toolBars;
    SplitPane splitPane;

    getProps().getDouble(Property.HEIGHT, 0);

    m_diskUsageMainData.mi_fullScreen = new FullScreen(stage);

    toolBars = new VBox(createMenuBar(), createToolBar());
    splitPane = new SplitPane();

    m_diskUsageMainData.mi_tabPaneData.init();

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

    menu = translate(new Menu("File"));
    menu.getItems().addAll(createScanFileTreeMenuItem(), createRecentFilesMenu(), createPreferencesMenuItem(),
        createExitMenuItem());
    menuBar.getMenus().add(menu);

    return menuBar;
  }

  private ToolBar createToolBar()
  {
    ToolBar toolBar;
    Navigation navigation;
    Button homeButton;
    Button forwardButton;
    Button backButton;
    Button refreshButton;
    ToggleButton fullScreenButton;
    ToggleButton showFileSizeButton;
    ToggleButton showFileCountButton;
    ToggleGroup showDisplayMetricGroup;
    SegmentedButton showDisplayMetricButton;
    ToggleButton sortAlphabeticallyButton;
    ToggleButton sortNumericButton;
    ToggleGroup sortGroup;
    SegmentedButton sortButton;
    Node filterPaneNode;

    toolBar = new ToolBar();
    navigation = m_diskUsageMainData.mi_navigation;

    backButton = new Button("", IconUtil.createIconNode("arrow-left", IconSize.SMALL));
    backButton.disableProperty().bind(navigation.backNavigationDisabledProperty());
    backButton.setOnAction((e) -> navigation.back());

    forwardButton = new Button("", IconUtil.createIconNode("arrow-right", IconSize.SMALL));
    forwardButton.disableProperty().bind(navigation.forwardNavigationDisabledProperty());
    forwardButton.setOnAction((e) -> navigation.forward());

    homeButton = new Button("", IconUtil.createIconNode("home", IconSize.SMALL));
    homeButton.disableProperty().bind(navigation.homeNavigationDisabledProperty());
    homeButton.setOnAction((e) -> navigation.home());

    refreshButton = new Button("", IconUtil.createIconNode("refresh", IconSize.SMALL));
    refreshButton.setOnAction((e) -> System.out.println("refresh"));

    fullScreenButton = new ToggleButton("", IconUtil.createIconNode("fullscreen", IconSize.SMALL));
    fullScreenButton.setOnAction((e) -> m_diskUsageMainData.mi_fullScreen.toggleFullScreen());

    showDisplayMetricGroup = new ToggleGroup();

    showFileSizeButton = new ToggleButton("", IconUtil.createIconNode("sigma", IconSize.SMALL));
    showFileSizeButton.setTooltip(translate(new Tooltip("Show file size")));
    showFileSizeButton.setToggleGroup(showDisplayMetricGroup);
    System.out.println(AppPreferences.displayMetricPreference.get());
    showFileSizeButton.setSelected(DisplayMetric.FILE_SIZE == AppPreferences.displayMetricPreference.get());
    showFileSizeButton.setOnAction((e) -> {
      AppPreferences.displayMetricPreference.set(DisplayMetric.FILE_SIZE);
    });

    showFileCountButton = new ToggleButton("", IconUtil.createIconNode("tally-mark-5", IconSize.SMALL));
    showFileCountButton.setTooltip(translate(new Tooltip("Show number of files")));
    showFileCountButton.setToggleGroup(showDisplayMetricGroup);
    showFileCountButton.setSelected(DisplayMetric.FILE_COUNT == AppPreferences.displayMetricPreference.get());
    showFileCountButton.setOnAction((e) -> {
      AppPreferences.displayMetricPreference.set(DisplayMetric.FILE_COUNT);
    });

    showDisplayMetricButton = new SegmentedButton();
    showDisplayMetricButton.getButtons().addAll(showFileSizeButton, showFileCountButton);

    sortGroup = new ToggleGroup();

    sortNumericButton = new ToggleButton("", IconUtil.createIconNode("sort-numeric-ascending", IconSize.SMALL));
    sortNumericButton.setToggleGroup(sortGroup);
    sortNumericButton.setOnAction((e) -> {
      AppPreferences.sortPreference.set(Sort.NUMERIC);
      Translator.getInstance().changeLocale(new Locale("nl"));
    });

    sortAlphabeticallyButton = new ToggleButton("",
        IconUtil.createIconNode("sort-alphabetical-ascending", IconSize.SMALL));
    sortAlphabeticallyButton.setToggleGroup(sortGroup);
    sortAlphabeticallyButton.setOnAction((e) -> {
      AppPreferences.sortPreference.set(Sort.ALPHABETICALLY);
      Translator.getInstance().changeLocale(Locale.CANADA);
    });

    sortButton = new SegmentedButton();
    sortButton.getButtons().addAll(sortAlphabeticallyButton, sortNumericButton);

    filterPaneNode = createFilterPane().getNode();
    filterPaneNode.setId("filterButton");
    HBox.setHgrow(filterPaneNode, Priority.ALWAYS);

    toolBar.getItems().addAll(backButton, forwardButton, homeButton, FxUtil.createHorizontalSpacer(20),
        fullScreenButton, refreshButton, FxUtil.createHorizontalSpacer(20), showDisplayMetricButton,
        FxUtil.createHorizontalSpacer(20), sortButton, FxUtil.createHorizontalSpacer(20), filterPaneNode);

    return toolBar;
  }

  private MenuItem createScanFileTreeMenuItem()
  {
    MenuItem menuItem;

    menuItem = translate(new MenuItem("Scan directory"));
    menuItem.setGraphic(IconUtil.createIconNode("file-search", IconSize.SMALLER));
    menuItem.setOnAction(e -> {
      scanDirectory(new ScanFileTreeDialog().chooseDirectory(m_diskUsageMainData.mi_fullScreen.getCurrentStage()));
    });

    return menuItem;
  }

  private Menu createRecentFilesMenu()
  {
    return m_diskUsageMainData.mi_recentFiles.createMenu();
  }

  private MenuItem createPreferencesMenuItem()
  {
    return m_diskUsageMainData.mi_preferences.createMenuItem();
  }

  private MenuItem createExitMenuItem()
  {
    MenuItem menuItem;

    menuItem = translate(new MenuItem("Exit"));
    menuItem.setGraphic(IconUtil.createIconNode("exit-to-app", IconSize.SMALLER));
    menuItem.setOnAction(e -> {
      System.exit(0);
    });

    return menuItem;
  }

  private FilterPane createFilterPane()
  {
    return m_diskUsageMainData.mi_filterPane;
  }

  private class FilterPane
  {
    private final Set<Filter> mi_filterSet = new LinkedHashSet<>();
    private final Map<String, Set<Filter>> mi_filterByTypeMap = new HashMap<>();
    private final Map<String, Pane> mi_filterTypePaneByTypeMap = new HashMap<>();
    private final HBox mi_filterActivationPane = new HBox();
    private final HBox mi_filterPane = new HBox();
    private final BorderPane contentPane;
    private Button mi_activateFilterButton;
    private Button mi_cancelFilterButton;
    private Button mi_clearFilterButton;

    private FilterPane()
    {
      HBox filterTextPane;

      contentPane = new BorderPane();

      filterTextPane = new HBox();
      filterTextPane.setPadding(new Insets(5, 10, 5, 10));
      filterTextPane.setAlignment(Pos.CENTER);
      filterTextPane.getChildren()
          .add(translate(new Label("Filter", IconUtil.createIconNode("filter", IconSize.SMALLER))));

      mi_filterPane.setId("filterPane");

      mi_filterActivationPane.setPadding(new Insets(0, 10, 0, 10));
      mi_filterActivationPane.setAlignment(Pos.CENTER);
      mi_filterActivationPane.setSpacing(2);

      contentPane.setLeft(filterTextPane);
      contentPane.setCenter(mi_filterPane);
      contentPane.setRight(mi_filterActivationPane);

      updateFilterActivationPane();
    }

    public Node getNode()
    {
      return contentPane;
    }

    private Set<Filter> getFilterSet()
    {
      return mi_filterSet;
    }

    public void addFilter(Filter filter, boolean activateFilterImmediately)
    {
      mi_filterSet.add(filter);

      reInitFilterPanel();

      if (activateFilterImmediately && mi_activateFilterButton != null)
      {
        mi_activateFilterButton.fire();
      }
    }

    private void reInitFilterPanel()
    {
      mi_filterPane.getChildren().clear();
      mi_filterActivationPane.getChildren().clear();
      mi_filterByTypeMap.clear();
      mi_filterTypePaneByTypeMap.clear();

      mi_filterSet.forEach(filter -> {
        if (mi_filterByTypeMap.computeIfAbsent(filter.getFilterType(), (k) -> new HashSet<>()).add(filter))
        {
          Pane filterTypePane;
          Node filterValueNode;

          filterValueNode = createFilterValueNode(filter);

          filterTypePane = mi_filterTypePaneByTypeMap.computeIfAbsent(filter.getFilterType(),
              filterType -> createFilterTypePane(filter));
          if (mi_filterByTypeMap.get(filter.getFilterType()).size() > 1)
          {
            filterTypePane.getChildren().add(getFilterTypePaneText("or"));
          }
          filterTypePane.getChildren().add(filterValueNode);
        }
      });

      updateFilterActivationPane();
    }

    private Label getFilterTypePaneText(String text)
    {
      Label label;

      label = translate(new Label(text));
      label.setId("filterTypePaneText");

      return label;
    }

    private Pane createFilterTypePane(Filter filter)
    {
      HBox filterPane;

      filterPane = new HBox();
      filterPane.setId("filterTypePane");

      filterPane.getChildren()
          .add(getFilterTypePaneText(translate(filter.getFilterType()) + " " + translate("is") + " "));

      mi_filterPane.getChildren().add(filterPane);

      return filterPane;
    }

    private Node createFilterValueNode(Filter filter)
    {
      Label filterNode;
      Node closeNode;

      // Wrap in HBox because adding a mouselistener to the ImageView does not work!
      // (Bug in JavaFX!)
      closeNode = new HBox(new FxIcon("close").size(IconSize.SMALLER).fillColor(IconColor.RED).getImageView());
      closeNode.setStyle("-fx-padding:2px 0px 1px 0px;");

      filterNode = translate(new Label(filter.getFilterValue(), closeNode));
      filterNode.setId("filterButton");
      filterNode.setContentDisplay(ContentDisplay.RIGHT);
      filterNode.setUserData(filter);
      filterNode.disableProperty().bind(filter.disabledProperty());

      closeNode.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
        removeFilters(filter);
      });

      return filterNode;
    }

    public void removeFilters(Filter... filters)
    {
      Stream.of(filters).forEach(filter -> {
        mi_filterSet.remove(filter);
      });

      reInitFilterPanel();
    }

    public boolean accept(FileNodeIF fileNode)
    {
      Set<Entry<String, Set<Filter>>> entries;

      entries = mi_filterByTypeMap.entrySet();
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
          mi_activateFilterButton = translate(
              new Button("Activate filter", IconUtil.createIconNode("filter-plus", IconSize.SMALL)));
          mi_activateFilterButton.setOnAction((ae) -> getFilterSet().forEach(filter -> {
            filter.disable(false);
            m_diskUsageMainData.mi_treePaneData.setFilter((fn) -> accept(fn));
            updateFilterActivationPane();
          }));
          mi_filterActivationPane.getChildren().add(mi_activateFilterButton);

          mi_cancelFilterButton = translate(
              new Button("Cancel filter", IconUtil.createIconNode("filter-minus", IconSize.SMALL)));
          mi_cancelFilterButton.setOnAction((ae) -> {
            removeFilters(getFilterSet().stream().filter(Filter::isDisabled).toArray(Filter[]::new));
          });
          mi_filterActivationPane.getChildren().add(mi_cancelFilterButton);

          mi_clearFilterButton = translate(
              new Button("Clear filter", IconUtil.createIconNode("filter-remove", IconSize.SMALL)));
          mi_clearFilterButton.setOnAction((ae) -> {
            removeFilters(getFilterSet().stream().toArray(Filter[]::new));
            m_diskUsageMainData.mi_treePaneData.setFilter((fn) -> accept(fn));
          });
          mi_filterActivationPane.getChildren().add(mi_clearFilterButton);
        }

        disabled = getFilterSet().stream().filter(filter -> filter.isDisabled()).findFirst().isPresent();
        mi_activateFilterButton.setDisable(!disabled);
        mi_cancelFilterButton.setDisable(!disabled);
      }
      else
      {
        if (!mi_filterActivationPane.getChildren().isEmpty())
        {
          mi_filterActivationPane.getChildren().clear();
          mi_activateFilterButton = null;
          mi_cancelFilterButton = null;
        }
      }
    }
  }

  public class Filter
      implements FilterIF
  {
    private final int mi_hashCode;
    private final String mi_filterType;
    private final String mi_filterValue;
    private final Predicate<FileNodeIF> mi_fileNodePredicate;
    private BooleanProperty mi_filterDisabled = new SimpleBooleanProperty(true);

    public Filter(String filterName, String filterValue, Predicate<FileNodeIF> fileNodePredicate)
    {
      mi_filterType = filterName;
      mi_filterValue = filterValue;
      mi_fileNodePredicate = fileNodePredicate;
      mi_hashCode = (filterName + filterValue).hashCode();
    }

    @Override
    public boolean accept(FileNodeIF fileNode)
    {
      return getPredicate().test(fileNode);
    }

    public void disable(boolean disable)
    {
      mi_filterDisabled.set(disable);
    }

    public BooleanProperty disabledProperty()
    {
      return mi_filterDisabled;
    }

    public boolean isDisabled()
    {
      return mi_filterDisabled.get();
    }

    public String getFilterType()
    {
      return mi_filterType;
    }

    public String getFilterValue()
    {
      return mi_filterValue;
    }

    public Predicate<FileNodeIF> getPredicate()
    {
      return mi_fileNodePredicate;
    }

    @Override
    public int hashCode()
    {
      return mi_hashCode;
    }

    @Override
    public boolean equals(Object obj)
    {
      Filter filter;

      if (obj == null || !(obj instanceof Filter))
      {
        return false;
      }

      filter = (Filter) obj;
      if (!filter.getFilterType().equals(getFilterType()))
      {
        return false;
      }

      return filter.getFilterValue().equals(getFilterValue());
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
      mi_treeTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
        m_diskUsageMainData.mi_tabPaneData.itemSelected();
      });
      mi_breadCrumbBar.selectedCrumbProperty().bind(mi_treeTableView.getSelectionModel().selectedItemProperty());
      mi_breadCrumbBar.setAutoNavigationEnabled(false);
      mi_breadCrumbBar.setOnCrumbAction((e) -> {
        m_diskUsageMainData.mi_navigation.navigateTo(e.getSelectedCrumb());
      });

      mi_treePane.setCenter(mi_treeTableView);
    }

    public void navigateTo(TreeItem<FileNodeIF> treeItem)
    {
      if (treeItem == null)
      {
        return;
      }

      if (mi_treeTableView != null)
      {
        Tab selectedTab;

        // Make sure the path to select is expanded, because selection alone doesn't
        // expand the tree
        TreeItem<FileNodeIF> parentTreeItem = treeItem;
        while ((parentTreeItem = parentTreeItem.getParent()) != null)
        {
          parentTreeItem.setExpanded(true);
        }

        mi_treeTableView.getSelectionModel().select(treeItem);

        // Scroll to the selected item to make sure it is visible for the user
        // mi_treeTableView.scrollTo(mi_treeTableView.getSelectionModel().getSelectedIndex());

        m_diskUsageMainData.mi_tabPaneData.mi_contentByTabId.clear();
        selectedTab = m_diskUsageMainData.mi_tabPaneData.mi_tabPane.getSelectionModel().getSelectedItem();
        m_diskUsageMainData.mi_tabPaneData.fillContent(selectedTab, m_diskUsageMainData.getSelectedTreeItem());

        Platform.runLater(() -> mi_treeTableView.requestFocus());
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
      DISTRIBUTION_SIZE("Size distribution", "chart-bell-curve", TabPaneData::fillSizeDistributionTab),
      DISTRIBUTION_MODIFIED("Last modified", "sort-calendar-ascending", TabPaneData::fillModifiedDistributionTab),
      DISTRIBUTION_TYPES("Types", "chart-pie", TabPaneData::fillTypeDistributionTab);

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

        tab = translate(new Tab(getName()));
        tab.setUserData(this);
        tab.setClosable(false);
        if (getIconName() != null)
        {
          Node icon;

          icon = IconUtil.createIconNode(getIconName(), IconSize.SMALL);
          icon.prefHeight(300);
          tab.setGraphic(icon);
        }

        return tab;
      }

      Node fillContent(DiskUsageMainData mainData)
      {
        return m_fillContent.apply(mainData);
      }
    }

    private final BorderPane mi_borderPane;
    private final TabPane mi_tabPane;
    private Map<TabData, Tab> mi_tabByTabId = new HashMap<>();
    private Map<TabData, Node> mi_contentByTabId = new HashMap<>();

    private TabPaneData()
    {
      mi_tabPane = new TabPane();
      mi_borderPane = new BorderPane();
      mi_borderPane.setCenter(mi_tabPane);
    }

    public void init()
    {
      String selectedTabDataName;

      Stream.of(TabPaneData.TabData.values()).forEach(this::createTab);

      mi_tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
        // Fill the new selected tab with data.
        fillContent(newTab, m_diskUsageMainData.getSelectedTreeItem());

        // Remember the last selected tab
        getProps().set(Property.SELECTED_ID, ((TabData) newTab.getUserData()).name());
      });

      // Select the tab that was in a previous version the last tab selected
      selectedTabDataName = getProps().getString(Property.SELECTED_ID, TabPaneData.TabData.values()[0].name());
      mi_tabPane.getTabs().stream().filter(tab -> ((TabData) tab.getUserData()).name().equals(selectedTabDataName))
          .findFirst().ifPresent(tab -> {
            mi_tabPane.getSelectionModel().select(tab);
          });
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

    public void fillContent(Tab tab, TreeItem<FileNodeIF> item)
    {
      Optional<Entry<TabData, Tab>> tabEntry;

      tabEntry = mi_tabByTabId.entrySet().stream().filter(entry -> tab.equals(entry.getValue())).findFirst();
      if (tabEntry.isPresent() && item != null)
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
      m_diskUsageMainData.mi_tabPaneData.fillContent(selectedTab, m_diskUsageMainData.getSelectedTreeItem());

      m_diskUsageMainData.mi_navigation.navigateTo(m_diskUsageMainData.getSelectedTreeItem());
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
      m_paneTypeByIdMap.values().stream().map(paneType -> {
        ToggleButton button;

        button = new ToggleButton();
        button.setTooltip(new Tooltip(paneType.description()));
        button.setGraphic(IconUtil.createIconNode(paneType.iconName(), IconSize.SMALLER));
        button.setUserData(paneType);
        button.setOnAction((ae) -> {
          setCurrentPaneType((PaneType) ((Node) ae.getSource()).getUserData());
        });
        if (mi_currentPaneType == paneType)
        {
          button.setSelected(true);
        }

        return button;
      }).forEach(button -> {
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
        mi_node.setCenter(mi_nodeByPaneTypeMap.computeIfAbsent(mi_currentPaneType, type -> {
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

    protected DisplayMetric getCurrentDisplayMetric()
    {
      return AppPreferences.displayMetricPreference.get();
    }

    Node getNode(TreePaneData treePaneData)
    {
      if (mi_currentTreePaneData != treePaneData || getCurrentTreeItem() != m_diskUsageMainData.getSelectedTreeItem())
      {
        mi_currentTreePaneData = treePaneData;
        mi_currentTreeItem = m_diskUsageMainData.getSelectedTreeItem();
        mi_nodeByPaneTypeMap.clear();
      }

      initCurrentNode();
      return mi_node;
    }

    protected void addFilter(Node node, String filterType, String filterValue, Predicate<FileNodeIF> fileNodePredicate)
    {
      node.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
        m_diskUsageMainData.addFilter(new Filter(filterType, filterValue, fileNodePredicate),
            event.getClickCount() == 2);
      });
    }

    public String getOtherText()
    {
      return translate("<Other>");
    }

    public String getNoneText()
    {
      return translate("<None>");
    }

    public abstract class PaneData
    {
      private TreeItem<FileNodeIF> mii_currentTreeItem;
      private DisplayMetric mii_currentDisplayMetric;

      private PaneData()
      {
      }

      public abstract void currentTreeItemChanged();

      public abstract void currentDisplayMetricChanged();

      public DisplayMetric getCurrentDisplayMetric()
      {
        return mii_currentDisplayMetric;
      }

      protected void checkInitData()
      {
        if (mii_currentTreeItem != m_diskUsageMainData.getSelectedTreeItem())
        {
          mii_currentTreeItem = m_diskUsageMainData.getSelectedTreeItem();
          currentTreeItemChanged();
        }

        if (mii_currentDisplayMetric != AbstractTabContentPane.this.getCurrentDisplayMetric())
        {
          mii_currentDisplayMetric = AbstractTabContentPane.this.getCurrentDisplayMetric();
          currentDisplayMetricChanged();
        }
      }
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

        record MyData(PieChart.Data pieChartData, TreeItem<FileNodeIF> treeItem) {
        }

        chart = FxUtil.createPieChart();
        treeItem.getChildren().stream().filter(item -> {
          return item.getValue().getSize() > minimumDataSize;
        }).limit(10).map(item -> {
          PieChart.Data data;

          data = new PieChart.Data(item.getValue().getName(), item.getValue().getSize());
          data.nameProperty().bind(Bindings.concat(data.getName(), "\n",
              AppPreferences.sizeSystemPreference.get().getFileSize(data.getPieValue())));

          return new MyData(data, item);
        }).forEach(tuple -> {
          chart.getData().add(tuple.pieChartData);
          tuple.pieChartData.getNode().setUserData(tuple.treeItem);
          tuple.pieChartData.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, (me) -> {
            m_diskUsageMainData.mi_navigation.navigateTo(tuple.treeItem);
          });
        });

        if (chart.getData().size() != treeItem.getChildren().size())
        {
          sum = chart.getData().stream().map(data -> data.getPieValue()).reduce(0.0d, Double::sum);
          chart.getData().add(new PieChart.Data(getOtherText(), treeItem.getValue().getSize() - sum));
        }

        return chart;
      }

      return translate(new Label("No data"));
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

        numberOfLinksColumn = table.addColumn("Number\nof links\nto file");
        numberOfLinksColumn.initPersistentPrefWidth(100.0);
        numberOfLinksColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
        numberOfLinksColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
        numberOfLinksColumn.setCellValueGetter((owi) -> owi.getObject().getNumberOfLinks());

        pathColumn = table.addColumn("Path");
        pathColumn.setCellValueGetter((owi) -> owi.getObject().getParentPath());

        table.setItems(list);

        return table;
      }

      return translate(new Label("No data"));
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
        return translate(mi_text);
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
        record MyData(Long numberOfFiles, Long sizeOfFiles) {
        }
        Map<SizeDistributionBucket, MyData> map;
        MyData dataDefault = new MyData(0l, 0l);

        pane = new GridPane();

        node = treeItem.getValue();
        map = node.streamNode().filter(FileNodeIF::isFile).map(FileNodeIF::getSize)
            .collect(Collectors.groupingBy(SizeDistributionBucket::findBucket,
                Collectors.teeing(Collectors.counting(), Collectors.summingLong(a -> a / 1000000),
                    (numberOfFiles, sizeOfFiles) -> new MyData(numberOfFiles, sizeOfFiles))));

        xAxis = new NumberAxis();
        yAxis = new CategoryAxis();
        barChart = FxUtil.createBarChart(xAxis, yAxis);
        barChart.setTitle(translate("Distribution of file sizes in") + " " + treeItem.getValue().getName());
        xAxis.setLabel(translate("Number of files"));
        yAxis.setLabel(translate("File sizes"));

        series1 = new XYChart.Series<>();
        Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
          MyData value;
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
        xAxis.setLabel(translate("Total size of files (in Gb)"));
        yAxis.setLabel(translate("File sizes"));

        series2 = new XYChart.Series<>();
        Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
          MyData value;
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
    private final long mi_todayMidnight = CommonUtil.getMidnight();

    private enum LastModifiedDistributionBucket
    {
      INVALID(() -> translate("Invalid"), Long.MAX_VALUE - 1l, Long.MAX_VALUE),
      LAST_MODIFIED_FUTURE(() -> translate("In the future"), -Long.MAX_VALUE, 0),
      LAST_MODIFIED_TODAY(() -> translate("Today"), days(0), days(1)),
      LAST_MODIFIED_YESTERDAY(() -> translate("Yesterday"), days(1), days(2)),
      LAST_MODIFIED_1_DAY_TILL_7_DAYS(() -> "2 - 7 " + translate("days"), days(2), days(8)),
      LAST_MODIFIED_7_DAYs_TILL_30_DAYS(() -> "7 - 30 " + translate("days"), days(8), days(31)),
      LAST_MODIFIED_30_DAYS_TILL_90_DAYS(() -> "30 - 90 " + translate("days"), days(31), days(91)),
      LAST_MODIFIED_90_DAYS_TILL_180_DAYS(() -> "90 - 180 " + translate("days"), days(91), days(181)),
      LAST_MODIFIED_180_DAYS_TILL_365_DAYS(() -> "180 - 365 " + translate("days"), days(181), years(1)),
      LAST_MODIFIED_1_YEAR_TILL_2_YEAR(() -> "1 - 2 " + translate("years"), years(1), years(2)),
      LAST_MODIFIED_2_YEAR_TILL_3_YEAR(() -> "2 - 3 " + translate("years"), years(2), years(3)),
      LAST_MODIFIED_3_YEAR_TILL_6_YEAR(() -> "3 - 6 " + translate("years"), years(3), years(6)),
      LAST_MODIFIED_6_YEAR_TILL_10_YEAR(() -> "6 - 10 " + translate("years"), years(6), years(10)),
      LAST_MODIFIED_OVER_10_YEARS(() -> translate("Over") + " 10 " + translate("years"), years(10), Long.MAX_VALUE);

      private final Supplier<String> mi_text;
      private final long mi_from;
      private final long mi_to;

      LastModifiedDistributionBucket(Supplier<String> text, long from, long to)
      {
        mi_text = text;
        mi_from = from;
        mi_to = to;
      }

      public String getText()
      {
        return mi_text.get();
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
        record MyData(Long numberOfFiles, Long sizeOfFiles) {
        }
        Map<LastModifiedDistributionBucket, MyData> map;
        ObservableList<Entry<LastModifiedDistributionBucket, MyData>> list;
        MyTableView<Entry<LastModifiedDistributionBucket, MyData>> table;
        MyTableColumn<Entry<LastModifiedDistributionBucket, MyData>, String> timeIntervalColumn;
        MyTableColumn<Entry<LastModifiedDistributionBucket, MyData>, Long> sumOfFileSizesColumn;
        MyTableColumn<Entry<LastModifiedDistributionBucket, MyData>, Long> numberOfFilesColumn;

        pane = new GridPane();

        node = treeItem.getValue();
        map = node.streamNode().filter(FileNodeIF::isFile)
            .collect(Collectors.groupingBy(this::findBucket,
                Collectors.teeing(Collectors.counting(),
                    Collectors.summingLong(fileNode -> fileNode.getSize() / 1000000),
                    (numberOfFiles, sizeOfFiles) -> new MyData(numberOfFiles, sizeOfFiles))));

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

    private LastModifiedDistributionBucket findBucket(FileNodeIF fileNode)
    {
      return LastModifiedDistributionBucket.findBucket(mi_todayMidnight, fileNode.getLastModifiedTime());
    }

    Node getPieChartNode()
    {
      return new Label("Bar chart");
    }

    Node getBarChartNode()
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
        record MyData(Long numberOfFiles, Long sizeOfFiles) {
        }
        Map<LastModifiedDistributionBucket, MyData> map;
        MyData dataDefault = new MyData(0l, 0l);

        pane = new GridPane();

        node = treeItem.getValue();
        map = node.streamNode().filter(FileNodeIF::isFile)
            .collect(Collectors.groupingBy(this::findBucket, LinkedHashMap::new,
                Collectors.teeing(Collectors.counting(),
                    Collectors.summingLong(fileNode -> fileNode.getSize() / 1000000),
                    (numberOfFiles, sizeOfFiles) -> new MyData(numberOfFiles, sizeOfFiles))));

        xAxis = new NumberAxis();
        yAxis = new CategoryAxis();
        barChart = FxUtil.createBarChart(xAxis, yAxis);
        barChart.setTitle(translate("Distribution of last modified dates in") + " " + treeItem.getValue().getName());
        xAxis.setLabel(translate("Number of files"));
        yAxis.setLabel(translate("Last modified date"));

        series1 = new XYChart.Series<>();
        barChart.getData().add(series1);

        Stream.of(LastModifiedDistributionBucket.values()).forEach(bucket -> {
          MyData value;
          XYChart.Data<Number, String> data;

          value = map.getOrDefault(bucket, dataDefault);
          data = new XYChart.Data<Number, String>(value.numberOfFiles(), bucket.getText());
          series1.getData().add(data);
          addFilter(data.getNode(), "Modification date", bucket.getText(), fileNode -> bucket == findBucket(fileNode));
        });

        pane.add(barChart, 0, 0);
        GridPane.setHgrow(barChart, Priority.ALWAYS);
        GridPane.setVgrow(barChart, Priority.ALWAYS);

        xAxis = new NumberAxis();
        yAxis = new CategoryAxis();
        barChart = FxUtil.createBarChart(xAxis, yAxis);
        xAxis.setLabel(translate("Total size of files (in Gb)"));
        yAxis.setLabel(translate("Last modified date"));

        series2 = new XYChart.Series<>();
        barChart.getData().add(series2);

        Stream.of(LastModifiedDistributionBucket.values()).forEach(bucket -> {
          MyData value;
          XYChart.Data<Number, String> data;

          value = map.getOrDefault(bucket, dataDefault);
          data = new XYChart.Data<Number, String>(value.sizeOfFiles(), bucket.getText());
          series2.getData().add(data);
          addFilter(data.getNode(), "Modification date", bucket.getText(), fileNode -> bucket == findBucket(fileNode));
        });

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
    private TypesPaneData mi_data = new TypesPaneData();

    private TypesPane()
    {
      createPaneType("PIECHART", "Show details table", "chart-pie", this::getPieChartNode);
      createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode);
      createPaneType("TABLE", "Show details table", "table", this::getTableNode);

      init();
    }

    Node getPieChartNode()
    {
      PieChart pieChart;

      pieChart = FxUtil.createPieChart();
      mi_data.getReducedMap().entrySet().forEach(entry -> {
        PieChart.Data data;
        String name;
        Predicate<FileNodeIF> test;

        name = entry.getKey() + "\n" + entry.getValue().getValueDescription(getCurrentDisplayMetric());
        data = new PieChart.Data(name, entry.getValue().getSize(getCurrentDisplayMetric()));
        pieChart.getData().add(data);

        if (!entry.getKey().equals(getOtherText()))
        {
          test = (fileNode) -> getFileType(fileNode.getName()).equals(entry.getKey());
        }
        else
        {
          test = (fileNode) -> !mi_data.getReducedMap().containsKey(getFileType(fileNode.getName()));
        }

        addFilter(data.getNode(), "File type", entry.getKey(), test);
      });

      return pieChart;
    }

    Node getBarChartNode()
    {
      BarChart<Number, String> barChart;
      XYChart.Series<Number, String> series1;
      ArrayList<Entry<String, FileAggregates>> fullList;
      GridPane gridPane;
      ScrollPane scrollPane;
      NumberAxis xAxis;
      CategoryAxis yAxis;

      scrollPane = new ScrollPane();
      scrollPane.setFitToWidth(true);
      gridPane = new GridPane();

      xAxis = new NumberAxis();
      xAxis.setSide(Side.TOP);
      yAxis = new CategoryAxis();
      barChart = FxUtil.createBarChart(xAxis, yAxis);
      barChart.setTitle(translate("Number of files"));
      GridPane.setHgrow(barChart, Priority.ALWAYS);
      GridPane.setVgrow(barChart, Priority.ALWAYS);

      series1 = new XYChart.Series<>();
      barChart.getData().add(series1);

      fullList = new ArrayList<>(mi_data.getFullMap().entrySet());
      Collections.reverse(fullList);
      fullList.forEach(entry -> {
        Data<Number, String> data;
        Predicate<FileNodeIF> test;

        data = new XYChart.Data<Number, String>(entry.getValue().getSize(getCurrentDisplayMetric()), entry.getKey());
        series1.getData().add(data);

        if (!entry.getKey().equals(getOtherText()))
        {
          test = (fileNode) -> getFileType(fileNode.getName()).equals(entry.getKey());
        }
        else
        {
          test = (fileNode) -> !mi_data.getReducedMap().containsKey(getFileType(fileNode.getName()));
        }

        addFilter(data.getNode(), "File type", entry.getKey(), test);
      });

      barChart.setPrefHeight(series1.getData().size() * 20);

      gridPane.add(barChart, 0, 0);
      scrollPane.setContent(gridPane);

      return scrollPane;
    }

    private class TypesPaneData
      extends PaneData
    {
      private Map<String, FileAggregates> mi_fullMap;
      private Map<String, FileAggregates> mi_reducedMap;

      private TypesPaneData()
      {
      }

      private Map<String, FileAggregates> getFullMap()
      {
        checkInitData();
        if (mi_fullMap == null)
        {
          mi_fullMap = getCurrentTreeItem().getValue().streamNode().filter(FileNodeIF::isFile)
              .collect(Collectors.groupingBy(fn -> TypesPane.this.getFileType(fn.getName()),
                  Collectors.teeing(Collectors.counting(), Collectors.summingLong(FileNodeIF::getSize),
                      (a, b) -> new FileAggregates(a, b))));
          mi_fullMap = mi_fullMap.entrySet().stream()
              .sorted(Comparator.comparing(e -> e.getValue().accumulatedSize, Comparator.reverseOrder()))
              .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (x, y) -> y, LinkedHashMap::new));
        }
        return mi_fullMap;
      }

      private Map<String, FileAggregates> getReducedMap()
      {
        checkInitData();
        if (mi_reducedMap == null)
        {
          long totalCount;
          double minimumCount;
          long otherCount;

          totalCount = getFullMap().values().stream().map(fa -> fa.getSize(getCurrentDisplayMetric())).reduce(0l,
              Long::sum);

          minimumCount = totalCount * 0.01; // Only types with a count larger than a percentage are shown
          mi_reducedMap = mi_fullMap.entrySet().stream()
              .filter(e -> e.getValue().getSize(getCurrentDisplayMetric()) > minimumCount).limit(10)
              .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (x, y) -> y, LinkedHashMap::new));

          otherCount = totalCount
              - mi_reducedMap.values().stream().map(fa -> fa.getSize(getCurrentDisplayMetric())).reduce(0l, Long::sum);
          if (otherCount != 0)
          {
            mi_reducedMap.put(getOtherText(), new FileAggregates(totalCount, otherCount));
          }
        }

        return mi_reducedMap;
      }

      @Override
      public void currentTreeItemChanged()
      {
        mi_fullMap = null;
        mi_reducedMap = null;
      }

      @Override
      public void currentDisplayMetricChanged()
      {
        mi_reducedMap = null;
      }
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

      return getNoneText();
    }
  }

  int counter = 0;

  private class RecentFilesMenu
  {
    private List<Menu> m_listenerList = new ArrayList<>();

    public Menu createMenu()
    {
      Menu menu;

      menu = translate(new Menu("Recent scans"));
      menu.setGraphic(IconUtil.createIconNode("history", IconSize.SMALLER));
      update(menu);
      m_listenerList.add(menu);

      return menu;
    }

    public void addFile(File file)
    {
      getProps().set(Property.RECENT_FILES,
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

      menuItem = translate(new MenuItem(path));
      menuItem.setGraphic(IconUtil.createIconNode("folder-outline", IconSize.SMALLER));
      menuItem.setOnAction(e -> {
        scanDirectory(path);
      });

      return menuItem;
    }
  }

  public void scanDirectory(String path)
  {
    scanDirectory(new ScanFileTreeDialog().scanDirectory(new File(path)));
  }

  private void scanDirectory(DirNode dirNode)
  {
    if (dirNode != null)
    {
      m_diskUsageMainData.mi_treePaneData.createTreeTableView(dirNode);
      m_diskUsageMainData.mi_recentFiles.addFile(new File(dirNode.getName()));
    }
  }

  private class PreferencesMenu
  {
    public MenuItem createMenuItem()
    {
      MenuItem menuItem;

      menuItem = translate(new MenuItem("Preferences"));
      menuItem.setGraphic(IconUtil.createIconNode("cog", IconSize.SMALLER));
      menuItem.setOnAction(e -> {
      });

      return menuItem;
    }
  }

  public record FileAggregates(long accumulatedSize, long fileCount) {
    public long getSize(DisplayMetric displayMetric)
    {
      switch (displayMetric)
      {
        case FILE_SIZE:
          return accumulatedSize();
        case FILE_COUNT:
          return fileCount();
        default:
          return -1;
      }
    }

    public String getValueDescription(DisplayMetric displayMetric)
    {
      switch (displayMetric)
      {
        case FILE_SIZE:
          return AppPreferences.sizeSystemPreference.get().getFileSize(accumulatedSize());
        case FILE_COUNT:
          return fileCount() + " files";
        default:
          return "";
      }
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

  private class FullScreen
  {
    private Stage mi_initialStage;
    private Stage mi_fullScreenStage;
    private Stage mi_currentStage;

    private FullScreen(Stage stage)
    {
      mi_initialStage = stage;
      mi_currentStage = mi_initialStage;
    }

    public Stage getCurrentStage()
    {
      return mi_currentStage;
    }

    private Stage getFullscreenStage()
    {
      if (mi_fullScreenStage == null)
      {
        mi_fullScreenStage = new Stage();
        mi_fullScreenStage.setTitle(mi_initialStage.getTitle());
        mi_fullScreenStage.setMaximized(true);
        mi_fullScreenStage.setResizable(false);
        mi_fullScreenStage.initStyle(StageStyle.UNDECORATED);
      }

      return mi_fullScreenStage;
    }

    public void toggleFullScreen()
    {
      if (OperatingSystemUtil.isLinux())
      {
        Scene currentScene;

        currentScene = mi_currentStage.getScene();
        mi_currentStage.setScene(null);
        mi_currentStage.hide();
        mi_currentStage = mi_initialStage == mi_currentStage ? getFullscreenStage() : mi_initialStage;
        mi_currentStage.setScene(currentScene);
        mi_currentStage.show();
      }
      else
      {
        mi_initialStage.setFullScreen(!mi_initialStage.isFullScreen());
      }
    }
  }

  private class Navigation
  {
    private List<NavigationItem> mi_navigationItemList = new ArrayList<NavigationItem>();
    private NavigationItem mi_currentNavigationItem;
    private BooleanProperty mi_homeNavigationDisabled = new SimpleBooleanProperty(true);
    private BooleanProperty mi_forwardNavigationDisabled = new SimpleBooleanProperty(true);
    private BooleanProperty mi_backNavigationDisabled = new SimpleBooleanProperty(true);
    private NavigationItem mi_navigatingTo;

    private Navigation()
    {
    }

    public void navigateTo(TreeItem<FileNodeIF> treeItem)
    {
      NavigationItem item;

      if (mi_navigatingTo != null)
      {
        return;
      }

      item = new NavigationItem(treeItem);
      mi_navigationItemList.add(item);

      item.navigateTo();
    }

    public void home()
    {
      navigateTo(0);
    }

    public void forward()
    {
      navigateTo(mi_navigationItemList.indexOf(mi_currentNavigationItem) + 1);
    }

    public void back()
    {
      navigateTo(mi_navigationItemList.indexOf(mi_currentNavigationItem) - 1);
    }

    private void navigateTo(int toIndex)
    {
      if (mi_navigatingTo == null && toIndex >= 0 && toIndex < mi_navigationItemList.size())
      {
        mi_navigatingTo = mi_navigationItemList.get(toIndex);
        try
        {
          mi_navigatingTo.navigateTo();
        }
        finally
        {
          mi_navigatingTo = null;
        }
      }
    }

    public BooleanProperty homeNavigationDisabledProperty()
    {
      return mi_homeNavigationDisabled;
    }

    public BooleanProperty backNavigationDisabledProperty()
    {
      return mi_backNavigationDisabled;
    }

    public BooleanProperty forwardNavigationDisabledProperty()
    {
      return mi_forwardNavigationDisabled;
    }

    private class NavigationItem
    {
      private final TreeItem<FileNodeIF> mii_treeItem;

      private NavigationItem(TreeItem<FileNodeIF> treeItem)
      {
        mii_treeItem = treeItem;
      }

      public void navigateTo()
      {
        int currentIndex;

        currentIndex = mi_navigationItemList.indexOf(this);

        mi_currentNavigationItem = this;
        mi_homeNavigationDisabled.set(mi_navigationItemList.isEmpty());
        mi_forwardNavigationDisabled.set(currentIndex < 0 || currentIndex >= (mi_navigationItemList.size() - 1));
        mi_backNavigationDisabled.set(currentIndex <= 0);

        m_diskUsageMainData.mi_treePaneData.navigateTo(mii_treeItem);
      }
    }
  }
}
