package org.kku.jdiskusage.ui.util;

public class ChartStyleSheet
{
  private String mi_styleSheet;

  public ChartStyleSheet()
  {
  }

  public String getStyleSheet()
  {
    if (mi_styleSheet == null)
    {
      final String indexText;
      StringBuilder cssBuilder;
      String chartCss;

      indexText = "${index}";
      cssBuilder = new StringBuilder("data:text/css,");
      cssBuilder.append("""

          .root {
            -fx-chart-color-0:  #f9d900; // Default gold
            -fx-chart-color-1:  #a9e200; // Default lime green
            -fx-chart-color-2:  #22bad9; // Default sky blue
            -fx-chart-color-3:  #0181e2; // Default azure
            -fx-chart-color-4:  #2f357f; // Default indigo
            -fx-chart-color-5:  #860061; // Default purple
            -fx-chart-color-6:  #c62b00; // Default rust
            -fx-chart-color-7:  #ff5700; // Default orange
            -fx-chart-color-8:  #aea300;
            -fx-chart-color-9:  #8b1d00;
            -fx-chart-color-10: #f9b800;
            -fx-chart-color-11: #188c98;
            -fx-chart-color-12: #1e1ac6;
            -fx-chart-color-13: #e22d00;
            -fx-chart-color-14: #7f9e00;
            -fx-chart-color-15: #b34900;
            -fx-chart-color-16: #d9b822;
            -fx-chart-color-17: #c60051;
            -fx-chart-color-18: #865f00;
            -fx-chart-color-19: #5e4300;
          }

          """);

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

      mi_styleSheet = cssBuilder.toString();
    }

    return mi_styleSheet;
  }
}