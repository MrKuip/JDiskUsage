package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.ArrayList;
import java.util.List;
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
import org.kku.jdiskusage.util.StringUtils;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
    ToolBar toolBar;
    Label searchLabel;
    ToggleButton regexButton;
    TextField searchTextField;
    Label maxResultLabel;
    TextField maxResultTextField;

    toolBar = new ToolBar();

    searchLabel = new Label(null, IconUtil.createIconNode("magnify", IconSize.SMALLER));

    regexButton = new ToggleButton(null, IconUtil.createIconNode("regex", IconSize.SMALLER));
    mi_data.mi_regexSelectedProperty = regexButton.selectedProperty();

    searchTextField = new TextField();
    searchTextField.setPromptText(translate("search"));
    searchTextField.setOnAction((ae) -> getDiskUsageData().refresh());
    mi_data.mi_searchTextProperty = searchTextField.textProperty();

    maxResultTextField = new TextField();
    maxResultTextField.setPrefColumnCount(5);
    maxResultLabel = new Label(translate("Max results"));
    maxResultLabel.setLabelFor(maxResultTextField);
    maxResultTextField.setText(AppPreferences.searchMaxResultPreference.get().toString());
    maxResultTextField.setOnAction(
        (ae) -> AppPreferences.searchMaxResultPreference.set(Integer.valueOf(maxResultTextField.getText())));
    mi_data.mi_maxResultProperty = maxResultTextField.textProperty();

    HBox.setHgrow(searchTextField, Priority.ALWAYS);

    toolBar.getItems().addAll(searchLabel, searchTextField, regexButton, maxResultLabel, maxResultTextField);

    getNode().setTop(toolBar);
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
      nameColumn.setColumnCount(20);
      nameColumn.setCellValueGetter((fn) -> fn.getAbsolutePath());

      fileSizeColumn = table.addColumn("File size");
      fileSizeColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      fileSizeColumn.setColumnCount(8);
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
    public StringProperty mi_maxResultProperty;
    public BooleanProperty mi_regexSelectedProperty;
    public StringProperty mi_searchTextProperty;
    private ObservableList<FileNodeIF> mi_list;

    private SearchPaneData()
    {
    }

    public ObservableList<FileNodeIF> getList()
    {
      if (mi_list == null)
      {
        String searchText;
        List<FileNodeIF> list;

        searchText = mi_searchTextProperty.get();
        try (PerformancePoint pp = Performance.start("Collecting data for search '%s'", searchText))
        {
          Pattern pattern;
          Matcher matcher;

          list = new ArrayList<>();

          if (mi_data.mi_regexSelectedProperty.get())
          {
            if (searchText.startsWith("*"))
            {
              // java throws a 'dangling meta character' exception which is the proper way
              // to do things (because * means repeat the letter before. And there is no letter)
              // But other regular expression handlers are way more lenient
              searchText = "." + searchText;
            }
            pattern = Pattern.compile(searchText);
            matcher = pattern.matcher("");
          }
          else
          {
            pattern = null;
            matcher = null;
          }

          if (!StringUtils.isEmpty(searchText))
          {
            String searchText2;
            int maxResult;

            searchText2 = searchText;
            maxResult = Integer.valueOf(mi_data.mi_maxResultProperty.get());
            new FileNodeIterator(getCurrentTreeItem().getValue()).forEach(fn -> {
              if (list.size() >= maxResult)
              {
                return false;
              }

              if (fn.isFile())
              {
                if (pattern != null)
                {
                  if (matcher.reset(fn.getAbsolutePath()).find())
                  {
                    list.add(fn);
                  }
                }
                else
                {
                  if (fn.getAbsolutePath().contains(searchText2))
                  {
                    list.add(fn);
                  }
                }
              }
              return true;
            });
          }

          list.sort(FileNodeIF.getSizeComparator());
        }

        mi_list = FXCollections.observableArrayList(list);
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