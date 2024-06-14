package org.kku.jdiskusage.ui.util;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
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

    return pieChart;
  }

  public static Region createHorizontalSpacer(int size)
  {
    Region spacer;

    spacer = new Region();
    spacer.setPrefWidth(size);

    return spacer;
  }

  public static Region createHorizontalFiller()
  {
    Region spacer;

    spacer = new Region();
    spacer.setMaxWidth(Double.MAX_VALUE);

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
}
