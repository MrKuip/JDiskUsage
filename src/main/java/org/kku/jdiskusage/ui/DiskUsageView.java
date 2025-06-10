package org.kku.jdiskusage.ui;

import static org.kku.fx.ui.util.TranslateUtil.translate;
import static org.kku.fx.ui.util.TranslateUtil.translatedTextProperty;
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
import org.kku.common.util.AppProperties.AppProperty;
import org.kku.common.util.Performance.PerformancePoint;
import org.kku.common.util.preferences.Sort;
import org.kku.common.util.Log;
import org.kku.common.util.Performance;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.fx.scene.control.DraggableTabPane;
import org.kku.fx.scene.control.SegmentedControl;
import org.kku.fx.ui.util.FullScreen;
import org.kku.fx.ui.util.FxIconUtil;
import org.kku.fx.ui.util.FxSettingsUtil;
import org.kku.fx.ui.util.Notifications;
import org.kku.fx.util.FxProperty;
import org.kku.jdiskusage.ui.common.AbstractFormPane;
import org.kku.jdiskusage.ui.common.Filter;
import org.kku.jdiskusage.ui.common.Navigation;
import org.kku.jdiskusage.ui.dialog.PreferencesDialog;
import org.kku.jdiskusage.ui.dialog.ScanFileTreeDialog;
import org.kku.jdiskusage.ui.dialog.ScanFileTreeDialog.ScanResult;
import org.kku.jdiskusage.util.AppSettings;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.PathList;
import org.kku.jdiskusage.util.RecentScanList;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.kku.jdiskusage.util.preferences.DisplayMetric;
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
    private final Navigation mi_navigation = new Navigation(this);
    private final FileTreePane mi_treePaneData = new FileTreePane(this);
    private final TabPaneData mi_tabPaneData = new TabPaneData();
    private final TreeMapChartFormPane mi_treeChartTab = new TreeMapChartFormPane(this);
    private final SizeFormPane mi_sizeTab = new SizeFormPane(this);
    private final TopRankingFormPane mi_topRankingTab = new TopRankingFormPane(this);
    private final LinkCountFormPane mi_linkCountTab = new LinkCountFormPane(this);
    private final SizeDistributionFormPane mi_sizeDistributionTab = new SizeDistributionFormPane(this);
    private final LastModifiedDistributionFormPane mi_modifiedDistributionTab = new LastModifiedDistributionFormPane(
        this);
    private final TypesFormPane mi_subTypesTab = new TypesFormPane(this, FileNodeIF::getFileSubType);
    private final TypesFormPane mi_typesTab = new TypesFormPane(this, FileNodeIF::getFileType);
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
    splitPane.getDividers().get(0).positionProperty().addListener(FxProperty.getChangeListener(getSplitPaneProperty()));
    SplitPane.setResizableWithParent(m_data.getTreePaneData().getNode(), false);
    SplitPane.setResizableWithParent(m_data.getTabPaneData().getNode(), false);

    splitPane.getDividers().get(0).setPosition(getSplitPaneProperty().get(0.40));

    m_content.add(toolBars, "dock north");
    m_content.add(splitPane, "dock center");
    // This places the taskview in the lower right corner of the content pane.
    // container.x2 = the leftmost point of the container
    // container.y2 = the lowest point of the container
    m_content.add(m_data.mi_taskView.getView(), "pos null null container.x2 container.y2");

    m_data.getTabPaneData().getNode().add(m_data.getTreePaneData().getBreadCrumbBar(), "dock north");
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
    menuBar.setUseSystemMenuBar(true);

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
    SegmentedControl showDisplayMetricButton;
    ToggleButton sortAlphabeticallyButton;
    ToggleButton sortNumericButton;
    ToggleGroup sortGroup;
    SegmentedControl sortButton;
    Node filterPaneNode;

    navigation = m_data.getNavigation();

    backButton = new Button("", FxIconUtil.createIconNode("arrow-left", IconSize.SMALL));
    backButton.disableProperty().bind(navigation.backNavigationDisabledProperty());
    backButton.setOnAction((_) -> navigation.back());

    forwardButton = new Button("", FxIconUtil.createIconNode("arrow-right", IconSize.SMALL));
    forwardButton.disableProperty().bind(navigation.forwardNavigationDisabledProperty());
    forwardButton.setOnAction((_) -> navigation.forward());

    homeButton = new Button("", FxIconUtil.createIconNode("home", IconSize.SMALL));
    homeButton.disableProperty().bind(navigation.homeNavigationDisabledProperty());
    homeButton.setOnAction((_) -> navigation.home());

    refreshButton = new Button("", FxIconUtil.createIconNode("refresh", IconSize.SMALL));
    refreshButton.setOnAction((_) -> m_data.refresh());

    fullScreenButton = new ToggleButton("", FxIconUtil.createIconNode("fullscreen", IconSize.SMALL));
    fullScreenButton.setOnAction((_) -> m_data.mi_fullScreen.toggleFullScreen());

    showDisplayMetricGroup = new ToggleGroup();

    showFileSizeButton = new ToggleButton("", FxIconUtil.createIconNode("sigma", IconSize.SMALL));
    showFileSizeButton.setTooltip(translate(new Tooltip("Show file size")));
    showFileSizeButton.setToggleGroup(showDisplayMetricGroup);
    showFileSizeButton.setSelected(DisplayMetric.FILE_SIZE == AppPreferences.displayMetricPreference.get());
    showFileSizeButton.setOnAction((_) -> {
      AppPreferences.displayMetricPreference.set(DisplayMetric.FILE_SIZE);
    });

    showFileCountButton = new ToggleButton("", FxIconUtil.createIconNode("tally-mark-5", IconSize.SMALL));
    showFileCountButton.setTooltip(translate(new Tooltip("Show number of files")));
    showFileCountButton.setToggleGroup(showDisplayMetricGroup);
    showFileCountButton.setSelected(DisplayMetric.FILE_COUNT == AppPreferences.displayMetricPreference.get());
    showFileCountButton.setOnAction((_) -> {
      AppPreferences.displayMetricPreference.set(DisplayMetric.FILE_COUNT);
    });

    showDisplayMetricButton = new SegmentedControl();
    showDisplayMetricButton.add(showFileSizeButton);
    showDisplayMetricButton.add(showFileCountButton);

    sortGroup = new ToggleGroup();

    sortAlphabeticallyButton = new ToggleButton("",
        FxIconUtil.createIconNode("sort-alphabetical-ascending", IconSize.SMALL));
    sortAlphabeticallyButton.setToggleGroup(sortGroup);
    sortAlphabeticallyButton.setSelected(Sort.ALPHABETICALLY == AppPreferences.sortPreference.get());
    sortAlphabeticallyButton.setOnAction((_) -> {
      AppPreferences.sortPreference.set(Sort.ALPHABETICALLY);
    });

    sortNumericButton = new ToggleButton("", FxIconUtil.createIconNode("sort-numeric-ascending", IconSize.SMALL));
    sortNumericButton.setToggleGroup(sortGroup);
    sortNumericButton.setSelected(Sort.NUMERIC == AppPreferences.sortPreference.get());
    sortNumericButton.setOnAction((_) -> {
      AppPreferences.sortPreference.set(Sort.NUMERIC);
    });

    sortButton = new SegmentedControl();
    sortButton.add(sortAlphabeticallyButton);
    sortButton.add(sortNumericButton);

    filterPaneNode = createFilterPane().getNode();
    filterPaneNode.setId("filterButton");

    toolBar = new MigPane("", "[pref][pref][pref]40[pref][pref]20[pref]20[pref]20[grow,fill]", "[pref:pref:pref, top]");
    toolBar.add(backButton);
    toolBar.add(forwardButton);
    toolBar.add(homeButton);
    toolBar.add(fullScreenButton);
    toolBar.add(refreshButton);
    toolBar.add(showDisplayMetricButton.getNode());
    toolBar.add(sortButton.getNode());
    toolBar.add(filterPaneNode, "grow");

    return toolBar;
  }

  private MenuItem createScanFileTreeMenuItem()
  {
    MenuItem menuItem;

    menuItem = translate(new MenuItem("Scan directory"));
    menuItem.setGraphic(FxIconUtil.createIconNode("file-search"));
    menuItem.setOnAction((_) -> {
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
    menuItem.setGraphic(FxIconUtil.createIconNode("exit-to-app"));
    menuItem.setOnAction((_) -> {
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
      TOP_RANKING(Bindings.concat(translatedTextProperty("Top"), " ",
          FxProperty.property(AppPreferences.maxNumberInTopRanking)), "trophy", (md) -> md.mi_topRankingTab),
      DISTRIBUTION_SIZE("Size distribution", "chart-bell-curve", (md) -> md.mi_sizeDistributionTab),
      DISTRIBUTION_MODIFIED("Last modified", "calendar-blank", (md) -> md.mi_modifiedDistributionTab),
      DISTRIBUTION_SUBTYPES("Subtypes", "chart-pie", (md) -> md.mi_subTypesTab),
      DISTRIBUTION_TYPES("Types", "chart-pie", (md) -> md.mi_typesTab),
      TREEMAP("Treemap", "chart-tree", (md) -> md.mi_treeChartTab),
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

      AbstractFormPane fillContent(DiskUsageData mainData)
      {
        try (PerformancePoint _ = Performance.measure("Filling content for %s", getName()))
        {
          AbstractFormPane contentPane;

          contentPane = getFormPane(mainData);
          contentPane.refresh();

          return contentPane;
        }
      }

      AbstractFormPane getFormPane(DiskUsageData mainData)
      {
        return m_tabPaneSupplier.apply(mainData);
      }
    }

    private final MigPane mi_borderPane;
    final DraggableTabPane mi_tabPane;
    private Map<TabData, Tab> mi_tabByTabId = new HashMap<>();
    Map<TabData, AbstractFormPane> mi_contentByTabId = new HashMap<>();

    private TabPaneData()
    {
      mi_tabPane = new DraggableTabPane();
      mi_borderPane = new MigPane();
      mi_borderPane.add(mi_tabPane, "dock center");
    }

    public void init()
    {
      FxSettingsUtil.initSelectedTabSetting(AppSettings.SELECTED_ID.forSubject(DiskUsageView.this), mi_tabPane);

      Stream.of(TabPaneData.TabData.values()).forEach(this::createTab);

      mi_tabPane.getSelectionModel().selectedItemProperty().addListener((_, _, newTab) -> {
        // Fill the new selected tab with data.
        Log.log.fine("fill tab content: %s", newTab.getText());
        fillContent(newTab);
      });
    }

    public MigPane getNode()
    {
      return mi_borderPane;
    }

    public Tab createTab(TabData tabData)
    {
      return mi_tabByTabId.computeIfAbsent(tabData, td -> {
        return td.initTab(mi_tabPane.createTab(td.getName(), td.getIconName(), td.getNameExpression()));
      });
    }

    public void fillContent(Tab tab)
    {
      Optional<Entry<TabData, Tab>> tabEntry;

      tabEntry = mi_tabByTabId.entrySet().stream().filter(entry -> Objects.equals(tab, entry.getValue())).findFirst();
      if (tabEntry.isPresent())
      {
        TabData tabData;
        AbstractFormPane formPane;

        tabData = tabEntry.get().getKey();
        formPane = mi_contentByTabId.get(tabData);
        if (formPane == null)
        {
          formPane = tabEntry.get().getKey().fillContent(m_data);
          tab.setContent(formPane.getNode());
          mi_contentByTabId.put(tabData, tabEntry.get().getKey().fillContent(m_data));
        }
        else
        {
          formPane.refresh();
        }
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
      menu.setGraphic(FxIconUtil.createIconNode("history"));
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

      menuItem = new MenuItem(pathList.toString());
      menuItem.setGraphic(FxIconUtil.createIconNode("folder-outline"));
      menuItem.setOnAction((_) -> {
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
      menuItem.setGraphic(FxIconUtil.createIconNode("cog"));
      menuItem.setOnAction((_) -> {
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

    public void add(int fileCount, long fileSize)
    {
      mi_fileCount += fileCount;
      mi_fileSize += fileSize;
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
