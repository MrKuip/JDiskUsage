package org.kku.jdiskusage.util;

import org.kku.jdiskusage.conf.Language;
import org.kku.jdiskusage.conf.LanguageConfiguration;
import org.kku.jdiskusage.ui.util.ColorPalette;
import javafx.scene.paint.Color;

public class Converters
  extends org.kku.common.util.Converters
{
  public static Converter<Language> getLanguageConverter()
  {
    return new Converter<Language>((s) -> LanguageConfiguration.getInstance().getLanguageById(s), (e) -> e.getName());
  }

  public static Converter<Color> getColorConverter()
  {
    return new Converter<Color>(Color::web, ColorPalette::toHexString);
  }
}
