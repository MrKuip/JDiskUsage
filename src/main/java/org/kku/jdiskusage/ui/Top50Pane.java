package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.DiskUsageView.ObjectWithIndex;
import org.kku.jdiskusage.ui.DiskUsageView.ObjectWithIndexFactory;
import org.kku.jdiskusage.ui.common.AbstractTabContentPane;
import org.kku.jdiskusage.ui.util.FormatterFactory;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.StreamUtil;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;

class Top50Pane
  extends AbstractTabContentPane
{
  Top50Pane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("PIECHART", "Show details table", "chart-pie", this::getPieChartNode);
    createPaneType("BARCHART", "Show bar chart", "chart-bar", this::getBarChartNode);
    createPaneType("TABLE", "Show details table", "table", this::getTableNode, true);

    init();
  }

  Node getPieChartNode()
  {
    return new Label("PieChart");
  }

  Node getBarChartNode()
  {
    return new Label("BarChart");
  }

  Node getTableNode()
  {
    TreeItem<FileNodeIF> treeItem;

    treeItem = getDiskUsageData().getSelectedTreeItem();
    if (!treeItem.getChildren().isEmpty())
    {
      FileNodeIF node;
      ObservableList<ObjectWithIndex<FileNodeIF>> list;
      MyTableView<ObjectWithIndex<FileNodeIF>> table;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, Integer> rankColumn;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, String> nameColumn;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, Long> fileSizeColumn;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, Date> lastModifiedColumn;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, Integer> numberOfLinksColumn;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, String> pathColumn;
      List<FileNodeIF> fnList;
      ObjectWithIndexFactory<FileNodeIF> objectWithIndexFactory;

      objectWithIndexFactory = new ObjectWithIndexFactory<>();

      node = treeItem.getValue();

      try (PerformancePoint pp = Performance.start("Collecting data for top 50 table"))
      {
        fnList = node.streamNode().filter(FileNodeIF::isFile)
            .collect(StreamUtil.createTopCollector(FileNodeIF.getSizeComparator(), 50));

        list = fnList.stream().map(objectWithIndexFactory::create)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
      }

      table = new MyTableView<>("Top50");
      table.setEditable(false);

      rankColumn = table.addColumn("Rank");
      rankColumn.initPersistentPrefWidth(100.0);
      rankColumn.setCellValueGetter(ObjectWithIndex<FileNodeIF>::getIndex);
      rankColumn.setCellValueAlignment(Pos.CENTER_RIGHT);

      nameColumn = table.addColumn("Name");
      nameColumn.initPersistentPrefWidth(200.0);
      nameColumn.setCellValueGetter((owi) -> owi.getObject().getName());

      fileSizeColumn = table.addColumn("File size");
      fileSizeColumn.initPersistentPrefWidth(100.0);
      fileSizeColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
      fileSizeColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      fileSizeColumn.setCellValueGetter((owi) -> owi.getObject().getSize());

      lastModifiedColumn = table.addColumn("Last modified");
      lastModifiedColumn.initPersistentPrefWidth(200.0);
      lastModifiedColumn.setCellValueGetter((owi) -> new Date(owi.getObject().getLastModifiedTime()));
      lastModifiedColumn.setCellValueFormatter(FormatterFactory.createSimpleDateFormatter("dd/MM/yyyy HH:mm:ss"));
      lastModifiedColumn.setCellValueAlignment(Pos.CENTER_RIGHT);

      numberOfLinksColumn = table.addColumn("Number\nof links\nto file");
      numberOfLinksColumn.initPersistentPrefWidth(100.0);
      numberOfLinksColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
      numberOfLinksColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      numberOfLinksColumn.setCellValueGetter((owi) -> owi.getObject().getNumberOfLinks());

      pathColumn = table.addColumn("Path");
      pathColumn.setCellValueGetter((owi) -> owi.getObject().getAbsolutePath());

      table.setItems(list);

      return table;
    }

    return translate(new Label("No data"));
  }
}