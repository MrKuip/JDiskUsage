package org.kku.jdiskusage.util;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;

public class Translator
  extends SimpleMapProperty<String, Object>
{
  private static Translator m_instance = new Translator();

  private String bundleName = "language"; // a file language.properties must be present at the root of your classpath

  private Translator()
  {
    super(FXCollections.observableHashMap());
    reload();
  }

  public static Translator getInstance()
  {
    return m_instance;
  }

  public void changeLocale(Locale newLocale)
  {
    Locale.setDefault(newLocale);
    reload();
  }

  public static StringBinding translatedTextProperty(String text)
  {
    String resourceKey;
    ObjectBinding<Object> binding;

    resourceKey = toResourceKey(text);
    binding = Bindings.valueAt(getInstance(), resourceKey);

    // if the text to be translated is not in the resourcebundle the text itself is
    // returned
    return Bindings.when(binding.isNull()).then(text).otherwise(binding.asString());
  }

  private void reload()
  {
    ResourceBundle bundle;

    clear();

    bundle = ResourceBundle.getBundle(bundleName);
    if (bundle != null)
    {
      bundle.keySet().forEach(key -> {
        String resourceKey = toResourceKey(key);
        String value = bundle.getString(key);

        System.out.println(resourceKey + " -> " + value);
        put(resourceKey, value);
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
