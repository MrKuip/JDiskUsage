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

  public final static AppProperty INITIAL_DIRECTORY = new AppProperty("ÏNITIAL_DIRECTORY", 10);
  public final static AppProperty RECENT_SCANS = new AppProperty("RECENT_SCANS");
  public final static AppProperty WIDTH = new AppProperty("WIDTH");
  public final static AppProperty HEIGHT = new AppProperty("HEIGHT");
  public final static AppProperty X = new AppProperty("X");
  public final static AppProperty Y = new AppProperty("Y");
  public final static AppProperty SPLIT_PANE_POSITION = new AppProperty("SPLIT_PANE_POSITION");
  public final static AppProperty PREF_SIZE = new AppProperty("PREF_SIZE");
  public final static AppProperty SELECTED_ID = new AppProperty("SELECTED_ID");

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

  public static class AppProperty
  {
    private final String mi_name;
    private final int mi_arrayLength;

    public AppProperty(String name, int arrayLength)
    {
      mi_name = name;
      mi_arrayLength = arrayLength;
    }

    public AppProperty(String name)
    {
      this(name, -1);
    }

    public String getName()
    {
      return mi_name;
    }

    public int getArrayLength()
    {
      return mi_arrayLength;
    }

    public AppProperty getIndexedProperty(int index)
    {
      return new AppProperty(mi_name + "." + index);
    }
  }

  public class Props
  {
    private final String mi_subject;

    private Props(String subject)
    {
      mi_subject = subject;
    }

    public void set(AppProperty property, Path path)
    {
      setPropertyValue(property, path != null ? path.toString() : "");
    }

    public void setPathLists(AppProperty property, List<PathList> pathLists)
    {
      for (int index = 0; index < MAX_SIZE_LIST; index++)
      {
        AppProperty indexedProperty;

        indexedProperty = property.getIndexedProperty(index);
        if (index < pathLists.size())
        {
          String propertyValue;

          propertyValue = pathLists.get(index).getPathList().stream().map(Path::toString)
              .collect(Collectors.joining(","));

          setPropertyValue(indexedProperty, propertyValue);
        }
        else
        {
          removePropertyValue(indexedProperty);
        }
      }
    }

    public void set(AppProperty property, Number value)
    {
      setPropertyValue(property, value.toString());
    }

    public void set(AppProperty property, String value)
    {
      setPropertyValue(property, value);
    }

    private void setPropertyValue(AppProperty property, String propertyValue)
    {
      getProperties().setProperty(getPropertyName(property), propertyValue);
      storeProperties();
    }

    private void removePropertyValue(AppProperty property)
    {
      getProperties().remove(getPropertyName(property));
      storeProperties();
    }

    public Path getPath(AppProperty property)
    {
      Path path;
      String pathName;

      pathName = getPropertyValue(property);
      if (pathName == null)
      {
        return null;
      }

      path = Path.of(pathName);
      return Files.exists(path) ? path : null;
    }

    public List<PathList> getPathLists(AppProperty property)
    {
      String pathListString;
      List<PathList> result;

      result = new ArrayList<>();

      for (int index = 0; index < MAX_SIZE_LIST; index++)
      {
        List<Path> fileList;

        pathListString = getPropertyValue(property.getIndexedProperty(index));
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

    public double getDouble(AppProperty property, double defaultValue)
    {
      return getPropertyValue(property) == null ? defaultValue : Double.valueOf(getPropertyValue(property));
    }

    public Integer getInteger(AppProperty property, Integer defaultValue)
    {
      return getPropertyValue(property) == null ? defaultValue : Integer.valueOf(getPropertyValue(property));
    }

    public Long getLong(AppProperty property, Long defaultValue)
    {
      return getPropertyValue(property) == null ? defaultValue : Long.valueOf(getPropertyValue(property));
    }

    public String getString(AppProperty property, String defaultValue)
    {
      return getPropertyValue(property) == null ? defaultValue : getPropertyValue(property);
    }

    private String getPropertyValue(AppProperty property)
    {
      return (String) getProperties().get(getPropertyName(property));
    }

    private String getPropertyName(AppProperty property)
    {
      return (mi_subject + property.getName()).toUpperCase().replace(' ', '_').replace('-', '_');
    }

    public ChangeListener<Number> getChangeListener(AppProperty property)
    {
      return (observable, oldValue, newValue) -> {
        set(property, newValue);
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
