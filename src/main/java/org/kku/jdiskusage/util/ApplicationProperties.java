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

    public void setFile(CharSequence propertyName, File file)
    {
      setPropertyValue(propertyName, file != null ? file.getPath() : "");
    }

    public File getFile(CharSequence propertyName)
    {
      File file;
      String fileName;

      fileName = getPropertyValue(propertyName);
      if (fileName == null)
      {
        return null;
      }

      file = new File(fileName);
      return file.exists() ? file : null;
    }

    public List<File> getFileList(CharSequence propertyName)
    {
      String files;

      files = getPropertyValue(propertyName);
      if (files == null)
      {
        return Collections.emptyList();
      }

      return Stream.of(files.split(",")).map(File::new).filter(File::exists).collect(Collectors.toList());
    }

    public void setFileList(CharSequence propertyName, List<File> fileList)
    {
      setPropertyValue(propertyName, fileList.stream().map(File::getAbsolutePath).collect(Collectors.joining(",")));
    }

    public void setPropertyValue(CharSequence propertyName, Number value)
    {
      setPropertyValue(propertyName, value.toString());
    }

    public double getDouble(CharSequence propertyName, double defaultValue)
    {
      return getPropertyValue(propertyName) == null ? defaultValue : Double.valueOf(getPropertyValue(propertyName));
    }

    private String getPropertyValue(CharSequence propertyName)
    {
      return (String) getProperties().get(getPropertyName(propertyName));
    }

    public void setPropertyValue(CharSequence propertyName, String propertyValue)
    {
      getProperties().setProperty(getPropertyName(propertyName), propertyValue);
      storeProperties();
    }

    private String getPropertyName(CharSequence propertyName)
    {
      return (mi_subject + propertyName).toUpperCase().replace(' ', '_').replace('-', '_');
    }

    public ChangeListener<Number> getChangeListener(CharSequence propertyName)
    {
      return (observable, oldValue, newValue) ->
      {
        setPropertyValue(propertyName, newValue);
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
    return new File(System.getProperty("user.home"), "JDiskUsage2.properties");
  }
}
