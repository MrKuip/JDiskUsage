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

abstract public class AbstractTabContentPane
{
  private DiskUsageData m_diskUsageData;
  private PaneData m_paneData;
  private Map<String, PaneType> m_paneTypeByIdMap = new LinkedHashMap<>();
  private final BorderPane mi_node = new BorderPane();
  private final SegmentedButton mi_segmentedButton = new SegmentedButton();
  private PaneType mi_currentPaneType;
  private Map<PaneType, Node> mi_nodeByPaneTypeMap = new HashMap<>();
  private FileTreePane mi_currentTreePaneData;
  private TreeItem<FileNodeIF> mi_currentTreeItem;

  private record PaneType(String description, String iconName, Supplier<Node> node) {
  };

  public AbstractTabContentPane(DiskUsageData diskUsageData)
  {
    m_diskUsageData = diskUsageData;
  }

  public void reset()
  {
    mi_nodeByPaneTypeMap.clear();
    if (m_paneData != null)
    {
      m_paneData.reset();
    }
  }

  protected BorderPane getNode()
  {
    return mi_node;
  }

  protected DiskUsageData getDiskUsageData()
  {
    return m_diskUsageData;
  }

  protected final void init()
  {
    if (m_paneTypeByIdMap.size() > 1)
    {
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

  public Node initNode(FileTreePane treePaneData)
  {
    if (mi_currentTreePaneData != treePaneData || getCurrentTreeItem() != m_diskUsageData.getSelectedTreeItem())
    {
      mi_currentTreePaneData = treePaneData;
      mi_currentTreeItem = m_diskUsageData.getSelectedTreeItem();
      mi_nodeByPaneTypeMap.clear();
    }

    initCurrentNode();
    return mi_node;
  }

  protected void addFilter(Node node, String filterType, String filterValue, Predicate<FileNodeIF> fileNodePredicate)
  {
    node.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
      m_diskUsageData.addFilter(new Filter(filterType, filterValue, fileNodePredicate), event.getClickCount() == 2);
    });
  }

  public abstract class PaneData
  {
    public PaneData()
    {
      m_diskUsageData.getSelectedTreeItemProperty().addListener((o, oldValue, newValue) -> reset());
      AppPreferences.displayMetricPreference.addListener((o, oldValue, newValue) -> reset());

      m_paneData = this;
    }

    public DisplayMetric getCurrentDisplayMetric()
    {
      return AppPreferences.displayMetricPreference.get();
    }

    protected abstract void reset();
  }
}