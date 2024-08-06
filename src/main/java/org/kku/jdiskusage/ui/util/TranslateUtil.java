package org.kku.jdiskusage.ui.util;

import org.kku.jdiskusage.util.Translator;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.Tooltip;

public class TranslateUtil
{
  private TranslateUtil()
  {
  }

  public static String translate(String text)
  {
    return Translator.getTranslatedText(text);
  }

  public static <T extends Labeled> T translate(T node)
  {
    bind(node.textProperty());
    return node;
  }

  public static <S, R, T extends TableColumnBase<S, R>> T translate(T node)
  {
    bind(node.textProperty());
    return node;
  }

  public static <T extends MenuItem> T translate(T node)
  {
    bind(node.textProperty());
    return node;
  }

  public static Tooltip translate(Tooltip node)
  {
    bind(node.textProperty());
    return node;
  }

  public static <T extends Tab> T translate(T node)
  {
    bind(node.textProperty());
    return node;
  }

  public static void bind(StringProperty textProperty)
  {
    textProperty.bind(translatedTextProperty(textProperty));
  }

  public static StringProperty translatedTextProperty(StringProperty textProperty)
  {
    return translatedTextProperty(textProperty.get());
  }

  public static StringProperty translatedTextProperty(String text)
  {
    return Translator.translatedTextProperty(text);
  }
}
