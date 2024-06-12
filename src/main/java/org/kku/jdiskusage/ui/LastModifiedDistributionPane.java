package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.DiskUsageView.FileNodeIterator;
import org.kku.jdiskusage.ui.common.AbstractTabContentPane;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.util.CommonUtil;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import org.kku.jdiskusage.util.preferences.DisplayMetric;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

class LastModifiedDistributionPane
  extends AbstractTabContentPane
{
  private LastModifiedDistributionPaneData mi_data = new LastModifiedDistributionPaneData();

  private enum LastModifiedDistributionBucket
  {
    INVALID(() -> translate("Invalid"), Long.MIN_VALUE + 1),
    LAST_MODIFIED_FUTURE(() -> translate("In the future"), 0),
    LAST_MODIFIED_TODAY(() -> translate("Today"), days(1)),
    LAST_MODIFIED_YESTERDAY(() -> translate("Yesterday"), days(2)),
    LAST_MODIFIED_1_DAY_TILL_7_DAYS(() -> "2 - 7 " + translate("days"), days(8)),
    LAST_MODIFIED_7_DAYs_TILL_30_DAYS(() -> "7 - 30 " + translate("days"), days(31)),
    LAST_MODIFIED_30_DAYS_TILL_90_DAYS(() -> "30 - 90 " + translate("days"), days(91)),
    LAST_MODIFIED_90_DAYS_TILL_180_DAYS(() -> "90 - 180 " + translate("days"), days(181)),
    LAST_MODIFIED_180_DAYS_TILL_365_DAYS(() -> "180 - 365 " + translate("days"), years(1)),
    LAST_MODIFIED_1_YEAR_TILL_2_YEAR(() -> "1 - 2 " + translate("years"), years(2)),
    LAST_MODIFIED_2_YEAR_TILL_3_YEAR(() -> "2 - 3 " + translate("years"), years(3)),
    LAST_MODIFIED_3_YEAR_TILL_6_YEAR(() -> "3 - 6 " + translate("years"), years(6)),
    LAST_MODIFIED_6_YEAR_TILL_10_YEAR(() -> "6 - 10 " + translate("years"), years(10)),
    LAST_MODIFIED_OVER_10_YEARS(() -> translate("Over") + " 10 " + translate("years"), Long.MAX_VALUE);

    private final Supplier<String> mi_text;
    private final long mi_to;

    LastModifiedDistributionBucket(Supplier<String> text, long to)
    {
      mi_text = text;
      mi_to = to;
    }

    public String getText()
    {
      return mi_text.get();
    }

    long getTo()
    {
      return mi_to;
    }

    static public LastModifiedDistributionBucket findBucket(long todayMidnight, long lastModified)
    {
      long ago;

      ago = todayMidnight - lastModified;

      int length;
      LastModifiedDistributionBucket[] buckets;

      buckets = LastModifiedDistributionBucket.values();
      length = buckets.length;
      for (int i = 0; i < length; i++)
      {
        // Buckets are ordered by size. So there is no need to check the getFrom()
        if (ago < buckets[i].getTo())
        {
          return buckets[i];
        }
      }

      return INVALID;
    }

    private static long days(long days)
    {
      return days * 24 * 60 * 60 * 1000;
    }

    private static long years(long years)
    {
      return days(years * 365);
    }
  }

  private class LastModifiedDistributionBucketData
  {
    public long mi_numberOfFiles;
    public long mi_sizeOfFiles;

    public LastModifiedDistributionBucketData(Long numberOfFiles, Long sizeOfFiles)
    {
      mi_numberOfFiles = numberOfFiles;
      mi_sizeOfFiles = sizeOfFiles;
    }

    public double getSize(DisplayMetric currentDisplayMetric)
    {
      switch (currentDisplayMetric)
      {
        case FILE_COUNT:
          return mi_numberOfFiles;
        case FILE_SIZE:
          return mi_sizeOfFiles;
        default:
          break;
      }
      return 0;
    }
  }

  private class LastModifiedDistributionPaneData
    extends PaneData
  {
    private final long mi_todayMidnight = CommonUtil.getMidnight();
    private Map<LastModifiedDistributionBucket, LastModifiedDistributionBucketData> mi_map;
    private ObservableList<Entry<LastModifiedDistributionBucket, LastModifiedDistributionBucketData>> mi_list;

    private LastModifiedDistributionPaneData()
    {
    }

    public ObservableList<Entry<LastModifiedDistributionBucket, LastModifiedDistributionBucketData>> getList()
    {
      if (mi_list == null)
      {
        mi_list = mi_map.entrySet().stream().collect(Collectors.toCollection(FXCollections::observableArrayList));
      }

      return mi_list;
    }

    public Map<LastModifiedDistributionBucket, LastModifiedDistributionBucketData> getMap()
    {
      if (mi_map == null)
      {
        try (PerformancePoint pp = Performance.start("Collecting data for last modified table"))
        {
          mi_map = new LinkedHashMap<>();
          Stream.of(LastModifiedDistributionBucket.values()).forEach(bucket -> {
            mi_map.put(bucket, new LastModifiedDistributionBucketData(0l, 0l));
          });

          new FileNodeIterator(getCurrentTreeItem().getValue()).forEach(fn -> {
            if (fn.isFile())
            {
              LastModifiedDistributionBucket bucket;
              LastModifiedDistributionBucketData data;

              bucket = findBucket(fn);
              data = mi_map.get(bucket);
              data.mi_numberOfFiles += 1;
              data.mi_sizeOfFiles += (fn.getSize() / 1000000);
            }
          });
        }
      }

      return mi_map;
    }

    @Override
    public void currentTreeItemChanged()
    {
      mi_list = null;
      mi_map = null;
    }

    @Override
    public void currentDisplayMetricChanged()
    {
      mi_list = null;
      mi_map = null;
    }
  }

  LastModifiedDistributionPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("PIECHART", "Show details table", "chart-pie", this::getPieChartNode);
    createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode);
    createPaneType("TABLE", "Show details table", "table", this::getTableNode);

    init();
  }

  private LastModifiedDistributionBucket findBucket(FileNodeIF fileNode)
  {
    return LastModifiedDistributionBucket.findBucket(mi_data.mi_todayMidnight, fileNode.getLastModifiedTime());
  }

  Node getPieChartNode()
  {
    PieChart pieChart;

    pieChart = FxUtil.createPieChart();
    mi_data.getMap().entrySet().forEach(entry -> {
      LastModifiedDistributionBucket bucket;
      LastModifiedDistributionBucketData bucketData;
      PieChart.Data data;

      bucket = entry.getKey();
      bucketData = entry.getValue();
      data = new PieChart.Data(bucket.getText(), bucketData.getSize(getCurrentDisplayMetric()));
      pieChart.getData().add(data);

      addFilter(data.getNode(), "Modification date", bucket.getText(), fileNode -> bucket == findBucket(fileNode));
    });

    return pieChart;
  }

  Node getBarChartNode()
  {
    TreeItem<FileNodeIF> treeItem;

    treeItem = getDiskUsageData().getSelectedTreeItem();
    if (treeItem != null && !treeItem.getChildren().isEmpty())
    {
      GridPane pane;
      NumberAxis xAxis;
      CategoryAxis yAxis;
      BarChart<Number, String> barChart;
      XYChart.Series<Number, String> series1;
      XYChart.Series<Number, String> series2;
      LastModifiedDistributionBucketData dataDefault;

      dataDefault = new LastModifiedDistributionBucketData(0l, 0l);

      pane = new GridPane();

      xAxis = new NumberAxis();
      yAxis = new CategoryAxis();
      barChart = FxUtil.createBarChart(xAxis, yAxis);
      barChart.setTitle(translate("Distribution of last modified dates in") + " " + treeItem.getValue().getName());
      xAxis.setLabel(translate("Number of files"));
      yAxis.setLabel(translate("Last modified date"));

      series1 = new XYChart.Series<>();
      barChart.getData().add(series1);

      Stream.of(LastModifiedDistributionBucket.values()).forEach(bucket -> {
        LastModifiedDistributionBucketData value;
        XYChart.Data<Number, String> data;

        value = mi_data.getMap().getOrDefault(bucket, dataDefault);
        data = new XYChart.Data<Number, String>(value.mi_numberOfFiles, bucket.getText());
        series1.getData().add(data);
        addFilter(data.getNode(), "Modification date", bucket.getText(), fileNode -> bucket == findBucket(fileNode));
      });

      pane.add(barChart, 0, 0);
      GridPane.setHgrow(barChart, Priority.ALWAYS);
      GridPane.setVgrow(barChart, Priority.ALWAYS);

      xAxis = new NumberAxis();
      yAxis = new CategoryAxis();
      barChart = FxUtil.createBarChart(xAxis, yAxis);
      xAxis.setLabel(translate("Total size of files (in Gb)"));
      yAxis.setLabel(translate("Last modified date"));

      series2 = new XYChart.Series<>();
      barChart.getData().add(series2);

      Stream.of(LastModifiedDistributionBucket.values()).forEach(bucket -> {
        LastModifiedDistributionBucketData value;
        XYChart.Data<Number, String> data;

        value = mi_data.getMap().getOrDefault(bucket, dataDefault);
        data = new XYChart.Data<Number, String>(value.mi_sizeOfFiles, bucket.getText());
        series2.getData().add(data);
        addFilter(data.getNode(), "Modification date", bucket.getText(), fileNode -> bucket == findBucket(fileNode));
      });

      pane.add(barChart, 0, 1);
      GridPane.setHgrow(barChart, Priority.ALWAYS);
      GridPane.setVgrow(barChart, Priority.ALWAYS);

      return pane;
    }

    return new Label("No data");
  }

  Node getTableNode()
  {
    TreeItem<FileNodeIF> treeItem;

    treeItem = getDiskUsageData().getSelectedTreeItem();
    if (treeItem != null && !treeItem.getChildren().isEmpty())
    {
      GridPane pane;
      MyTableView<Entry<LastModifiedDistributionBucket, LastModifiedDistributionBucketData>> table;
      MyTableColumn<Entry<LastModifiedDistributionBucket, LastModifiedDistributionBucketData>, String> timeIntervalColumn;
      MyTableColumn<Entry<LastModifiedDistributionBucket, LastModifiedDistributionBucketData>, Long> sumOfFileSizesColumn;
      MyTableColumn<Entry<LastModifiedDistributionBucket, LastModifiedDistributionBucketData>, Long> numberOfFilesColumn;

      pane = new GridPane();
      table = new MyTableView<>("LastModifiedDistribution");
      table.setEditable(false);

      timeIntervalColumn = table.addColumn("Time interval");
      timeIntervalColumn.initPersistentPrefWidth(300.0);
      timeIntervalColumn.setCellValueGetter((o) -> o.getKey().getText());
      sumOfFileSizesColumn = table.addColumn("Sum of file sizes");
      sumOfFileSizesColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      sumOfFileSizesColumn.initPersistentPrefWidth(100.0);
      sumOfFileSizesColumn.setCellValueGetter((o) -> o.getValue().mi_sizeOfFiles);
      numberOfFilesColumn = table.addColumn("Sum of file sizes");
      numberOfFilesColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      numberOfFilesColumn.initPersistentPrefWidth(100.0);
      numberOfFilesColumn.setCellValueGetter((o) -> o.getValue().mi_numberOfFiles);

      table.setItems(mi_data.getList());

      pane.add(table, 0, 1);
      GridPane.setHgrow(table, Priority.ALWAYS);
      GridPane.setVgrow(table, Priority.ALWAYS);

      return pane;
    }

    return new Label("No data");
  }

}