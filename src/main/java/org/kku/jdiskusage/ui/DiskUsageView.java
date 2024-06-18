package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.controlsfx.control.SegmentedButton;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.common.AbstractTabContentPane;
import org.kku.jdiskusage.ui.common.Filter;
import org.kku.jdiskusage.ui.common.FullScreen;
import org.kku.jdiskusage.ui.common.Navigation;
import org.kku.jdiskusage.ui.common.TaskView;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.ApplicationPropertyExtensionIF;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import org.kku.jdiskusage.util.Translator;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.kku.jdiskusage.util.preferences.DisplayMetric;
import org.kku.jdiskusage.util.preferences.Sort;
import org.tbee.javafx.scene.layout.MigPane;
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
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DiskUsageView
    implements ApplicationPropertyExtensionIF
{
  private DiskUsageData m_data = new DiskUsageData();
  private MigPane m_content = new MigPane();

  public class DiskUsageData
  {
    private FullScreen mi_fullScreen;
    private final ObjectProperty<TreeItem<FileNodeIF>> mi_selectedItemProperty = new SimpleObjectProperty<>();
    private final Navigation mi_navigation = new Navigation(this);
    private final FileTreePane mi_treePaneData = new FileTreePane(this);
    private final TabPaneData mi_tabPaneData = new TabPaneData();
    private final SizePane mi_sizeTab = new SizePane(this);
    private final Top50Pane mi_top50Tab = new Top50Pane(this);
    private final SizeDistributionPane mi_sizeDistributionTab = new SizeDistributionPane(this);
    private final LastModifiedDistributionPane mi_modifiedDistributionTab = new LastModifiedDistributionPane(this);
    private final TypesPane mi_typesTab = new TypesPane(this);
    private final SearchPane mi_searchTab = new SearchPane(this);
    private final RecentFilesMenu mi_recentFiles = new RecentFilesMenu();
    private final PreferencesMenu mi_preferences = new PreferencesMenu();
    private final FilterPane mi_filterPane = new FilterPane(this);
    private final TaskView mi_taskView = new TaskView();

    private DiskUsageData()
    {
    }

    public void refresh()
    {
      mi_tabPaneData.refresh();
    }

    public ObjectProperty<TreeItem<FileNodeIF>> getSelectedTreeItemProperty()
    {
      return mi_selectedItemProperty;
    }

    public TreeItem<FileNodeIF> getSelectedTreeItem()
    {
      if (getTreePaneData() == null || getTreePaneData().mi_treeTableView == null)
      {
        return null;
      }
      return getTreePaneData().mi_treeTableView.getSelectionModel().getSelectedItem();
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

    getProps().getDouble(Property.HEIGHT, 0);

    m_data.mi_fullScreen = new FullScreen(stage);

    toolBars = new VBox(createMenuBar(), createToolBar());
    splitPane = new SplitPane();

    m_data.getTabPaneData().init();

    splitPane.getItems().addAll(m_data.getTreePaneData().getNode(), m_data.getTabPaneData().getNode());
    splitPane.getDividers().get(0).positionProperty()
        .addListener(getProps().getChangeListener(Property.SPLIT_PANE_POSITION));
    SplitPane.setResizableWithParent(m_data.getTreePaneData().getNode(), false);
    SplitPane.setResizableWithParent(m_data.getTabPaneData().getNode(), false);

    splitPane.getDividers().get(0).setPosition(getProps().getDouble(Property.SPLIT_PANE_POSITION, 0.40));

    m_content.add(toolBars, "dock north");
    m_content.add(splitPane, "dock center");
    // This places the taskview in the lower right corner of the content pane.
    // container.x2 = the leftmost point of the container
    // container.y2 = the lowest point of the container
    m_content.add(m_data.mi_taskView, "pos null null container.x2 container.y2");

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
    //HBox.setHgrow(filterPaneNode, Priority.ALWAYS);

    toolBar.getItems().addAll(backButton, forwardButton, homeButton, FxUtil.createHorizontalSpacer(200),
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
    menuItem.setGraphic(IconUtil.createIconNode("exit-to-app", IconSize.SMALLER));
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
      TOP50("Top 50", "trophy", (md) -> md.mi_top50Tab),
      DISTRIBUTION_SIZE("Size distribution", "chart-bell-curve", (md) -> md.mi_sizeDistributionTab),
      DISTRIBUTION_MODIFIED("Last modified", "sort-calendar-ascending", (md) -> md.mi_modifiedDistributionTab),
      DISTRIBUTION_TYPES("Types", "chart-pie", (md) -> md.mi_typesTab),
      SEARCH_TYPES("Search", "magnify", (md) -> md.mi_searchTab);

      private final String m_name;
      private final String m_iconName;
      private final Function<DiskUsageData, ? extends AbstractTabContentPane> m_tabPaneSupplier;

      private TabData(String name, String iconName,
          Function<DiskUsageData, ? extends AbstractTabContentPane> tabPaneGetter)
      {
        m_name = name;
        m_iconName = iconName;
        m_tabPaneSupplier = tabPaneGetter;
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

      Node fillContent(DiskUsageData mainData)
      {
        Node node;

        try (PerformancePoint pp = Performance.start("Filling content for %s", getName()))
        {
          node = m_tabPaneSupplier.apply(mainData).initNode(mainData.getTreePaneData());
        }

        return node;
      }
    }

    private final BorderPane mi_borderPane;
    final TabPane mi_tabPane;
    private Map<TabData, Tab> mi_tabByTabId = new HashMap<>();
    Map<TabData, Node> mi_contentByTabId = new HashMap<>();

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
        fillContent(newTab, m_data.getSelectedTreeItem());

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

    public void itemSelected()
    {
      mi_contentByTabId.clear();
      m_data.getNavigation().navigateTo(m_data.getSelectedTreeItem());
    }
  }

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

    public void addPath(Path path)
    {
      getProps().set(Property.RECENT_FILES,
          Stream.concat(Stream.of(path), getProps().getPathList(Property.RECENT_FILES).stream()).distinct().limit(10)
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
      return getProps().getPathList(Property.RECENT_FILES).stream().map(this::createMenuItem)
          .collect(Collectors.toList());
    }

    private MenuItem createMenuItem(Path path)
    {
      MenuItem menuItem;

      menuItem = translate(new MenuItem(path.toString()));
      menuItem.setGraphic(IconUtil.createIconNode("folder-outline", IconSize.SMALLER));
      menuItem.setOnAction(e -> {
        scanDirectory(path);
      });

      return menuItem;
    }
  }

  public void scanDirectory(Path path)
  {
    scanDirectory(new ScanFileTreeDialog().scanDirectory(Arrays.asList(path)));
  }

  private void scanDirectory(DirNode dirNode)
  {
    if (dirNode != null)
    {
      m_data.getTreePaneData().createTreeTableView(dirNode);
      m_data.mi_recentFiles.addPath(Path.of(dirNode.getName()));
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

  public static class FileAggregates
  {
    public long mi_fileSize;
    public long mi_fileCount;

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
  }

  public static String getOtherText()
  {
    return translate("<Other>");
  }

  public static String getNoneText()
  {
    return translate("<None>");
  }
}
