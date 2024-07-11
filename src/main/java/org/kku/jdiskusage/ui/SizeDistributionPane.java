package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn.ButtonProperty;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.AbstractTabContentPane;
import org.kku.jdiskusage.ui.common.FileNodeIterator;
import org.kku.jdiskusage.ui.common.Filter;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.ui.util.IconUtil;
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

public class SizeDistributionPane
  extends AbstractTabContentPane
{
  private SizeDistributionPaneData mi_data = new SizeDistributionPaneData();

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

    static public SizeDistributionBucket findBucket(long value)
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

  SizeDistributionPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("PIECHART", "Show pie chart", "chart-pie", this::getPieChartNode);
    createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode, true);
    createPaneType("TABLE", "Show details table", "table", this::getTableNode);

    init();
  }

  private SizeDistributionBucket findBucket(FileNodeIF fileNode)
  {
    return SizeDistributionBucket.findBucket(fileNode.getSize());
  }

  Node getPieChartNode()
  {
    PieChart pieChart;

    pieChart = FxUtil.createPieChart();
    mi_data.getMap().entrySet().forEach(entry -> {
      SizeDistributionBucket bucket;
      SizeDistributionBucketData bucketData;
      PieChart.Data data;

      bucket = entry.getKey();
      bucketData = entry.getValue();
      data = new PieChart.Data(bucket.getText(), bucketData.getSize(getCurrentDisplayMetric()));
      pieChart.getData().add(data);

      addFilterHandler(data.getNode(), "Size", bucket.getText(), fileNode -> findBucket(fileNode) == bucket);
    });

    return pieChart;
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
      SizeDistributionBucketData dataDefault;

      dataDefault = new SizeDistributionBucketData(0l, 0l);
      pane = new GridPane();

      xAxis = new NumberAxis();
      yAxis = new CategoryAxis();
      barChart = FxUtil.createBarChart(xAxis, yAxis);
      barChart.setTitle(translate("Distribution of file sizes in") + " " + treeItem.getValue().getName());
      xAxis.setLabel(translate("Number of files"));
      yAxis.setLabel(translate("File sizes"));

      series1 = new XYChart.Series<>();
      barChart.getData().add(series1);

      Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
        SizeDistributionBucketData value;
        XYChart.Data<Number, String> data;

        value = mi_data.getMap().getOrDefault(bucket, dataDefault);
        data = new XYChart.Data<Number, String>(value.mi_numberOfFiles, bucket.getText());
        series1.getData().add(data);
        addFilterHandler(data.getNode(), "File size", bucket.getText(), fileNode -> findBucket(fileNode) == bucket);
      });

      pane.add(barChart, 0, 0);
      GridPane.setHgrow(barChart, Priority.ALWAYS);
      GridPane.setVgrow(barChart, Priority.ALWAYS);

      xAxis = new NumberAxis();
      yAxis = new CategoryAxis();
      barChart = FxUtil.createBarChart(xAxis, yAxis);
      xAxis.setLabel(translate("Total size of files (in Gb)"));
      yAxis.setLabel(translate("File sizes"));

      series2 = new XYChart.Series<>();
      barChart.getData().add(series2);

      Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
        SizeDistributionBucketData value;
        XYChart.Data<Number, String> data;

        value = mi_data.getMap().getOrDefault(bucket, dataDefault);
        data = new XYChart.Data<Number, String>(value.mi_sizeOfFiles, bucket.getText());
        series2.getData().add(data);
        addFilterHandler(data.getNode(), "File size", bucket.getText(), fileNode -> findBucket(fileNode) == bucket);
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
      MyTableView<Entry<SizeDistributionBucket, SizeDistributionBucketData>> table;
      MyTableColumn<Entry<SizeDistributionBucket, SizeDistributionBucketData>, String> timeIntervalColumn;
      MyTableColumn<Entry<SizeDistributionBucket, SizeDistributionBucketData>, Long> sumOfFileSizesColumn;
      MyTableColumn<Entry<SizeDistributionBucket, SizeDistributionBucketData>, Long> numberOfFilesColumn;
      MyTableColumn<Entry<SizeDistributionBucket, SizeDistributionBucketData>, Void> filterColumn;
      MyTableColumn<Entry<SizeDistributionBucket, SizeDistributionBucketData>, ButtonProperty<Entry<SizeDistributionBucket, SizeDistributionBucketData>>> filterEqualColumn;
      MyTableColumn<Entry<SizeDistributionBucket, SizeDistributionBucketData>, ButtonProperty<Entry<SizeDistributionBucket, SizeDistributionBucketData>>> filterGreaterThanColumn;
      MyTableColumn<Entry<SizeDistributionBucket, SizeDistributionBucketData>, ButtonProperty<Entry<SizeDistributionBucket, SizeDistributionBucketData>>> filterLessThanColumn;
      ButtonProperty<Entry<SizeDistributionBucket, SizeDistributionBucketData>> filterItemEqualProperty;
      ButtonProperty<Entry<SizeDistributionBucket, SizeDistributionBucketData>> filterItemGreaterThanProperty;
      ButtonProperty<Entry<SizeDistributionBucket, SizeDistributionBucketData>> filterItemLessThanProperty;

      pane = new GridPane();
      table = new MyTableView<>("SizeDistribution");
      table.setEditable(false);

      table.addRankColumn("Rank");

      timeIntervalColumn = table.addColumn("Size");
      timeIntervalColumn.setCellValueAlignment(Pos.BASELINE_LEFT);
      timeIntervalColumn.setColumnCount(12);
      timeIntervalColumn.setCellValueGetter((o) -> o.getKey().getText());

      sumOfFileSizesColumn = table.addColumn("Sum of file sizes");
      sumOfFileSizesColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);
      sumOfFileSizesColumn.setColumnCount(8);
      sumOfFileSizesColumn.setCellValueGetter((o) -> o.getValue().mi_sizeOfFiles);

      numberOfFilesColumn = table.addColumn("Sum of number of files");
      numberOfFilesColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);
      numberOfFilesColumn.setColumnCount(8);
      numberOfFilesColumn.setCellValueGetter((o) -> o.getValue().mi_numberOfFiles);

      filterColumn = table.addColumn("Filter file size");

      filterItemLessThanProperty = new ButtonProperty<>(() -> IconUtil.createIconNode("filter", IconSize.SMALLER));
      filterLessThanColumn = table.addColumn(filterColumn, "<=");
      filterLessThanColumn.setCellValueAlignment(Pos.CENTER);
      filterLessThanColumn.setEditable(true);
      filterLessThanColumn.setCellValueGetter((e) -> filterItemLessThanProperty);
      filterLessThanColumn.setAction((event, entry) -> {
        Predicate<FileNodeIF> filterPredicate;

        filterPredicate = (fileNode) -> findBucket(fileNode).ordinal() <= entry.getKey().ordinal();
        getDiskUsageData().addFilter(new Filter("File size", "<=", entry.getKey().getText(), filterPredicate),
            event.getClickCount() == 2);
      });

      filterItemEqualProperty = new ButtonProperty<>(() -> IconUtil.createIconNode("filter", IconSize.SMALLER));
      filterEqualColumn = table.addColumn(filterColumn, "is");
      filterEqualColumn.setCellValueAlignment(Pos.CENTER);
      filterEqualColumn.setEditable(true);
      filterEqualColumn.setCellValueGetter((e) -> filterItemEqualProperty);
      filterEqualColumn.setAction((event, entry) -> {
        Predicate<FileNodeIF> filterPredicate;

        filterPredicate = (fileNode) -> findBucket(fileNode) == entry.getKey();
        getDiskUsageData().addFilter(new Filter("Link count", entry.getKey().getText(), filterPredicate),
            event.getClickCount() == 2);
      });

      filterItemGreaterThanProperty = new ButtonProperty<>(() -> IconUtil.createIconNode("filter", IconSize.SMALLER));
      filterGreaterThanColumn = table.addColumn(filterColumn, ">=");
      filterGreaterThanColumn.setCellValueAlignment(Pos.CENTER);
      filterGreaterThanColumn.setEditable(true);
      filterGreaterThanColumn.setCellValueGetter((e) -> filterItemGreaterThanProperty);
      filterGreaterThanColumn.setAction((event, entry) -> {
        Predicate<FileNodeIF> filterPredicate;

        filterPredicate = (fileNode) -> findBucket(fileNode).ordinal() >= entry.getKey().ordinal();
        getDiskUsageData().addFilter(new Filter("Link count", ">=", entry.getKey().getText(), filterPredicate),
            event.getClickCount() == 2);
      });
      table.setItems(mi_data.getList());

      pane.add(table, 0, 1);
      GridPane.setHgrow(table, Priority.ALWAYS);
      GridPane.setVgrow(table, Priority.ALWAYS);

      return pane;
    }

    return new Label("No data");
  }

  private class SizeDistributionPaneData
    extends PaneData
  {
    private Map<SizeDistributionBucket, SizeDistributionBucketData> mi_map;
    private ObservableList<Entry<SizeDistributionBucket, SizeDistributionBucketData>> mi_list;

    private SizeDistributionPaneData()
    {
    }

    public ObservableList<Entry<SizeDistributionBucket, SizeDistributionBucketData>> getList()
    {
      if (mi_list == null)
      {
        mi_list = getMap().entrySet().stream().collect(Collectors.toCollection(FXCollections::observableArrayList));
      }

      return mi_list;
    }

    public Map<SizeDistributionBucket, SizeDistributionBucketData> getMap()
    {
      if (mi_map == null)
      {
        try (PerformancePoint pp = Performance.start("Collecting data for size distribution"))
        {
          mi_map = new LinkedHashMap<>();
          Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
            mi_map.put(bucket, new SizeDistributionBucketData(0l, 0l));
          });

          new FileNodeIterator(getCurrentTreeItem().getValue()).forEach(fn -> {
            if (fn.isFile())
            {
              SizeDistributionPane.SizeDistributionBucket bucket;
              SizeDistributionBucketData data;

              bucket = SizeDistributionBucket.findBucket(fn.getSize());
              data = mi_map.get(bucket);
              data.mi_numberOfFiles += 1;
              data.mi_sizeOfFiles += (fn.getSize() / 1000000);
            }

            return true;
          });
        }
      }

      return mi_map;
    }

    @Override
    public void reset()
    {
      mi_map = null;
      mi_list = null;
    }
  }
}