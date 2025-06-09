package org.kku.jdiskusage.javafx.scene.control;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.kku.common.util.AppProperties.AppProperty;
import org.kku.fx.ui.util.FxUtil;
import org.kku.fx.util.FxProperty;
import org.kku.jdiskusage.ui.util.FormatterIF;
import org.kku.jdiskusage.ui.util.StyledText;
import org.kku.jdiskusage.util.AppSettings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class MyTableColumn<T, R>
  extends TableColumn<T, R>
{
  private FormatterIF<R> m_formatter;
  private Pos m_alignment;
  private BiConsumer<MouseEvent, T> m_action;
  private BiConsumer<T, R> m_setter;

  public MyTableColumn()
  {
    init();
  }

  @SuppressWarnings("unchecked")
  public void setColumnCount(int columnCount)
  {
    setPrefWidth(getPrefSizeProperty().get(FxUtil.getColumnCountWidth(columnCount)));
    widthProperty().addListener(FxProperty.getChangeListener(getPrefSizeProperty()));
  }

  public void setCellValueAlignment(Pos alignment)
  {
    m_alignment = alignment;
  }

  public void setCellValueFormatter(FormatterIF<R> formatter)
  {
    m_formatter = formatter;
  }

  public void setCellValueSetter(BiConsumer<T, R> setter)
  {
    m_setter = setter;
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
    this.setCellFactory((_) -> {
      return new TableCell<>()
      {
        final Button button = new Button();
        final CheckBox checkBox = new CheckBox();

        {
          if (m_setter != null)
          {
            checkBox.setOnAction((_) -> {
              T item = getTableRow().getItem();
              if (item != null)
              {
                m_setter.accept(item, (R) Boolean.valueOf(checkBox.isSelected()));
                getTableView().refresh();
              }
            });
          }
        }

        @Override
        protected void updateItem(R item, boolean empty)
        {
          String text;
          Node graphic;

          super.updateItem(item, empty);

          if (item != null && item instanceof StyledText styledText)
          {
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
          else if (item != null && item instanceof ButtonCell buttonCell)
          {
            button.setText("");
            button.setOnAction(null);
            button.setGraphic(buttonCell.getGraphic());
            button.setOnMouseClicked((ae) -> m_action.accept(ae, getTableRow().getItem()));
            graphic = button;
            text = null;
          }
          else if (item != null && item instanceof Boolean checked)
          {
            checkBox.setSelected(checked);
            text = "";
            graphic = checkBox;
          }
          else if (item != null && item instanceof Node node)
          {
            text = "";
            graphic = node;
          }
          else
          {
            graphic = null;
            if (empty || item == null)
            {
              text = "";
            }
            else
            {
              if (m_formatter != null)
              {
                text = m_formatter.format(item);
              }
              else
              {
                text = item.toString();
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
    });
  }

  private AppProperty<Double> getPrefSizeProperty()
  {
    assert getTableView().getId() != null;
    return AppSettings.PREF_SIZE.forSubject(getTableView().getId() + "_" + getId());
  }

  static public class ButtonCell
  {
    private Supplier<Node> mi_graphic;

    public ButtonCell(Supplier<Node> graphic)
    {
      mi_graphic = graphic;
    }

    public Node getGraphic()
    {
      return mi_graphic.get();
    }
  }
}