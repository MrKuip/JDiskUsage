package org.kku.jdiskusage.javafx.scene.chart;

import static org.kku.fx.ui.util.TranslateUtil.translate;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.kku.fx.ui.util.ColorPalette;
import org.kku.fx.ui.util.FxIconUtil;
import org.kku.jdiskusage.javafx.scene.chart.TreeMapChart.TreeMapColors.MyColor;
import org.kku.jdiskusage.ui.TreeMapChartFormPane.PathNodeTreeMapNode;
import org.kku.jdiskusage.util.Loggers;
import org.kku.jdiskusage.util.Performance;
import org.kku.jdiskusage.util.Performance.PerformancePoint;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;

public class TreeMapChart<T extends TreeMapNode>
  extends Pane
{
  private final static double MAX_DEPTH = 15.0;

  private TreeMapModel<T> m_model;
  private PixelDraw m_pixelDraw;
  private TreeMapColors m_treeMapColors = new TreeMapColors();
  private Pane m_selectionOverlay;
  private MigPane m_overlay;
  private Rectangle m_selection;
  private Rectangle m_selectionBorder;
  private Button m_reselectButton;

  public TreeMapChart()
  {
    init();
  }

  private void init()
  {
    Tooltip tooltip;

    tooltip = new Tooltip("");
    Tooltip.install(this, tooltip);
    tooltip.activatedProperty().addListener((_, _, currentActivated) -> {
      if (currentActivated)
      {
        Point2D mousePosition;
        TreeMapNode tmn;

        mousePosition = this.screenToLocal(new Robot().getMousePosition());
        tmn = getNodeAt((int) mousePosition.getX(), (int) mousePosition.getY());
        if (tmn != null)
        {
          tooltip.setText(tmn.getTooltipText());
        }
        else
        {
          tooltip.setText("");
        }
      }
    });

    m_reselectButton = translate(new Button("", FxIconUtil.createIconNode("selection")));
  }

  public void setModel(TreeMapModel<T> model)
  {
    m_model = model;
  }

  private int m_layout = 3;

  @Override
  protected void layoutChildren()
  {
    if (m_pixelDraw == null || (getWidth() != m_pixelDraw.getWidth() && getWidth() != m_pixelDraw.getWidth()))
    {
      m_pixelDraw = new PixelDraw((int) getWidth(), (int) getHeight());
      m_selection = null;
      m_selectionBorder = null;

      getChildren().clear();
      layoutChart();
    }
  }

  private void layoutChart()
  {
    try (PerformancePoint _ = Performance.measure("Layout nodes: " + m_layout))
    {
      // Inject the colorIndex in the low depth TreeMapNodes.
      // The nodes with a higher depth will derive their color from its parent
      try (PerformancePoint _ = Performance.measure("Setting colorIndex for nodes with depth <= 1"))
      {
        m_model.getRootNode().setColorIndex(m_treeMapColors.getNextColorIndex());
        m_model.getRootNode().getChildList().stream().sorted(Comparator.comparing(TreeMapNode::getSize).reversed())
            .forEach(tmn -> {
              tmn.setColorIndex(m_treeMapColors.getNextColorIndex());
            });
      }

      try (PerformancePoint _ = Performance.measure("Squarify/Create rectangles"))
      {
        // Try to create TreeMapNodes that are squarish in order for a user to comprehend the chart better than
        //   small long rectangles
        m_model.getRootNode().setBounds(0, 0, m_pixelDraw.getWidth(), m_pixelDraw.getHeight());
        m_pixelDraw.accept(Arrays.asList(m_model.getRootNode()));
        m_model.getRootNode().streamNode().filter(TreeMapNode::hasChildren)
            .sorted(Comparator.comparingInt(TreeMapNode::getDepth)).forEach(fn -> {
              if (fn.getWidth() > 0 && fn.getHeight() > 0)
              {
                new TreeMapSquarifyAlgoritm(fn.getX(), fn.getY(), fn.getWidth(), fn.getHeight(), fn.getChildList(),
                    m_pixelDraw).evaluate();
              }
            });
      }

      BorderPane bPane;
      StackPane pane;

      m_overlay = new MigPane();
      m_selectionOverlay = new Pane();
      m_selectionOverlay.setMouseTransparent(true);

      m_overlay.add(m_reselectButton);

      pane = new StackPane();
      pane.getChildren().add(m_pixelDraw.getCanvas());
      pane.getChildren().add(m_selectionOverlay);
      pane.getChildren().add(m_overlay);
      bPane = new BorderPane();
      bPane.setCenter(pane);

      getChildren().add(bPane);
    }
  }

  private class PixelDraw
      implements Consumer<List<TreeMapNode>>
  {
    private ExecutorService mi_executor = Executors.newFixedThreadPool(1);
    private int[] mi_buffer;
    private int mi_width;
    private int mi_height;
    private final WritablePixelFormat<IntBuffer> mi_pixelFormat = PixelFormat.getIntArgbPreInstance();
    private Canvas mi_canvas;

    public PixelDraw(int width, int height)
    {
      mi_width = width;
      mi_height = height;
      mi_buffer = new int[(mi_width + 1) * (mi_height + 1)];
      mi_canvas = new Canvas(mi_width, mi_height);
    }

    public int getWidth()
    {
      return mi_width;
    }

    public int getHeight()
    {
      return mi_height;
    }

    @Override
    public void accept(List<TreeMapNode> tmnList)
    {
      //mi_executor.submit(() -> drawRectangles(tmnList));
      drawRectangles(tmnList);
    }

    private void drawRectangles(List<TreeMapNode> tmnList)
    {
      for (TreeMapNode tmn : tmnList)
      {
        try
        {
          int x, y, width, height;
          MyColor myColor;

          x = tmn.getX();
          y = tmn.getY();
          width = tmn.getWidth();
          height = tmn.getHeight();

          Loggers.treemap.fine("Draw : %s", tmn);

          if (height < 0 || width < 0 || x < 0 || y < 0)
          {
            // DO NOT COMMIT
            //System.out.println("he?");
            return;
          }

          if (height == 0 || width == 0)
          {
            return;
          }

          myColor = m_treeMapColors.getColor(tmn.getColorIndex(), tmn.getDepth());

          int color = myColor.getIntColor();
          int darkerColor = myColor.getIntDarkerColor();

          int x1 = x;
          int x2 = x + width;
          int y1 = y;
          int y2 = y + height;
          int fromIndex;
          int toIndex;

          if (x1 + y2 * width > mi_buffer.length)
          {
            return;
          }

          for (int dy = y1; dy < y2; dy++)
          {
            fromIndex = x1 + mi_width * dy;
            toIndex = fromIndex + width;
            for (int index = fromIndex; index < toIndex; index++)
            {
              mi_buffer[index] = color;
            }
          }

          fromIndex = x1 + mi_width * y1;
          toIndex = fromIndex + width;
          for (int index = fromIndex; index < toIndex; index++)
          {
            mi_buffer[index] = darkerColor;
          }

          fromIndex = x1 + mi_width * y2;
          toIndex = fromIndex + width;
          for (int index = fromIndex; index < toIndex; index++)
          {
            mi_buffer[index] = darkerColor;
          }

          fromIndex = x1 + mi_width * y1;
          toIndex = x1 + mi_width * y2;
          for (int index = fromIndex; index < toIndex; index += mi_width)
          {
            mi_buffer[index] = darkerColor;
          }

          fromIndex = x2 + mi_width * y1;
          toIndex = x2 + mi_width * y2;
          for (int index = fromIndex; index < toIndex; index += mi_width)
          {
            mi_buffer[index] = darkerColor;
          }

          /*
          for (int dx = 0; dx < width; dx++)
          {
            mi_buffer[(x1 + dx) + mi_width * y] = darkerColor;
            mi_buffer[(x1 + dx) + mi_width * y2] = darkerColor;
          }
          
          for (int dy = 0; dy < height; dy++)
          {
            mi_buffer[x1 + mi_width * (y1 + dy)] = darkerColor;
            mi_buffer[x2 + mi_width * (y1 + dy)] = darkerColor;
          }
          */
        }
        catch (Throwable ex)
        {
          ex.printStackTrace();
        }
      }
    }

    private double getCushionEffect(TreeMapNode node, int x, int y)
    {
      double centerX = (node.getX() + node.getWidth()) / 2.0;
      double centerY = (node.getY() + node.getHeight()) / 2.0;
      double a = (node.getX() + node.getWidth()) / 2.0;
      double b = (node.getY() + node.getHeight()) / 2.0;
      double dx = x - centerX;
      double dy = y - centerY;
      double z = 1.0 - ((dx * dx) / (a * a)) - ((dy * dy) / (b * b));

      return z;
    }

    private double applyCushionEffect(TreeMapNode node, int x, int y)
    {
      if (true) return 1.0;
      // Parameters for the hyperbolic function
      double a = node.getWidth() / 2.0;
      double b = node.getHeight() / 2.0;

      double x1 = node.getWidth() * 0.4;
      double y1 = node.getHeight() / 2;

      double x2 = node.getWidth() * 0.6;
      double y2 = node.getHeight() / 2;

      double dx;
      double dy;

      if (x < x1)
      {
        dx = x1 - x;
        dy = y1 - y;
      }
      else if (x >= x1 && x < x2)
      {
        dx = 0;
        dy = y - y1;
      }
      else
      {
        dx = x2 - x;
        dy = y2 - y;
      }

      double z = 1.0 - (dx * dx) / (a * a) - (dy * dy) / (b * b);

      if (z < 0) z = 0; // Ensure shading stays within valid range

      return z;
    }

    public Canvas getCanvas()
    {
      PixelWriter pixelWriter;

      mi_executor.shutdown();
      try
      {
        mi_executor.awaitTermination(60, TimeUnit.SECONDS);
      }
      catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      pixelWriter = mi_canvas.getGraphicsContext2D().getPixelWriter();
      pixelWriter.setPixels(0, 0, mi_width, mi_height, mi_pixelFormat, mi_buffer, 0, mi_width);

      return mi_canvas;
    }
  }

  private TreeMapNode getNodeAt(int x, int y)
  {
    return m_model.getRootNode().getNodeAt(x, y);
  }

  static class TreeMapColors
  {
    private Map<Integer, Map<Integer, MyColor>> m_colorMap = new HashMap<>();
    private int m_currentColorIndex;

    private MyColor getColor(int colorIndex, int depth)
    {
      return m_colorMap.computeIfAbsent(colorIndex, (_) -> new HashMap<Integer, MyColor>()).computeIfAbsent(depth,
          (_) -> {
            return new MyColor(ColorPalette.getColorList().get(colorIndex % ColorPalette.getColorList().size())
                .getColor(1 - (depth / MAX_DEPTH)));
          });
    }

    public int getNextColorIndex()
    {
      return ++m_currentColorIndex;
    }

    static class MyColor
    {
      private final Color mi_color;
      private final int mi_intColor;
      private final Color mi_darkerColor;
      private final int mi_intDarkerColor;

      public MyColor(Color color)
      {
        mi_color = color;
        mi_intColor = colorToInt(color);
        mi_darkerColor = mi_color.darker();
        mi_intDarkerColor = colorToInt(mi_darkerColor);
      }

      public Color getColor()
      {
        return mi_color;
      }

      public int getIntColor()
      {
        return mi_intColor;
      }

      public Color getDarkerColor()
      {
        return mi_darkerColor;
      }

      public int getIntDarkerColor()
      {
        return mi_intDarkerColor;
      }

      private int colorToInt(Color c)
      {
        return (255 << 24) | ((int) (c.getRed() * 255) << 16) | ((int) (c.getGreen() * 255) << 8)
            | ((int) (c.getBlue() * 255));
      }
    }
  }

  public boolean hasSelection()
  {
    return false;
  }

  public void select(PathNodeTreeMapNode fntmn)
  {
    if (m_selection == null)
    {
      m_selection = new Rectangle();
      m_selection.setStroke(Color.BLACK);
      m_selection.setMouseTransparent(true);
      m_selection.setStrokeWidth(3.0);
      m_selection.getStrokeDashArray().addAll(20d, 10d);
      m_selection.setFill(Color.TRANSPARENT);

      m_selectionBorder = new Rectangle();
      m_selectionBorder.setMouseTransparent(true);
      m_selectionBorder.setStroke(Color.WHITE);
      m_selectionBorder.setStrokeWidth(5.0);
      m_selectionBorder.setFill(Color.TRANSPARENT);

      m_selectionOverlay.getChildren().addAll(m_selectionBorder, m_selection);
    }

    m_selection.setLayoutX(fntmn.getX());
    m_selection.setLayoutY(fntmn.getY());
    m_selection.setWidth(fntmn.getWidth());
    m_selection.setHeight(fntmn.getHeight());

    m_selectionBorder.setLayoutX(fntmn.getX());
    m_selectionBorder.setLayoutY(fntmn.getY());
    m_selectionBorder.setWidth(fntmn.getWidth());
    m_selectionBorder.setHeight(fntmn.getHeight());
  }

  public void addReselectListener(EventHandler<ActionEvent> eventHandler)
  {
    m_reselectButton.setOnAction(eventHandler);
  }
}
