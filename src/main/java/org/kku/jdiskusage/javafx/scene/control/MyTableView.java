package org.kku.jdiskusage.javafx.scene.control;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.HashMap;
import java.util.Map;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn.ButtonCell;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.ui.util.TableUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

public class MyTableView<T>
  extends TableView<T>
{
  private RankColumnData m_rankColumnData;

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

  public MyTableColumn<T, ButtonCell> addFilterColumn(MyTableColumn<T, Void> nestedColumn, String name)
  {
    ButtonCell buttonProperty;
    MyTableColumn<T, ButtonCell> column;

    buttonProperty = new ButtonCell(() -> IconUtil.createIconNode("filter"));

    column = addColumn(nestedColumn, name);
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

    m_rankColumnData = new RankColumnData();

    rankColumn = addColumn(columnName);
    rankColumn.setColumnCount(5);
    rankColumn.setCellValueGetter(m_rankColumnData::getRank);
    rankColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);

    return rankColumn;
  }

  private class RankColumnData
  {
    private final Map<Integer, Integer> m_lineNumberMap = new HashMap<>();

    private RankColumnData()
    {
      itemsProperty().addListener((o, oldItemList, newItemList) -> {
        m_lineNumberMap.clear();
        for (int index = 0; index < newItemList.size(); index++)
        {
          m_lineNumberMap.put(System.identityHashCode(newItemList.get(index)), index + 1);
        }
      });
    }

    public Integer getRank(T item)
    {
      return m_lineNumberMap.get(System.identityHashCode(item));
    }
  }
}
