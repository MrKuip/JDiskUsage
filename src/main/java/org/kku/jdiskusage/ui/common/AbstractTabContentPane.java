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
  private Map<String, AbstractTabContentPane.PaneType> m_paneTypeByIdMap = new LinkedHashMap<>();
  private final BorderPane mi_node = new BorderPane();
  private final SegmentedButton mi_segmentedButton = new SegmentedButton();
  private AbstractTabContentPane.PaneType mi_currentPaneType;
  private Map<AbstractTabContentPane.PaneType, Node> mi_nodeByPaneTypeMap = new HashMap<>();
  private FileTreePane mi_currentTreePaneData;
  private TreeItem<FileNodeIF> mi_currentTreeItem;

  private record PaneType(String description, String iconName, Supplier<Node> node) {
  };

  public AbstractTabContentPane(DiskUsageData diskUsageData)
  {
    m_diskUsageData = diskUsageData;
  }

  protected DiskUsageData getDiskUsageData()
  {
    return m_diskUsageData;
  }

  protected void init()
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
        setCurrentPaneType((AbstractTabContentPane.PaneType) ((Node) ae.getSource()).getUserData());
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

  protected AbstractTabContentPane.PaneType createPaneType(String paneTypeId, String description, String iconName,
      Supplier<Node> node)
  {
    return createPaneType(paneTypeId, description, iconName, node, false);
  }

  protected AbstractTabContentPane.PaneType createPaneType(String paneTypeId, String description, String iconName,
      Supplier<Node> node, boolean current)
  {
    AbstractTabContentPane.PaneType paneType;

    paneType = m_paneTypeByIdMap.computeIfAbsent(paneTypeId,
        (panelTypeId) -> new PaneType(description, iconName, node));
    if (mi_currentPaneType == null || current)
    {
      mi_currentPaneType = paneType;
    }

    return paneType;
  }

  private void setCurrentPaneType(AbstractTabContentPane.PaneType paneType)
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

  public Node getNode(FileTreePane treePaneData)
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
    private TreeItem<FileNodeIF> mii_currentTreeItem;
    private DisplayMetric mii_currentDisplayMetric;

    public PaneData()
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
      if (mii_currentTreeItem != m_diskUsageData.getSelectedTreeItem())
      {
        mii_currentTreeItem = m_diskUsageData.getSelectedTreeItem();
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