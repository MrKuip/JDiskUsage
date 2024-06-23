package org.kku.jdiskusage.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.kku.jdiskusage.util.Converters.Converter;
import org.kku.jdiskusage.util.DirectoryChooser.PathList;

import javafx.beans.value.ChangeListener;

public class AppProperties
{
  private final static AppProperties m_instance = new AppProperties();

  public final static AppPropertyType<Path> INITIAL_DIRECTORY;
  public final static AppPropertyType<PathList> RECENT_SCANS;
  public final static AppPropertyType<Double> WIDTH;
  public final static AppPropertyType<Double> HEIGHT;
  public final static AppPropertyType<Double> X;
  public final static AppPropertyType<Double> Y;
  public final static AppPropertyType<Double> SPLIT_PANE_POSITION;
  public final static AppPropertyType<Double> PREF_SIZE;
  public final static AppPropertyType<String> SELECTED_ID;

  static
  {
    INITIAL_DIRECTORY = new AppPropertyType<>("INITIAL_DIRECTORY", Converters.getPathConverter());
    RECENT_SCANS = new AppPropertyType<>("RECENT_SCANS", Converters.getPathListConverter(), 10);
    WIDTH = new AppPropertyType<>("WIDTH", Converters.getDoubleConverter());
    HEIGHT = new AppPropertyType<>("HEIGHT", Converters.getDoubleConverter());
    X = new AppPropertyType<>("X", Converters.getDoubleConverter());
    Y = new AppPropertyType<>("Y", Converters.getDoubleConverter());
    SPLIT_PANE_POSITION = new AppPropertyType<>("SPLIT_PANE_POSITION", Converters.getDoubleConverter());
    PREF_SIZE = new AppPropertyType<>("PREF_SIZE", Converters.getDoubleConverter());
    SELECTED_ID = new AppPropertyType<>("SELECTED_ID", Converters.getStringConverter());
  }

  private String m_propertyFileName = "JDiskUsage2.properties";
  private Properties m_properties;

  private AppProperties()
  {
  }

  public static AppProperties getInstance()
  {
    return m_instance;
  }

  public void setPropertyFileName(String propertyFileName)
  {
    m_propertyFileName = propertyFileName;
  }

  public static class AppProperty<T>
  {
    private final AppPropertyType<T> mi_type;
    private final String mi_subject;

    public AppProperty(AppPropertyType<T> type, String subject)
    {
      mi_type = type;
      mi_subject = subject;
    }

    public String getName()
    {
      return mi_type.getName();
    }

    public List<T> getList()
    {
      List<T> list;

      list = new ArrayList<>();
      for (int index = 0; index < mi_type.getListSize(); index++)
      {
        T value;

        value = get(getPropertyNameList(index), null);
        if (value != null)
        {
          list.add(value);
        }
      }

      return list;
    }

    public void setList(List<T> list)
    {
      for (int index = 0; index < mi_type.getListSize(); index++)
      {
        T value;

        value = index < list.size() ? list.get(index) : null;
        if (value != null)
        {
          set(getPropertyNameList(index), value);
        }
        else
        {
          remove(getPropertyNameList(index));
        }
      }
    }

    private String getPropertyNameList(int index)
    {
      return getPropertyName() + "_" + index;
    }

    public T get(T defaultValue)
    {
      return get(getPropertyName(), defaultValue);
    }

    public T get(String propertyName, T defaultValue)
    {
      String stringValue;

      stringValue = (String) getInstance().getProperties().get(propertyName);
      if (stringValue != null)
      {
        return mi_type.getConverter().fromString(stringValue);
      }

      return defaultValue;
    }

    public void set(T value)
    {
      set(getPropertyName(), value);
    }

    private void set(String propertyName, T value)
    {
      String stringValue;

      stringValue = mi_type.getConverter().toString(value);
      getInstance().getProperties().put(propertyName, stringValue);
      getInstance().storeProperties();
    }

    private void remove(String propertyName)
    {
      getInstance().getProperties().remove(propertyName);
      getInstance().storeProperties();
    }

    public ChangeListener<T> getChangeListener()
    {
      return (observable, oldValue, newValue) -> {
        set(newValue);
      };
    }

    private String getPropertyName()
    {
      return (mi_subject + "_" + getName()).toUpperCase().replace(' ', '_').replace('-', '_');
    }
  }

  public static class AppPropertyType<T>
  {
    private final String mi_name;
    private final Converter<T> mi_converter;
    private final int mi_listSize;

    public AppPropertyType(String name, Converter<T> converter)
    {
      this(name, converter, -1);
    }

    public AppPropertyType(String name, Converter<T> converter, int listSize)
    {
      mi_name = name;
      mi_converter = converter;
      mi_listSize = listSize;
    }

    public AppProperty<T> forSubject(Object subject)
    {
      return new AppProperty<>(this, subject.getClass().getSimpleName());
    }

    public AppProperty<T> forSubject(String subject)
    {
      return new AppProperty<>(this, subject);
    }

    public String getName()
    {
      return mi_name;
    }

    public Converter<T> getConverter()
    {
      return mi_converter;
    }

    public int getListSize()
    {
      return mi_listSize;
    }
  }

  private Properties getProperties()
  {
    if (m_properties == null)
    {
      m_properties = new Properties();

      try
      {
        m_properties.load(Files.newInputStream(getPropertyPath()));
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

    return m_properties;
  }

  private void storeProperties()
  {
    try
    {
      m_properties.store(Files.newOutputStream(getPropertyPath()), "store to properties file");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public Path getPropertyPath()
  {
    return Path.of(System.getProperty("user.home"), m_propertyFileName);
  }
}
