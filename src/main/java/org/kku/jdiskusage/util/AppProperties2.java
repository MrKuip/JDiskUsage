package org.kku.jdiskusage.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Properties;
import org.kku.jdiskusage.util.Converters.Converter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public abstract class AppProperties2
{
  private final PropertyStore m_propertyStore;

  protected AppProperties2(String propertyFileName)
  {
    m_propertyStore = new PropertyStore(propertyFileName);
  }

  protected PropertyStore getStore()
  {
    return m_propertyStore;
  }

  protected <T> AppPropertyType<T> createType(String name, Converter<T> converter)
  {
    return new AppPropertyType<>(name, converter);
  }

  protected <T> AppPropertyType<T> createListType(String name, Converter<T> converter, int listSize)
  {
    return new AppPropertyType<>(name, converter, listSize);
  }

  public interface AppPropertyTypeIF<T>
  {
  }

  public class AppPropertyType<T>
      implements AppPropertyTypeIF<T>
  {
    private final String mi_name;
    private final Converter<T> mi_converter;
    private final int mi_listSize;

    private AppPropertyType(String name, Converter<T> converter)
    {
      this(name, converter, -1);
    }

    private AppPropertyType(String name, Converter<T> converter, int listSize)
    {
      mi_name = name;
      mi_converter = converter;
      mi_listSize = listSize;
    }

    public AppPropertyIF<T> forSubject(Object subject)
    {
      return forSubject(subject, null);
    }

    public AppPropertyIF<T> forSubject(Object subject, T defaultValue)
    {
      Class<?> subjectClass;

      subjectClass = subject instanceof Class ? (Class<?>) subject : subject.getClass();
      return new AppProperty<>(this, subjectClass.getSimpleName(), defaultValue);
    }

    public AppProperty<T> forSubject(String subject)
    {
      return forSubject(subject, null);
    }

    public AppProperty<T> forSubject(String subject, T defaultValue)
    {
      return new AppProperty<>(this, subject, defaultValue);
    }

    public String getName()
    {
      return mi_name;
    }

    public Converter<T> getConverter()
    {
      return mi_converter;
    }
  }

  public interface AppPropertyIF<T>
  {
  }

  public class AppProperty<T>
      implements AppPropertyIF<T>
  {
    private final AppPropertyType<T> mi_type;
    private final String mi_subject;
    private final T mi_defaultValue;
    private final ObjectProperty<T> mi_property;

    public AppProperty(AppPropertyType<T> type, String subject, T defaultValue)
    {
      mi_type = type;
      mi_subject = subject;
      mi_defaultValue = defaultValue;
      mi_property = new SimpleObjectProperty<>(null, mi_type.getName());

      initProperty();
    }

    private void initProperty()
    {
      mi_property.set(mi_type.mi_converter.fromString(m_propertyStore.getPropertyValue(getPropertyName())));
      mi_property.addListener((c) -> {
        m_propertyStore.putProperty(getPropertyName(), mi_type.mi_converter.toString(mi_property.getValue()));
      });
    }

    public String getName()
    {
      return mi_type.getName();
    }

    private String getPropertyName()
    {
      return (mi_subject + "_" + getName()).toUpperCase().replace(' ', '_').replace('-', '_');
    }

    private String getPropertyNameList(int index)
    {
      return getPropertyName() + "_" + index;
    }

    public T get()
    {
      assert mi_defaultValue != null;
      return get(mi_defaultValue);
    }

    public T get(T defaultValue)
    {
      T value;

      assert defaultValue != null;
      value = property().get();
      if (value == null)
      {
        value = defaultValue;
      }

      return value;
    }

    public void reset()
    {
      property().set(null);
    }

    public void set(T value)
    {
      property().set(value);
    }

    public ObjectProperty<T> property()
    {
      if (mi_property == null)
      {
        return mi_property;
      }

      return mi_property;
    }
  }

  static class PropertyStore
  {
    private final String mi_fileName;
    private Properties mi_properties;

    public PropertyStore(String fileName)
    {
      mi_fileName = fileName;
    }

    public void putProperty(String propertyName, String stringValue)
    {
      getProperties().put(propertyName, stringValue);
      save();
    }

    public void removeProperty(String propertyName)
    {
      getProperties().remove(propertyName);
      save();
    }

    private String getPropertyValue(String propertyKey)
    {
      return (String) getProperties().get(propertyKey);
    }

    private Properties getProperties()
    {
      load();
      return mi_properties;
    }

    private Properties load()
    {
      if (mi_properties == null)
      {
        mi_properties = new Properties();

        Log.log.debug("Load properties from %s", getFilePath());
        try (InputStream is = Files.newInputStream(getFilePath()))
        {
          mi_properties.load(is);
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
      Log.log.debug("Save properties to %s", getFilePath());
      try (OutputStream os = Files.newOutputStream(getFilePath()))
      {
        getProperties().store(os, "store to properties file");
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }

    public Path getFilePath()
    {
      return Path.of(System.getProperty("user.home"), mi_fileName);
    }

    void clear() throws IOException
    {
      Files.delete(getFilePath());
      mi_properties = null;
    }
  }
}
