package org.kku.jdiskusage.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.value.ChangeListener;

public class ApplicationProperties
{
  private static ApplicationProperties m_instance = new ApplicationProperties();

  private Properties m_properties;

  private ApplicationProperties()
  {
  }

  public static ApplicationProperties getInstance()
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

    public void set(CharSequence propertyName, List<Path> pathList)
    {
      setPropertyValue(propertyName, pathList.stream().map(Path::toString).collect(Collectors.joining(",")));
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

    public List<Path> getPathList(CharSequence propertyName)
    {
      String paths;

      paths = getPropertyValue(propertyName);
      if (paths == null)
      {
        return Collections.emptyList();
      }

      return Stream.of(paths.split(",")).map(fileName -> Path.of(fileName)).filter(file -> Files.exists(file))
          .collect(Collectors.toList());
    }

    public double getDouble(CharSequence propertyName, double defaultValue)
    {
      return getPropertyValue(propertyName) == null ? defaultValue : Double.valueOf(getPropertyValue(propertyName));
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
    return Path.of(System.getProperty("user.home"), "JDiskUsage2.properties");
  }
}
