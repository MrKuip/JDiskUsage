package org.kku.jdiskusage.ui;

import org.kku.fx.ui.util.FxUtil;
import org.kku.jdiskusage.javafx.scene.chart.SunburstChart;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.AbstractFormPane;
import org.kku.jdiskusage.ui.util.ColorPalette;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;

class SizeFormPane
  extends AbstractFormPane
{
  SizeFormPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("PIECHART", "Show pie chart", "chart-pie", this::getPieChartNode, true);
    createPaneType("BARCHART", "Show sunburst chart", "chart-donut-variant", this::getSunburstChartNode);
    createPaneType("TABLE", "Show details table", "table", this::getTableNode);

    init();
  }

  Node getPieChartNode()
  {
    PieChart chart;
    double sum;
    double totalSize;
    double minimumDataSize;

    totalSize = getCurrentFileNode().getSize();
    minimumDataSize = totalSize * (AppPreferences.minPercentageChartElement.get() / 100.0);

    record MyData(PieChart.Data pieChartData, TreeItem<FileNodeIF> treeItem) {
    }

    chart = FxUtil.createPieChart();
    getCurrentTreeItem().getChildren().stream().filter(item -> {
      return item.getValue().getSize() > minimumDataSize;
    }).limit(AppPreferences.maxNumberOfChartElements.get()).map(item -> {
      PieChart.Data data;

      data = new PieChart.Data(item.getValue().getName(), item.getValue().getSize());
      data.nameProperty().bind(Bindings.concat(data.getName(), "\n",
          AppPreferences.sizeSystemPreference.get().getFileSize(data.getPieValue())));

      return new MyData(data, item);
    }).forEach(tuple -> {
      chart.getData().add(tuple.pieChartData);
      tuple.pieChartData.getNode().setUserData(tuple.treeItem);
      tuple.pieChartData.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, (me) -> {
        if (tuple.treeItem.getValue().isDirectory())
        {
          getDiskUsageData().getTreePaneData().navigateTo(tuple.treeItem);
        }
      });
    });

    if (chart.getData().size() != getCurrentTreeItem().getChildren().size())
    {
      sum = chart.getData().stream().map(data -> data.getPieValue()).reduce(0.0d, Double::sum);
      chart.getData()
          .add(new PieChart.Data(DiskUsageView.getOtherText(), getCurrentTreeItem().getValue().getSize() - sum));
    }

    return chart;
  }

  Node getSunburstChartNode()
  {
    SunburstChart<FileNodeIF> chart;

    chart = new SunburstChart<>(fn -> (double) fn.getSize());
    chart.setMaxLevel(AppPreferences.maxNumberOfElementsInSunburstChart.get());
    chart.setNodeCreationCallBack((node, treeNode) -> {
      FileNodeIF fileNode;

      fileNode = treeNode.getValue();

      Tooltip.install(node, new Tooltip(fileNode.getAbsolutePath()));

      node.addEventHandler(MouseEvent.MOUSE_CLICKED, (me) -> {
        if (fileNode.isDirectory())
        {
          getDiskUsageData().getTreePaneData().navigateTo(treeNode);
        }
        else
        {
          getDiskUsageData().getTreePaneData().navigateTo(treeNode.getParent());
        }
      });
    });
    chart.setColorList(ColorPalette.getColorList());
    chart.setModel(getCurrentTreeItem());

    return chart;
  }

  Node getTableNode()
  {
    return new Label("Table chart");
  }
}