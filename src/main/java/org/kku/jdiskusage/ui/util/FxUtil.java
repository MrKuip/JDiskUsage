package org.kku.jdiskusage.ui.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

public class FxUtil
{
  private static Map<Integer, Double> m_columnWidthByColumnCountMap = new HashMap<>();

  private FxUtil()
  {
  }

  public static BarChart<Number, String> createBarChart(NumberAxis xAxis, CategoryAxis yAxis)
  {
    BarChart<Number, String> barChart;

    barChart = new BarChart<>(xAxis, yAxis);
    barChart.setLegendVisible(false);
    barChart.setBarGap(1.0);
    barChart.setCategoryGap(4.0);

    return barChart;
  }

  public static PieChart createPieChart()
  {
    PieChart pieChart;

    pieChart = new PieChart();
    pieChart.setLegendVisible(false);
    pieChart.setStartAngle(90.0);

    pieChart.getData().addListener(new ListChangeListener<PieChart.Data>()
    {
      @Override
      public void onChanged(Change<? extends Data> c)
      {
        if (c.next())
        {
          IntStream.range(c.getFrom(), c.getTo()).forEach(index -> {
            System.out.println("index=" + index);
            pieChart.getData().get(index).getNode().setStyle("-fx-pie-color: " + PieChartColors.getColor(index) + ";");
          });
        }
      }
    });

    return pieChart;
  }

  public class PieChartColors
  {
    private static final String[] chartColorPalette =
    {
        "#f9d900", // Default gold
        "#a9e200", // Default lime green
        "#22bad9", // Default sky blue
        "#0181e2", // Default azure
        "#2f357f", // Default indigo
        "#860061", // Default purple
        "#c62b00", // Default rust
        "#ff5700", // Default orange
        "#e6007e", // Default magenta
        "#008080", // Default teal
        "#808000", // Default olive
        "#000080", // Default navy
        "#800000", // Default maroon
        "#00FF00", // Default lime
        "#00FFFF", // Default aqua
        "#FF7F50", // Default coral
        "#FA8072", // Default salmon
        "#EE82EE", // Default violet
        "#A0522D", // Default sienna
        "#6A5ACD" // Default slate blue
    };

    public static String getColor(int index)
    {
      return chartColorPalette[index % chartColorPalette.length];
    }
  }

  public static Region createHorizontalSpacer(int size)
  {
    Region spacer;

    spacer = new Region();
    spacer.setPrefWidth(size);

    return spacer;
  }

  /**
   * Given the column count calculate the pixel width of the control.
   * 
   * @param columnCount the number of columns
   * @return the width in pixels
   */
  static public double getColumnCountWidth(int columnCount)
  {
    return m_columnWidthByColumnCountMap.computeIfAbsent(columnCount, cc -> {
      TextField field;
      @SuppressWarnings("unused")
      Scene scene;

      field = new TextField();
      // Hack: Add to scene otherwise the prefWidth will be -1
      scene = new Scene(field);
      // Hack: Call applyCss otherwise the prefWidth will be -1
      field.applyCss();
      field.setPrefColumnCount(columnCount);

      return field.prefWidth(-1);
    });
  }

  /**
   * Set a node in 'warning' mode if the boolean is true.
   * <p>
   * The warning style is in the css file.
   * <p>
   * Usage:
   * 
   * <pre>
   * TextField textField = new TextField("text");
   * BooleanProperty warningProperty = new SimpleBooleanProperty();
   * 
   * warningProperty.addListener(showWarning(textField));
   * </pre>
   * 
   * @param node
   * @return the changelistener
   */

  public static ChangeListener<? super Boolean> showWarning(Node node)
  {
    return (o, oldValue, newValue) -> {
      if (newValue)
      {
        node.getStyleClass().add("warning");
      }
      else
      {
        node.getStyleClass().remove("warning");
      }
    };
  }
}
