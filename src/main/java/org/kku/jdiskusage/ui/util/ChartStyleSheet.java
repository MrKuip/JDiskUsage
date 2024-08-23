package org.kku.jdiskusage.ui.util;

public class ChartStyleSheet
{
  public ChartStyleSheet()
  {
  }

  public String getStyleSheet()
  {
    final String indexText;
    StringBuilder cssBuilder;
    String chartCss;
    String colorChart;

    indexText = "${index}";
    cssBuilder = new StringBuilder("data:text/css,");
    colorChart = """

        .root {
          -fx-chart-color-0:  ${CHART_COLOR_0}; // Default gold
          -fx-chart-color-1:  ${CHART_COLOR_1}; // Default lime green
          -fx-chart-color-2:  ${CHART_COLOR_2}; // Default sky blue
          -fx-chart-color-3:  ${CHART_COLOR_3}; // Default azure
          -fx-chart-color-4:  ${CHART_COLOR_4}; // Default indigo
          -fx-chart-color-5:  ${CHART_COLOR_5}; // Default purple
          -fx-chart-color-6:  ${CHART_COLOR_6}; // Default rust
          -fx-chart-color-7:  ${CHART_COLOR_7}; // Default orange
          -fx-chart-color-8:  ${CHART_COLOR_8};
          -fx-chart-color-9:  ${CHART_COLOR_9};
          -fx-chart-color-10: ${CHART_COLOR_10};
          -fx-chart-color-11: ${CHART_COLOR_11};
          -fx-chart-color-12: ${CHART_COLOR_12};
          -fx-chart-color-13: ${CHART_COLOR_13};
          -fx-chart-color-14: ${CHART_COLOR_14};
          -fx-chart-color-15: ${CHART_COLOR_15};
          -fx-chart-color-16: ${CHART_COLOR_16};
          -fx-chart-color-17: ${CHART_COLOR_17};
          -fx-chart-color-18: ${CHART_COLOR_18};
          -fx-chart-color-19: ${CHART_COLOR_19};
        }

        """;

    for (int index = 0; index < Colors.values().length; index++)
    {
      Colors color = Colors.values()[index];
      colorChart = colorChart.replace("${" + color.name() + "}", color.getWebColor());
    }

    cssBuilder.append(colorChart);

    chartCss = """
        .piechart .data${index} {
          -fx-pie-color: -fx-chart-color-${index};
        }

        .linechart .series${index} {
          -fx-stroke: -fx-chart-color-${index};
          -fx-background-color: -fx-chart-color-${index}, white;
        }

        .barchart .series${index} {
          -fx-bar-fill: -fx-chart-color-${index};
        }

        .stackedbarchart .series${index} {
          -fx-bar-fill: -fx-chart-color-${index};
        }

        .scatterchart .series${index} {
          -fx-background-color: -fx-chart-color-${index};
        }
        """;

    for (int i = 0; i < 20; i++)
    {
      cssBuilder.append(chartCss.replace(indexText, String.valueOf(i)));
    }

    return cssBuilder.toString();
  }
}