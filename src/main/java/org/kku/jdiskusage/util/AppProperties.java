package org.kku.jdiskusage.util;

import java.nio.file.Path;

public class AppProperties
  extends AppSettings
{
  private final static AppProperties m_instance = new AppProperties();

  public final static AppSettingType<Path> INITIAL_DIRECTORY;
  public final static AppSettingType<RecentScanList> RECENT_SCANS;
  public final static AppSettingType<DirectoryList> FAVORITE_DIRECTORIES;
  public final static AppSettingType<Double> WIDTH;
  public final static AppSettingType<Double> HEIGHT;
  public final static AppSettingType<Double> X;
  public final static AppSettingType<Double> Y;
  public final static AppSettingType<Double> SPLIT_PANE_POSITION;
  public final static AppSettingType<Double> PREF_SIZE;
  public final static AppSettingType<String> SELECTED_ID;

  static
  {
    INITIAL_DIRECTORY = m_instance.createAppSettingType("INITIAL_DIRECTORY", Converters.getPathConverter());
    RECENT_SCANS = m_instance.createAppSettingType("RECENT_SCANS", RecentScanList.getConverter());
    FAVORITE_DIRECTORIES = m_instance.createAppSettingType("FAVORITE_DIRECTORIES", DirectoryList.getConverter());
    WIDTH = m_instance.createAppSettingType("WIDTH", Converters.getDoubleConverter());
    HEIGHT = m_instance.createAppSettingType("HEIGHT", Converters.getDoubleConverter());
    X = m_instance.createAppSettingType("X", Converters.getDoubleConverter());
    Y = m_instance.createAppSettingType("Y", Converters.getDoubleConverter());
    SPLIT_PANE_POSITION = m_instance.createAppSettingType("SPLIT_PANE_POSITION", Converters.getDoubleConverter());
    PREF_SIZE = m_instance.createAppSettingType("PREF_SIZE", Converters.getDoubleConverter());
    SELECTED_ID = m_instance.createAppSettingType("SELECTED_ID", Converters.getStringConverter());
  }

  private AppProperties()
  {
    super("JDiskUsage.properties");
  }

  public static AppProperties getInstance()
  {
    return m_instance;
  }
}
