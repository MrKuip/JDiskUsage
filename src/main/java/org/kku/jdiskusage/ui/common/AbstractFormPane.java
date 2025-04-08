package org.kku.jdiskusage.ui.common;

import static org.kku.fx.ui.util.TranslateUtil.translate;
import static org.kku.fx.ui.util.TranslateUtil.translatedTextProperty;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.kku.common.util.Log;
import org.kku.fx.ui.util.FxIconUtil;
import org.kku.fx.util.AppProperties.AppProperty;
import org.kku.jdiskusage.javafx.scene.control.SegmentedControl;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.util.AppSettings;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
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
  private final SegmentedControl m_segmentedControl = new SegmentedControl();
  private PaneType m_currentPaneType;
  private Map<PaneType, Node> m_nodeByPaneTypeMap = new HashMap<>();
  private TreeItem<FileNodeIF> m_currentTreeItem;

  private record PaneType(String id, String description, String iconName, Supplier<Node> node) {};

  public AbstractFormPane(DiskUsageData diskUsageData)
  {
    m_diskUsageData = diskUsageData;

    m_diskUsageData.selectedTreeItemProperty().addListener((o, oldValue, newValue) -> refresh(newValue));
    AppPreferences.displayMetricPreference.addListener((o, oldValue, newValue) -> refresh());
    Log.log.fine("Create content pane %s", getClass().getSimpleName());
  }

  public Node getNode()
  {
    return m_node;
  }

  public void refresh(TreeItem<FileNodeIF> selectedTreeItem)
  {
    refresh(selectedTreeItem, false);
  }

  public void refresh()
  {
    refresh(m_diskUsageData.getSelectedTreeItem(), true);
  }

  public void refresh(TreeItem<FileNodeIF> selectedTreeItem, boolean init)
  {
    boolean needInit;

    m_currentTreeItem = selectedTreeItem;

    needInit = false;
    if (!init)
    {
      needInit = needInit(m_node);
    }

    reset();

    if (init || needInit)
    {
      Log.log.info("refresh[init=%b, needInit=%b %s-%s] filenode=%s", init, needInit, getClass().getSimpleName(),
          m_currentPaneType.description(), m_currentTreeItem == null ? "<no selection>" : m_currentTreeItem.getValue());
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

    if (node.getParent() == null)
    {
      return false;
    }

    if (m_currentTreeItem == null)
    {
      // Display label with 'no data'
      return true;
    }

    return !Stream.iterate(node, Objects::nonNull, Node::getParent).filter(Predicate.not(Node::isVisible)).findFirst()
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
      String defaultPaneType;

      defaultPaneType = m_paneTypeByIdMap.entrySet().iterator().next().getKey();
      m_currentPaneType = m_paneTypeByIdMap.get(getSelectedIdProperty().get(defaultPaneType));
      if (m_currentPaneType == null)
      {
        m_currentPaneType = m_paneTypeByIdMap.get(defaultPaneType);
      }

      m_paneTypeByIdMap.values().stream().map(paneType -> {
        ToggleButton button;

        button = new ToggleButton();
        if (paneType.iconName() != null)
        {
          button.setGraphic(FxIconUtil.createIconNode(paneType.iconName()));
          button.setTooltip(translate(new Tooltip(paneType.description())));
        }
        else
        {
          button.textProperty().bind(translatedTextProperty(paneType.description()));
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
        m_segmentedControl.add(button);
      });

      BorderPane.setMargin(m_segmentedControl.getNode(), new Insets(2, 2, 2, 2));

      m_node.setBottom(m_segmentedControl.getNode());
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

  private AppProperty<String> getSelectedIdProperty()
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