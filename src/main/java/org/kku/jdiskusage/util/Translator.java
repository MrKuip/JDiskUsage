package org.kku.jdiskusage.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.kku.jdiskusage.util.LanguageList.Language;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Translator
{
  private static Translator m_instance = new Translator();

  private Map<String, String> mi_translationByIdMap = new HashMap<>();
  private Map<String, StringProperty> mi_translationPropertyByIdMap = new HashMap<>();

  private String bundleName = "/translations/messages";

  private Translator()
  {
    reload();
  }

  public static Translator getInstance()
  {
    return m_instance;
  }

  public void changeLanguage(Language language)
  {
    Locale.setDefault(language.getLocale());
    reload();
  }

  public static StringProperty translatedTextProperty(String text)
  {
    StringProperty stringProperty;

    stringProperty = m_instance.mi_translationPropertyByIdMap.computeIfAbsent(text, (k) -> new SimpleStringProperty());
    stringProperty.set(getTranslatedText(text));

    return stringProperty;
  }

  public static String getTranslatedText(String text)
  {
    String translatedText;
    String resourceKey;

    resourceKey = toResourceKey(text);

    translatedText = m_instance.mi_translationByIdMap.get(resourceKey);
    if (StringUtils.isEmpty(translatedText))
    {
      translatedText = text;
    }
    return translatedText;
  }

  private void reload()
  {
    ResourceBundle bundle;

    mi_translationByIdMap.clear();

    bundle = ResourceBundle.getBundle(bundleName);
    if (bundle != null)
    {
      bundle.keySet().forEach(key -> {
        String resourceKey;
        String value;

        resourceKey = toResourceKey(key);
        value = bundle.getString(key);

        mi_translationByIdMap.put(resourceKey, value);
      });
    }

    mi_translationPropertyByIdMap.entrySet().forEach(entry -> {
      entry.getValue().set(getTranslatedText(entry.getKey()));
    });
  }

  private static String toResourceKey(String text)
  {
    if (text == null)
    {
      return "";
    }

    return text.toLowerCase().replace(' ', '-').replace('\n', '-');
  }
}
