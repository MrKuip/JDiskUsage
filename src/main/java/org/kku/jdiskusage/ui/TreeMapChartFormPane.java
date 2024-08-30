package org.kku.jdiskusage.ui;

import java.util.ArrayList;
import java.util.List;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapChart;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapModel;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapNode;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.AbstractFormPane;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;

public class TreeMapChartFormPane
  extends AbstractFormPane
{
  private final TreeMapChartPaneData m_data = new TreeMapChartPaneData();
  private TreeMapChart<FileNodeTreeMapNode> m_treeMap;
  private TreeItem<FileNodeIF> m_root;

  TreeMapChartFormPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("TREECHART", "Show tree chart", "chart-tree", this::getTreeChartNode);

    init();
  }

  @Override
  public void refresh(TreeItem<FileNodeIF> selectedTreeItem)
  {
    TreeItem<FileNodeIF> root;

    root = getRoot(selectedTreeItem);
    if (root != m_root)
    {
      super.refresh(selectedTreeItem);
      m_root = root;
    }
    else
    {
      m_data.select(selectedTreeItem.getValue());
    }
  }

  private TreeItem<FileNodeIF> getRoot(TreeItem<FileNodeIF> selectedTreeItem)
  {
    return selectedTreeItem.getParent() == null ? selectedTreeItem : getRoot(selectedTreeItem.getParent());
  }

  Node getTreeChartNode()
  {
    BorderPane pane;

    m_treeMap = new TreeMapChart<>();
    m_treeMap.setModel(m_data.getModel());

    pane = new BorderPane();
    pane.setCenter(m_treeMap);

    return pane;
  }

  class TreeMapChartPaneData
    extends PaneData
  {
    private TreeMapModel<FileNodeTreeMapNode> mi_model;

    public TreeMapModel<FileNodeTreeMapNode> getModel()
    {
      if (mi_model == null)
      {
        try (PerformancePoint pp = Performance.measure("Collecting data for tree chart tab"))
        {
          mi_model = new TreeMapModel<>(new FileNodeTreeMapNode(getCurrentFileNode()));
        }
      }

      return mi_model;
    }

    public void select(FileNodeIF value)
    {
      if (mi_model != null)
      {
        mi_model.getRootNode().streamNode().map(tmn -> (FileNodeTreeMapNode) tmn)
            .filter(fntmn -> fntmn.getFileNode() == value).findFirst().ifPresent(fntmn -> {
              m_treeMap.select(fntmn);
            });
      }
    }

    @Override
    public void reset()
    {
      mi_model = null;
    }
  }

  public static class FileNodeTreeMapNode
    extends TreeMapNode
  {
    private FileNodeIF mi_fileNode;

    FileNodeTreeMapNode(FileNodeIF fileNode)
    {
      mi_fileNode = fileNode;
    }

    public FileNodeIF getFileNode()
    {
      return mi_fileNode;
    }

    @Override
    public String getTooltipText()
    {
      return mi_fileNode.getAbsolutePath() + " (" + mi_fileNode.getSize() + " bytes)";
    }

    @Override
    public String getName()
    {
      return mi_fileNode.getName();
    }

    @Override
    public long getSize()
    {
      return mi_fileNode.getSize();
    }

    @Override
    protected List<TreeMapNode> initChildList()
    {
      if (mi_fileNode instanceof DirNode dirNode)
      {
        return dirNode.getChildList().stream().map(FileNodeTreeMapNode::new).map(TreeMapNode.class::cast).toList();
      }

      return new ArrayList<>();
    }
  }
}