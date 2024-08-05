package org.kku.jdiskusage.ui.common;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.controlsfx.control.SegmentedButton;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.AppSettings;
import org.kku.jdiskusage.util.AppProperties.AppSetting;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Log;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.kku.jdiskusage.util.preferences.DisplayMetric;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

abstract public class AbstractFormPane
{
  private DiskUsageData m_diskUsageData;
  private PaneData m_paneData;
  private Map<String, PaneType> m_paneTypeByIdMap = new LinkedHashMap<>();
  private final BorderPane m_node = new BorderPane();
  private final SegmentedButton m_segmentedButton = new SegmentedButton();
  private PaneType m_currentPaneType;
  private Map<PaneType, Node> m_nodeByPaneTypeMap = new HashMap<>();
  private TreeItem<FileNodeIF> m_currentTreeItem;

  private record PaneType(String id, String description, String iconName, Supplier<Node> node) {};

  public AbstractFormPane(DiskUsageData diskUsageData)
  {
    m_diskUsageData = diskUsageData;

    m_diskUsageData.selectedTreeItemProperty().addListener((o, oldValue, newValue) -> refresh(newValue));
    AppPreferences.displayMetricPreference.addListener((o, oldValue, newValue) -> refresh());
    Log.log.debug("Create content pane %s", getClass().getSimpleName());
  }

  public void refreshView()
  {

  }

  public void refreshModel()
  {

  }

  public Node getNode()
  {
    return m_node;
  }

  public void refresh(TreeItem<FileNodeIF> selectedTreeItem)
  {
    refresh(selectedTreeItem, m_nodeByPaneTypeMap.get(m_currentPaneType) != null);
  }

  public void refresh()
  {
    refresh(m_diskUsageData.getSelectedTreeItem(), true);
  }

  public void refresh(TreeItem<FileNodeIF> selectedTreeItem, boolean init)
  {
    m_currentTreeItem = selectedTreeItem;

    init = init || needInit(m_node);

    reset();

    Log.log.info("refresh[showing=%b, %s-%s] filenode=%s", init, getClass().getSimpleName(),
        m_currentPaneType.description(), m_currentTreeItem == null ? "<no selection>" : m_currentTreeItem.getValue());
    if (init)
    {
      //initNode(m_diskUsageData.getTreePaneData());
      initCurrentNode();
    }
  }

  private boolean needInit(Node node)
  {
    if (node == null)
    {
      return false;
    }

    if (m_currentTreeItem == null)
    {
      // Display label with 'no data'
      return true;
    }

    Log.log.debug("====is visible ? : %s", node);
    return Stream.iterate(node, Objects::nonNull, Node::getParent).filter(Predicate.not(Node::isVisible)).findFirst()
        .isPresent();
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
    return AppSettings.SELECTED_ID.forSubject(this);
  }

  private Node initCurrentNode()
  {
    if (m_currentPaneType != null)
    {
      m_node.setCenter(m_nodeByPaneTypeMap.computeIfAbsent(m_currentPaneType, type -> {
        if (getDiskUsageData().getSelectedTreeItem() == null)
        {
          return translate(new Label("No data"));
        }

        return type.node().get();
      }));
    }
    else
    {
      m_node.setCenter(new Label(""));
    }

    return m_node;
  }

  protected TreeItem<FileNodeIF> getCurrentTreeItem()
  {
    return m_currentTreeItem;
  }

  protected FileNodeIF getCurrentFileNode()
  {
    return getCurrentTreeItem().getValue();
  }

  protected DisplayMetric getCurrentDisplayMetric()
  {
    return AppPreferences.displayMetricPreference.get();
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
      m_paneData = this;
    }

    public DisplayMetric getCurrentDisplayMetric()
    {
      return AppPreferences.displayMetricPreference.get();
    }

    protected abstract void reset();
  }
}