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
import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;

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
    return new AppSettingType<>(name, converter);
  }

  public class AppSettingType<T>
  {
    private final String mi_name;
    private final Converter<T> mi_converter;

    private AppSettingType(String name, Converter<T> converter)
    {
      mi_name = name;
      mi_converter = converter;
    }

    public AppSetting<T> forSubject(Object subject)
    {
      return forSubject(subject, null);
    }

    public AppSetting<T> forSubject(Object subject, T defaultValue)
    {
      Class<?> subjectClass;

      subjectClass = subject instanceof Class ? (Class<?>) subject : subject.getClass();
      return new AppSetting<>(this, subjectClass.getSimpleName(), defaultValue);
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

    public void reset()
    {
      set(null);
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

      try (InputStream is = Files.newInputStream(getSettingPath()))
      {
        m_settings.load(is);
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

  public void storeProperties()
  {
    try (OutputStream os = Files.newOutputStream(getSettingPath()))
    {
      m_settings.store(os, "store to properties file");
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

  public static void bind(AppSetting<Boolean> autocollapsetreenode2, CheckBox autoExpandCheckBox)
  {
  }
}
