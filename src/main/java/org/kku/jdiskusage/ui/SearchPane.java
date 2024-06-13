package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.DiskUsageView.FileNodeIterator;
import org.kku.jdiskusage.ui.common.AbstractTabContentPane;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class SearchPane
  extends AbstractTabContentPane
{
  private SearchPaneData mi_data = new SearchPaneData();

  public SearchPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("TABLE", "Show details table", "table", this::getTableNode);

    init();
    initPane();
  }

  private void initPane()
  {
    BorderPane buttonPane;
    Label searchLabel;
    TextField searchTextField;

    buttonPane = new BorderPane();
    buttonPane.setPadding(new Insets(5, 10, 5, 10));

    searchLabel = new Label(null, IconUtil.createIconNode("magnify", IconSize.SMALL));
    searchTextField = new TextField();
    searchTextField.setPromptText(translate("search"));
    searchTextField.setOnAction((ae) -> getDiskUsageData().refresh());

    mi_data.mi_textProperty = searchTextField.textProperty();

    buttonPane.setLeft(searchLabel);
    buttonPane.setCenter(searchTextField);

    getNode().setTop(buttonPane);
  }

  Node getTableNode()
  {
    TreeItem<FileNodeIF> treeItem;

    treeItem = getDiskUsageData().getSelectedTreeItem();
    if (treeItem != null && !treeItem.getChildren().isEmpty())
    {
      GridPane pane;
      MyTableView<FileNodeIF> table;
      MyTableColumn<FileNodeIF, String> nameColumn;
      MyTableColumn<FileNodeIF, Long> fileSizeColumn;

      pane = new GridPane();
      table = new MyTableView<>("Search");
      table.setEditable(false);

      table.addRankColumn("Rank");

      nameColumn = table.addColumn("Name");
      nameColumn.initPersistentPrefWidth(500.0);
      nameColumn.setCellValueGetter((fn) -> fn.getAbsolutePath());

      fileSizeColumn = table.addColumn("File size");
      fileSizeColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      fileSizeColumn.initPersistentPrefWidth(150.0);
      fileSizeColumn.setCellValueGetter((fn) -> fn.getSize());

      table.setItems(mi_data.getList());

      pane.add(table, 0, 1);
      GridPane.setHgrow(table, Priority.ALWAYS);
      GridPane.setVgrow(table, Priority.ALWAYS);

      return pane;
    }

    return new Label("No data");
  }

  private class SearchPaneData
    extends PaneData
  {
    public StringProperty mi_textProperty;
    private ObservableList<FileNodeIF> mi_list;

    private SearchPaneData()
    {
    }

    public ObservableList<FileNodeIF> getList()
    {
      if (mi_list == null)
      {
        try (PerformancePoint pp = Performance.start("Collecting data for last modified table"))
        {
          String searchText;
          List<FileNodeIF> list;

          list = new ArrayList<>();

          searchText = mi_textProperty.get();
          System.out.println("searchText=" + searchText);
          if (searchText.startsWith("*"))
          {
            // java throws a 'dangling meta character' exception which is the proper way
            // to do things (because * means repeat the letter before. And there is no letter)
            // But other regular expression handlers are way more lenient
            searchText = "." + searchText;
          }
          Pattern pattern = Pattern.compile(searchText);

          new FileNodeIterator(getCurrentTreeItem().getValue()).forEach(fn -> {
            if (fn.isFile())
            {
              //if (searchText != null && fn.getName().contains(searchText))
              if (pattern != null && pattern.matcher(fn.getName()).matches())
              {
                list.add(fn);
              }
            }
          });

          list.sort(FileNodeIF.getSizeComparator());

          mi_list = FXCollections.observableArrayList();
          mi_list.addAll(list);
        }
      }

      return mi_list;
    }

    @Override
    protected void reset()
    {
      mi_list = null;
    }
  }

}