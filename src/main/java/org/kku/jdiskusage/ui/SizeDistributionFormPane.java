package org.kku.jdiskusage.ui;

import static org.kku.fx.ui.util.TranslateUtil.translate;
import static org.kku.fx.ui.util.TranslateUtil.translatedTextProperty;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.common.util.Performance;
import org.kku.common.util.Performance.PerformancePoint;
import org.kku.common.util.StringUtils;
import org.kku.fx.scene.control.Filter;
import org.kku.fx.ui.util.FxUtil;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn.ButtonCell;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.AbstractFormPane;
import org.kku.jdiskusage.ui.common.FileNodeIterator;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.kku.jdiskusage.util.preferences.DisplayMetric;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;

public class SizeDistributionFormPane
  extends AbstractFormPane
{
  private SizeDistributionPaneData mi_data = new SizeDistributionPaneData();
  private static final String FILTER_NAME = "File size";

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
      SizeDistributionFormPane.SizeDistributionBucket[] buckets;

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

  static private class SizeDistributionBucketData
  {
    public long mi_numberOfFiles;
    public double mi_sizeOfFiles;

    public SizeDistributionBucketData(Long numberOfFiles, double sizeOfFiles)
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

  SizeDistributionFormPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("PIECHART", "Show pie chart", "mdi-chart-pie", this::getPieChartNode);
    createPaneType("BARCHART", "Show bar chart", "mdi-chart-bar", this::getBarChartNode, true);
    createPaneType("TABLE", "Show details table", "mdi-table", this::getTableNode);

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
    mi_data.getMap().entrySet().forEach(e -> {
      SizeDistributionBucket bucket;
      SizeDistributionBucketData bucketData;
      PieChart.Data data;

      bucket = e.getKey();
      bucketData = e.getValue();
      data = new PieChart.Data(bucket.getText(), bucketData.getSize(getCurrentDisplayMetric()));
      System.out.println("add data:" + data.getName() + " -> " + data.getPieValue());
      pieChart.getData().add(data);

      addFilterHandler(data.getNode(), FILTER_NAME, bucket.getText(), fileNode -> findBucket(fileNode) == bucket);
    });

    return pieChart;
  }

  Node getBarChartNode()
  {
    ScrollPane scrollPane;
    MigPane pane;
    NumberAxis xAxis;
    CategoryAxis yAxis;
    BarChart<Number, String> barChart;
    XYChart.Series<Number, String> series1;
    XYChart.Series<Number, String> series2;
    SizeDistributionBucketData dataDefault;

    dataDefault = new SizeDistributionBucketData(0l, 0l);

    pane = new MigPane("wrap 1", "[grow,fill]", "[][]");

    xAxis = new NumberAxis();
    yAxis = new CategoryAxis();
    barChart = FxUtil.createBarChart(xAxis, yAxis);
    pane.add(barChart);
    barChart.titleProperty().bind(
        translatedTextProperty("Distribution of file sizes in").concat(" ").concat(getCurrentFileNode().getName()));
    xAxis.labelProperty().bind(translatedTextProperty("Number of files"));
    yAxis.labelProperty().bind(translatedTextProperty("File sizes"));

    series1 = new XYChart.Series<>();
    barChart.getData().add(series1);

    Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
      SizeDistributionBucketData value;
      XYChart.Data<Number, String> data;

      value = mi_data.getMap().getOrDefault(bucket, dataDefault);
      data = new XYChart.Data<Number, String>(value.mi_numberOfFiles,
          StringUtils.truncate(bucket.getText(), AppPreferences.maxLabelSizeChart.get()));
      series1.getData().add(data);
      addFilterHandler(data.getNode(), FILTER_NAME, bucket.getText(), fileNode -> findBucket(fileNode) == bucket);
    });

    xAxis = new NumberAxis();
    yAxis = new CategoryAxis();
    barChart = FxUtil.createBarChart(xAxis, yAxis);
    pane.add(barChart);
    xAxis.labelProperty().bind(translatedTextProperty("Total size of files (in Gb)"));
    yAxis.labelProperty().bind(translatedTextProperty("File sizes"));

    series2 = new XYChart.Series<>();
    barChart.getData().add(series2);

    Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
      SizeDistributionBucketData value;
      XYChart.Data<Number, String> data;

      value = mi_data.getMap().getOrDefault(bucket, dataDefault);
      data = new XYChart.Data<Number, String>(value.mi_sizeOfFiles,
          StringUtils.truncate(bucket.getText(), AppPreferences.maxLabelSizeChart.get()));
      series2.getData().add(data);
      addFilterHandler(data.getNode(), FILTER_NAME, bucket.getText(), fileNode -> findBucket(fileNode) == bucket);
    });

    scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    scrollPane.setContent(pane);

    return scrollPane;
  }

  Node getTableNode()
  {
    MyTableView<SizeDistributionEntry> table;
    MyTableColumn<SizeDistributionEntry, String> timeIntervalColumn;
    MyTableColumn<SizeDistributionEntry, Double> sumOfFileSizesColumn;
    MyTableColumn<SizeDistributionEntry, Long> numberOfFilesColumn;
    MyTableColumn<SizeDistributionEntry, Void> filterColumn;
    MyTableColumn<SizeDistributionEntry, ButtonCell> filterEqualColumn;
    MyTableColumn<SizeDistributionEntry, ButtonCell> filterGreaterThanColumn;
    MyTableColumn<SizeDistributionEntry, ButtonCell> filterLessThanColumn;

    table = new MyTableView<>("SizeDistribution");
    table.setEditable(false);

    table.addRankColumn("Rank");

    timeIntervalColumn = table.addColumn("Size");
    timeIntervalColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);
    timeIntervalColumn.setColumnCount(12);
    timeIntervalColumn.setCellValueGetter((o) -> o.bucket().getText());

    sumOfFileSizesColumn = table.addColumn("Sum of file sizes");
    sumOfFileSizesColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);
    sumOfFileSizesColumn.setColumnCount(8);
    sumOfFileSizesColumn.setCellValueGetter((o) -> o.data().mi_sizeOfFiles);

    numberOfFilesColumn = table.addColumn("Sum of number of files");
    numberOfFilesColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);
    numberOfFilesColumn.setColumnCount(8);
    numberOfFilesColumn.setCellValueGetter((o) -> o.data().mi_numberOfFiles);

    filterColumn = table.addColumn("Filter file size");

    filterLessThanColumn = table.addFilterColumn(filterColumn, "<=");
    filterLessThanColumn.setAction((event, e) -> {
      getDiskUsageData().addFilter(new Filter<>(FILTER_NAME, "<=", e.bucket().getText(),
          (fileNode) -> findBucket(fileNode).ordinal() <= e.bucket().ordinal()), event.getClickCount() == 2);
    });

    filterEqualColumn = table.addFilterColumn(filterColumn, "==");
    filterEqualColumn.setAction((event, e) -> {
      getDiskUsageData().addFilter(
          new Filter<>(FILTER_NAME, e.bucket().getText(), (fileNode) -> findBucket(fileNode) == e.bucket()),
          event.getClickCount() == 2);
    });

    filterGreaterThanColumn = table.addFilterColumn(filterColumn, ">=");
    filterGreaterThanColumn.setAction((event, e) -> {
      getDiskUsageData().addFilter(new Filter<>(FILTER_NAME, ">=", e.bucket().getText(),
          (fileNode) -> findBucket(fileNode).ordinal() >= e.bucket().ordinal()), event.getClickCount() == 2);
    });
    table.setItems(mi_data.getList());

    return table;
  }

  record SizeDistributionEntry(SizeDistributionBucket bucket, SizeDistributionBucketData data) {
  };

  private class SizeDistributionPaneData
    extends PaneData
  {
    private Map<SizeDistributionBucket, SizeDistributionBucketData> mi_map;
    private ObservableList<SizeDistributionEntry> mi_list;

    private SizeDistributionPaneData()
    {
    }

    public ObservableList<SizeDistributionEntry> getList()
    {
      if (mi_list == null)
      {
        mi_list = getMap().entrySet().stream().map(e -> new SizeDistributionEntry(e.getKey(), e.getValue()))
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
      }

      return mi_list;
    }

    public Map<SizeDistributionBucket, SizeDistributionBucketData> getMap()
    {
      if (mi_map == null)
      {
        try (PerformancePoint _ = Performance.measure("Collecting data for size distribution tab"))
        {
          mi_map = new LinkedHashMap<>();
          Stream.of(SizeDistributionBucket.values()).forEach(bucket -> {
            mi_map.put(bucket, new SizeDistributionBucketData(0l, 0l));
          });

          new FileNodeIterator(getCurrentFileNode()).forEach(fn -> {
            if (fn.isFile())
            {
              SizeDistributionFormPane.SizeDistributionBucket bucket;
              SizeDistributionBucketData data;

              bucket = SizeDistributionBucket.findBucket(fn.getSize());
              data = mi_map.get(bucket);
              data.mi_numberOfFiles += 1;
              data.mi_sizeOfFiles += (fn.getSize() / 1000000.0);
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
