package org.kku.jdiskusage.javafx.scene.chart;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.kku.jdiskusage.ui.util.Colors;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;

public class TreeMapChart
  extends Pane
{
  private final static String TREE_MAP_NODE_PROPERTY = "TREE_MAP_NODE_PROPERTY";

  private TreeMapModel m_model;
  private double m_width = 0;
  private double m_height = 0;
  private int m_colorIndex;
  private double m_maxDepth;
  private Map<Integer, Map<Integer, Color>> m_colorMap = new HashMap<>();
  private Map<Integer, Map<Integer, Color>> m_darkerColorMap = new HashMap<>();

  public TreeMapChart()
  {
    init();
  }

  private void init()
  {
    Tooltip tooltip;

    tooltip = new Tooltip("");
    Tooltip.install(this, tooltip);
    tooltip.activatedProperty().addListener((a, previousActivated, currentActivated) -> {
      if (currentActivated)
      {
        Point2D mousePosition;
        Node node;

        mousePosition = this.screenToLocal(new Robot().getMousePosition());
        node = getNodeAt(this, mousePosition.getX(), mousePosition.getY());
        if (node != null && node.getProperties().get(TREE_MAP_NODE_PROPERTY) instanceof TreeMapNode tmn)
        {
          tooltip.setText(tmn.getTooltipText());
        }
        else
        {
          tooltip.setText("");
        }
      }
    });
  }

  public void setModel(TreeMapModel model)
  {
    m_model = model;
  }

  @Override
  protected void layoutChildren()
  {
    layoutChart();
  }

  private void layoutChart()
  {
    if (getWidth() != m_width && getHeight() != m_height)
    {
      m_width = getWidth();
      m_height = getHeight();

      try (PerformancePoint pp = Performance.measure("Squarify nodes"))
      {
        // Try to create rectangles that are squarish in order for a user to understand it better than
        //   small long rectangles
        m_model.getRootNode().setBounds(0, 0, m_width, m_height);
        m_model.getRootNode().streamNode().filter(TreeMapNode::hasChildren)
            .sorted(Comparator.comparingInt(TreeMapNode::getDepth)).forEach(fn -> {
              new TreeMapSquarifyAlgoritm(fn.getName(), fn.getX(), fn.getY(), fn.getWidth(), fn.getHeight(),
                  fn.getChildList().stream().map(TreeMapNode.class::cast).toList()).evaluate();
            });
      }

      try (PerformancePoint pp = Performance.measure("Creating nodes for tree chart"))
      {
        getChildren().clear();

        // Inject the colorIndex in the low depth TreeMapNodes.
        // The nodes with a higher depth will derive their color from its parent
        m_model.getRootNode().streamNode().filter(tmn -> tmn.getDepth() <= 1)
            .sorted(Comparator.comparing(TreeMapNode::getSize).reversed()).forEach(tmn -> {
              tmn.setColorIndex(m_colorIndex++);
            });

        // The maximum depth of the file system is used when determining the brightness of the color of the node
        m_maxDepth = m_model.getRootNode().streamNode().mapToInt(TreeMapNode::getDepth).max().getAsInt();

        // Previously I drew the rectangles in a Canvas but that was MUCH MUCH slower
        // Create rectangles for each file (not directory) that have a minimum size (0.1 pixel)
        getChildren().addAll(m_model.getRootNode().streamNode().filter(tmn -> tmn.isLeaf())
            .filter(tmn -> tmn.getWidth() > 0.1 && tmn.getHeight() > 0.1)
            .sorted(Comparator.comparingInt(TreeMapNode::getDepth)).map(this::createRectangle).toList());
      }
    }
  }

  private Rectangle createRectangle(TreeMapNode tmn)
  {
    Rectangle rect;

    rect = new Rectangle(tmn.getX(), tmn.getY(), tmn.getWidth(), tmn.getHeight());
    rect.setFill(getColor(tmn.getColorIndex(), tmn.getDepth()));
    rect.setStrokeWidth(0.5);
    rect.setStroke(getDarkerColor(tmn.getColorIndex(), tmn.getDepth()));
    rect.getProperties().put(TREE_MAP_NODE_PROPERTY, tmn);

    return rect;
  }

  private Color getDarkerColor(int colorIndex, int depth)
  {
    return m_darkerColorMap.computeIfAbsent(colorIndex, ci -> new HashMap<Integer, Color>()).computeIfAbsent(depth,
        d -> {
          return getColor(colorIndex, depth).darker();
        });
  }

  private Color getColor(int colorIndex, int depth)
  {
    return m_colorMap.computeIfAbsent(colorIndex, ci -> new HashMap<Integer, Color>()).computeIfAbsent(depth, d -> {
      return Colors.values()[colorIndex % Colors.values().length].getColor(1 - depth / m_maxDepth);
    });
  }

  private Node getNodeAt(Pane pane, double x, double y)
  {
    for (Node node : pane.getChildren())
    {
      if (node.getBoundsInParent().contains(x, y))
      {
        return node;
      }
    }
    return null;
  }
}
