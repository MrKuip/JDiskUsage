package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.AbstractTabContentPane;
import org.kku.jdiskusage.ui.util.FormatterFactory;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import org.kku.jdiskusage.util.StreamUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;

class Top50Pane
  extends AbstractTabContentPane
{
  private final Top50PaneData mi_data = new Top50PaneData();

  private enum FileNodeComparator
  {
    LARGEST("Largest", FileNodeIF.getSizeComparator()),
    OLDEST("Oldest", Comparator.comparing(FileNodeIF::getLastModifiedTime)),
    NEWEST("Newest", Comparator.comparing(FileNodeIF::getLastModifiedTime).reversed());

    private final String mi_description;
    private final Comparator<FileNodeIF> mi_comparator;

    FileNodeComparator(String description, Comparator<FileNodeIF> comparator)
    {
      mi_description = description;
      mi_comparator = comparator;
    }

    public String getDescription()
    {
      return mi_description;
    }

    public Comparator<FileNodeIF> getComparator()
    {
      return mi_comparator;
    }
  }

  Top50Pane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    Stream.of(FileNodeComparator.values()).forEach(fc -> {
      createPaneType(fc.name(), fc.getDescription(), null, () -> getTableNode(fc));
    });

    init();
  }

  Node getTableNode(FileNodeComparator fc)
  {
    TreeItem<FileNodeIF> treeItem;

    treeItem = getDiskUsageData().getSelectedTreeItem();
    if (!treeItem.getChildren().isEmpty())
    {
      MyTableView<FileNodeIF> table;
      MyTableColumn<FileNodeIF, String> nameColumn;
      MyTableColumn<FileNodeIF, Long> fileSizeColumn;
      MyTableColumn<FileNodeIF, Date> lastModifiedColumn;
      MyTableColumn<FileNodeIF, Integer> numberOfLinksColumn;
      MyTableColumn<FileNodeIF, String> pathColumn;

      table = new MyTableView<>("Top50");
      table.setEditable(false);

      table.addRankColumn("Rank");

      nameColumn = table.addColumn("Name");
      nameColumn.setColumnCount(20);
      nameColumn.setCellValueGetter(FileNodeIF::getName);

      fileSizeColumn = table.addColumn("File size");
      fileSizeColumn.setColumnCount(8);
      fileSizeColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
      fileSizeColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      fileSizeColumn.setCellValueGetter(FileNodeIF::getSize);

      lastModifiedColumn = table.addColumn("Last modified");
      lastModifiedColumn.setColumnCount(15);
      lastModifiedColumn.setCellValueGetter(fn -> new Date(fn.getLastModifiedTime()));
      lastModifiedColumn.setCellValueFormatter(FormatterFactory.createSimpleDateFormatter("dd/MM/yyyy HH:mm:ss"));
      lastModifiedColumn.setCellValueAlignment(Pos.CENTER_RIGHT);

      numberOfLinksColumn = table.addColumn("Number\nof links\nto file");
      numberOfLinksColumn.setColumnCount(8);
      numberOfLinksColumn.setCellValueFormatter(FormatterFactory.createStringFormatFormatter("%,d"));
      numberOfLinksColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      numberOfLinksColumn.setCellValueGetter(FileNodeIF::getNumberOfLinks);

      pathColumn = table.addColumn("Path");
      pathColumn.setCellValueGetter(FileNodeIF::getAbsolutePath);

      table.setItems(mi_data.getList(fc));

      return table;
    }

    return translate(new Label("No data"));
  }

  class Top50PaneData
    extends PaneData
  {
    private Map<FileNodeComparator, ObservableList<FileNodeIF>> mi_map = new HashMap<>();

    public ObservableList<FileNodeIF> getList(FileNodeComparator fc)
    {
      return mi_map.computeIfAbsent(fc, (key) -> {
        try (PerformancePoint pp = Performance.start("Collecting data for top 50 table"))
        {
          List<FileNodeIF> fnList;

          fnList = getCurrentTreeItem().getValue().streamNode().filter(FileNodeIF::isFile)
              .collect(StreamUtil.createTopCollector(fc.getComparator(), 50));

          return FXCollections.observableArrayList(fnList);
        }
      });
    }

    @Override
    public void reset()
    {
      mi_map.clear();
    }
  }
}