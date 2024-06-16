package org.kku.jdiskusage.javafx.scene.control;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.HashMap;
import java.util.Map;
import org.kku.jdiskusage.ui.util.TableUtils;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;

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

  @Override
  protected Skin<?> createDefaultSkin()
  {
    return new TableViewSkin<>(this)
    {
      @Override
      protected TableHeaderRow createTableHeaderRow()
      {
        return new TableHeaderRow(this)
        {
          @Override
          protected NestedTableColumnHeader createRootHeader()
          {
            return new NestedTableColumnHeader(null)
            {
              private TableColumnHeader label;

              @Override
              protected TableColumnHeader createTableColumnHeader(TableColumnBase col)
              {
                label = super.createTableColumnHeader(col);
                return label;
              }
            };
          }
        };
      }
    };
  }

  @SuppressWarnings(
  {
      "rawtypes", "unchecked"
  })
  public <R> MyTableColumn<T, R> addColumn(MyTableColumn nestedColumn, String name)
  {
    MyTableColumn<T, R> column;
    column = translate(new MyTableColumn<T, R>(name));
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

  public <R> MyTableColumn<T, R> addColumn(String name)
  {
    return addColumn(null, name);
  }

  public MyTableColumn<T, Integer> addRankColumn(String columnName)
  {
    MyTableColumn<T, Integer> rankColumn;

    rankColumn = addColumn(columnName);
    rankColumn.setColumnCount(5);
    rankColumn.setCellValueGetter(this::getLineNumber);
    rankColumn.setCellValueAlignment(Pos.BASELINE_RIGHT);

    itemsProperty().addListener((o, oldValue, newValue) -> {
      System.out.println("Clear line numbers");
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

  public void setItemsAsync(Runnable runnable)
  {
    Node originalPlaceHolder;

    originalPlaceHolder = getPlaceholder();
    setPlaceholder(new Label("Searching..."));
    new Thread(() -> {
      runnable.run();
      Platform.runLater(() -> {
        if (getItems().size() == 0)
        {
          setPlaceholder(new Label("No search data"));
        }
        else
        {
          setPlaceholder(originalPlaceHolder);
        }
      });
    }).start();
  }
}
