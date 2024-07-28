package org.kku.jdiskusage.ui;

import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.AbstractFormPane;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;

class SizeFormPane
  extends AbstractFormPane
{
  SizeFormPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("PIECHART", "Show pie chart", "chart-pie", this::getPieChartNode, true);
    createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode);
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
    minimumDataSize = totalSize * 0.02;

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

    applyCustomColorSequence(chart.getData());

    return chart;
  }

  private static final String[] COMBINED_COLORS =
  {
      // Default JavaFX colors
      "#f9d900", // Default gold
      "#a9e200", // Default lime green
      "#22bad9", // Default sky blue
      "#0181e2", // Default azure
      "#2f357f", // Default indigo
      "#860061", // Default purple
      "#c62b00", // Default rust
      "#ff5700", // Default orange

      // Custom colors
      "#FFD700", // Custom golden yellow
      "#98FB98", // Custom pale green
      "#00CED1", // Custom dark turquoise
      "#4169E1", // Custom royal blue
      "#483D8B", // Custom dark slate blue
      "#9932CC", // Custom dark orchid
      "#FF4500", // Custom orange red
      "#FF8C00" // Custom dark orange
  };

  private void applyCustomColorSequence(ObservableList<PieChart.Data> pieChartData)
  {
    int i = 0;
    for (PieChart.Data data : pieChartData)
    {
      data.getNode().setStyle("-fx-pie-color: " + COMBINED_COLORS[i % COMBINED_COLORS.length] + ";");
      i++;
    }
  }

  Node getBarChartNode()
  {
    return new Label("Bar chart");
  }

  Node getTableNode()
  {
    return new Label("Table chart");
  }
}