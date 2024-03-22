package org.kku.jdiskusage.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.value.ChangeListener;

public enum DiskUsageProperties
{
  INITIAL_DIRECTORY,
  RECENT_FILES,
  SCENE_WIDTH,
  SCENE_HEIGHT,
  SCENE_LOCATION_X,
  SCENE_LOCATION_Y,
  SPLIT_PANE_POSITION,
  TREE_TABLE_COLUMN1_SIZE,
  TREE_TABLE_COLUMN2_SIZE,
  TREE_TABLE_COLUMN3_SIZE;

  private static Properties m_properties;

  public void setFile(File directory)
  {
    setPropertyValue(directory != null ? directory.getPath() : "");
  }

  public File getFile()
  {
    File file;
    String fileName;

    fileName = getPropertyValue();
    if (fileName == null)
    {
      return null;
    }

    file = new File(fileName);
    return file.exists() ? file : null;
  }

  public List<File> getFileList()
  {
    String files;

    files = getPropertyValue();
    if (files == null)
    {
      return Collections.emptyList();
    }

    return Stream.of(files.split(",")).map(File::new).filter(File::exists).collect(Collectors.toList());
  }

  public void setFileList(List<File> fileList)
  {
    setPropertyValue(fileList.stream().map(File::getAbsolutePath).collect(Collectors.joining(",")));
  }

  public void setPropertyValue(Number value)
  {
    setPropertyValue(value.toString());
  }

  public double getDouble(double defaultValue)
  {
    return getPropertyValue() == null ? defaultValue : Double.valueOf(getPropertyValue());
  }

  private String getPropertyValue()
  {
    return (String) getProperties().get(this.name());
  }

  public void setPropertyValue(String value)
  {
    getProperties().setProperty(this.name(), value);
    storeProperties();
  }

  private Properties getProperties()
  {
    if (m_properties == null)
    {
      m_properties = new Properties();

      try
      {
        InputStream is;

        is = new FileInputStream(getPropertyFile());
        m_properties.load(is);
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
      m_properties.store(new FileOutputStream(getPropertyFile()), "store to properties file");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private File getPropertyFile()
  {
    return new File(System.getProperty("user.home"), "JDiskUsage.properties");
  }

  public ChangeListener<Number> getChangeListener()
  {
    return (observable, oldValue, newValue) ->
    {
      this.setPropertyValue(newValue);
    };
  }
}
