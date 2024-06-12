package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.DiskUsageView.ObjectWithIndex;
import org.kku.jdiskusage.ui.DiskUsageView.ObjectWithIndexFactory;
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

  private enum FileComparator
  {
    LARGEST("Largest", FileNodeIF.getSizeComparator()),
    OLDEST("Oldest", Comparator.comparing(FileNodeIF::getLastModifiedTime)),
    NEWEST("Newest", Comparator.comparing(FileNodeIF::getLastModifiedTime).reversed());

    private final String mi_description;
    private final Comparator<FileNodeIF> mi_comparator;

    FileComparator(String description, Comparator<FileNodeIF> comparator)
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

    Stream.of(FileComparator.values()).forEach(fc -> {
      createPaneType(fc.name(), fc.getDescription(), null, () -> getTableNode(fc));
    });

    init();
  }

  class Top50PaneData
    extends PaneData
  {
    private Map<FileComparator, ObservableList<ObjectWithIndex<FileNodeIF>>> mi_map = new HashMap<>();

    public ObservableList<ObjectWithIndex<FileNodeIF>> getList(FileComparator fc)
    {
      return mi_map.computeIfAbsent(fc, (key) -> {
        try (PerformancePoint pp = Performance.start("Collecting data for top 50 table"))
        {
          List<FileNodeIF> fnList;
          ObjectWithIndexFactory<FileNodeIF> objectWithIndexFactory;

          objectWithIndexFactory = new ObjectWithIndexFactory<>();

          fnList = getCurrentTreeItem().getValue().streamNode().filter(FileNodeIF::isFile)
              .collect(StreamUtil.createTopCollector(fc.getComparator(), 50));

          return fnList.stream().map(objectWithIndexFactory::create)
              .collect(Collectors.toCollection(FXCollections::observableArrayList));
        }
      });
    }

    @Override
    public void currentTreeItemChanged()
    {
      mi_map.clear();
    }

    @Override
    public void currentDisplayMetricChanged()
    {
      mi_map.clear();
    }
  }

  Node getPieChartNode()
  {
    return new Label("PieChart");
  }

  Node getBarChartNode()
  {
    return new Label("BarChart");
  }

  Node getTableNode(FileComparator fc)
  {
    TreeItem<FileNodeIF> treeItem;

    treeItem = getDiskUsageData().getSelectedTreeItem();
    if (!treeItem.getChildren().isEmpty())
    {
      MyTableView<ObjectWithIndex<FileNodeIF>> table;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, Integer> rankColumn;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, String> nameColumn;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, Long> fileSizeColumn;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, Date> lastModifiedColumn;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, Integer> numberOfLinksColumn;
      MyTableColumn<ObjectWithIndex<FileNodeIF>, String> pathColumn;

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

      table.setItems(mi_data.getList(fc));

      return table;
    }

    return translate(new Label("No data"));
  }
}