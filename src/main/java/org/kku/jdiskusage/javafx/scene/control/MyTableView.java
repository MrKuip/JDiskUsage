package org.kku.jdiskusage.javafx.scene.control;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn.ButtonProperty;
import org.kku.jdiskusage.ui.util.ConcurrentUtil;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.ui.util.TableUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

public class MyTableView<T>
  extends TableView<T>
{
  private final Map<Integer, Integer> m_lineNumberMap = new HashMap<>();

  public MyTableView(String id)
  {
    super();
    setId(id);

    getSelectionModel().setCellSelectionEnabled(true);
    TableUtils.installCopyPasteHandler(this);
  }

  /**
   * Create a table column.
   * 
   * It's text is automatically translated.<br>
   * The column is default not editable.<br>
   * If nextedColumn is not null the new column is added to the nestedColumn.
   * 
   * @param <R>
   * @param nestedColumn
   * @param name
   * @return
   */
  public <R> MyTableColumn<T, R> addColumn(MyTableColumn<T, Void> nestedColumn, String name)
  {
    MyTableColumn<T, R> column;
    Label label;
    StackPane stack;

    column = translate(new MyTableColumn<T, R>());
    column.setCellValueAlignment(Pos.BASELINE_LEFT);
    column.setEditable(false);
    column.setId(name);

    label = translate(new Label(name));
    label.setWrapText(true);
    label.setAlignment(Pos.CENTER);
    label.setTextAlignment(TextAlignment.CENTER);

    stack = new StackPane();
    stack.getChildren().add(label);
    stack.prefWidthProperty().bind(column.widthProperty());
    label.prefWidthProperty().bind(stack.prefWidthProperty());

    column.setGraphic(stack);

    if (nestedColumn != null)
    {
      nestedColumn.getColumns().add(column);
    }
    else
    {
      getColumns().add(column);
    }

    return column;
  }

  public MyTableColumn<T, ButtonProperty> addFilterColumn(MyTableColumn<T, Void> nestedColumn, String name)
  {
    ButtonProperty buttonProperty;
    MyTableColumn<T, ButtonProperty> column;

    buttonProperty = new ButtonProperty(() -> IconUtil.createIconNode("filter", IconSize.SMALLER));

    column = addColumn(nestedColumn, translate(name));
    column.setColumnCount(2);
    column.setCellValueAlignment(Pos.CENTER);
    column.setEditable(true);
    column.setCellValueGetter((e) -> buttonProperty);

    return column;
  }

  /**
   * Add a column that doesn't nest.
   * 
   * @param <R>
   * @param name
   * @return
   */
  public <R> MyTableColumn<T, R> addColumn(String name)
  {
    return addColumn(null, name);
  }

  /**
   * Add A rank column.
   * 
   * @param columnName
   * @return
   */

  public MyTableColumn<T, Integer> addRankColumn(String columnName)
  {
    MyTableColumn<T, Integer> rankColumn;

    rankColumn = addColumn(columnName);
    rankColumn.setColumnCount(5);
    rankColumn.setCellValueGetter(this::getLineNumber);
    rankColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);

    itemsProperty().addListener((o, oldValue, newValue) -> {
      m_lineNumberMap.clear();
    });

    return rankColumn;
  }

  private Integer getLineNumber(T t1)
  {
    return m_lineNumberMap.computeIfAbsent(System.identityHashCode(t1), (key) -> {
      int index = getItems().indexOf(t1);
      if (index < 0)
      {
        return 0;
      }
      return index + 1;
    });
  }

  public void setItems(Supplier<List<T>> itemSupplier)
  {
    Node originalPlaceHolder;

    originalPlaceHolder = getPlaceholder();
    setPlaceholder(new Label("Searching..."));

    ConcurrentUtil.getInstance().getDefaultExecutor().submit(() -> {
      List<T> itemList;

      itemList = itemSupplier.get();
      Platform.runLater(() -> {
        setItems(FXCollections.observableArrayList(itemList));
        if (itemList.size() == 0)
        {
          setPlaceholder(new Label("No search data"));
        }
        else
        {
          setPlaceholder(originalPlaceHolder);
        }
      });
    });
  }

}
