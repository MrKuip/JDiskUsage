package org.kku.jdiskusage.ui.util;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

public class FxUtil
{
  private final static Map<Integer, Double> m_columnWidthByColumnCountMap = new HashMap<>();

  private FxUtil()
  {
  }

  public static BarChart<Number, String> createBarChart(NumberAxis xAxis, CategoryAxis yAxis)
  {
    BarChart<Number, String> chart;

    chart = new BarChart<>(xAxis, yAxis);
    chart.getStyleClass().add("barchart");
    chart.setLegendVisible(false);
    chart.setBarGap(1.0);
    chart.setCategoryGap(4.0);

    return chart;
  }

  public static PieChart createPieChart()
  {
    PieChart chart;

    chart = new PieChart();
    chart.getStyleClass().add("piechart");
    chart.setLegendVisible(false);
    chart.setStartAngle(90.0);

    return chart;
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
