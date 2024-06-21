package org.kku.jdiskusage.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kku.jdiskusage.util.DirectoryChooser.PathList;

import javafx.beans.value.ChangeListener;

public class AppProperties
{
  private final static int MAX_SIZE_LIST = 20;
  private final static AppProperties m_instance = new AppProperties();
  private final static String APP_PROPERTIES_FILE_NAME = "JDiskUsage.properties";

  private Properties m_properties;

  private AppProperties()
  {
  }

  public static AppProperties getInstance()
  {
    return m_instance;
  }

  public Props getProps(String subject)
  {
    return new Props(subject);
  }

  public class Props
  {
    private final String mi_subject;

    private Props(String subject)
    {
      mi_subject = subject;
    }

    public void set(CharSequence propertyName, Path path)
    {
      setPropertyValue(propertyName, path != null ? path.toString() : "");
    }

    public void setPathLists(CharSequence propertyName, List<PathList> pathLists)
    {
      for (int index = 0; index < MAX_SIZE_LIST; index++)
      {
        String propertyKey;

        propertyKey = propertyName + "." + index;
        if (index < pathLists.size())
        {
          String propertyValue;

          propertyValue = pathLists.get(index).getPathList().stream().map(Path::toString)
              .collect(Collectors.joining(","));

          setPropertyValue(propertyKey, propertyValue);
        }
        else
        {
          removePropertyValue(propertyKey);
        }
      }
    }

    public void set(CharSequence propertyName, Number value)
    {
      setPropertyValue(propertyName, value.toString());
    }

    public void set(CharSequence propertyName, String value)
    {
      setPropertyValue(propertyName, value);
    }

    private void setPropertyValue(CharSequence propertyName, String propertyValue)
    {
      getProperties().setProperty(getPropertyName(propertyName), propertyValue);
      storeProperties();
    }

    private void removePropertyValue(CharSequence propertyName)
    {
      getProperties().remove(getPropertyName(propertyName));
      storeProperties();
    }

    public Path getPath(CharSequence propertyName)
    {
      Path path;
      String pathName;

      pathName = getPropertyValue(propertyName);
      if (pathName == null)
      {
        return null;
      }

      path = Path.of(pathName);
      return Files.exists(path) ? path : null;
    }

    public List<PathList> getPathLists(CharSequence propertyName)
    {
      String pathListString;
      List<PathList> result;

      result = new ArrayList<>();

      for (int index = 0; index < MAX_SIZE_LIST; index++)
      {
        List<Path> fileList;

        pathListString = getPropertyValue(propertyName + "." + index);
        if (pathListString != null)
        {
          fileList = Stream.of(pathListString.split(",")).map(fileName -> Path.of(fileName))
              .filter(file -> Files.exists(file)).collect(Collectors.toList());
          fileList = Stream.of(pathListString.split(",")).map(fileName -> Path.of(fileName))
              .collect(Collectors.toList());

          result.add(PathList.of(fileList));
        }
      }

      return result;
    }

    public double getDouble(CharSequence propertyName, double defaultValue)
    {
      return getPropertyValue(propertyName) == null ? defaultValue : Double.valueOf(getPropertyValue(propertyName));
    }

    public Integer getInteger(CharSequence propertyName, Integer defaultValue)
    {
      return getPropertyValue(propertyName) == null ? defaultValue : Integer.valueOf(getPropertyValue(propertyName));
    }

    public Long getLong(CharSequence propertyName, Long defaultValue)
    {
      return getPropertyValue(propertyName) == null ? defaultValue : Long.valueOf(getPropertyValue(propertyName));
    }

    public String getString(CharSequence propertyName, String defaultValue)
    {
      return getPropertyValue(propertyName) == null ? defaultValue : getPropertyValue(propertyName);
    }

    private String getPropertyValue(CharSequence propertyName)
    {
      return (String) getProperties().get(getPropertyName(propertyName));
    }

    private String getPropertyName(CharSequence propertyName)
    {
      return (mi_subject + propertyName).toUpperCase().replace(' ', '_').replace('-', '_');
    }

    public ChangeListener<Number> getChangeListener(CharSequence propertyName)
    {
      return (observable, oldValue, newValue) -> {
        set(propertyName, newValue);
      };
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

  private Path getPropertyPath()
  {
    return Path.of(System.getProperty("user.home"), APP_PROPERTIES_FILE_NAME);
  }
}
