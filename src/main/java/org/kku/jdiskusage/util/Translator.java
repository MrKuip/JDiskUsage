package org.kku.jdiskusage.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.kku.jdiskusage.conf.Language;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Translator
{
  // Singleton
  private static Translator m_instance = new Translator();

  private Map<String, String> m_translationByIdMap = new HashMap<>();
  private Map<String, StringProperty> m_translationPropertyByIdMap = new HashMap<>();
  private ObjectProperty<Language> m_languageProperty = new SimpleObjectProperty<>();

  private String bundleName = "/translations/messages";

  private Translator()
  {
    m_languageProperty.addListener((o, oldValue, newValue) -> changeLanguage(newValue));
    m_languageProperty.bind(AppPreferences.languagePreference.property());
  }

  private void changeLanguage(Language language)
  {
    Locale.setDefault(language.getLocale());
    reload();
  }

  public static StringProperty translatedTextProperty(String text)
  {
    StringProperty stringProperty;

    stringProperty = m_instance.m_translationPropertyByIdMap.computeIfAbsent(text, (k) -> new SimpleStringProperty());
    stringProperty.set(getTranslatedText(text));

    return stringProperty;
  }

  public static String getTranslatedText(String text)
  {
    String translatedText;
    String resourceKey;

    resourceKey = toResourceKey(text);

    translatedText = m_instance.m_translationByIdMap.get(resourceKey);
    if (StringUtils.isEmpty(translatedText))
    {
      translatedText = text;
    }
    return translatedText;
  }

  private void reload()
  {
    m_translationByIdMap.clear();

    // First choice is translations from the current language setting.
    reload(m_languageProperty.get().getLocale());
    // Second choice is the translations in english.
    reload(Locale.ENGLISH);
    // Last choice is the untranslated key (as programmed in the software)

    m_translationPropertyByIdMap.entrySet().forEach(entry -> {
      entry.getValue().set(getTranslatedText(entry.getKey()));
    });
  }

  private void reload(Locale locale)
  {
    ResourceBundle bundle;

    bundle = ResourceBundle.getBundle(bundleName, locale);
    if (bundle != null)
    {
      bundle.keySet().forEach(key -> {
        String resourceKey;
        String value;

        resourceKey = toResourceKey(key);
        value = bundle.getString(key);

        m_translationByIdMap.putIfAbsent(resourceKey, value);
      });
    }
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
