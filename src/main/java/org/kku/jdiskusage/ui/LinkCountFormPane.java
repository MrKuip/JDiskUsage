package org.kku.jdiskusage.ui;

import static org.kku.fx.ui.util.TranslateUtil.translatedTextProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.kku.fx.ui.util.FxUtil;
import org.kku.fx.util.FxProperty;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn.ButtonCell;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.DiskUsageView.FileAggregates;
import org.kku.jdiskusage.ui.DiskUsageView.FileAggregatesEntry;
import org.kku.jdiskusage.ui.common.AbstractFormPane;
import org.kku.jdiskusage.ui.common.FileNodeIterator;
import org.kku.jdiskusage.ui.common.Filter;
import org.kku.jdiskusage.ui.util.FormatterFactory;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ScrollPane;

class LinkCountFormPane
  extends AbstractFormPane
{
  private LinkCountPaneData mi_data = new LinkCountPaneData();

  LinkCountFormPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("PIECHART", "Show pie chart", "chart-pie", this::getPieChartNode);
    createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode);
    createPaneType("TABLE", "Show details table", "table", this::getTableNode);

    init();
  }

  Node getPieChartNode()
  {
    PieChart pieChart;

    pieChart = FxUtil.createPieChart();
    mi_data.getReducedList().forEach(e -> {
      PieChart.Data data;
      String name;
      Predicate<FileNodeIF> test;

      name = e.bucket() + "\n" + e.aggregates().getValueDescription(getCurrentDisplayMetric());
      data = new PieChart.Data(name, e.aggregates().getSize(getCurrentDisplayMetric()));
      pieChart.getData().add(data);

      test = (fileNode) -> fileNode.getNumberOfLinks() == Integer.parseInt(e.bucket());
      addFilterHandler(data.getNode(), "Link count", Objects.toString(e.bucket()), test);
    });

    return pieChart;
  }

  Node getBarChartNode()
  {
    BarChart<Number, String> barChart;
    XYChart.Series<Number, String> series1;
    List<FileAggregatesEntry> list;
    ScrollPane scrollPane;
    NumberAxis xAxis;
    CategoryAxis yAxis;
    StringExpression titleExpression;
    StringExpression xAxisLabelExpression;

    xAxis = new NumberAxis();
    xAxis.setSide(Side.TOP);
    yAxis = new CategoryAxis();
    barChart = FxUtil.createBarChart(xAxis, yAxis);

    switch (FxProperty.property(AppPreferences.displayMetricPreference).get())
    {
      case FILE_COUNT:
        titleExpression = translatedTextProperty("Distribution of number of files by number of links in").concat(" ")
            .concat(getCurrentFileNode().getName());
        xAxisLabelExpression = translatedTextProperty("Number of files");
        break;
      case FILE_SIZE:
        titleExpression = translatedTextProperty("Distribution of file size by number of links in").concat(" ")
            .concat(getCurrentFileNode().getName());
        xAxisLabelExpression = translatedTextProperty("File size");
        break;
      default:
        titleExpression = new SimpleStringProperty("?");
        xAxisLabelExpression = new SimpleStringProperty("?");
        break;
    }

    barChart.titleProperty().bind(titleExpression);
    xAxis.labelProperty().bind(xAxisLabelExpression);
    yAxis.labelProperty().bind(translatedTextProperty("Number of links"));

    series1 = new XYChart.Series<>();
    barChart.getData().add(series1);

    list = new ArrayList<>(mi_data.getList());
    Collections.reverse(list);
    list.forEach(e -> {
      Data<Number, String> data;
      Predicate<FileNodeIF> test;

      data = new XYChart.Data<Number, String>(e.aggregates().getSize(getCurrentDisplayMetric()),
          Objects.toString(e.bucket()));
      series1.getData().add(data);

      test = (fileNode) -> fileNode.getNumberOfLinks() == Integer.parseInt(e.bucket());
      addFilterHandler(data.getNode(), "Link count", Objects.toString(e.bucket()), test);
    });

    barChart.setPrefHeight(series1.getData().size() * 20);

    scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    scrollPane.setContent(barChart);

    return scrollPane;
  }

  Node getTableNode()
  {
    ObservableList<FileAggregatesEntry> list;
    MyTableView<FileAggregatesEntry> table;
    MyTableColumn<FileAggregatesEntry, String> linkCountColumn;
    MyTableColumn<FileAggregatesEntry, Void> fileSizeColumn;
    MyTableColumn<FileAggregatesEntry, Long> fileSizeBytesColumn;
    MyTableColumn<FileAggregatesEntry, Double> fileSizePercentageColumn;
    MyTableColumn<FileAggregatesEntry, Void> numberOfFilesColumn;
    MyTableColumn<FileAggregatesEntry, Long> numberOfFilesCountColumn;
    MyTableColumn<FileAggregatesEntry, Double> numberOfFilesPercentageColumn;
    MyTableColumn<FileAggregatesEntry, Void> filterColumn;
    MyTableColumn<FileAggregatesEntry, ButtonCell> filterEqualColumn;
    MyTableColumn<FileAggregatesEntry, ButtonCell> filterGreaterThanColumn;
    MyTableColumn<FileAggregatesEntry, ButtonCell> filterLessThanColumn;
    long totalFileSize;
    long totalNumberOfFiles;

    list = mi_data.getList().stream().collect(Collectors.toCollection(FXCollections::observableArrayList));

    totalFileSize = list.stream().map(e -> e.aggregates().getFileSize()).reduce(0l, (a, b) -> a + b);
    totalNumberOfFiles = list.stream().map(e -> e.aggregates().getFileCount()).reduce(0l, (a, b) -> a + b);

    table = new MyTableView<>("Link count");
    table.setEditable(true);

    linkCountColumn = table.addColumn("Link count");
    linkCountColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);
    linkCountColumn.setColumnCount(10);
    linkCountColumn.setCellValueGetter(FileAggregatesEntry::bucket);

    fileSizeColumn = table.addColumn("File size");

    fileSizeBytesColumn = table.addColumn(fileSizeColumn, "Bytes");
    fileSizeBytesColumn.setColumnCount(8);
    fileSizeBytesColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
    fileSizeBytesColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);
    fileSizeBytesColumn.setCellValueGetter((e) -> e.aggregates().getFileSize());

    fileSizePercentageColumn = table.addColumn(fileSizeColumn, "%");
    fileSizePercentageColumn.setColumnCount(5);
    fileSizePercentageColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%3.2f%%"));
    fileSizePercentageColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);
    fileSizePercentageColumn.setCellValueGetter((e) -> (e.aggregates().getFileSize() * 100.0) / totalFileSize);

    numberOfFilesColumn = table.addColumn("Number of Files");

    numberOfFilesCountColumn = table.addColumn(numberOfFilesColumn, "Count");
    numberOfFilesCountColumn.setColumnCount(8);
    numberOfFilesCountColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
    numberOfFilesCountColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);
    numberOfFilesCountColumn.setCellValueGetter((e) -> e.aggregates().getFileCount());

    numberOfFilesPercentageColumn = table.addColumn(numberOfFilesColumn, "%");
    numberOfFilesPercentageColumn.setColumnCount(5);
    numberOfFilesPercentageColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%3.2f%%"));
    numberOfFilesPercentageColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);
    numberOfFilesPercentageColumn
        .setCellValueGetter((e) -> (e.aggregates().getFileCount() * 100.0) / totalNumberOfFiles);

    filterColumn = table.addColumn("Filter link count");

    filterLessThanColumn = table.addFilterColumn(filterColumn, "<=");
    filterLessThanColumn.setAction((event, e) -> {
      getDiskUsageData().addFilter(new Filter("Link count", "<=", e.bucket(),
          (fileNode) -> fileNode.getNumberOfLinks() <= Integer.parseInt(e.bucket())), event.getClickCount() == 2);
    });

    filterEqualColumn = table.addFilterColumn(filterColumn, "is");
    filterEqualColumn.setAction((event, e) -> {
      getDiskUsageData().addFilter(new Filter("Link count", e.bucket(),
          (fileNode) -> fileNode.getNumberOfLinks() == Integer.parseInt(e.bucket())), event.getClickCount() == 2);
    });

    filterGreaterThanColumn = table.addFilterColumn(filterColumn, ">=");
    filterGreaterThanColumn.setAction((event, e) -> {
      getDiskUsageData().addFilter(new Filter("Link count", ">=", e.bucket(),
          fileNode -> fileNode.getNumberOfLinks() > Integer.parseInt(e.bucket())), event.getClickCount() == 2);
    });

    table.setItems(list);

    return table;
  }

  private class LinkCountPaneData
    extends PaneData
  {
    private List<FileAggregatesEntry> mi_list;
    private List<FileAggregatesEntry> mi_reducedList;

    private LinkCountPaneData()
    {
    }

    private List<FileAggregatesEntry> getList()
    {
      if (mi_list == null)
      {
        try (PerformancePoint _ = Performance.measure("Collecting data for link count tab"))
        {
          Map<String, FileAggregates> map;

          map = new HashMap<String, FileAggregates>();
          new FileNodeIterator(getCurrentFileNode()).forEach(fn -> {
            if (fn.isFile())
            {
              String bucket;
              FileAggregates data;

              bucket = Objects.toString(fn.getNumberOfLinks());
              data = map.computeIfAbsent(bucket, (_) -> new FileAggregates(0l, 0l));
              data.add(1, fn.getSize());
            }
            return true;
          });

          mi_list = map.entrySet().stream().sorted(Comparator.comparing(e -> Integer.valueOf(e.getKey())))
              .map(e -> new FileAggregatesEntry(e.getKey(), e.getValue())).toList();
        }
      }

      return mi_list;
    }

    private List<FileAggregatesEntry> getReducedList()
    {
      if (mi_reducedList == null)
      {
        long totalCount;
        double minimumCount;
        long otherCount;

        totalCount = getList().stream().map(e -> e.aggregates().getSize(getCurrentDisplayMetric())).reduce(0l,
            Long::sum);

        minimumCount = totalCount * 0.01; // Only types with a count larger than a percentage are shown
        mi_reducedList = getList().stream()
            .filter(e -> e.aggregates().getSize(getCurrentDisplayMetric()) > minimumCount).limit(10)
            .collect(Collectors.toCollection(ArrayList::new));

        otherCount = totalCount
            - mi_reducedList.stream().map(e -> e.aggregates().getSize(getCurrentDisplayMetric())).reduce(0l, Long::sum);
        if (otherCount != 0)
        {
          mi_reducedList
              .add(new FileAggregatesEntry(DiskUsageView.getOtherText(), new FileAggregates(totalCount, otherCount)));
        }
      }

      return mi_reducedList;
    }

    @Override
    public void reset()
    {
      mi_list = null;
      mi_reducedList = null;
    }
  }
}