package org.kku.jdiskusage.main;

import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.kku.jdiskusage.ui.util.ChartStyleSheet;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class ChartDemo
  extends Application
{
  private BorderPane m_content;
  private ChartStyleSheet m_chartStyleSheet = new ChartStyleSheet();

  enum ChartType
  {
    PieChart("PieChart", ChartDemo::getPieChart),
    LineChart("LineChart", ChartDemo::getLineChart),
    BarChart("BarChart", ChartDemo::getBarChart),
    StackedBarChart("StackedBarChart", ChartDemo::getStackedBarChart),
    ScatterChart("ScatterChart", ChartDemo::getScatterChart),
    BubbleChart("BubbleChart", ChartDemo::getBubbleChart),
    AreaChart("AreaChart", ChartDemo::getAreaChart);

    private final String m_name;
    private final Supplier<? extends Parent> m_chart;

    private ChartType(String name, Supplier<? extends Parent> chart)
    {
      m_name = name;
      m_chart = chart;
    }
  }

  @Override
  public void start(Stage stage)
  {
    // Create and show the scene
    Scene scene;
    FlowPane buttonPane;

    m_content = new BorderPane();
    buttonPane = new FlowPane();
    m_content.setTop(buttonPane);
    m_content.setBottom(getColorPane());

    System.out.println(m_chartStyleSheet.getStyleSheet());

    buttonPane.getChildren().addAll(Stream.of(ChartType.values()).map(chartType -> {
      Button button;

      button = new Button(chartType.m_name);
      button.setOnAction((ae) -> {
        Parent node;
        node = chartType.m_chart.get();
        node.getStylesheets().add(m_chartStyleSheet.getStyleSheet());
        m_content.setCenter(node);
      });

      return button;
    }).toList());

    ((Button) buttonPane.getChildren().get(0)).fire();

    scene = new Scene(m_content, 800, 600);
    scene.getStylesheets().add("jdiskusage.css");
    scene.getStylesheets().add(m_chartStyleSheet.getStyleSheet());
    stage.setScene(scene);
    stage.setTitle("Custom Pie Chart Colors");
    stage.show();
  }

  public static Chart getPieChart()
  {
    PieChart chart;

    chart = new PieChart();
    chart.getStyleClass().add("piechart");
    chart.getData()
        .addAll(IntStream.range(0, 25).mapToObj(number -> new PieChart.Data(String.valueOf(number), 1)).toList());

    return chart;
  }

  public static Chart getLineChart()
  {
    LineChart<Number, Number> chart;

    chart = new LineChart<>(new NumberAxis(), new NumberAxis());
    chart.getStyleClass().add("linechart");

    for (int series = 0; series < 25; series++)
    {
      XYChart.Series<Number, Number> dataSeries1;

      dataSeries1 = new XYChart.Series<>();
      dataSeries1.setName(String.valueOf(series));

      dataSeries1.getData().add(new XYChart.Data<Number, Number>(1 + series * 4, 5));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(5 + series * 4, 100));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(10 + series * 4, 300));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(20 + series * 4, 480));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(40 + series * 4, 610));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(80 + series * 4, 850));

      chart.getData().add(dataSeries1);
    }

    return chart;
  }

  public static Chart getBarChart()
  {
    BarChart<String, Number> chart;

    chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
    chart.getStyleClass().add("barchart");
    chart.setStyle("-fx-chart-color-0: black");

    for (int series = 0; series < 25; series++)
    {
      XYChart.Series<String, Number> dataSeries1;

      dataSeries1 = new XYChart.Series<>();
      dataSeries1.setName(String.valueOf(series));

      dataSeries1.getData().add(new XYChart.Data<String, Number>("a", 1 + series * 4));
      dataSeries1.getData().add(new XYChart.Data<String, Number>("b", 5 + series * 4));

      chart.getData().add(dataSeries1);
    }

    return chart;
  }

  public static Chart getStackedBarChart()
  {
    StackedBarChart<String, Number> chart;

    chart = new StackedBarChart<>(new CategoryAxis(), new NumberAxis());
    chart.getStyleClass().add("stackedbarchart");

    for (int series = 0; series < 25; series++)
    {
      XYChart.Series<String, Number> dataSeries1;

      dataSeries1 = new XYChart.Series<>();
      dataSeries1.setName(String.valueOf(series));

      dataSeries1.getData().add(new XYChart.Data<String, Number>("a", 1 + series * 2));
      dataSeries1.getData().add(new XYChart.Data<String, Number>("b", 5 + series * 2));

      chart.getData().add(dataSeries1);
    }

    return chart;
  }

  public static Chart getScatterChart()
  {
    ScatterChart<Number, Number> chart;

    chart = new ScatterChart<>(new NumberAxis(), new NumberAxis());
    chart.getStyleClass().add("scatterchart");

    for (int series = 0; series < 25; series++)
    {
      XYChart.Series<Number, Number> dataSeries1;

      dataSeries1 = new XYChart.Series<>();
      dataSeries1.setName(String.valueOf(series));

      dataSeries1.getData().add(new XYChart.Data<Number, Number>(1 + series * 4, 5));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(5 + series * 4, 100));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(10 + series * 4, 300));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(20 + series * 4, 480));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(40 + series * 4, 610));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(80 + series * 4, 850));

      chart.getData().add(dataSeries1);
    }

    return chart;
  }

  public static Chart getBubbleChart()
  {
    BubbleChart<Number, Number> chart;

    chart = new BubbleChart<>(new NumberAxis(), new NumberAxis());
    chart.getStyleClass().add("scatterchart");

    for (int series = 0; series < 25; series++)
    {
      XYChart.Series<Number, Number> dataSeries1;

      dataSeries1 = new XYChart.Series<>();
      dataSeries1.setName(String.valueOf(series));

      dataSeries1.getData().add(new XYChart.Data<Number, Number>(1 + series * 4, 5, 1));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(5 + series * 4, 100, 2));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(10 + series * 4, 300, 3));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(20 + series * 4, 480, 4));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(40 + series * 4, 610, 5));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(80 + series * 4, 850, 6));

      chart.getData().add(dataSeries1);
    }

    return chart;
  }

  public static Chart getAreaChart()
  {
    AreaChart<Number, Number> chart;

    chart = new AreaChart<>(new NumberAxis(), new NumberAxis());
    chart.getStyleClass().add("linechart");

    for (int series = 0; series < 25; series++)
    {
      XYChart.Series<Number, Number> dataSeries1;

      dataSeries1 = new XYChart.Series<>();
      dataSeries1.setName(String.valueOf(series));

      dataSeries1.getData().add(new XYChart.Data<Number, Number>(1 + series * 4, 5));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(5 + series * 4, 100));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(10 + series * 4, 300));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(20 + series * 4, 480));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(40 + series * 4, 610));
      dataSeries1.getData().add(new XYChart.Data<Number, Number>(80 + series * 4, 850));

      chart.getData().add(dataSeries1);
    }

    return chart;
  }

  public static MigPane getColorPane()
  {
    MigPane migPane;

    migPane = new MigPane("wrap 10, fillx");

    IntStream.range(0, 20).forEach(number -> {
      Label label;

      label = new Label(String.valueOf(number));
      label.setStyle("-fx-background-color: -fx-chart-color-" + (number % 20));

      migPane.add(label, "grow");
    });

    return migPane;
  }

  public static void main(String[] args)
  {
    launch(args);
  }
}
