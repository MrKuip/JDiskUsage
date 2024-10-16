package org.kku.jdiskusage.javafx.scene.control;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.kku.jdiskusage.ui.util.Colors;
import org.kku.jdiskusage.util.value.MutableDouble;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
  private final IntegerProperty m_detectedMaxLevelProperty = new SimpleIntegerProperty();

  public SunburstChart(TreeItem<T> root, int maxLevel, Function<T, Double> valueSupplier,
      BiConsumer<Node, TreeItem<T>> nodeCreationCallback)
  {
    m_root = root;
    m_maxLevel = maxLevel;
    m_valueSupplier = valueSupplier;
    m_nodeCreationCallback = nodeCreationCallback;

    m_centerXProperty.bind(this.widthProperty().divide(2));
    m_centerYProperty.bind(this.heightProperty().divide(2));

    createNodes(root, 90.0, -360.0, 1);
  }

  /**
   * Recursively create arc's for required levels.
   * 
   * @param root
   * @param startAngle
   * @param maxLength
   * @param level
   */

  private void createNodes(TreeItem<T> root, double startAngle, double maxLength, int level)
  {
    root.getChildren().stream().map(TreeItem::getValue).map(m_valueSupplier).reduce(Double::sum).ifPresent(sum -> {
      MutableDouble previousStartAngle;

      previousStartAngle = new MutableDouble(startAngle);
      root.getChildren().forEach(item -> {
        Arc arc;

        arc = new Arc();
        arc.setType(ArcType.ROUND);
        arc.setStroke(Color.WHITE);
        arc.setFill(calculateColor(item, level));
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
          createNodes(item, arc.getStartAngle(), arc.getLength(), level + 1);
        }

        // First the arc's of the highest level are added (This is necessary because they need to be 
        // painted over by arc's of a lower level) 
        if (Math.abs(arc.getLength()) > 0.5)
        {
          m_nodeCreationCallback.accept(arc, item);
          getChildren().add(arc);

          if (m_detectedMaxLevelProperty.get() < level)
          {
            m_detectedMaxLevelProperty.set(level);
          }
        }
      });
    });
  }

  private Color calculateColor(TreeItem<T> item, int level)
  {
    Optional<TreeItem<T>> parent;

    parent = Stream.iterate(item, TreeItem::getParent).filter(itm -> itm.getParent() == m_root).findFirst();
    if (parent.isPresent())
    {
      Color color;

      // The basic color of a child is determined from it's ultimate parent that is not the root.
      // (That is the parent just 1 level above the root)
      color = m_colorByItemMap.computeIfAbsent(parent.get(),
          key -> Colors.values()[m_colorByItemMap.size() % Colors.values().length].getColor());

      // The basic color is made 'darker' depended on it's level. The higher the level the darker the arc.
      return m_colorByLevelMap.computeIfAbsent(color.toString() + "-" + level, key -> {
        return color.deriveColor(0, 1.0, 1.0 - +level * 0.05, 1.0);
      });
    }

    // This will never happen!
    return Color.RED;
  }

  private NumberBinding calculateRadixProperty(int level)
  {
    return m_radixPropertyMap.computeIfAbsent(level,
        key -> Bindings.min(SunburstChart.this.widthProperty(), SunburstChart.this.heightProperty())
            .divide(m_detectedMaxLevelProperty).divide(2).multiply(level));
  }
}
