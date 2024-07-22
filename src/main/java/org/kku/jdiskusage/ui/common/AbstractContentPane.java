package org.kku.jdiskusage.ui.common;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.controlsfx.control.SegmentedButton;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.FileTreePane;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.AppProperties;
import org.kku.jdiskusage.util.AppSettings.AppSetting;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.kku.jdiskusage.util.preferences.DisplayMetric;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

abstract public class AbstractContentPane
{
  private DiskUsageData m_diskUsageData;
  private PaneData m_paneData;
  private Map<String, PaneType> m_paneTypeByIdMap = new LinkedHashMap<>();
  private final BorderPane m_node = new BorderPane();
  private final SegmentedButton m_segmentedButton = new SegmentedButton();
  private PaneType m_currentPaneType;
  private Map<PaneType, Node> m_nodeByPaneTypeMap = new HashMap<>();
  private FileTreePane m_currentTreePaneData;
  private TreeItem<FileNodeIF> m_currentTreeItem;

  private record PaneType(String id, String description, String iconName, Supplier<Node> node) {};

  public AbstractContentPane(DiskUsageData diskUsageData)
  {
    m_diskUsageData = diskUsageData;

    m_diskUsageData.getSelectedTreeItemProperty().addListener((o, oldValue, newValue) -> refresh());
    AppPreferences.displayMetricPreference.addListener((o, oldValue, newValue) -> refresh());
  }

  public void refresh()
  {
    boolean init;

    init = isShowing(m_nodeByPaneTypeMap.get(m_currentPaneType));

    System.out.print(getClass().getSimpleName() + "." + m_currentPaneType.description + " " + init);
    reset();

    if (init)
    {
      initNode(m_diskUsageData.getTreePaneData());
      initCurrentNode();
      System.out.print("INIT_CURRENT_NODE:" + getClass().getSimpleName() + "." + m_currentPaneType.description + " ");
    }
  }

  private static boolean isShowing(Node node)
  {
    if (node == null)
    {
      return false;
    }

    //System.out.println("====is visible :" + node);
    do
    {
      if (!node.isVisible())
      {
        return false;
      }

      if (node instanceof TabPane tabPane)
      {
        //System.out.println("selectedItem: " + tabPane.getSelectionModel().getSelectedItem());
        //System.out.println("selectedContent: " + tabPane.getSelectionModel().getSelectedItem().getContent());
      }

      //System.out.println("node=" + node);
    }
    while ((node = node.getParent()) != null);

    return true;
  }

  public void reset()
  {
    m_nodeByPaneTypeMap.clear();
    if (m_paneData != null)
    {
      m_paneData.reset();
    }
  }

  protected void setTop(Node node)
  {
    m_node.setTop(node);
  }

  protected DiskUsageData getDiskUsageData()
  {
    return m_diskUsageData;
  }

  protected final void init()
  {
    if (m_paneTypeByIdMap.size() > 1)
    {
      m_currentPaneType = m_paneTypeByIdMap
          .get(getSelectedIdProperty().get(m_paneTypeByIdMap.entrySet().iterator().next().getKey()));

      m_paneTypeByIdMap.values().stream().map(paneType -> {
        ToggleButton button;

        button = new ToggleButton();
        if (paneType.iconName() != null)
        {
          button.setGraphic(IconUtil.createIconNode(paneType.iconName(), IconSize.SMALLER));
          button.setTooltip(new Tooltip(translate(paneType.description())));
        }
        else
        {
          button.setText(translate(paneType.description()));
        }
        button.setUserData(paneType);
        button.setOnAction((ae) -> {
          setCurrentPaneType((PaneType) ((Node) ae.getSource()).getUserData());
        });
        if (m_currentPaneType == paneType)
        {
          button.setSelected(true);
        }

        return button;
      }).forEach(button -> {
        m_segmentedButton.getButtons().add(button);
      });

      BorderPane.setMargin(m_segmentedButton, new Insets(2, 2, 2, 2));
      m_node.setBottom(m_segmentedButton);
    }
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
        (panelTypeId) -> new PaneType(paneTypeId, description, iconName, node));
    if (m_currentPaneType == null || current)
    {
      m_currentPaneType = paneType;
    }

    return paneType;
  }

  public void setCurrentPaneType(PaneType paneType)
  {
    m_currentPaneType = paneType;
    initCurrentNode();

    getSelectedIdProperty().set(paneType.id());
  }

  private AppSetting<String> getSelectedIdProperty()
  {
    return AppProperties.SELECTED_ID.forSubject(this);
  }

  private void initCurrentNode()
  {
    if (m_currentPaneType != null)
    {
      m_node.setCenter(m_nodeByPaneTypeMap.computeIfAbsent(m_currentPaneType, type -> {
        System.out.println("Creating new node: " + type.toString());
        return type.node().get();
      }));
    }
    else
    {
      m_node.setCenter(new Label(""));
    }
  }

  protected TreeItem<FileNodeIF> getCurrentTreeItem()
  {
    return m_currentTreeItem;
  }

  protected DisplayMetric getCurrentDisplayMetric()
  {
    return AppPreferences.displayMetricPreference.get();
  }

  public Node initNode(FileTreePane treePaneData)
  {
    if (m_currentTreePaneData != treePaneData || getCurrentTreeItem() != m_diskUsageData.getSelectedTreeItem())
    {
      m_currentTreePaneData = treePaneData;
      m_currentTreeItem = m_diskUsageData.getSelectedTreeItem();
      m_nodeByPaneTypeMap.clear();
    }

    initCurrentNode();
    return m_node;
  }

  protected void addFilterHandler(Node node, String filterType, String filterValue,
      Predicate<FileNodeIF> fileNodePredicate)
  {
    node.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
      m_diskUsageData.addFilter(new Filter(filterType, filterValue, fileNodePredicate), event.getClickCount() == 2);
    });
  }

  public abstract class PaneData
  {
    public PaneData()
    {
      //m_diskUsageData.getSelectedTreeItemProperty().addListener((o, oldValue, newValue) -> reset());
      //AppPreferences.displayMetricPreference.addListener((o, oldValue, newValue) -> reset());

      m_paneData = this;
    }

    public DisplayMetric getCurrentDisplayMetric()
    {
      return AppPreferences.displayMetricPreference.get();
    }

    protected abstract void reset();
  }
}