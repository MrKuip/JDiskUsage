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
import javafx.scene.layout.BorderPane;

class TreeMapChartFormPane
  extends AbstractFormPane
{
  private final TreeMapChartPaneData m_data = new TreeMapChartPaneData();
  private BorderPane pane = new BorderPane();

  TreeMapChartFormPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("TREECHART", "Show tree chart", "chart-tree", this::getTreeChartNode);

    init();
  }

  Node getTreeChartNode()
  {
    TreeMapChart treeMap;

    treeMap = new TreeMapChart();
    treeMap.setModel(m_data.getModel());

    pane.setCenter(treeMap);

    return pane;
  }

  class TreeMapChartPaneData
    extends PaneData
  {
    private TreeMapModel mi_model;

    public TreeMapModel getModel()
    {
      if (mi_model == null)
      {
        try (PerformancePoint pp = Performance.measure("Collecting data for tree chart tab"))
        {
          mi_model = new TreeMapModel(new FileNodeTreeMapNode(getCurrentFileNode()));
        }
      }

      return mi_model;
    }

    @Override
    public void reset()
    {
      mi_model = null;
    }
  }

  class FileNodeTreeMapNode
    extends TreeMapNode
  {
    private FileNodeIF mi_fileNode;

    FileNodeTreeMapNode(FileNodeIF fileNode)
    {
      mi_fileNode = fileNode;
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
    public double getSize()
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