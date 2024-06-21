package org.kku.jdiskusage.util.properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import javafx.beans.property.SimpleObjectProperty;

public abstract class AppProperties
{
  private final String m_propertyFileName;
  private final PersistentPropertyFile m_persistentPropertFile = new PersistentPropertyFile();

  protected AppProperties(String propertyFileName)
  {
    m_propertyFileName = propertyFileName;
  }

  public class AppProperty<T> extends SimpleObjectProperty<T>
  {
    private final String m_owner;
    private final T m_initialValue;
    private final BiFunction<AppProperty<T>, String, T> m_convertFromString;
    private final BiFunction<AppProperty<T>, T, String> m_convertToString;

    public AppProperty(String owner, String name, T initialValue)
    {
      this(owner, name, initialValue, null, null);
    }

    public AppProperty(String owner, String name, T initialValue, BiFunction<AppProperty<T>, String, T> loader,
        BiFunction<AppProperty<T>, T, String> saver)
    {
      super(null, name, initialValue);

      m_owner = owner;
      m_initialValue = initialValue;
      m_convertFromString = loader;
      m_convertToString = saver;

      addListener(change -> {
        save();
      });
    }

    public T getInitialValue()
    {
      return m_initialValue;
    }

    public void reset()
    {
      set(m_initialValue);
    }

    private void load()
    {
      set(m_convertFromString.apply(this, m_persistentPropertFile.getProperties().getProperty(getKey())));
    }

    private void save()
    {
      m_persistentPropertFile.getProperties().setProperty(getKey(), m_convertToString.apply(this, get()));
    }

    private String getKey()
    {
      return (m_owner + "." + getName()).toUpperCase().replace(' ', '_').replace('-', '_');
    }
  }

  public class PersistentPropertyFile
  {
    private Properties mi_properties;

    public PersistentPropertyFile()
    {
    }

    public Properties getProperties()
    {
      return load();
    }

    private Properties load()
    {
      if (mi_properties == null)
      {
        mi_properties = new Properties();

        try
        {
          mi_properties.load(Files.newInputStream(getPropertyPath()));
        }
        catch (FileNotFoundException e)
        {
          // This can happen! First time the application is started.
        }
        catch (NoSuchFileException e)
        {
          // This can happen! First time the application is started.
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }

      return mi_properties;
    }

    private void save()
    {
      try
      {
        mi_properties.store(Files.newOutputStream(getPropertyPath()), "store to properties file");
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }

    private Path getPropertyPath()
    {
      return Path.of(System.getProperty("user.home"), m_propertyFileName);
    }
  }

  public static <T> T convertStringToEnum(AppProperty<T> preference, String stringValue)
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

  public static String convertLocaleToString(AppProperty<Locale> preference, Locale value)
  {
    return value.toLanguageTag();
  }

  public static <T> T convertStringToLocale(AppProperty<T> preference, String stringValue)
  {
    return (T) Locale.forLanguageTag(stringValue);
  }

  public static String convertIntegerToString(AppProperty<Integer> preference, Integer value)
  {
    return value.toString();
  }

  public static Integer convertStringToInteger(AppProperty<Integer> preference, String stringValue)
  {
    return Integer.valueOf(stringValue);
  }

  public static String convertBooleanToString(AppProperty<Boolean> preference, Boolean value)
  {
    return value.toString();
  }

  public static Boolean convertStringToBoolean(AppProperty<Boolean> preference, String stringValue)
  {
    return Boolean.valueOf(stringValue);
  }

  public static <T> String convertObjectToString(AppProperty<T> preference, T value)
  {
    return value.toString();
  }

}
