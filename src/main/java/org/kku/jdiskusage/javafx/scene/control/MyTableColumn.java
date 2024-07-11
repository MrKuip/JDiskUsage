package org.kku.jdiskusage.javafx.scene.control;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.kku.jdiskusage.ui.util.FormatterIF;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.ui.util.StyledText;
import org.kku.jdiskusage.util.AppProperties;
import org.kku.jdiskusage.util.AppSettings.AppSetting;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class MyTableColumn<T, R>
  extends TableColumn<T, R>
{
  private FormatterIF<R> mi_formatter;
  private Pos m_alignment;
  private BiConsumer<MouseEvent, T> m_action;

  public MyTableColumn()
  {
    init();
  }

  @SuppressWarnings("unchecked")
  public void setColumnCount(int columnCount)
  {
    AppSetting<Double> prefSizeProperty;

    prefSizeProperty = AppProperties.PREF_SIZE.forSubject(getTableView().getId() + "_" + getId());

    setPrefWidth(prefSizeProperty.get(FxUtil.getColumnCountWidth(columnCount)));
    widthProperty().addListener(prefSizeProperty.getChangeListener());
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

  public void setAction(BiConsumer<MouseEvent, T> action)
  {
    m_action = action;
  }

  private void init()
  {
    this.setCellFactory(column -> {
      TableCell<T, R> cell;
      final Button button = new Button();

      cell = new TableCell<>()
      {
        @Override
        protected void updateItem(R value, boolean empty)
        {
          String text;
          Node graphic;

          super.updateItem(value, empty);

          button.setText("");
          button.setOnAction(null);

          if (value != null && value instanceof StyledText)
          {
            StyledText styledText;

            styledText = (StyledText) value;
            if (styledText.isPlainText())
            {
              text = styledText.getPlainText();
              graphic = null;
            }
            else
            {
              text = null;
              graphic = styledText.getTextFlow();
              setPrefHeight(1);
            }
          }
          else if (value != null && value instanceof ButtonProperty)
          {
            button.setGraphic(((ButtonProperty) value).getGraphic());
            button.setOnMouseClicked((ae) -> m_action.accept(ae, getTableRow().getItem()));
            graphic = button;
            text = null;
          }
          else if (value != null && value instanceof Node)
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

      return cell;
    });
  }

  static public class ButtonProperty
  {
    private Supplier<Node> mi_graphic;

    public ButtonProperty(Supplier<Node> graphic)
    {
      mi_graphic = graphic;
    }

    public Node getGraphic()
    {
      return mi_graphic.get();
    }
  }
}