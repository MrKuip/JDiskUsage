package org.kku.jdiskusage.ui.util;

import java.util.ArrayList;
import java.util.List;
import org.kku.jdiskusage.util.AppProperties.AppProperty;
import org.kku.jdiskusage.util.Converters;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

public class ColorPalette
{
  private static List<ChartColor> m_chartColorList = new ArrayList<>();
  public static final ChartColor m_chartColor0 = new ChartColor("#f9d900"); // Default gold
  public static final ChartColor m_chartColor1 = new ChartColor("#a9e200"); // Default lime green
  public static final ChartColor m_chartColor2 = new ChartColor("#22bad9"); // Default sky blue
  public static final ChartColor m_chartColor3 = new ChartColor("#0181e2"); // Default azure
  public static final ChartColor m_chartColor4 = new ChartColor("#2f357f"); // Default indigo
  public static final ChartColor m_chartColor5 = new ChartColor("#860061"); // Default purple
  public static final ChartColor m_chartColor6 = new ChartColor("#c62b00"); // Default rust
  public static final ChartColor m_chartColor7 = new ChartColor("#ff5700"); // Default orange
  public static final ChartColor m_chartColor8 = new ChartColor("#aea300");
  public static final ChartColor m_chartColor9 = new ChartColor("#8b1d00");
  public static final ChartColor m_chartColor10 = new ChartColor("#f9b800");
  public static final ChartColor m_chartColor11 = new ChartColor("#188c98");
  public static final ChartColor m_chartColor12 = new ChartColor("#1e1ac6");
  public static final ChartColor m_chartColor13 = new ChartColor("#e22d00");
  public static final ChartColor m_chartColor14 = new ChartColor("#7f9e00");
  public static final ChartColor m_chartColor15 = new ChartColor("#b34900");
  public static final ChartColor m_chartColor16 = new ChartColor("#d9b822");
  public static final ChartColor m_chartColor17 = new ChartColor("#c60051");
  public static final ChartColor m_chartColor18 = new ChartColor("#865f00");
  public static final ChartColor m_chartColor19 = new ChartColor("#5e4300");

  private ColorPalette()
  {
  }

  public static List<ChartColor> getColorList()
  {
    return m_chartColorList;
  }

  static public class ChartColor
  {
    private final String mi_variableName;
    private final AppProperty<Color> mi_preference;
    private final StringProperty mi_backgroundCssProperty = new SimpleStringProperty();
    private String mi_webColor;

    private ChartColor(String defaultColor)
    {
      int index;

      index = m_chartColorList.size();

      mi_variableName = "CHART_COLOR_" + index;
      mi_preference = AppPreferences.createPreference("color" + index, Converters.getColorConverter(),
          Color.web(defaultColor));

      mi_preference.addListener((obs, oldValue, newValue) -> {
        ChartStyleSheet.getInstance().refresh();
      });

      mi_backgroundCssProperty.set(getBackgroundCss());

      m_chartColorList.add(this);
    }

    public String getVariableName()
    {
      return mi_variableName;
    }

    public Color getColor()
    {
      return mi_preference.get();
    }

    public ObservableValue<Color> colorProperty()
    {
      return mi_preference.property();
    }

    public ObservableValue<Color> colorProperty(double brightness)
    {
      return colorProperty().map(color -> getColor(brightness));
    }

    public Color getColor(double brightness)
    {
      if (brightness < 0.0)
      {
        brightness = 0.0;
      }

      if (brightness > 1.0)
      {
        brightness = 1.0;
      }

      brightness = 0.20 + brightness * 0.8;

      return Color.hsb(getColor().getHue(), getColor().getSaturation(), brightness);
    }

    public String getWebColor()
    {
      if (mi_webColor == null)
      {
        mi_webColor = toHexString(getColor());
        mi_backgroundCssProperty.set(getBackgroundCss());
      }

      return mi_webColor;
    }

    public StringProperty backgroundCssProperty()
    {
      return mi_backgroundCssProperty;
    }

    public String getBackgroundCss()
    {
      return "-fx-background-color: " + getWebColor() + ";";
    }

    public String getBackgroundCss(double newBrightness)
    {
      return "-fx-background-color: " + toHexString(getColor(newBrightness)) + ";";
    }

    public void reset()
    {
      mi_webColor = null;
      mi_preference.reset();
    }

    public void setColor(Color color)
    {
      mi_webColor = null;
      mi_preference.set(color);
    }

  }

  public static String toHexString(Color color)
  {
    if (color == null)
    {
      return null;
    }

    return String.format("#%02X%02X%02X%02X", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
        (int) (color.getBlue() * 255), (int) (color.getOpacity() * 255));
  }

}
