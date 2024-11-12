package org.kku.jdiskusage.javafx.scene.control;

import java.util.function.Function;
import org.kku.jdiskusage.ui.util.FormatterIF;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.ui.util.Percent;
import org.kku.jdiskusage.util.AppProperties.AppProperty;
import org.kku.jdiskusage.util.AppSettings;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

public class MyTreeTableColumn<T, R>
  extends TreeTableColumn<T, R>
{
  public MyTreeTableColumn()
  {
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
    setPrefWidth(getPrefSizeProperty().get(FxUtil.getColumnCountWidth(columnCount)));
    widthProperty().addListener(getPrefSizeProperty().getChangeListener());
  }

  public void setCellValueAlignment(Pos pos)
  {
    setStyle("-fx-alignment: " + pos + ";");
  }

  private AppProperty<Double> getPrefSizeProperty()
  {
    assert getTreeTableView().getId() != null;
    return AppSettings.PREF_SIZE.forSubject(getTreeTableView().getId() + "_" + getId());
  }

  static class MyTreeTableCell<T, R>
    extends TreeTableCell<T, R>
  {
    private final FormatterIF<R> mi_formatter;
    private final MigPane mi_progressPane = new MigPane("ins 0", "[grow][pref:pref:pref]", "[]");
    private final ProgressBar mi_progressBar = new ProgressBar();
    private final Label mi_progressLabel = new Label();

    MyTreeTableCell(FormatterIF<R> formatter)
    {
      mi_formatter = formatter;
      initProgressPane();
    }

    private void initProgressPane()
    {
      mi_progressPane.getChildren().addAll(mi_progressBar, mi_progressLabel);
      mi_progressPane.getStylesheets().add("jdiskusage.css");
      mi_progressPane.getStyleClass().add("progress-bar-cell");
    }

    @Override
    protected void updateItem(R item, boolean empty)
    {
      super.updateItem(item, empty);

      setText("");
      setGraphic(null);

      if (item instanceof Percent percent)
      {
        mi_progressLabel.setText(mi_formatter.format(item));
        if (AppPreferences.showProgressInTable.get())
        {
          mi_progressBar.setProgress(percent.getPercent());
          setGraphic(mi_progressPane);
        }
        else
        {
          setGraphic(mi_progressLabel);
        }
      }
      else
      {
        setText(mi_formatter.format(item));
      }
    }
  }
}