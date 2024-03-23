package org.kku.jdiskusage.ui.util;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;

public class FxUtil
{
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
}
