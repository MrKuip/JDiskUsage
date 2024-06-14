package org.kku.jdiskusage.javafx.scene.control;

import java.util.function.Function;
import org.kku.jdiskusage.ui.util.FormatterIF;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.util.ApplicationProperties.Props;
import org.kku.jdiskusage.util.ApplicationPropertyExtensionIF;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class MyTreeTableColumn<T, R>
  extends TreeTableColumn<T, R>
    implements ApplicationPropertyExtensionIF
{
  public MyTreeTableColumn(String id, String text)
  {
    super(text);
    setId(id);
  }

  public void setCellValueGetter(Function<TreeItem<T>, R> function)
  {
    this.setCellValueFactory(new Callback<CellDataFeatures<T, R>, ObservableValue<R>>()
    {
      @Override
      public ObservableValue<R> call(CellDataFeatures<T, R> p)
      {
        return new ReadOnlyObjectWrapper<R>(function.apply(p.getValue()));
      }
    });
  }

  public void setCellValueFormatter(FormatterIF<R> formatter)
  {
    this.setCellFactory(column -> {
      return new MyTreeTableCell<T, R>(formatter);
    });
  }

  public void setColumnCount(int columnCount)
  {
    Props props;

    props = getProps(getTreeTableView().getId() + "_" + getId());
    setPrefWidth(props.getDouble(Property.PREF_SIZE, FxUtil.getColumnCountWidth(columnCount)));
    widthProperty().addListener(props.getChangeListener(Property.PREF_SIZE));
  }

  public void setCellValueAlignment(Pos pos)
  {
    setStyle("-fx-alignment: " + pos + ";");
  }

  static class MyTreeTableCell<T, R>
    extends TreeTableCell<T, R>
  {
    private final FormatterIF<R> mi_formatter;

    MyTreeTableCell(FormatterIF<R> formatter)
    {
      mi_formatter = formatter;
    }

    @Override
    protected void updateItem(R date, boolean empty)
    {
      super.updateItem(date, empty);

      if (empty || date == null)
      {
        setText("");
      }
      else
      {
        setText(mi_formatter.format(date));
      }
    }
  }
}