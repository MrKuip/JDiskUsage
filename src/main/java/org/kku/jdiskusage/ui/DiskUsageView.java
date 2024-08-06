package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import static org.kku.jdiskusage.ui.util.TranslateUtil.translatedTextProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.controlsfx.control.SegmentedButton;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.javafx.scene.control.DraggableTabPane;
import org.kku.jdiskusage.ui.ScanFileTreeDialog.ScanResult;
import org.kku.jdiskusage.ui.common.AbstractFormPane;
import org.kku.jdiskusage.ui.common.Filter;
import org.kku.jdiskusage.ui.common.FullScreen;
import org.kku.jdiskusage.ui.common.Navigation;
import org.kku.jdiskusage.ui.common.Notifications;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.AppProperties.AppProperty;
import org.kku.jdiskusage.util.AppSettings;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Log;
import org.kku.jdiskusage.util.PathList;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import org.kku.jdiskusage.util.RecentScanList;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.kku.jdiskusage.util.preferences.DisplayMetric;
import org.kku.jdiskusage.util.preferences.Sort;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DiskUsageView
{
  private DiskUsageData m_data = new DiskUsageData();
  private MigPane m_content = new MigPane();

  public class DiskUsageData
  {
    private FullScreen mi_fullScreen;
    private final ObjectProperty<TreeItem<FileNodeIF>> mi_selectedTreeItemProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Tab> mi_selectedTabProperty = new SimpleObjectProperty<>();
    private final Navigation mi_navigation = new Navigation(this);
    private final FileTreePane mi_treePaneData = new FileTreePane(this);
    private final TabPaneData mi_tabPaneData = new TabPaneData();
    private final SizeFormPane mi_sizeTab = new SizeFormPane(this);
    private final TopRankingFormPane mi_topRankingTab = new TopRankingFormPane(this);
    private final LinkCountFormPane mi_linkCountTab = new LinkCountFormPane(this);
    private final SizeDistributionFormPane mi_sizeDistributionTab = new SizeDistributionFormPane(this);
    private final LastModifiedDistributionFormPane mi_modifiedDistributionTab = new LastModifiedDistributionFormPane(
        this);
    private final TypesFormPane mi_typesTab = new TypesFormPane(this);
    private final SearchFormPane mi_searchTab = new SearchFormPane(this);
    private final HelpFormPane mi_helpTab = new HelpFormPane(this);
    private final RecentFilesMenu mi_recentFiles = new RecentFilesMenu();
    private final PreferencesMenu mi_preferences = new PreferencesMenu();
    private final FilterPane mi_filterPane = new FilterPane(this);
    private final Notifications mi_taskView = Notifications.getInstance();
    //

    private DiskUsageData()
    {
    }

    public void refresh()
    {
      mi_tabPaneData.refresh();
    }

    public ObjectProperty<Tab> selectedTabProperty()
    {
      return mi_selectedTabProperty;
    }

    public ObjectProperty<TreeItem<FileNodeIF>> selectedTreeItemProperty()
    {
      return mi_selectedTreeItemProperty;
    }

    public TreeItem<FileNodeIF> getSelectedTreeItem()
    {
      return selectedTreeItemProperty().get();
    }

    public void addFilter(Filter filter, boolean activateFilterImmediately)
    {
      mi_filterPane.addFilter(filter, activateFilterImmediately);
    }

    public Navigation getNavigation()
    {
      return mi_navigation;
    }

    public TabPaneData getTabPaneData()
    {
      return mi_tabPaneData;
    }

    public FileTreePane getTreePaneData()
    {
      return mi_treePaneData;
    }
  }

  public DiskUsageView(Stage stage)
  {
    VBox toolBars;
    SplitPane splitPane;

    m_data.mi_fullScreen = new FullScreen(stage);

    toolBars = new VBox(createMenuBar(), createToolBar());
    splitPane = new SplitPane();

    m_data.getTabPaneData().init();

    splitPane.getItems().addAll(m_data.getTreePaneData().getNode(), m_data.getTabPaneData().getNode());
    splitPane.getDividers().get(0).positionProperty().addListener(getSplitPaneProperty().getChangeListener());
    SplitPane.setResizableWithParent(m_data.getTreePaneData().getNode(), false);
    SplitPane.setResizableWithParent(m_data.getTabPaneData().getNode(), false);

    splitPane.getDividers().get(0).setPosition(getSplitPaneProperty().get(0.40));

    m_content.add(toolBars, "dock north");
    m_content.add(splitPane, "dock center");
    // This places the taskview in the lower right corner of the content pane.
    // container.x2 = the leftmost point of the container
    // container.y2 = the lowest point of the container
    m_content.add(m_data.mi_taskView.getView(), "pos null null container.x2 container.y2");

    m_data.getTabPaneData().getNode().setTop(m_data.getTreePaneData().createBreadCrumbBar());
  }

  public Pane getContent()
  {
    return m_content;
  }

  private MenuBar createMenuBar()
  {
    MenuBar menuBar;
    Menu menu;

    menuBar = new MenuBar();

    menu = translate(new Menu("File"));
    menu.getItems().addAll(createScanFileTreeMenuItem(), createRecentFilesMenu(), createPreferencesMenuItem(),
        createExitMenuItem());
    menuBar.getMenus().addAll(menu);

    return menuBar;
  }

  private Pane createToolBar()
  {
    MigPane toolBar;
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

    navigation = m_data.getNavigation();

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
    refreshButton.setOnAction((e) -> m_data.refresh());

    fullScreenButton = new ToggleButton("", IconUtil.createIconNode("fullscreen", IconSize.SMALL));
    fullScreenButton.setOnAction((e) -> m_data.mi_fullScreen.toggleFullScreen());

    showDisplayMetricGroup = new ToggleGroup();

    showFileSizeButton = new ToggleButton("", IconUtil.createIconNode("sigma", IconSize.SMALL));
    showFileSizeButton.setTooltip(translate(new Tooltip("Show file size")));
    showFileSizeButton.setToggleGroup(showDisplayMetricGroup);
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

    sortAlphabeticallyButton = new ToggleButton("",
        IconUtil.createIconNode("sort-alphabetical-ascending", IconSize.SMALL));
    sortAlphabeticallyButton.setToggleGroup(sortGroup);
    sortAlphabeticallyButton.setSelected(Sort.ALPHABETICALLY == AppPreferences.sortPreference.get());
    sortAlphabeticallyButton.setOnAction((e) -> {
      AppPreferences.sortPreference.set(Sort.ALPHABETICALLY);
    });

    sortNumericButton = new ToggleButton("", IconUtil.createIconNode("sort-numeric-ascending", IconSize.SMALL));
    sortNumericButton.setToggleGroup(sortGroup);
    sortNumericButton.setSelected(Sort.NUMERIC == AppPreferences.sortPreference.get());
    sortNumericButton.setOnAction((e) -> {
      AppPreferences.sortPreference.set(Sort.NUMERIC);
    });

    sortButton = new SegmentedButton();
    sortButton.getButtons().addAll(sortAlphabeticallyButton, sortNumericButton);

    filterPaneNode = createFilterPane().getNode();
    filterPaneNode.setId("filterButton");

    toolBar = new MigPane("", "[pref][pref][pref]40[pref][pref]20[pref]20[pref]20[grow,fill]", "[pref:pref:pref, top]");
    toolBar.add(backButton);
    toolBar.add(forwardButton);
    toolBar.add(homeButton);
    toolBar.add(fullScreenButton);
    toolBar.add(refreshButton);
    toolBar.add(showDisplayMetricButton);
    toolBar.add(sortButton);
    toolBar.add(filterPaneNode, "grow");

    return toolBar;
  }

  private MenuItem createScanFileTreeMenuItem()
  {
    MenuItem menuItem;

    menuItem = translate(new MenuItem("Scan directory"));
    menuItem.setGraphic(IconUtil.createIconNode("file-search"));
    menuItem.setOnAction(e -> {
      scanDirectory(new ScanFileTreeDialog().chooseDirectory(m_data.mi_fullScreen.getCurrentStage()));
    });

    return menuItem;
  }

  private Menu createRecentFilesMenu()
  {
    return m_data.mi_recentFiles.createMenu();
  }

  private MenuItem createPreferencesMenuItem()
  {
    return m_data.mi_preferences.createMenuItem();
  }

  private MenuItem createExitMenuItem()
  {
    MenuItem menuItem;

    menuItem = translate(new MenuItem("Exit"));
    menuItem.setGraphic(IconUtil.createIconNode("exit-to-app"));
    menuItem.setOnAction(e -> {
      System.exit(0);
    });

    return menuItem;
  }

  private FilterPane createFilterPane()
  {
    return m_data.mi_filterPane;
  }

  public class TabPaneData
  {
    private enum TabData
    {
      SIZE("Size", "chart-pie", (md) -> md.mi_sizeTab),
      TOP_RANKING(Bindings.concat(translatedTextProperty("Top"), " ", AppPreferences.maxNumberInTopRanking.property()),
          "trophy", (md) -> md.mi_topRankingTab),
      DISTRIBUTION_SIZE("Size distribution", "chart-bell-curve", (md) -> md.mi_sizeDistributionTab),
      DISTRIBUTION_MODIFIED("Last modified", "calendar-blank", (md) -> md.mi_modifiedDistributionTab),
      DISTRIBUTION_TYPES("Types", "chart-pie", (md) -> md.mi_typesTab),
      LINK_COUNT("Link count", "file-link", (md) -> md.mi_linkCountTab),
      SEARCH_TYPES("Search", "magnify", (md) -> md.mi_searchTab),
      HELP("Help", "help", (md) -> md.mi_helpTab);

      private final StringExpression m_name;
      private final String m_iconName;
      private final Function<DiskUsageData, ? extends AbstractFormPane> m_tabPaneSupplier;

      private TabData(String name, String iconName, Function<DiskUsageData, ? extends AbstractFormPane> tabPaneGetter)
      {
        this(Bindings.concat(translatedTextProperty(name)), iconName, tabPaneGetter);
      }

      private TabData(StringExpression name, String iconName,
          Function<DiskUsageData, ? extends AbstractFormPane> tabPaneGetter)
      {
        m_name = name;
        m_iconName = iconName;
        m_tabPaneSupplier = tabPaneGetter;
      }

      public String getName()
      {
        return m_name.get();
      }

      public StringExpression getNameExpression()
      {
        return m_name;
      }

      public String getIconName()
      {
        return m_iconName;
      }

      public Tab initTab(Tab tab)
      {
        tab.setUserData(this);
        return tab;
      }

      Node fillContent(DiskUsageData mainData)
      {
        try (PerformancePoint pp = Performance.measure("Filling content for %s", getName()))
        {
          AbstractFormPane contentPane;

          contentPane = m_tabPaneSupplier.apply(mainData);
          contentPane.refresh();

          return contentPane.getNode();
        }
      }
    }

    private final BorderPane mi_borderPane;
    final DraggableTabPane mi_tabPane;
    private Map<TabData, Tab> mi_tabByTabId = new HashMap<>();
    Map<TabData, Node> mi_contentByTabId = new HashMap<>();

    private TabPaneData()
    {
      mi_tabPane = new DraggableTabPane();
      mi_borderPane = new BorderPane();
      mi_borderPane.setCenter(mi_tabPane);
    }

    public void init()
    {
      String selectedTabDataName;
      AppProperty<String> selectedIdProperty;

      Stream.of(TabPaneData.TabData.values()).forEach(this::createTab);

      selectedIdProperty = AppSettings.SELECTED_ID.forSubject(DiskUsageView.this);

      m_data.mi_selectedTabProperty.bind(mi_tabPane.getSelectionModel().selectedItemProperty());

      m_data.selectedTabProperty().addListener((observable, oldTab, newTab) -> {
        Log.log.debug("fill tab content: %s", newTab.getText());

        // Fill the new selected tab with data.
        fillContent(newTab);

        // Remember the last selected tab
        selectedIdProperty.set(((TabData) newTab.getUserData()).name());
      });

      // Select the tab that was in a previously selected (in a previous run)
      selectedTabDataName = selectedIdProperty.get(TabPaneData.TabData.values()[0].name());
      mi_tabPane.getTabs().stream()
          .filter(tab -> Objects.equals(((TabData) tab.getUserData()).name(), selectedTabDataName)).findFirst()
          .ifPresent(tab -> {
            Log.log.debug("select tab: %s", tab.getText());
            mi_tabPane.getSelectionModel().select(tab);
            fillContent(tab);
          });
    }

    public BorderPane getNode()
    {
      return mi_borderPane;
    }

    public Tab createTab(TabData tabData)
    {
      return mi_tabByTabId.computeIfAbsent(tabData, td -> {
        return td.initTab(mi_tabPane.createTab(td.getIconName(), td.getNameExpression()));
      });
    }

    public void fillContent(Tab tab)
    {
      Optional<Entry<TabData, Tab>> tabEntry;

      tabEntry = mi_tabByTabId.entrySet().stream().filter(entry -> Objects.equals(tab, entry.getValue())).findFirst();
      if (tabEntry.isPresent())
      {
        TabData tabData;

        tabData = tabEntry.get().getKey();
        tab.setContent(mi_contentByTabId.computeIfAbsent(tabData, td -> tabEntry.get().getKey().fillContent(m_data)));
      }
      else
      {
        tab.setContent(new Label(""));
      }
    }

    public void refresh()
    {
      mi_contentByTabId.clear();
      Stream.of(TabData.values()).forEach(tabData -> tabData.m_tabPaneSupplier.apply(m_data).reset());
      m_data.getTreePaneData().navigateTo(m_data.getSelectedTreeItem());
    }
  }

  private class RecentFilesMenu
  {
    private List<Menu> m_listenerList = new ArrayList<>();

    public Menu createMenu()
    {
      Menu menu;

      menu = translate(new Menu("Recent scans"));
      menu.setGraphic(IconUtil.createIconNode("history"));
      update(menu);
      m_listenerList.add(menu);

      return menu;
    }

    public void addPath(PathList pathList)
    {
      AppProperty<RecentScanList> recentScansProperty;
      RecentScanList recentScanList;

      recentScansProperty = getRecentScansProperty();
      recentScanList = recentScansProperty.get();
      recentScanList = recentScanList.add(0, pathList);
      recentScansProperty.set(recentScanList);

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
      return getRecentScansProperty().get().getRecentScanList().stream().map(this::createMenuItem)
          .collect(Collectors.toList());
    }

    private MenuItem createMenuItem(PathList pathList)
    {
      MenuItem menuItem;

      menuItem = translate(new MenuItem(pathList.toString()));
      menuItem.setGraphic(IconUtil.createIconNode("folder-outline"));
      menuItem.setOnAction(e -> {
        scanDirectory(pathList);
      });

      return menuItem;
    }

    private AppProperty<RecentScanList> getRecentScansProperty()
    {
      return AppSettings.RECENT_SCANS.forSubject(this, RecentScanList.empty());
    }
  }

  public void scanDirectory(PathList pathList)
  {
    scanDirectory(new ScanFileTreeDialog().scanDirectory(pathList));
  }

  private void scanDirectory(ScanResult scanResult)
  {
    if (scanResult != null && scanResult.hasResult())
    {
      m_data.getTreePaneData().createTreeTableView(scanResult.getResult());
      m_data.mi_recentFiles.addPath(scanResult.getDirectoryList());
    }
  }

  static private class PreferencesMenu
  {
    public MenuItem createMenuItem()
    {
      MenuItem menuItem;

      menuItem = translate(new MenuItem("Preferences"));
      menuItem.setGraphic(IconUtil.createIconNode("cog"));
      menuItem.setOnAction(e -> {
        new PreferencesDialog().show();
      });

      return menuItem;
    }
  }

  record FileAggregatesEntry(String bucket, FileAggregates aggregates) {};

  public static class FileAggregates
  {
    private long mi_fileSize;
    private long mi_fileCount;

    public FileAggregates(long accumulatedSize, long fileCount)
    {
      mi_fileSize = accumulatedSize;
      mi_fileCount = fileCount;
    }

    public long getFileSize()
    {
      return mi_fileSize;
    }

    public long getFileCount()
    {
      return mi_fileCount;
    }

    public long getSize(DisplayMetric displayMetric)
    {
      switch (displayMetric)
      {
        case FILE_SIZE:
          return getFileSize();
        case FILE_COUNT:
          return getFileCount();
        default:
          return -1;
      }
    }

    public String getValueDescription(DisplayMetric displayMetric)
    {
      switch (displayMetric)
      {
        case FILE_SIZE:
          return AppPreferences.sizeSystemPreference.get().getFileSize(getFileSize());
        case FILE_COUNT:
          return getFileCount() + " files";
        default:
          return "";
      }
    }

    @Override
    public String toString()
    {
      return "FileAggregates(" + getValueDescription(DisplayMetric.FILE_COUNT) + ", "
          + getValueDescription(DisplayMetric.FILE_SIZE) + ")";
    }

    public void add(int i, long size)
    {
      mi_fileCount += 1;
      mi_fileSize += size;
    }
  }

  public static String getOtherText()
  {
    return translate("<Other>");
  }

  public static String getNoneText()
  {
    return translate("<None>");
  }

  private AppProperty<Double> getSplitPaneProperty()
  {
    return AppSettings.SPLIT_PANE_POSITION.forSubject(this);
  }
}
