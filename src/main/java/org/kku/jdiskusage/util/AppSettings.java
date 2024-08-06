package org.kku.jdiskusage.util;

import java.nio.file.Path;

public class AppSettings
  extends AppProperties
{
  private final static AppSettings m_instance = new AppSettings();

  public final static AppPropertyType<Path> INITIAL_DIRECTORY;
  public final static AppPropertyType<RecentScanList> RECENT_SCANS;
  public final static AppPropertyType<DirectoryList> FAVORITE_DIRECTORIES;
  public final static AppPropertyType<Double> WIDTH;
  public final static AppPropertyType<Double> HEIGHT;
  public final static AppPropertyType<Double> X;
  public final static AppPropertyType<Double> Y;
  public final static AppPropertyType<Double> SPLIT_PANE_POSITION;
  public final static AppPropertyType<Double> PREF_SIZE;
  public final static AppPropertyType<String> SELECTED_ID;

  static
  {
    INITIAL_DIRECTORY = m_instance.createAppPropertyType("INITIAL_DIRECTORY", Converters.getPathConverter());
    RECENT_SCANS = m_instance.createAppPropertyType("RECENT_SCANS", RecentScanList.getConverter());
    FAVORITE_DIRECTORIES = m_instance.createAppPropertyType("FAVORITE_DIRECTORIES", DirectoryList.getConverter());
    WIDTH = m_instance.createAppPropertyType("WIDTH", Converters.getDoubleConverter());
    HEIGHT = m_instance.createAppPropertyType("HEIGHT", Converters.getDoubleConverter());
    X = m_instance.createAppPropertyType("X", Converters.getDoubleConverter());
    Y = m_instance.createAppPropertyType("Y", Converters.getDoubleConverter());
    SPLIT_PANE_POSITION = m_instance.createAppPropertyType("SPLIT_PANE_POSITION", Converters.getDoubleConverter());
    PREF_SIZE = m_instance.createAppPropertyType("PREF_SIZE", Converters.getDoubleConverter());
    SELECTED_ID = m_instance.createAppPropertyType("SELECTED_ID", Converters.getStringConverter());
  }

  private AppSettings()
  {
    super("JDiskUsage.settings");
  }
}
