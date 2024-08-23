package org.kku.jdiskusage.ui.util;

import javafx.scene.paint.Color;

public enum Colors
{
  CHART_COLOR_0("#f9d900"), // Default gold
  CHART_COLOR_1("#a9e200"), // Default lime green
  CHART_COLOR_2("#22bad9"), // Default sky blue
  CHART_COLOR_3("#0181e2"), // Default azure
  CHART_COLOR_4("#2f357f"), // Default indigo
  CHART_COLOR_5("#860061"), // Default purple
  CHART_COLOR_6("#c62b00"), // Default rust
  CHART_COLOR_7("#ff5700"), // Default orange
  CHART_COLOR_8("#aea300"),
  CHART_COLOR_9("#8b1d00"),
  CHART_COLOR_10("#f9b800"),
  CHART_COLOR_11("#188c98"),
  CHART_COLOR_12("#1e1ac6"),
  CHART_COLOR_13("#e22d00"),
  CHART_COLOR_14("#7f9e00"),
  CHART_COLOR_15("#b34900"),
  CHART_COLOR_16("#d9b822"),
  CHART_COLOR_17("#c60051"),
  CHART_COLOR_18("#865f00"),
  CHART_COLOR_19("#5e4300");

  private final String mi_webColor;
  private final Color mi_color;

  Colors(String webColor)
  {
    mi_webColor = webColor;
    mi_color = Color.web(webColor);
  }

  public String getWebColor()
  {
    return mi_webColor;
  }

  public Color getColor()
  {
    return mi_color;
  }

  public Color getColor(double newBrightness)
  {
    if (newBrightness < 0.0)
    {
      newBrightness = 0.0;
    }

    if (newBrightness > 1.0)
    {
      newBrightness = 1.0;
    }

    newBrightness = 0.20 + newBrightness * 0.8;

    return Color.hsb(getColor().getHue(), getColor().getSaturation(), newBrightness);
  }

  public String getBackgroundCss()
  {
    return "-fx-background-color: " + mi_webColor + ";";
  }

  public String getBackgroundCss(double newBrightness)
  {
    return "-fx-background-color: " + toHexString(getColor(newBrightness)) + ";";
  }

  public static String toHexString(Color color)
  {
    int r = (int) (color.getRed() * 255);
    int g = (int) (color.getGreen() * 255);
    int b = (int) (color.getBlue() * 255);
    int a = (int) (color.getOpacity() * 255);

    return String.format("#%02X%02X%02X%02X", r, g, b, a);
  }
}