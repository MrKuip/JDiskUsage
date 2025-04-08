package org.kku.jdiskusage.javafx.scene.control;

import static org.kku.fx.ui.util.TranslateUtil.translate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.fx.ui.util.FxIconUtil;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn.ButtonCell;
import org.kku.jdiskusage.ui.util.TableUtils;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

public class MyTableView<T>
  extends TableView<T>
{
  private RankColumnData m_rankColumnData;
  private SelectionContext m_selectionContext = new SelectionContext();

  public MyTableView(String id)
  {
    setId(id);
    setEditable(true);

    getSelectionModel().setCellSelectionEnabled(true);
    TableUtils.installCopyPasteHandler(this);

    m_selectionContext.init();
  }

  public SelectionContext getSelectionContext()
  {
    return m_selectionContext;
  }

  public class SelectionContext
  {
    private Set<T> mi_selectedSet = new HashSet<T>();
    private MyTableColumn<T, Boolean> mi_selectionColumn;

    private void init()
    {
      mi_selectionColumn = addColumn("Selection");
      mi_selectionColumn.setEditable(true);
      mi_selectionColumn.setCellValueAlignment(Pos.BASELINE_CENTER);
      mi_selectionColumn.setCellValueGetter(this::isSelected);
      mi_selectionColumn.setCellValueSetter((item, selection) -> select(item, selection));
      mi_selectionColumn.setColumnCount(4);
      mi_selectionColumn.setVisible(false);

      MyTableView.this.setContextMenu(m_selectionContext.getContextMenu());
    }

    public ContextMenu getContextMenu()
    {
      ContextMenu menu;
      MenuItem selectMenuItem;
      MenuItem selectAllMenuItem;
      MenuItem deleteMenuItem;

      menu = new ContextMenu();

      selectMenuItem = translate(new MenuItem("Select", FxIconUtil.createIconNode("checkbox-outline", IconSize.SMALL)));
      selectMenuItem.setOnAction((ae) -> {
        MyTableView.this.getSelectionContext().select(MyTableView.this.getSelectionModel().getSelectedItem());
      });
      selectAllMenuItem = translate(
          new MenuItem("Select all", FxIconUtil.createIconNode("checkbox-multiple-outline", IconSize.SMALL)));
      selectAllMenuItem.setOnAction((ae) -> {
        MyTableView.this.getSelectionContext().selectAll(MyTableView.this.getItems());
      });

      deleteMenuItem = translate(new MenuItem("Delete selected", FxIconUtil.createIconNode("delete", IconSize.SMALL)));

      menu.getItems().addAll(selectMenuItem, selectAllMenuItem, deleteMenuItem);

      return menu;
    }

    public void showSelectionColumn(boolean show)
    {
      mi_selectionColumn.setVisible(show);
    }

    public boolean isSelected(T o)
    {
      return getSelectedSet().contains(o);
    }

    public Set<T> getSelectedSet()
    {
      return mi_selectedSet;
    }

    public void select(T row)
    {
      select(row, true);
    }

    public void selectAll(List<T> rowList)
    {
      mi_selectedSet.addAll(rowList);
      showSelectionColumn(!mi_selectedSet.isEmpty());
      MyTableView.this.refresh();
    }

    public void deSelect(T row)
    {
      select(row, false);
    }

    public void select(T row, boolean selection)
    {
      if (selection)
      {
        System.out.println("select:" + row);
        mi_selectedSet.add(row);
      }
      else
      {
        System.out.println("deselect:" + row);
        mi_selectedSet.remove(row);
      }

      showSelectionColumn(!mi_selectedSet.isEmpty());
      MyTableView.this.refresh();
    }
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

    buttonProperty = new ButtonCell(() -> FxIconUtil.createIconNode("filter"));

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
