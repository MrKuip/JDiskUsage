package org.kku.jdiskusage.javafx.scene.control;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.kku.jdiskusage.ui.util.Colors;
import org.kku.jdiskusage.util.value.MutableDouble;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

public class SunburstChart<T>
  extends Region
{
  private final TreeItem<T> m_root;
  private final int m_maxLevel;
  private final Function<T, Double> m_valueSupplier;
  private final BiConsumer<Node, TreeItem<T>> m_nodeCreationCallback;

  private final Map<TreeItem<T>, Color> m_colorByItemMap = new HashMap<>();
  private final Map<String, Color> m_colorByLevelMap = new HashMap<>();
  private final Map<Integer, NumberBinding> m_radixPropertyMap = new HashMap<>();

  private final DoubleProperty m_centerXProperty = new SimpleDoubleProperty();
  private final DoubleProperty m_centerYProperty = new SimpleDoubleProperty();

  public SunburstChart(TreeItem<T> root, int maxLevel, Function<T, Double> valueSupplier,
      BiConsumer<Node, TreeItem<T>> nodeCreationCallback)
  {
    m_root = root;
    m_maxLevel = maxLevel;
    m_valueSupplier = valueSupplier;
    m_nodeCreationCallback = nodeCreationCallback;

    m_centerXProperty.bind(this.widthProperty().divide(2));
    m_centerYProperty.bind(this.heightProperty().divide(2));

    init(root, 90.0, -360.0, 1);
  }

  private void init(TreeItem<T> root, double previousAngle, double maxLength, int level)
  {
    root.getChildren().stream().map(TreeItem::getValue).map(m_valueSupplier).reduce(Double::sum).ifPresent(sum -> {
      MutableDouble previousMutableAngle;

      previousMutableAngle = new MutableDouble(previousAngle);
      root.getChildren().forEach(item -> {
        MyArc arc;

        arc = new MyArc(item, level, previousMutableAngle.get(), sum, maxLength);
        m_nodeCreationCallback.accept(arc, item);

        previousMutableAngle.add(arc.getLength());

        if (level < m_maxLevel && item.getChildren().size() > 0 && Math.abs(arc.getLength()) > 2)
        {
          init(item, arc.getStartAngle(), arc.getLength(), level + 1);
        }

        if (Math.abs(arc.getLength()) > 0.5)
        {
          getChildren().add(arc);
        }
      });
    });
  }

  private class MyArc
    extends Arc
  {
    private final TreeItem<T> m_item;

    MyArc(TreeItem<T> item, int level, double previousAngle, double sum, double maxLength)
    {
      m_item = item;

      setType(ArcType.ROUND);
      setStroke(Color.WHITE);
      setFill(calculateColor(level));
      centerXProperty().bind(m_centerXProperty);
      centerYProperty().bind(m_centerYProperty);
      radiusXProperty().bind(calculateRadixProperty(level));
      radiusYProperty().bind(calculateRadixProperty(level));
      setStartAngle(previousAngle);
      setLength((m_valueSupplier.apply(item.getValue()) / sum) * maxLength);
    }

    private Color calculateColor(int level)
    {
      TreeItem<T> item;

      item = m_item;
      while (true)
      {
        if (item.getParent() == m_root)
        {
          Color color;

          color = m_colorByItemMap.computeIfAbsent(item,
              key -> Colors.values()[m_colorByItemMap.size() % Colors.values().length].getColor());

          return m_colorByLevelMap.computeIfAbsent(color.toString() + "-" + level, key -> {
            return color.deriveColor(0, 1.0, 1.0 - +level * 0.05, 1.0);
          });
        }

        item = item.getParent();
      }
    }

    private NumberBinding calculateRadixProperty(int level)
    {
      return m_radixPropertyMap.computeIfAbsent(level,
          key -> Bindings.min(SunburstChart.this.widthProperty(), SunburstChart.this.heightProperty())
              .divide(m_maxLevel * 2).multiply(level));
    }
  }
}
