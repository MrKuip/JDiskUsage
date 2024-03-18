package org.kku.jdiskusage.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public enum DiskUsageProperties
{
  INITIAL_DIRECTORY;

  private static Properties m_properties;

  public void set(File directory)
  {
    String path;

    path = directory != null ? directory.getPath() : "";
    getProperties().setProperty(this.name(), path);
    storeProperties();
  }

  public File getFile()
  {
    File file;
    String fileName;

    fileName = (String) getProperties().get(this.name());
    if (fileName == null)
    {
      return null;
    }

    file = new File(fileName);
    return file.exists() ? file : null;
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
}
