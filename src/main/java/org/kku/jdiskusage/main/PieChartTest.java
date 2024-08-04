package org.kku.jdiskusage.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class PieChartTest
  extends Application
{
  // Define the custom color palette
  private static final String[] CUSTOM_COLORS =
  {
      "#f9d900", "#a9e200", "#22bad9", "#0181e2", "#2f357f", "#860061", "#c62b00", "#ff5700", "#e6007e", "#008080",
      "#808000", "#000080", "#800000", "#00FF00", "#00FFFF", "#FF7F50", "#FA8072", "#EE82EE", "#A0522D", "#6A5ACD"
  };

  private final Random rng = new Random();

  @Override
  public void start(Stage primaryStage) throws Exception
  {
    PieChart chart = new PieChart();
    Button button = new Button("Generate Data");
    button.setOnAction(e -> updateChart(chart));
    BorderPane root = new BorderPane(chart);
    HBox controls = new HBox(button);
    controls.setAlignment(Pos.CENTER);
    controls.setPadding(new Insets(5));
    root.setTop(controls);

    Scene scene = new Scene(root, 600, 600);
    scene.getStylesheets().add("jdiskusage.css");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private void updateChart(PieChart chart)
  {
    chart.getData().clear();
    int numValues = 14;

    List<String> colors = new ArrayList<>();
    List<PieChart.Data> data = new ArrayList<>();

    for (int i = 0; i < numValues; i++)
    {
      colors.add(CUSTOM_COLORS[i % CUSTOM_COLORS.length]);
      PieChart.Data d = new PieChart.Data("Item " + i, rng.nextDouble() * 100);
      data.add(d);
      chart.getData().add(d);
    }

    chart.requestLayout();
    chart.applyCss();

    for (int i = 0; i < data.size(); i++)
    {
      String colorClass = "";
      for (String cls : data.get(i).getNode().getStyleClass())
      {
        if (cls.startsWith("default-color"))
        {
          colorClass = cls;
          break;
        }
      }
      for (Node n : chart.lookupAll("." + colorClass))
      {
        n.setStyle("-fx-pie-color: " + colors.get(i));
      }
    }
  }

  public static void main(String[] args)
  {
    Application.launch(args);
  }
}
