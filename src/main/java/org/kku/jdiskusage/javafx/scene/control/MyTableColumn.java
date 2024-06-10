package org.kku.jdiskusage.javafx.scene.control;

import java.util.EnumSet;
import java.util.function.Function;
import org.kku.jdiskusage.ui.MyTableFilter.Totals;
import org.kku.jdiskusage.ui.util.FormatterIF;
import org.kku.jdiskusage.util.ApplicationProperties.Props;
import org.kku.jdiskusage.util.ApplicationPropertyExtensionIF;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.util.Callback;

public class MyTableColumn<T, R>
  extends TableColumn<T, R>
    implements ApplicationPropertyExtensionIF
{
  private EnumSet<Totals> mi_totalsSet = EnumSet.noneOf(Totals.class);
  private FormatterIF<R> mi_formatter;
  private Pos m_alignment;

  public MyTableColumn(String text)
  {
    super(text);
    setId(text);
    init();
  }

  public void initPersistentPrefWidth(double defaultWidth)
  {
    Props props;

    props = getProps(getTableView().getId() + "_" + getId());
    setPrefWidth(props.getDouble(Property.PREF_SIZE, defaultWidth));
    widthProperty().addListener(props.getChangeListener(Property.PREF_SIZE));
  }

  public void setCellValueAlignment(Pos alignment)
  {
    m_alignment = alignment;
  }

  public void setCellValueFormatter(FormatterIF<R> formatter)
  {
    mi_formatter = formatter;
  }

  public void setCellValueGetter(Function<T, R> function)
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

  /*
   * public void setCellValueGetter2(Function<T, R> function) {
   * this.setCellValueFactory(new Callback<CellDataFeatures<T, R>,
   * ObservableValue<R>>() {
   * 
   * @Override public ObservableValue<R> call(CellDataFeatures<T, R> p) { if
   * (p.getValue() instanceof Totals) { MyTableColumn myTableColumn; MyTableView
   * myTableView; Totals totals;
   * 
   * totals = (Totals) p.getValue(); myTableColumn = (MyTableColumn)
   * p.getTableColumn(); myTableView = (MyTableView) p.getTableView(); if
   * (!myTableView.hasTotals(totals)) { return new ReadOnlyObjectWrapper(new
   * Wrapper("")); }
   * 
   * if (myTableColumn == myTableView.getColumns().get(0)) { return new
   * ReadOnlyObjectWrapper(new Wrapper(totals.getText())); }
   * 
   * if (myTableColumn.hasTotal(totals)) { Integer v =
   * p.getTableView().getItems().stream().filter(item -> !(item instanceof
   * Totals)) .map(item -> function.apply(item)).filter(value -> value instanceof
   * Integer).map(Integer.class::cast)
   * .collect(Collectors.summingInt(Integer::intValue));
   * 
   * System.out.println(v + " " + v.getClass().getSimpleName());
   * 
   * return new ReadOnlyObjectWrapper<R>((R) new Wrapper(v)); }
   * 
   * return new ReadOnlyObjectWrapper(new Wrapper("")); } return new
   * ReadOnlyObjectWrapper<R>(function.apply(p.getValue())); } }); }
   */

  public static class Wrapper<T>
  {
    private T m_object;

    private Wrapper(T object)
    {
      m_object = object;
    }

    public T getObject()
    {
      return m_object;
    }
  }

  private void init()
  {
    this.setCellFactory(column -> {
      TableCell<T, R> cell;
      MyTableColumn myTableColumn;

      myTableColumn = (MyTableColumn) column;

      cell = new TableCell<>()
      {
        @Override
        protected void updateItem(R value, boolean empty)
        {
          String text;
          Node graphic;

          super.updateItem(value, empty);

          if (value instanceof Wrapper)
          {
            value = (R) ((Wrapper) value).getObject();
            setBackground(new Background(new BackgroundFill(Color.PINK, CornerRadii.EMPTY, Insets.EMPTY)));
            setBorder(new Border(
                new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
          }

          if (value != null && value instanceof Node)
          {
            text = "";
            graphic = (Node) value;
          }
          else
          {
            graphic = null;
            if (empty || value == null)
            {
              text = "";
            }
            else
            {
              if (value != null && mi_formatter != null)
              {
                text = mi_formatter.format(value);
              }
              else
              {
                text = value.toString();
              }
            }
          }

          setText(text);
          setGraphic(graphic);

          if (m_alignment != null)
          {
            setAlignment(m_alignment);
          }
        }
      };

      cell.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>()
      {
        @Override
        public void handle(MouseEvent event)
        {
          if (event.getClickCount() > 1)
          {
            TableCell c = (TableCell) event.getSource();
          }
        }
      });

      return cell;
    });
  }

  public void addTotals(Totals totals)
  {
    mi_totalsSet.add(totals);
  }

  public boolean hasTotal(Totals totals)
  {
    return mi_totalsSet.contains(totals);
  }
}