package org.kku.jdiskusage.javafx.scene.control;

import java.util.stream.Stream;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class BreadCrumbBar<T>
  extends HBox
{
  private Property<TreeItem<T>> m_treeItem = new SimpleObjectProperty<>();
  private Property<TreeItem<T>> m_selectedTreeItem = new SimpleObjectProperty<>();

  public BreadCrumbBar()
  {
    m_treeItem.addListener((o, oldValue, newValue) -> { setModel(newValue); });
    setSpacing(0.0);
    setPadding(Insets.EMPTY);
  }

  private void setModel(TreeItem<T> model)
  {
    getChildren().clear();
    if (model != null)
    {
      Stream.iterate(model, parent -> parent != null, TreeItem::getParent).toList().reversed().stream()
          .forEach(this::addButton);
    }
  }

  private void addButton(TreeItem<T> item)
  {
    Button button;

    button = new BreadCrumbButton(item.getValue().toString());
    button.setOnAction((ae) -> {
      m_selectedTreeItem.setValue(item);
    });

    getChildren().add(button);
  }

  public Property<TreeItem<T>> treeItem()
  {
    return m_treeItem;
  }

  public Property<TreeItem<T>> selectedTreeItem()
  {
    return m_selectedTreeItem;
  }

  /**
   * Represents a BreadCrumb Button
   * 
   * <pre>
   * ----------
   *  \         \
   *  /         /
   * ----------
   * </pre>
   * 
   * 
   */
  public class BreadCrumbButton
    extends Button
  {
    private static final double arrowWidth = 5;
    private static final double arrowHeight = 20;

    /**
     * Create a BreadCrumbButton
     * 
     * @param text Buttons text
     */
    public BreadCrumbButton(String text)
    {
      super(text);

      setTooltip(new Tooltip(text));

      setShape(createButtonShape());
      setPickOnBounds(false);

      // This works around a bug in javaFX.
      // If this button is made small enough that 3 dots appear it will create a bounding box that
      //   takes into consideration the location of the mnemomic of the original text (which is very
      //   small hence the 3 dots)
      setMnemonicParsing(false);

      if (!isFirst())
      {
        HBox.setMargin(this, new Insets(0, 0, 0, -getArrowWidth()));
      }
    }

    /**
     * Gets the crumb arrow with
     * @return
     */
    public double getArrowWidth()
    {
      return arrowWidth;
    }

    /**
     * Create an arrow path
     * 
     * Based upon Uwe / Andy Till code snippet found here:
     * @see http://ustesis.wordpress.com/2013/11/04/implementing-breadcrumbs-in-javafx/
     * @return
     */
    private Path createButtonShape()
    {
      Path path;
      MoveTo e1;
      HLineTo e2;
      LineTo e3;
      LineTo e4;
      HLineTo e5;
      LineTo e6;
      ArcTo arcTo;
      ClosePath e7;

      // build the following shape (or home without left arrow)

      //   --------
      //  \         \
      //  /         /
      //   --------
      path = new Path();

      // begin in the upper left corner
      e1 = new MoveTo(0, 0);
      path.getElements().add(e1);

      // draw a horizontal line that defines the width of the shape
      e2 = new HLineTo();
      // bind the width of the shape to the width of the button
      e2.xProperty().bind(this.widthProperty().subtract(arrowWidth));
      path.getElements().add(e2);

      // draw upper part of right arrow
      e3 = new LineTo();
      // the x endpoint of this line depends on the x property of line e2
      e3.xProperty().bind(e2.xProperty().add(arrowWidth));
      e3.setY(arrowHeight / 2.0);
      path.getElements().add(e3);

      // draw lower part of right arrow
      e4 = new LineTo();
      // the x endpoint of this line depends on the x property of line e2
      e4.xProperty().bind(e2.xProperty());
      e4.setY(arrowHeight);
      path.getElements().add(e4);

      // draw lower horizontal line
      e5 = new HLineTo(0);
      path.getElements().add(e5);

      if (!isFirst())
      {
        // draw lower part of left arrow
        // we simply can omit it for the first Button
        e6 = new LineTo(arrowWidth, arrowHeight / 2.0);
        path.getElements().add(e6);
      }
      else
      {
        // draw an arc for the first bread crumb
        arcTo = new ArcTo();
        arcTo.setSweepFlag(true);
        arcTo.setX(0);
        arcTo.setY(0);
        arcTo.setRadiusX(15.0f);
        arcTo.setRadiusY(15.0f);
        path.getElements().add(arcTo);
      }

      // close path
      e7 = new ClosePath();
      path.getElements().add(e7);
      // this is a dummy color to fill the shape, it won't be visible
      path.setFill(Color.BLACK);
      path.setOpacity(0.5);

      return path;
    }
  }

  public boolean isFirst()
  {
    return getChildren().isEmpty();
  }
}
