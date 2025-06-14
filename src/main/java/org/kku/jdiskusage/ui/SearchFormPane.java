package org.kku.jdiskusage.ui;

import static org.kku.fx.ui.util.TranslateUtil.translate;
import static org.kku.fx.ui.util.TranslateUtil.translatedTextProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kku.common.util.Performance;
import org.kku.common.util.StringUtils;
import org.kku.common.util.Performance.PerformancePoint;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.fx.scene.control.NumericTextField;
import org.kku.fx.ui.util.FxIconUtil;
import org.kku.fx.ui.util.FxUtil;
import org.kku.fx.util.FxProperty;
import org.kku.jdiskusage.concurrent.FxTask;
import org.kku.jdiskusage.concurrent.ProgressData;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.AbstractFormPane;
import org.kku.jdiskusage.ui.common.FileNodeIterator;
import org.kku.jdiskusage.ui.util.StyledText;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;

public class SearchFormPane
  extends AbstractFormPane
{
  private SearchPaneData m_data = new SearchPaneData();
  private MyTableView<FileNodeIF> m_table;
  private Label m_searchActivityLabel = new Label("");

  public SearchFormPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("TABLE", "Show details table", "table", this::getTableNode);

    init();
    initPane();
  }

  private void initPane()
  {
    MigPane toolBar;
    Label searchLabel;
    ToggleButton regexButton;
    Button cancelButton;
    TextField searchTextField;
    Label maxCountLabel;
    NumericTextField<Integer> maxCountTextField;
    Label maxTimeLabel;
    NumericTextField<Integer> maxTimeTextField;
    ProgressBar progressBar;

    searchLabel = new Label(null, FxIconUtil.createIconNode("magnify"));

    regexButton = new ToggleButton(null, FxIconUtil.createIconNode("regex"));
    regexButton.selectedProperty().bindBidirectional(FxProperty.property(AppPreferences.searchRegexPreference));
    m_data.mi_regexSelectedProperty = regexButton.selectedProperty();

    searchTextField = new TextField();
    searchTextField.promptTextProperty().bind(translatedTextProperty("search"));
    searchTextField.setOnAction((_) -> search());
    m_data.mi_searchTextProperty = searchTextField.textProperty();

    maxCountTextField = NumericTextField.integerField();
    maxCountTextField.setPrefColumnCount(5);
    maxCountLabel = translate(new Label("Max items"));
    maxCountLabel.setLabelFor(maxCountTextField);
    maxCountTextField.valueProperty().bindBidirectional(FxProperty.property(AppPreferences.searchMaxCountPreference));
    m_data.mi_maxCountProperty = maxCountTextField.valueProperty();
    m_data.mi_progress.mi_stoppedOnMaxCountProperty.addListener(FxUtil.showWarning(maxCountTextField));

    maxTimeTextField = NumericTextField.integerField();
    maxTimeTextField.setPrefColumnCount(5);
    maxTimeLabel = translate(new Label("Max time (seconds)"));
    maxTimeLabel.setLabelFor(maxTimeTextField);
    maxTimeTextField.valueProperty().bindBidirectional(FxProperty.property(AppPreferences.searchMaxTimePreference));
    m_data.mi_maxTimeProperty = maxTimeTextField.valueProperty();
    m_data.mi_progress.mi_stoppedOnTimeoutProperty.addListener(FxUtil.showWarning(maxTimeTextField));

    cancelButton = new Button(null,
        FxIconUtil.createFxIcon("cancel", IconSize.SMALLER).fillColor(Color.RED).getIconLabel());

    toolBar = new MigPane("", "[pref][pref][grow,fill][pref][pref][pref][pref]", "[pref]1[pref]0");
    toolBar.add(cancelButton);
    toolBar.add(searchLabel);
    toolBar.add(searchTextField);
    toolBar.add(regexButton);
    toolBar.add(maxCountLabel);
    toolBar.add(maxCountTextField);
    toolBar.add(maxTimeLabel);
    toolBar.add(maxTimeTextField);

    progressBar = new ProgressBar();
    progressBar.setMaxWidth(Double.MAX_VALUE);
    progressBar.setProgress(0.25f);
    progressBar.getStyleClass().add("thin-progress-bar");
    progressBar.progressProperty().bind(Bindings.divide(m_data.mi_progress.mi_progressProperty, 100.0));

    toolBar.add(progressBar, "newline, span, grow, gap 0");

    setTop(toolBar);
  }

  private void search()
  {
    getDiskUsageData().refresh();
    m_data.mi_progress.mi_searchActivityProperty.set("Searching...");
    m_table.getItems().clear();

    new FxTask<>((progressData) -> m_data.getList(progressData), (itemList) -> {
      if (itemList.isEmpty())
      {
        m_data.mi_progress.mi_searchActivityProperty.set("No results found");
      }
      else
      {
        SearchFormPane.this.m_table.setItems(itemList);
      }
    }, SearchProgressData::new).execute();
  }

  Node getTableNode()
  {
    if (m_table == null)
    {
      MyTableColumn<FileNodeIF, StyledText> nameColumn;
      MyTableColumn<FileNodeIF, Long> fileSizeColumn;

      m_table = new MyTableView<>("Search");
      m_table.setEditable(false);

      m_table.addRankColumn("Rank");

      nameColumn = m_table.addColumn("Name");
      nameColumn.setColumnCount(300);
      nameColumn.setCellValueGetter((fn) -> {
        return m_data.getAbsolutePathWithSearchHighlighted(fn);
      });

      fileSizeColumn = m_table.addColumn("File size");
      fileSizeColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
      fileSizeColumn.setColumnCount(8);
      fileSizeColumn.setCellValueGetter((fn) -> fn.getSize());

      m_table.setPlaceholder(translate(m_searchActivityLabel));

      m_searchActivityLabel.textProperty().bind(m_data.mi_progress.mi_searchActivityProperty);
    }

    //m_data.mi_progress.mi_searchActivityProperty.set("No data");
    //m_table.getItems().clear();

    return m_table;
  }

  @Override
  public void refresh(TreeItem<FileNodeIF> selectedTreeItem)
  {
    super.refresh(selectedTreeItem);
  }

  private static class SearchProgressData
    extends ProgressData
  {
    private final BooleanProperty mi_stoppedOnMaxCountProperty = new SimpleBooleanProperty();
    private final BooleanProperty mi_stoppedOnTimeoutProperty = new SimpleBooleanProperty();
    private final IntegerProperty mi_progressProperty = new SimpleIntegerProperty(0);
    private final StringProperty mi_searchActivityProperty = new SimpleStringProperty();
  }

  private class SearchPaneData
    extends PaneData
  {
    public ObjectProperty<Integer> mi_maxCountProperty;
    public ObjectProperty<Integer> mi_maxTimeProperty;
    public BooleanProperty mi_regexSelectedProperty;
    public StringProperty mi_searchTextProperty;
    private SearchProgressData mi_progress = new SearchProgressData();
    private ObservableList<FileNodeIF> mi_list;
    private Map<String, SearchMatch> mi_searchPositionByPathMap = new HashMap<>();

    public SearchPaneData()
    {
    }

    public ObservableList<FileNodeIF> getList(SearchProgressData progressData)
    {
      if (mi_list == null)
      {
        String searchText;
        List<FileNodeIF> list;

        searchText = m_data.mi_searchTextProperty.get();
        try (PerformancePoint _ = Performance.measure("Collecting data for search '%s'", searchText))
        {
          Pattern pattern;
          Matcher matcher;

          list = new ArrayList<>();

          if (m_data.mi_regexSelectedProperty.get())
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

            fnIterator = new FileNodeIterator(getCurrentFileNode());
            fnIterator.setStoppedOnMaxCountProperty(mi_progress.mi_stoppedOnMaxCountProperty);
            fnIterator.setStoppedOnTimeoutProperty(mi_progress.mi_stoppedOnTimeoutProperty);
            fnIterator.enableProgress(mi_progress.mi_progressProperty);
            fnIterator.setMaxCount(m_data.mi_maxCountProperty.get());
            fnIterator.setTimeoutInSeconds(m_data.mi_maxTimeProperty.get());
            fnIterator.forEach(fn -> {
              String searchedString;

              if (!fn.isFile())
              {
                return false;
              }

              searchedString = fn.getAbsolutePath();
              if (pattern != null)
              {
                if (matcher.reset(searchedString).find())
                {
                  addSearchMatch(searchedString, matcher.start(), matcher.end());
                  matcher.end();
                  list.add(fn);
                  return true;
                }
              }
              else
              {
                int index;

                index = searchedString.indexOf(searchText2);
                if (index != -1)
                {
                  addSearchMatch(searchedString, index, index + searchText2.length());
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

    public StyledText getAbsolutePathWithSearchHighlighted(FileNodeIF fileNode)
    {
      SearchMatch searchMatch;
      String absolutePath;
      StyledText styledText;

      styledText = new StyledText();
      absolutePath = fileNode.getAbsolutePath();

      searchMatch = mi_searchPositionByPathMap.get(absolutePath);
      if (searchMatch != null)
      {
        styledText.addItem(absolutePath.substring(0, searchMatch.getBegin()));
        styledText.addItem(absolutePath.substring(searchMatch.getBegin(), searchMatch.getEnd()),
            "-fx-font-weight: bold");
        styledText.addItem(absolutePath.substring(searchMatch.getEnd()));
      }
      else
      {
        styledText.addItem(absolutePath);
      }

      return styledText;
    }

    private void addSearchMatch(String searchString, int start, int end)
    {
      mi_searchPositionByPathMap.put(searchString, new SearchMatch(start, end));
    }

    @Override
    protected void reset()
    {
      mi_list = null;
      mi_searchPositionByPathMap.clear();
    }
  }

  public static class SearchMatch
  {
    private final int mi_begin;
    private final int mi_end;

    private SearchMatch(int begin, int end)
    {
      mi_begin = begin;
      mi_end = end;
    }

    private int getBegin()
    {
      return mi_begin;
    }

    private int getEnd()
    {
      return mi_end;
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