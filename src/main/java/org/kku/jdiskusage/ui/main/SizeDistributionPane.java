package org.kku.jdiskusage.ui.main;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.kku.jdiskusage.ui.main.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.main.DiskUsageView.FileNodeIterator;
import org.kku.jdiskusage.ui.main.common.AbstractTabContentPane;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class SizeDistributionPane
  extends AbstractTabContentPane
{
  public enum SizeDistributionBucket
  {
    INVALID("Invalid", -Double.MAX_VALUE, 0),
    SIZE_0_KB_TO_1_KB("0 KB - 1 KB", kilo_bytes(0), kilo_bytes(1)),
    SIZE_1_KB_TO_4_KB("1 KB - 4 KB", kilo_bytes(1), kilo_bytes(4)),
    SIZE_4_KB_TO_16_KB("4 KB - 16 KB", kilo_bytes(4), kilo_bytes(16)),
    SIZE_16_KB_TO_64_KB("16 KB - 64 KB", kilo_bytes(16), kilo_bytes(64)),
    SIZE_64_KB_TO_256_KB("64 KB - 256 KB", kilo_bytes(64), kilo_bytes(256)),
    SIZE_256_KB_TO_1_MB("256 KB - 1 MB", kilo_bytes(256), mega_bytes(1)),
    SIZE_1_MB_TO_4_MB("1 MB - 4 MB", mega_bytes(1), mega_bytes(4)),
    SIZE_4_MB_TO_16_MB("4 MB - 16 MB", mega_bytes(4), mega_bytes(16)),
    SIZE_16_MB_TO_64_MB("16 MB - 64 MB", mega_bytes(16), mega_bytes(64)),
    SIZE_64_MB_TO_256_MB("64 MB - 256 MB", mega_bytes(64), mega_bytes(256)),
    SIZE_256_MB_TO_1_GB("256 MB - 1 GB", mega_bytes(256), giga_bytes(1)),
    SIZE_1_GB_TO_4_GB("1 GB - 4 GB", giga_bytes(1), giga_bytes(4)),
    SIZE_4_GB_TO_16_GB("4 GB - 16 GB", giga_bytes(4), giga_bytes(16)),
    SIZE_OVER_16_GB("Over 16 GB", giga_bytes(16), Double.MAX_VALUE);

    private final String mi_text;
    private final double mi_from;
    private final double mi_to;

    SizeDistributionBucket(String text, double from, double to)
    {
      mi_text = text;
      mi_from = from;
      mi_to = to;
    }

    public String getText()
    {
      return translate(mi_text);
    }

    double getFrom()
    {
      return mi_from;
    }

    double getTo()
    {
      return mi_to;
    }

    static public SizeDistributionPane.SizeDistributionBucket findBucket(long value)
    {
      int length;
      SizeDistributionPane.SizeDistributionBucket[] buckets;

      buckets = SizeDistributionBucket.values();
      length = buckets.length;
      for (int i = 0; i < length; i++)
      {
        // Buckets are ordered by size. So there is no need to check the getFrom()
        if (value < buckets[i].getTo())
        {
          return buckets[i];
        }
      }

      return INVALID;
    }

    static private double kilo_bytes(double value)
    {
      return 1000 * value;
    }

    static private double mega_bytes(double value)
    {
      return kilo_bytes(value) * 1000;
    }

    static private double giga_bytes(double value)
    {
      return mega_bytes(value) * 1000;
    }
  }

  private class SizeDistributionBucketData
  {
    public long mi_numberOfFiles;
    public long mi_sizeOfFiles;

    public SizeDistributionBucketData(Long numberOfFiles, Long sizeOfFiles)
    {
      mi_numberOfFiles = numberOfFiles;
      mi_sizeOfFiles = sizeOfFiles;
    }
  }

  SizeDistributionPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("PIECHART", "Show details table", "chart-pie", this::getPieChartNode);
    createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode, true);
    createPaneType("TABLE", "Show details table", "table", this::getTableNode);

    init();
  }

  Node getPieChartNode()
  {
    return new Label("Pie chart");
  }

  Node getBarChartNode()
  {
    TreeItem<FileNodeIF> treeItem;

    treeItem = getDiskUsageData().getSelectedTreeItem();
    if (!treeItem.getChildren().isEmpty())
    {
      GridPane pane;
      NumberAxis xAxis;
      CategoryAxis yAxis;
      BarChart<Number, String> barChart;
      XYChart.Series<Number, String> series1;
      XYChart.Series<Number, String> series2;
      FileNodeIF node;
      Map<SizeDistributionPane.SizeDistributionBucket, SizeDistributionBucketData> map;
      Map<SizeDistributionPane.SizeDistributionBucket, SizeDistributionBucketData> map2;
      SizeDistributionBucketData dataDefault;

      dataDefault = new SizeDistributionBucketData(0l, 0l);

      pane = new GridPane();

      node = treeItem.getValue();

      try (PerformancePoint pp = Performance.start("Collecting data for last modified barchart"))
      {
        map = new HashMap<>();
        new FileNodeIterator(node).forEach(fn -> {
          if (fn.isFile())
          {
            SizeDistributionPane.SizeDistributionBucket bucket;
            SizeDistributionBucketData data;

            bucket = SizeDistributionBucket.findBucket(fn.getSize());
            data = map.computeIfAbsent(bucket, (a) -> new SizeDistributionBucketData(0l, 0l));
            data.mi_numberOfFiles += 1;
            data.mi_sizeOfFiles += (fn.getSize() / 1000000);
          }
        });
      }

      xAxis = new NumberAxis();
      yAxis = new CategoryAxis();
      barChart = FxUtil.createBarChart(xAxis, yAxis);
      barChart.setTitle(translate("Distribution of file sizes in") + " " + treeItem.getValue().getName());
      xAxis.setLabel(translate("Number of files"));
      yAxis.setLabel(translate("File sizes"));

      series1 = new XYChart.Series<>();
      Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
        SizeDistributionBucketData value;
        value = map.getOrDefault(bucket, dataDefault);
        series1.getData().add(new XYChart.Data<Number, String>(value.mi_numberOfFiles, bucket.getText()));
      });

      barChart.getData().add(series1);
      pane.add(barChart, 0, 0);
      GridPane.setHgrow(barChart, Priority.ALWAYS);
      GridPane.setVgrow(barChart, Priority.ALWAYS);

      xAxis = new NumberAxis();
      yAxis = new CategoryAxis();
      barChart = FxUtil.createBarChart(xAxis, yAxis);
      xAxis.setLabel(translate("Total size of files (in Gb)"));
      yAxis.setLabel(translate("File sizes"));

      series2 = new XYChart.Series<>();
      Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
        SizeDistributionBucketData value;
        value = map.getOrDefault(bucket, dataDefault);
        series2.getData().add(new XYChart.Data<Number, String>(value.mi_sizeOfFiles, bucket.getText()));
      });

      barChart.getData().add(series2);
      pane.add(barChart, 0, 1);
      GridPane.setHgrow(barChart, Priority.ALWAYS);
      GridPane.setVgrow(barChart, Priority.ALWAYS);

      return pane;
    }

    return new Label("No data");
  }

  Node getTableNode()
  {
    return new Label("Table");
  }
}