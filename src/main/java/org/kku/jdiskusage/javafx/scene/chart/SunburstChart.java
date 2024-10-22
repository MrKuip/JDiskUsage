package org.kku.jdiskusage.javafx.scene.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.kku.jdiskusage.ui.util.ColorPalette.ChartColor;
import org.kku.jdiskusage.util.value.MutableDouble;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

public class SunburstChart<T>
  extends Region
{
  private TreeItem<T> m_root;
  private int m_maxLevel = 3;
  private final Function<T, Double> m_valueSupplier;
  private BiConsumer<Node, TreeItem<T>> m_nodeCreationCallback;
  private List<ChartColor> m_colorList = new ArrayList<>();

  private final Map<TreeItem<T>, ChartColor> m_colorByItemMap = new HashMap<>();
  private final Map<String, Color> m_colorByLevelMap = new HashMap<>();
  private final Map<Integer, NumberBinding> m_radixPropertyMap = new HashMap<>();

  private final DoubleProperty m_centerXProperty = new SimpleDoubleProperty();
  private final DoubleProperty m_centerYProperty = new SimpleDoubleProperty();
  private final IntegerProperty m_detectedMaxLevelProperty = new SimpleIntegerProperty();

  public SunburstChart(Function<T, Double> valueSupplier)
  {
    m_valueSupplier = valueSupplier;

    m_centerXProperty.bind(this.widthProperty().divide(2));
    m_centerYProperty.bind(this.heightProperty().divide(2));

    refresh();
  }

  public void setMaxLevel(int maxLevel)
  {
    m_maxLevel = maxLevel;
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
      createNodes(Arrays.asList(m_root), 90.0, -360.0, 1);
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

  private void createNodes(List<TreeItem<T>> children, double startAngle, double maxLength, int level)
  {
    children.stream().map(TreeItem::getValue).map(m_valueSupplier).reduce(Double::sum).ifPresent(sum -> {
      MutableDouble previousStartAngle;

      previousStartAngle = new MutableDouble(startAngle);
      children.forEach(item -> {
        Arc arc;

        arc = new Arc();
        arc.setType(ArcType.ROUND);
        arc.setStroke(Color.WHITE);
        arc.fillProperty().bind(calculateColor(item, level));
        arc.centerXProperty().bind(m_centerXProperty);
        arc.centerYProperty().bind(m_centerYProperty);
        arc.radiusXProperty().bind(calculateRadixProperty(level));
        arc.radiusYProperty().bind(calculateRadixProperty(level));
        arc.setStartAngle(previousStartAngle.get());
        arc.setLength((m_valueSupplier.apply(item.getValue()) / sum) * maxLength);
        previousStartAngle.add(arc.getLength());

        // Recursively add the arc's from the next level
        if (level < m_maxLevel && item.getChildren().size() > 0 && Math.abs(arc.getLength()) > 2)
        {
          createNodes(item.getChildren(), arc.getStartAngle(), arc.getLength(), level + 1);
        }

        // First the arc's of the highest level are added (This is necessary because they need to be 
        // painted over by arc's of a lower level) 
        if (Math.abs(arc.getLength()) > 0.5)
        {
          if (m_nodeCreationCallback != null)
          {
            m_nodeCreationCallback.accept(arc, item);
          }
          getChildren().add(arc);
          if (item == m_root)
          {
            Label label;

            label = new Label(item.getValue().toString());
            label.layoutXProperty().bind(arc.centerXProperty().subtract(label.widthProperty().divide(2)));
            label.layoutYProperty().bind(arc.centerYProperty().subtract(label.heightProperty().divide(2)));
            getChildren().add(label);
          }

          if (m_detectedMaxLevelProperty.get() < level)
          {
            m_detectedMaxLevelProperty.set(level);
          }
        }
      });
    });
  }

  private ObservableValue<Color> calculateColor(TreeItem<T> item, int level)
  {
    Optional<TreeItem<T>> parent;

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
      ChartColor color;

      // The basic color of a child is determined from it's ultimate parent that is not the root.
      // (That is the parent just 1 level above the root)
      color = m_colorByItemMap.computeIfAbsent(parent.get(),
          key -> m_colorList.get(m_colorByItemMap.size() % m_colorList.size()));

      // The basic color is made 'darker' depended on it's level. The higher the level the darker the arc.
      return color.colorProperty(1.0 - ((0.6 / m_maxLevel) * level));
    }

    // This will never happen!
    return null;
  }

  private NumberBinding calculateRadixProperty(int level)
  {
    return m_radixPropertyMap.computeIfAbsent(level,
        key -> Bindings.min(SunburstChart.this.widthProperty(), SunburstChart.this.heightProperty())
            .divide(m_detectedMaxLevelProperty).divide(2).multiply(level));
  }
}
