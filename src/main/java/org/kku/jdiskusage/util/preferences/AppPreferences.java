package org.kku.jdiskusage.util.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.prefs.Preferences;
import java.util.stream.Stream;
import javafx.beans.property.SimpleObjectProperty;

public class AppPreferences
{
  private final static List<AppPreference<?>> m_preferencesList = new ArrayList<>();
  private final static PersistentPreferences persistentPreferences = new PersistentPreferences();

  public final static AppPreference<SizeSystem> sizeSystemPreference;
  public final static AppPreference<DisplayMetric> displayMetricPreference;
  public final static AppPreference<Sort> sortPreference;
  public final static AppPreference<Locale> localePreference;
  public final static AppPreference<Integer> searchMaxCountPreference;
  public final static AppPreference<Integer> searchMaxTimePreference;
  public final static AppPreference<Boolean> searchRegexPreference;

  static
  {
    sizeSystemPreference = new AppPreference<>("Size system", SizeSystem.BINARY);
    displayMetricPreference = new AppPreference<>("Display metric", DisplayMetric.FILE_SIZE);
    sortPreference = new AppPreference<>("Sort", Sort.NUMERIC);
    localePreference = new AppPreference<>("Locale", new Locale("nl"), AppPreferences::convertStringToLocale,
        AppPreferences::convertLocaleToString);
    searchMaxCountPreference = new AppPreference<>("Max count", Integer.valueOf(100),
        AppPreferences::convertStringToInteger, AppPreferences::convertIntegerToString);
    searchMaxTimePreference = new AppPreference<>("Max time", Integer.valueOf(10),
        AppPreferences::convertStringToInteger, AppPreferences::convertIntegerToString);
    searchRegexPreference = new AppPreference<>("Use regex search", Boolean.FALSE);
  }

  public static class AppPreference<T>
    extends SimpleObjectProperty<T>
  {
    private final T m_initialValue;
    private final BiFunction<AppPreference<T>, String, T> m_loader;
    private final BiFunction<AppPreference<T>, T, String> m_saver;

    private AppPreference(String name, T initialValue)
    {
      this(name, initialValue, null, null);
    }

    private AppPreference(String name, T initialValue, BiFunction<AppPreference<T>, String, T> loader,
        BiFunction<AppPreference<T>, T, String> saver)
    {
      super(null, name, initialValue);

      m_initialValue = initialValue;
      m_loader = loader == null ? persistentPreferences.getDefaultLoader(m_initialValue) : loader;
      m_saver = saver == null ? persistentPreferences.getDefaultSaver(m_initialValue) : saver;

      persistentPreferences.load(this);
      AppPreferences.m_preferencesList.add(this);

      addListener(change -> { persistentPreferences.save(this); });
    }

    public T getInitialValue()
    {
      return m_initialValue;
    }

    public void reset()
    {
      set(m_initialValue);
    }

    private void load(String stringValue)
    {
      if (stringValue != null)
      {
        set(m_loader.apply(this, stringValue));
      }
    }

    private String save()
    {
      return m_saver.apply(this, get());
    }
  }

  public static class PersistentPreferences
  {
    private final Preferences mi_preferences;

    public PersistentPreferences()
    {
      mi_preferences = Preferences.userRoot().node(this.getClass().getName());
    }

    private <T> BiFunction<AppPreference<T>, String, T> getDefaultLoader(T initialValue)
    {
      if (initialValue.getClass().isEnum())
      {
        return AppPreferences::convertStringToEnum;
      }

      if (initialValue.getClass().equals(Locale.class))
      {
        return AppPreferences::convertStringToLocale;
      }

      return null;
    }

    private <T> BiFunction<AppPreference<T>, T, String> getDefaultSaver(T intialValue)
    {
      return AppPreferences::convertObjectToString;
    }

    public <T> void load(AppPreference<T> preference)
    {
      preference.load(mi_preferences.get(preference.getName(), null));
    }

    public <T> void save(AppPreference<T> preference)
    {
      mi_preferences.put(preference.getName(), preference.save());
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T convertStringToEnum(AppPreference<T> preference, String stringValue)
  {
    Class<?> clazz;

    clazz = preference.getInitialValue().getClass();
    if (clazz.isEnum())
    {
      Optional<?> result;

      result = Stream.of(clazz.getEnumConstants()).map(Enum.class::cast).filter(e -> e.name().equals(stringValue))
          .findFirst();

      if (result.isPresent())
      {
        return (T) result.get();
      }
    }

    return null;
  }

  public static String convertLocaleToString(AppPreference<Locale> preference, Locale value)
  {
    return value.toLanguageTag();
  }

  @SuppressWarnings("unchecked")
  public static <T> T convertStringToLocale(AppPreference<T> preference, String stringValue)
  {
    return (T) Locale.forLanguageTag(stringValue);
  }

  public static String convertIntegerToString(AppPreference<Integer> preference, Integer value)
  {
    return value.toString();
  }

  public static Integer convertStringToInteger(AppPreference<Integer> preference, String stringValue)
  {
    return Integer.valueOf(stringValue);
  }

  public static String convertBooleanToString(AppPreference<Boolean> preference, Boolean value)
  {
    return value.toString();
  }

  public static Boolean convertStringToBoolean(AppPreference<Boolean> preference, String stringValue)
  {
    return Boolean.valueOf(stringValue);
  }

  public static <T> String convertObjectToString(AppPreference<T> preference, T value)
  {
    return value.toString();
  }

}
