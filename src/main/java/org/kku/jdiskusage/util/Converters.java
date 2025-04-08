package org.kku.jdiskusage.util;

import org.kku.jdiskusage.ui.util.ColorPalette;
import javafx.scene.paint.Color;

public class Converters
  extends org.kku.common.util.Converters
{
  public static Converter<Color> getColorConverter()
  {
    return new Converter<Color>(Color::web, ColorPalette::toHexString);
  }
}
