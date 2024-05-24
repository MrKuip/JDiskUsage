package org.kku.jdiskusage.javafx.scene.control;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;
import org.kku.jdiskusage.ui.MyTableFilter.Totals;
import org.kku.jdiskusage.ui.util.TableUtils;
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
  private EnumSet<Totals> mi_totalsSet = EnumSet.noneOf(Totals.class);

  public MyTableView(String id)
  {
    super();
    setId(id);

    getSelectionModel().setCellSelectionEnabled(true);
    TableUtils.installCopyPasteHandler(this);

    Skin<?> skin = skinProperty().get();
    System.out.println("skin = " + skin);
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

  public <R> MyTableColumn<T, R> addColumn(String name)
  {
    MyTableColumn<T, R> column;
    column = new MyTableColumn<T, R>(name);
    getColumns().add(column);
    return column;
  }

  @SuppressWarnings("unchecked")
  public void initTotals()
  {
    Stream.of(Totals.values()).forEach(totals ->
    {
      if (getColumns().stream().map(MyTableColumn.class::cast).filter(c -> c.hasTotal(totals)).findFirst().isPresent())
      {
        @SuppressWarnings("rawtypes")
        List items = getItems();
        items.add(totals);
        mi_totalsSet.add(totals);
      }
    });
  }

  public boolean hasTotals(Totals totals)
  {
    return !mi_totalsSet.isEmpty();
  }
}