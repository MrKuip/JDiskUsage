package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.AbstractTabContentPane;
import org.kku.jdiskusage.ui.common.FileNodeIterator;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import org.kku.jdiskusage.util.StringUtils;
import org.kku.jdiskusage.util.preferences.AppPreferences;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class SearchPane extends AbstractTabContentPane
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
    Button cancelButton;
    TextField searchTextField;
    Label maxCountLabel;
    TextField maxCountTextField;
    Label maxTimeLabel;
    TextField maxTimeTextField;
    VBox box;
    ProgressBar progressBar;

    toolBar = new ToolBar();

    searchLabel = new Label(null, IconUtil.createIconNode("magnify", IconSize.SMALLER));

    regexButton = new ToggleButton(null, IconUtil.createIconNode("regex", IconSize.SMALLER));
    mi_data.mi_regexSelectedProperty = regexButton.selectedProperty();

    searchTextField = new TextField();
    searchTextField.setPromptText(translate("search"));
    searchTextField.setOnAction((ae) -> getDiskUsageData().refresh());
    mi_data.mi_searchTextProperty = searchTextField.textProperty();

    maxCountTextField = new TextField();
    maxCountTextField.setPrefColumnCount(5);
    maxCountLabel = new Label(translate("Max items"));
    maxCountLabel.setLabelFor(maxCountTextField);
    maxCountTextField.setText(AppPreferences.searchMaxCountPreference.get().toString());
    maxCountTextField
        .setOnAction((ae) -> AppPreferences.searchMaxCountPreference.set(Integer.valueOf(maxCountTextField.getText())));
    mi_data.mi_maxCountProperty = maxCountTextField.textProperty();
    mi_data.mi_stoppedOnMaxCountProperty.addListener(FxUtil.showWarning(maxCountTextField));

    maxTimeTextField = new TextField();
    maxTimeTextField.setPrefColumnCount(5);
    maxTimeLabel = new Label(translate("Max time (seconds)"));
    maxTimeLabel.setLabelFor(maxTimeTextField);
    maxTimeTextField.setText(AppPreferences.searchMaxTimePreference.get().toString());
    maxTimeTextField
        .setOnAction((ae) -> AppPreferences.searchMaxTimePreference.set(Integer.valueOf(maxTimeTextField.getText())));
    mi_data.mi_maxTimeProperty = maxTimeTextField.textProperty();
    mi_data.mi_stoppedOnTimeoutProperty.addListener(FxUtil.showWarning(maxTimeTextField));

    cancelButton = new Button(null, IconUtil.createFxIcon("cancel", IconSize.SMALLER).fillColor(Color.RED).getCanvas());

    HBox.setHgrow(searchTextField, Priority.ALWAYS);

    toolBar.getItems().addAll(cancelButton, searchLabel, searchTextField, regexButton, maxCountLabel, maxCountTextField,
        maxTimeLabel, maxTimeTextField);

    progressBar = new ProgressBar();
    progressBar.setMaxWidth(Double.MAX_VALUE);
    progressBar.setProgress(0.25f);
    progressBar.getStyleClass().add("thin-progress-bar");
    progressBar.progressProperty().bind(Bindings.divide(mi_data.mi_progressProperty, 100.0));

    box = new VBox();
    box.getChildren().addAll(toolBar, progressBar);

    getNode().setTop(box);
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
      nameColumn.setColumnCount(300);
      nameColumn.setCellValueGetter((fn) -> fn.getAbsolutePath());

      fileSizeColumn = table.addColumn("File size");
      fileSizeColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      fileSizeColumn.setColumnCount(8);
      fileSizeColumn.setCellValueGetter((fn) -> fn.getSize());

      table.getPlaceholder();
      table.setPlaceholder(new Label("Searching..."));

      new TaskRunner<>(() -> mi_data.getList(), (itemList) -> table.setItems(itemList)).run();

      pane.add(table, 0, 1);
      GridPane.setHgrow(table, Priority.ALWAYS);
      GridPane.setVgrow(table, Priority.ALWAYS);

      return pane;
    }

    return new Label("No data");
  }

  static class TaskRunner<T>
  {
    private final Supplier<T> mi_asyncCommand;
    private final Consumer<T> mi_syncCommand;

    public TaskRunner(Supplier<T> async, Consumer<T> sync)
    {
      mi_asyncCommand = async;
      mi_syncCommand = sync;
    }

    public void run()
    {
      new Thread(() -> {
        T result;

        result = mi_asyncCommand.get();
        Platform.runLater(() -> {
          mi_syncCommand.accept(result);
        });

      }).start();
    }
  }

  private class SearchPaneData extends PaneData
  {
    public StringProperty mi_maxCountProperty;
    public StringProperty mi_maxTimeProperty;
    public BooleanProperty mi_regexSelectedProperty;
    public StringProperty mi_searchTextProperty;
    private ObservableList<FileNodeIF> mi_list;
    private final BooleanProperty mi_stoppedOnMaxCountProperty = new SimpleBooleanProperty();
    private final BooleanProperty mi_stoppedOnTimeoutProperty = new SimpleBooleanProperty();
    private final IntegerProperty mi_progressProperty = new SimpleIntegerProperty(0);

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
            FileNodeIterator fnIterator;

            searchText2 = searchText;

            fnIterator = new FileNodeIterator(getCurrentTreeItem().getValue());
            fnIterator.setStoppedOnMaxCountProperty(mi_data.mi_stoppedOnMaxCountProperty);
            fnIterator.setStoppedOnTimeoutProperty(mi_data.mi_stoppedOnTimeoutProperty);
            fnIterator.enableProgress(mi_data.mi_progressProperty);
            fnIterator.setMaxCount(Integer.valueOf(mi_data.mi_maxCountProperty.get()));
            fnIterator.setTimeoutInSeconds(Integer.valueOf(mi_data.mi_maxTimeProperty.get()));
            fnIterator.forEach(fn -> {
              if (!fn.isFile())
              {
                return false;
              }

              if (pattern != null)
              {
                if (matcher.reset(fn.getAbsolutePath()).find())
                {
                  list.add(fn);
                  return true;
                }
              }
              else
              {
                if (fn.getAbsolutePath().contains(searchText2))
                {
                  list.add(fn);
                  return true;
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

  static public class Counter
  {
    private int mi_counter;

    public Counter(int initialCount)
    {
      mi_counter = initialCount;
    }

    public void increment()
    {
      mi_counter++;
    }

    public int get()
    {
      return mi_counter;
    }
  }
}