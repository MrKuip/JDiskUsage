package org.kku.jdiskusage.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.kku.jdiskusage.util.Converters.Converter;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

public abstract class AppSettings
{
  private final PropertyStore m_propertyStore;

  protected AppSettings(String propertyFileName)
  {
    m_propertyStore = new PropertyStore(propertyFileName);
  }

  protected PropertyStore getStore()
  {
    return m_propertyStore;
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
    private final ObjectProperty<T> mi_property;

    public AppSetting(AppSettingType<T> type, String subject, T defaultValue)
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

    public void addListener(ChangeListener<T> listener)
    {
      property().addListener(listener);
    }

    public ObjectProperty<T> property()
    {
      if (mi_property == null)
      {
        return mi_property;
      }

      return mi_property;
    }

    /*
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
  }

  static class PropertyStore
  {
    private final String mi_fileName;
    private Properties mi_properties;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> mi_scheduledFuture;

    public PropertyStore(String fileName)
    {
      mi_fileName = fileName;
    }

    public void putProperty(String propertyName, String stringValue)
    {
      Log.log.debug("Mark properties[%s] dirty to %s because property %s changed from %s to %s", getFilePath(),
          propertyName, getProperties().get(propertyName), stringValue);
      getProperties().put(propertyName, stringValue);
      markDirty();
    }

    public void removeProperty(String propertyName)
    {
      Log.log.debug("Mark properties[%s] dirty because property %s is removed", getFilePath(), propertyName);
      getProperties().remove(propertyName);
      markDirty();
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

    private void markDirty()
    {
      if (mi_scheduledFuture != null)
      {
        mi_scheduledFuture.cancel(false);
      }
      mi_scheduledFuture = scheduler.schedule(this::save, 1, TimeUnit.SECONDS);
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
