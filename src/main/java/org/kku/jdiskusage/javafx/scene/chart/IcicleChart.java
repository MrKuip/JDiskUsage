package org.kku.jdiskusage.javafx.scene.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.kku.common.util.value.MutableObject;
import org.kku.fx.ui.util.ColorPalette;
import org.kku.fx.ui.util.ColorPalette.ChartColor;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class IcicleChart<T>
  extends Region
{
  private TreeItem<T> m_root;
  private final Function<T, Double> m_valueSupplier;
  private BiConsumer<Node, TreeItem<T>> m_nodeCreationCallback;
  private List<ChartColor> m_colorList = new ArrayList<>();

  private final Map<TreeItem<T>, ChartColor> m_colorByItemMap = new HashMap<>();
  private final Map<Integer, NumberBinding> m_radixPropertyMap = new HashMap<>();

  private DoubleProperty m_deltaXProperty = new SimpleDoubleProperty(0.0);
  private DoubleProperty m_deltaYProperty = new SimpleDoubleProperty(0.0);
  private DoubleProperty m_factorHeightProperty = new SimpleDoubleProperty(1.0);
  private DoubleProperty m_itemWidthProperty = new SimpleDoubleProperty();
  private IntegerProperty m_maxLevelProperty = new SimpleIntegerProperty(4);
  private final IntegerProperty m_detectedMaxLevelProperty = new SimpleIntegerProperty();

  public IcicleChart(Function<T, Double> valueSupplier)
  {
    m_valueSupplier = valueSupplier;

    refresh();
  }

  private DoubleProperty itemWidthProperty()
  {
    if (m_itemWidthProperty == null)
    {
      m_itemWidthProperty = new SimpleDoubleProperty();
      m_itemWidthProperty.bind(widthProperty().divide(m_maxLevelProperty));
    }

    return m_itemWidthProperty;
  }

  public void setMaxLevel(int maxLevel)
  {
    m_maxLevelProperty.set(maxLevel);
    m_itemWidthProperty = null;
    refresh();
  }

  public void setColorList(List<ChartColor> colorList)
  {
    m_colorList = colorList;
    refresh();
  }

  public void setNodeCreationCallBack(BiConsumer<Node, TreeItem<T>> nodeCreationCallback)
  {
    m_nodeCreationCallback = nodeCreationCallback;
    refresh();
  }

  public void setModel(TreeItem<T> root)
  {
    m_root = root;
    refresh();
  }

  private void refresh()
  {
    if (m_root != null)
    {
      getChildren().clear();
      createNodes(Arrays.asList(m_root), null, 0);
    }
  }

  /**
   * Recursively create arc's for required levels.
   * 
   * @param root
   * @param startAngle
   * @param maxLength
   * @param level
   */

  private Double getTreeItemValue(TreeItem<T> treeItem)
  {
    return m_valueSupplier.apply(treeItem.getValue());
  }

  private void createNodes(List<TreeItem<T>> children, TreeItemRectangle parentRect, int level)
  {
    children.stream().map(ti -> getTreeItemValue(ti)).reduce(Double::sum).ifPresent(sum -> {
      final MutableObject<TreeItemRectangle> previous = new MutableObject<>(null);

      children.stream().sorted(Comparator.comparing(IcicleChart.this::getTreeItemValue, Comparator.reverseOrder()))
          .forEach(treeItem -> {
            TreeItemRectangle tir;
            Double percent;
            DoubleBinding xProperty;

            percent = m_valueSupplier.apply(treeItem.getValue()) / sum;
            xProperty = itemWidthProperty().multiply(level);

            if (percent > 0.005)
            {
              tir = new TreeItemRectangle(sum, parentRect, treeItem.toString(), percent, level);
              tir.setStroke(Color.WHITE);
              tir.fillProperty().bind(calculateColor(treeItem, level));
              tir.xProperty().bind(xProperty);
              tir.widthProperty().bind(itemWidthProperty());
              if (level == 0)
              {
                tir.heightProperty().bind(IcicleChart.this.heightProperty().multiply(m_factorHeightProperty));
                tir.yProperty().bind(m_deltaYProperty);
              }
              else
              {
                if (previous.get() == null)
                {
                  tir.yProperty().bind(parentRect.yProperty());
                }
                else
                {
                  tir.yProperty().bind(previous.get().yProperty().add(previous.get().heightProperty()));
                }
                tir.heightProperty().bind(parentRect.heightProperty().multiply(tir.getPercent()));
              }

              previous.set(tir);

              tir.setId(treeItem.getValue().toString());
              tir.addEventHandler(MouseEvent.MOUSE_CLICKED, (_) -> {
                double fromFactorHeight;
                double toFactorHeight;
                double fromDeltaY;
                double toDeltaY;

                System.out.println("icicly chart height=" + IcicleChart.this.getHeight());
                System.out.println("rect height=" + tir.getHeight());
                System.out.println("rect y=" + tir.getY());
                System.out.println("multiplier =" + (IcicleChart.this.getHeight() / tir.getHeight()));

                fromFactorHeight = m_factorHeightProperty.get();
                fromDeltaY = m_deltaYProperty.get();
                if (tir.getLevel() < 0)
                {
                  toFactorHeight = 1.0;
                  toDeltaY = 0;
                }
                else
                {
                  double currentFactor;

                  currentFactor = IcicleChart.this.getHeight() / tir.getHeight();
                  toFactorHeight = fromFactorHeight * currentFactor;
                  toDeltaY = (fromDeltaY + -tir.getY()) * currentFactor;
                }

                System.out.println("height factor: " + fromFactorHeight + " -> " + toFactorHeight);
                System.out.println("delta y: " + fromDeltaY + " -> " + toDeltaY);

                Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                        new KeyValue(m_factorHeightProperty, fromFactorHeight, Interpolator.EASE_BOTH),
                        new KeyValue(m_deltaYProperty, fromDeltaY, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.seconds(0.5),
                        new KeyValue(m_factorHeightProperty, toFactorHeight, Interpolator.EASE_BOTH),
                        new KeyValue(m_deltaYProperty, toDeltaY, Interpolator.EASE_BOTH)));
                timeline.play();
                timeline.setOnFinished((_) -> {
                  IcicleChart.this.getChildren().forEach(node -> {
                    if (node instanceof TreeItemRectangle rect1)
                    {
                      System.out.println(
                          "Node[" + node.getId() + "] rect.y=" + rect1.getY() + ", rect.height=" + rect1.getHeight());
                    }

                    IcicleChart.this.getParent().getParent().requestLayout();
                  });
                });
              });

              getChildren().add(tir);

              // Recursively add the arc's from the next level
              if (level <= m_maxLevelProperty.get() && treeItem.getChildren().size() > 0)
              {
                createNodes(treeItem.getChildren(), tir, level + 1);
              }

              if (m_detectedMaxLevelProperty.get() < level)
              {
                m_detectedMaxLevelProperty.set(level);
              }
            }
          });
    });
  }

  private static class TreeItemRectangle
    extends Group
  {
    private final Rectangle mi_rectangle;
    private final Text mi_text;
    private final double mi_percent;
    private final int mi_level;

    private TreeItemRectangle(double sum, TreeItemRectangle parent, String text, double percent, int level)
    {
      mi_rectangle = new Rectangle();
      mi_percent = percent;
      mi_level = level;

      mi_text = new Text(text);
      mi_text.xProperty().bind(mi_rectangle.xProperty().add(2.0));
      mi_text.yProperty().bind(mi_rectangle.yProperty().add(12.0));

      Rectangle clipRectangle = new Rectangle();
      clipRectangle.xProperty().bind(mi_rectangle.xProperty().add(mi_rectangle.strokeWidthProperty().divide(2)));
      clipRectangle.yProperty().bind(mi_rectangle.yProperty().add(mi_rectangle.strokeWidthProperty().divide(2)));
      clipRectangle.widthProperty().bind(mi_rectangle.widthProperty().subtract(mi_rectangle.strokeWidthProperty()));
      clipRectangle.heightProperty().bind(mi_rectangle.heightProperty().subtract(mi_rectangle.strokeWidthProperty()));
      mi_text.setClip(clipRectangle);

      getChildren().add(mi_rectangle);
      getChildren().add(mi_text);
    }

    public double getPercent()
    {
      return mi_percent;
    }

    public int getLevel()
    {
      return mi_level;
    }

    public final void setStroke(Paint value)
    {
      mi_rectangle.setStroke(value);
    }

    public final ObjectProperty<Paint> fillProperty()
    {
      return mi_rectangle.fillProperty();
    }

    public final DoubleProperty xProperty()
    {
      return mi_rectangle.xProperty();
    }

    public final double getY()
    {
      return mi_rectangle.getY();
    }

    public final DoubleProperty yProperty()
    {
      return mi_rectangle.yProperty();
    }

    public final DoubleProperty widthProperty()
    {
      return mi_rectangle.widthProperty();
    }

    public final double getHeight()
    {
      return mi_rectangle.getHeight();
    }

    public final DoubleProperty heightProperty()
    {
      return mi_rectangle.heightProperty();
    }
  }

  private ObservableValue<? extends Paint> calculateColor(TreeItem<T> item, int level)
  {
    Optional<TreeItem<T>> parent;
    Color color;

    color = null;
    if (item.isLeaf())
    {
      color = ColorPalette.RUST.getColor();
    }
    else if (!item.isLeaf())
    {
      color = ColorPalette.SKY_BLUE.getColor();
    }

    if (color != null)
    {
      return new SimpleObjectProperty<>(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, new Stop(0, color),
          new Stop(1, color.darker().darker())));
    }

    if (item == m_root)
    {
      parent = Optional.of(item);
    }
    else
    {
      parent = Stream.iterate(item, TreeItem::getParent).filter(itm -> itm.getParent() == m_root).findFirst();
    }
    if (parent.isPresent())
    {
      ChartColor chartColor;

      // The basic color of a child is determined from it's ultimate parent that is not the root.
      // (That is the parent just 1 level above the root)
      chartColor = m_colorByItemMap.computeIfAbsent(parent.get(),
          (_) -> m_colorList.get(m_colorByItemMap.size() % m_colorList.size()));

      // The basic color is made 'darker' depended on it's level. The higher the level the darker the arc.
      //return color.colorProperty(1.0 - ((0.6 / m_maxLevel) * level));
      return chartColor.colorProperty(1.0);
    }

    // This will never happen!
    return null;
  }

  private NumberBinding calculateRadixProperty(int level)
  {
    return m_radixPropertyMap.computeIfAbsent(level,
        (_) -> Bindings.min(IcicleChart.this.widthProperty(), IcicleChart.this.heightProperty())
            .divide(m_detectedMaxLevelProperty).divide(2).multiply(level));
  }
}
