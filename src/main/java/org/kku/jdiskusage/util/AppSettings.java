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
import javafx.beans.value.ChangeListener;

public abstract class AppSettings
{
  private String m_settingFileName;
  private Properties m_settings;

  protected AppSettings(String settingFileName)
  {
    m_settingFileName = settingFileName;
  }

  protected <T> AppSettingType<T> createAppSettingType(String name, Converter<T> converter)
  {
    return new AppSettingType<>(name, converter, -1);
  }

  protected <T> AppSettingType<T> createAppSettingType(String name, Converter<T> converter, int listSize)
  {
    return new AppSettingType<>(name, converter, listSize);
  }

  public class AppSettingType<T>
  {
    private final String mi_name;
    private final Converter<T> mi_converter;
    private final int mi_listSize;

    private AppSettingType(String name, Converter<T> converter, int listSize)
    {
      mi_name = name;
      mi_converter = converter;
      mi_listSize = listSize;
    }

    public AppSetting<T> forSubject(Object subject)
    {
      return forSubject(subject, null);
    }

    public AppSetting<T> forSubject(Object subject, T defaultValue)
    {
      return new AppSetting<>(this, subject.getClass().getSimpleName(), defaultValue);
    }

    public AppSetting<T> forSubject(String subject)
    {
      return forSubject(subject, null);
    }

    public AppSetting<T> forSubject(String subject, T defaultValue)
    {
      return new AppSetting<>(this, subject, defaultValue);
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

  public class AppSetting<T>
  {
    private final AppSettingType<T> mi_type;
    private final String mi_subject;
    private final T mi_defaultValue;
    private final WeakReferenceList<ChangeListener<T>> mi_changeListenerList = new WeakReferenceList<>();

    public AppSetting(AppSettingType<T> type, String subject, T defaultValue)
    {
      mi_type = type;
      mi_subject = subject;
      mi_defaultValue = defaultValue;
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

        value = getProperty(getSettingNameList(index), null);
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
          setProperty(getSettingNameList(index), value);
        }
        else
        {
          remove(getSettingNameList(index));
        }
      }
    }

    private String getSettingNameList(int index)
    {
      return getSettingName() + "_" + index;
    }

    public T get()
    {
      assert mi_defaultValue != null;
      return get(mi_defaultValue);
    }

    public T get(T defaultValue)
    {
      return getProperty(getSettingName(), defaultValue);
    }

    public T getProperty(String propertyName)
    {
      assert mi_defaultValue != null;
      return getProperty(propertyName, mi_defaultValue);
    }

    public T getProperty(String propertyName, T defaultValue)
    {
      String stringValue;

      stringValue = (String) getProperties().get(propertyName);
      if (stringValue != null)
      {
        return mi_type.getConverter().fromString(stringValue);
      }

      return defaultValue;
    }

    public void set(T value)
    {
      setProperty(getSettingName(), value);
    }

    private void setProperty(String propertyName, T value)
    {
      String stringValue;

      mi_changeListenerList.getElements().forEach(cl -> cl.changed(null, null, value));

      stringValue = mi_type.getConverter().toString(value);
      getProperties().put(propertyName, stringValue);
      storeProperties();
    }

    private void remove(String propertyName)
    {
      getProperties().remove(propertyName);
      storeProperties();
    }

    public void addListener(ChangeListener<T> listener)
    {
      mi_changeListenerList.add(listener);
    }

    /**
     * Get a changelistener that will set the value of this property
     * 
     * WATCH OUT: This changelistener cannot be parameterized because for instance a
     * double property expects a Changelistener<? extends Number> and NOT
     * ChangeListener<Double>. This won't even compile! The FX team decided on this
     * because of lots of additional code. Now we are left with the baked pears!
     * 
     * @return
     */
    @SuppressWarnings(
    {
        "unchecked", "rawtypes"
    })
    public ChangeListener getChangeListener()
    {
      return (observable, oldValue, newValue) -> {
        set((T) newValue);
      };
    }

    private String getSettingName()
    {
      return (mi_subject + "_" + getName()).toUpperCase().replace(' ', '_').replace('-', '_');
    }
  }

  private Properties getProperties()
  {
    if (m_settings == null)
    {
      m_settings = new Properties();

      try
      {
        m_settings.load(Files.newInputStream(getSettingPath()));
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

    return m_settings;
  }

  private void storeProperties()
  {
    try
    {
      m_settings.store(Files.newOutputStream(getSettingPath()), "store to properties file");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  protected Path getSettingPath()
  {
    return Path.of(System.getProperty("user.home"), m_settingFileName);
  }
}
