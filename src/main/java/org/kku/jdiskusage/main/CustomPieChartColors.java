package org.kku.jdiskusage.main;

import org.kku.jdiskusage.ui.util.FxUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.stage.Stage;

public class CustomPieChartColors
  extends Application
{

  // Define the custom color palette
  private static final String[] CUSTOM_COLORS =
  {
      "#f9d900", "#a9e200", "#22bad9", "#0181e2", "#2f357f", "#860061", "#c62b00", "#ff5700", "#e6007e", "#008080",
      "#808000", "#000080", "#800000", "#00FF00", "#00FFFF", "#FF7F50", "#FA8072", "#EE82EE", "#A0522D", "#6A5ACD"
  };

  @Override
  public void start(Stage stage)
  {
    PieChart pieChart;

    pieChart = FxUtil.createPieChart();

    // Create multiple pie chart data entries
    for (int i = 0; i < CUSTOM_COLORS.length + 2; i++)
    {
      PieChart.Data data;

      data = new PieChart.Data("Slice " + (i + 1), 100 / CUSTOM_COLORS.length);
      pieChart.getData().add(data);
    }

    //print(pieChart);

    // Create and show the scene
    Scene scene = new Scene(pieChart, 800, 600);
    scene.getStylesheets().add("jdiskusage.css");
    stage.setScene(scene);
    stage.setTitle("Custom Pie Chart Colors");
    stage.show();

    pieChart.lookupAll(".data0").forEach(d -> {
      d.getCssMetaData().forEach(meta -> {
        System.out.println("meta=" + meta);
      });
    });

    print(pieChart);
  }

  private void print(PieChart pieChart)
  {
    String text = ".chart-legend";
    pieChart.lookupAll(text).forEach(d -> {
      System.out.println(text + ": " + d);

      String text2 = ".chart-legend-item";
      d.lookupAll(text2).forEach(d1 -> {
        System.out.println("  " + text2 + ": " + d);

        String text3 = ".chart-legend-item-symbol";
        d1.lookupAll(text3).forEach(d2 -> {
          System.out.println("    " + text3 + ": " + d);
        });
      });
    });
  }

  private void adjustChartColors(PieChart pieChart)
  {
    // Apply custom color to the legend
    for (int i = 0; i < pieChart.getData().size(); i++)
    {
      PieChart.Data data;
      String color;
      String selector;

      data = pieChart.getData().get(i);
      color = CUSTOM_COLORS[i % CUSTOM_COLORS.length];
      System.out.println("legend:" + data.getName() + " color->" + color);
      selector = ".data" + i;

      pieChart.lookupAll(selector).forEach(dataNode -> {
        System.out.println(selector + " : " + dataNode);
        if (dataNode.getStyleClass().contains("pie-legend-symbol"))
        {
          dataNode.setStyle("-fx-background-color: " + color + ";");
          dataNode.getStyleClass().stream().filter(sc -> sc.contains("default-color")).findFirst().ifPresent(sc -> {
            dataNode.getStyleClass().remove(sc);
          });
        }
        else
        {
          dataNode.setStyle("-fx-pie-color: " + color + ";");
        }
      });
    }
  }

  public static void main(String[] args)
  {
    launch(args);
  }
}
