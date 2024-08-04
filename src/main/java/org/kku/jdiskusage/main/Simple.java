package org.kku.jdiskusage.main;

import java.util.stream.IntStream;
import org.kku.jdiskusage.ui.util.ChartStyleSheet;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;

public class Simple
  extends Application
{

  @Override
  public void start(Stage stage)
  {
    Scene scene;
    PieChart pieChart;

    pieChart = new PieChart();
    // Make sure the rules in the chart style sheet for the PieChart are activated
    pieChart.getStyleClass().add("piechart");
    // The color scheme can be easily changed
    pieChart.setStyle("-fx-chart-color-0: black; -fx-chart-color-1: red");
    pieChart.getData()
        .addAll(IntStream.range(0, 25).mapToObj(number -> new PieChart.Data(String.valueOf(number), 1)).toList());

    scene = new Scene(pieChart, 800, 600);
    // Set the stylesheet on the scene and not on the charts
    scene.getStylesheets().add(new ChartStyleSheet().getStyleSheet());
    stage.setScene(scene);
    stage.setTitle("Custom Pie Chart Colors");
    stage.show();
  }

  public static void main(String[] args)
  {
    launch(args);
  }
}