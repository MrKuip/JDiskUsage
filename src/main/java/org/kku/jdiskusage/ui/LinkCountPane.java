package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn.ButtonProperty;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.DiskUsageView.FileAggregates;
import org.kku.jdiskusage.ui.common.AbstractTabContentPane;
import org.kku.jdiskusage.ui.common.FileNodeIterator;
import org.kku.jdiskusage.ui.common.Filter;
import org.kku.jdiskusage.ui.util.FormatterFactory;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

class LinkCountPane
  extends AbstractTabContentPane
{
  private LinkCountPaneData mi_data = new LinkCountPaneData();

  LinkCountPane(DiskUsageData diskUsageData)
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
    mi_data.getReducedMap().entrySet().forEach(entry -> {
      PieChart.Data data;
      String name;
      Predicate<FileNodeIF> test;

      name = entry.getKey() + "\n" + entry.getValue().getValueDescription(getCurrentDisplayMetric());
      data = new PieChart.Data(name, entry.getValue().getSize(getCurrentDisplayMetric()));
      pieChart.getData().add(data);

      test = (fileNode) -> Integer.valueOf(entry.getKey()) == fileNode.getNumberOfLinks();
      addFilterHandler(data.getNode(), "Link count", Objects.toString(entry.getKey()), test);
    });

    return pieChart;
  }

  Node getBarChartNode()
  {
    BarChart<Number, String> barChart;
    XYChart.Series<Number, String> series1;
    ArrayList<Entry<String, FileAggregates>> fullList;
    GridPane gridPane;
    ScrollPane scrollPane;
    NumberAxis xAxis;
    CategoryAxis yAxis;

    scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    gridPane = new GridPane();

    xAxis = new NumberAxis();
    xAxis.setSide(Side.TOP);
    yAxis = new CategoryAxis();
    barChart = FxUtil.createBarChart(xAxis, yAxis);
    GridPane.setHgrow(barChart, Priority.ALWAYS);
    GridPane.setVgrow(barChart, Priority.ALWAYS);

    series1 = new XYChart.Series<>();
    barChart.getData().add(series1);

    fullList = new ArrayList<>(mi_data.getFullMap().entrySet());
    Collections.reverse(fullList);
    fullList.forEach(entry -> {
      Data<Number, String> data;
      Predicate<FileNodeIF> test;

      data = new XYChart.Data<Number, String>(entry.getValue().getSize(getCurrentDisplayMetric()),
          Objects.toString(entry.getKey()));
      series1.getData().add(data);

      test = (fileNode) -> Integer.valueOf(entry.getKey()) == fileNode.getNumberOfLinks();
      addFilterHandler(data.getNode(), "Link count", Objects.toString(entry.getKey()), test);
    });

    barChart.setPrefHeight(series1.getData().size() * 20);

    gridPane.add(barChart, 0, 0);
    scrollPane.setContent(gridPane);

    return scrollPane;
  }

  Node getTableNode()
  {
    TreeItem<FileNodeIF> treeItem;

    treeItem = getDiskUsageData().getSelectedTreeItem();
    if (!treeItem.getChildren().isEmpty())
    {
      ObservableList<Entry<String, FileAggregates>> list;
      MyTableView<Entry<String, FileAggregates>> table;
      MyTableColumn<Entry<String, FileAggregates>, String> linkCountColumn;
      MyTableColumn<Entry<String, FileAggregates>, Long> fileSizeColumn;
      MyTableColumn<Entry<String, FileAggregates>, Long> fileSizeBytesColumn;
      MyTableColumn<Entry<String, FileAggregates>, Double> fileSizePercentageColumn;
      MyTableColumn<Entry<String, FileAggregates>, Long> numberOfFilesColumn;
      MyTableColumn<Entry<String, FileAggregates>, Long> numberOfFilesCountColumn;
      MyTableColumn<Entry<String, FileAggregates>, Double> numberOfFilesPercentageColumn;
      MyTableColumn<Entry<String, FileAggregates>, ButtonProperty<Entry<String, FileAggregates>>> filterColumn;
      MyTableColumn<Entry<String, FileAggregates>, ButtonProperty<Entry<String, FileAggregates>>> filterEqualColumn;
      MyTableColumn<Entry<String, FileAggregates>, ButtonProperty<Entry<String, FileAggregates>>> filterGreaterThanColumn;
      MyTableColumn<Entry<String, FileAggregates>, ButtonProperty<Entry<String, FileAggregates>>> filterLessThanColumn;
      Map<String, FileAggregates> fullMap;
      ButtonProperty<Entry<String, FileAggregates>> filterItemEqualProperty;
      ButtonProperty<Entry<String, FileAggregates>> filterItemGreaterThanProperty;
      ButtonProperty<Entry<String, FileAggregates>> filterItemLessThanProperty;
      long totalFileSize;
      long totalNumberOfFiles;

      try (PerformancePoint pp = Performance.start("Collecting data for types table"))
      {
        fullMap = mi_data.getFullMap();
        list = fullMap.entrySet().stream().collect(Collectors.toCollection(FXCollections::observableArrayList));

        totalFileSize = fullMap.values().stream().map(FileAggregates::getFileSize).reduce(0l, (a, b) -> a + b);
        totalNumberOfFiles = fullMap.values().stream().map(FileAggregates::getFileCount).reduce(0l, (a, b) -> a + b);
      }

      table = new MyTableView<>("File types in %d");
      table.setEditable(true);

      linkCountColumn = table.addColumn("Link count");
      linkCountColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);
      linkCountColumn.setColumnCount(10);
      linkCountColumn.setCellValueGetter(Entry::getKey);

      fileSizeColumn = table.addColumn("File size");

      fileSizeBytesColumn = table.addColumn(fileSizeColumn, "Bytes");
      fileSizeBytesColumn.setColumnCount(8);
      fileSizeBytesColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
      fileSizeBytesColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      fileSizeBytesColumn.setCellValueGetter((e) -> e.getValue().getFileSize());

      fileSizePercentageColumn = table.addColumn(fileSizeColumn, "%");
      fileSizePercentageColumn.setColumnCount(5);
      fileSizePercentageColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%3.2f%%"));
      fileSizePercentageColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      fileSizePercentageColumn.setCellValueGetter((e) -> (e.getValue().getFileSize() * 100.0) / totalFileSize);

      numberOfFilesColumn = table.addColumn("Number of Files");

      numberOfFilesCountColumn = table.addColumn(numberOfFilesColumn, "Count");
      numberOfFilesCountColumn.setColumnCount(8);
      numberOfFilesCountColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
      numberOfFilesCountColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      numberOfFilesCountColumn.setCellValueGetter((e) -> e.getValue().getFileCount());

      numberOfFilesPercentageColumn = table.addColumn(numberOfFilesColumn, "%");
      numberOfFilesPercentageColumn.setColumnCount(5);
      numberOfFilesPercentageColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%3.2f%%"));
      numberOfFilesPercentageColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      numberOfFilesPercentageColumn
          .setCellValueGetter((e) -> (e.getValue().getFileCount() * 100.0) / totalNumberOfFiles);

      filterColumn = table.addColumn("Filter link count");

      filterItemLessThanProperty = new ButtonProperty<>(() -> IconUtil.createIconNode("filter", IconSize.SMALLER));
      filterLessThanColumn = table.addColumn(filterColumn, "<=");
      filterLessThanColumn.setCellValueAlignment(Pos.CENTER);
      filterLessThanColumn.setEditable(true);
      filterLessThanColumn.setCellValueGetter((e) -> filterItemLessThanProperty);
      filterLessThanColumn.setAction((event, entry) -> {
        Predicate<FileNodeIF> filterPredicate;

        filterPredicate = (fileNode) -> fileNode.getNumberOfLinks() <= Integer.valueOf(entry.getKey());
        getDiskUsageData().addFilter(new Filter("Link count", "<=", entry.getKey(), filterPredicate),
            event.getClickCount() == 2);
      });

      filterItemEqualProperty = new ButtonProperty<>(() -> IconUtil.createIconNode("filter", IconSize.SMALLER));
      filterEqualColumn = table.addColumn(filterColumn, "is");
      filterEqualColumn.setCellValueAlignment(Pos.CENTER);
      filterEqualColumn.setEditable(true);
      filterEqualColumn.setCellValueGetter((e) -> filterItemEqualProperty);
      filterEqualColumn.setAction((event, entry) -> {
        Predicate<FileNodeIF> filterPredicate;

        filterPredicate = (fileNode) -> fileNode.getNumberOfLinks() == Integer.valueOf(entry.getKey());
        getDiskUsageData().addFilter(new Filter("Link count", entry.getKey(), filterPredicate),
            event.getClickCount() == 2);
      });

      filterItemGreaterThanProperty = new ButtonProperty<>(() -> IconUtil.createIconNode("filter", IconSize.SMALLER));
      filterGreaterThanColumn = table.addColumn(filterColumn, ">=");
      filterGreaterThanColumn.setCellValueAlignment(Pos.CENTER);
      filterGreaterThanColumn.setEditable(true);
      filterGreaterThanColumn.setCellValueGetter((e) -> filterItemGreaterThanProperty);
      filterGreaterThanColumn.setAction((event, entry) -> {
        Predicate<FileNodeIF> filterPredicate;

        filterPredicate = (fileNode) -> fileNode.getNumberOfLinks() > Integer.valueOf(entry.getKey());
        getDiskUsageData().addFilter(new Filter("Link count", ">=", entry.getKey(), filterPredicate),
            event.getClickCount() == 2);
      });

      table.setItems(list);

      return table;
    }

    return translate(new Label("No data"));
  }

  private class LinkCountPaneData
    extends PaneData
  {
    private Map<String, FileAggregates> mi_map;
    private Map<String, FileAggregates> mi_reducedMap;

    private LinkCountPaneData()
    {
    }

    private Map<String, FileAggregates> getFullMap()
    {
      if (mi_map == null)
      {
        mi_map = new HashMap<String, FileAggregates>();
        new FileNodeIterator(getCurrentTreeItem().getValue()).forEach(fn -> {
          if (fn.isFile())
          {
            String bucket;
            FileAggregates data;

            bucket = Objects.toString(fn.getNumberOfLinks());
            if (bucket.equals("-1"))
            {
              System.out.println("file[linkcount=-1]=" + fn.getAbsolutePath());
            }
            data = mi_map.computeIfAbsent(bucket, (a) -> new FileAggregates(0l, 0l));
            data.mi_fileCount += 1;
            data.mi_fileSize += fn.getSize();
          }
          return true;
        });
        mi_map = mi_map.entrySet().stream().sorted(Comparator.comparing(e -> Integer.valueOf(e.getKey())))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (x, y) -> y, LinkedHashMap::new));
      }
      return mi_map;
    }

    private Map<String, FileAggregates> getReducedMap()
    {
      if (mi_reducedMap == null)
      {
        long totalCount;
        double minimumCount;
        long otherCount;

        totalCount = getFullMap().values().stream().map(fa -> fa.getSize(getCurrentDisplayMetric())).reduce(0l,
            Long::sum);

        minimumCount = totalCount * 0.01; // Only types with a count larger than a percentage are shown
        mi_reducedMap = getFullMap().entrySet().stream()
            .filter(e -> e.getValue().getSize(getCurrentDisplayMetric()) > minimumCount).limit(10)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (x, y) -> y, LinkedHashMap::new));

        otherCount = totalCount
            - mi_reducedMap.values().stream().map(fa -> fa.getSize(getCurrentDisplayMetric())).reduce(0l, Long::sum);
        if (otherCount != 0)
        {
          mi_reducedMap.put(DiskUsageView.getOtherText(), new FileAggregates(totalCount, otherCount));
        }
      }

      return mi_reducedMap;
    }

    @Override
    public void reset()
    {
      mi_map = null;
      mi_reducedMap = null;
    }
  }
}