package org.kku.jdiskusage.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapChart;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapModel;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapNode;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.TreeMapChartFormPane.PathNodeTreeMapNode;
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
  private TreeMapChart<TreeMapNode> m_treeMap;
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
    if (!isDecendantOf(m_root, selectedTreeItem))
    {
      // If the selected tree item is not part of the root tree -> draw the tree map chart from scratch
      super.refresh(selectedTreeItem);
      m_root = selectedTreeItem;
    }
    else
    {
      // If the selected tree item is part of the root tree -> select the part of the chart with a red border
      m_data.select(selectedTreeItem);
    }
  }

  private boolean isDecendantOf(TreeItem<FileNodeIF> parent, TreeItem<FileNodeIF> child)
  {
    while (child != null)
    {
      if (Objects.equals(parent, child))
      {
        return true;
      }

      child = child.getParent();
    }

    return false;
  }

  Node getTreeChartNode()
  {
    BorderPane pane;

    m_treeMap = new TreeMapChart<>();
    m_treeMap.setModel(m_data.getModel());
    m_treeMap.addReselectListener((ae) -> {
      m_root = null;
      refresh(getDiskUsageData().selectedTreeItemProperty().get());
    });

    pane = new BorderPane();
    pane.setCenter(m_treeMap);

    return pane;
  }

  class TreeMapChartPaneData
    extends PaneData
  {
    private TreeMapModel<TreeMapNode> mi_model;

    public TreeMapModel<TreeMapNode> getModel()
    {
      if (mi_model == null)
      {
        try (PerformancePoint pp = Performance.measure("Collecting data for tree chart tab"))
        {
          mi_model = new TreeMapModel<>(PathNodeTreeMapNode.create(getCurrentFileNode()));
        }
      }

      return mi_model;
    }

    public void select(TreeItem<FileNodeIF> selectedTreeItem)
    {
      if (mi_model != null)
      {
        FileNodeIF fileNodeParent;
        List<String> list;
        TreeMapNode selectedNode;
        int rootFound;

        // Collect the path of the fileNode
        fileNodeParent = selectedTreeItem.getValue();
        list = new ArrayList<>();
        while (fileNodeParent != null)
        {
          list.add(fileNodeParent.getName());
          fileNodeParent = fileNodeParent.getParent();
        }
        list = list.reversed();

        selectedNode = mi_model.getRootNode();
        rootFound = -1;
        for (int i = 0; i < list.size(); i++)
        {
          String path;

          path = list.get(i);
          if (path.equals(selectedNode.getName()))
          {
            rootFound = i;
            break;
          }
        }

        if (rootFound != -1)
        {
          for (int i = rootFound + 1; i < list.size(); i++)
          {
            if (selectedNode != null)
            {
              selectedNode = selectedNode.getChild(list.get(i));
            }
          }

          if (selectedNode != mi_model.getRootNode() && selectedNode != null)
          {
            m_treeMap.select((PathNodeTreeMapNode) selectedNode);
            return;
          }
        }

        mi_model.getRootNode().streamNode().map(tmn -> (PathNodeTreeMapNode) tmn)
            .filter(fntmn -> fntmn.getFileNode() == selectedTreeItem.getValue()).findFirst().ifPresent(fntmn -> {
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
    extends PathNodeTreeMapNode
  {
    public FileNodeTreeMapNode(FileNodeIF fileNode)
    {
      super(fileNode);
    }

    @Override
    public boolean hasChildren()
    {
      return false;
    }

    @Override
    protected List<TreeMapNode> initChildList()
    {
      return Collections.emptyList();
    }
  }

  public static class DirNodeTreeMapNode
    extends PathNodeTreeMapNode
  {
    private DirNode mi_dirNode;

    public DirNodeTreeMapNode(DirNode fileNode)
    {
      super(fileNode);

      mi_dirNode = fileNode;
    }

    @Override
    protected List<TreeMapNode> initChildList()
    {
      List<TreeMapNode> resultList;

      resultList = new ArrayList<>(mi_dirNode.getChildList().size());
      mi_dirNode.getChildList().forEach(fn -> {
        resultList.add(PathNodeTreeMapNode.create(fn));
      });

      return resultList;
    }
  }

  public abstract static class PathNodeTreeMapNode
    extends TreeMapNode
  {
    private FileNodeIF mi_fileNode;

    protected PathNodeTreeMapNode(FileNodeIF fileNode)
    {
      mi_fileNode = fileNode;
    }

    public static TreeMapNode create(FileNodeIF fileNode)
    {
      if (fileNode instanceof DirNode dirNode)
      {
        return new DirNodeTreeMapNode(dirNode);
      }
      else
      {
        return new FileNodeTreeMapNode(fileNode);
      }
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
  }
}