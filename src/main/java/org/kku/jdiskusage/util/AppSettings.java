package org.kku.jdiskusage.util;

import java.nio.file.Path;
import org.kku.common.util.AppProperties.AppPropertyType;

public class AppSettings
  extends org.kku.common.util.AppSettings
{
  public static final AppPropertyType<Path> INITIAL_DIRECTORY;
  public static final AppPropertyType<RecentScanList> RECENT_SCANS;
  public static final AppPropertyType<DirectoryList> FAVORITE_DIRECTORIES;
  public static final AppPropertyType<Double> SPLIT_PANE_POSITION;
  public static final AppPropertyType<Double> PREF_SIZE;
  public static final AppPropertyType<SearchHistoryList> SEARCH_HISTORY;

  private AppSettings()
  {
  }

  static
  {
    INITIAL_DIRECTORY = createAppPropertyType("INITIAL_DIRECTORY", Converters.getPathConverter());
    RECENT_SCANS = createAppPropertyType("RECENT_SCANS", RecentScanList.getConverter());
    FAVORITE_DIRECTORIES = createAppPropertyType("FAVORITE_DIRECTORIES", DirectoryList.getConverter());
    SPLIT_PANE_POSITION = createAppPropertyType("SPLIT_PANE_POSITION", Converters.getDoubleConverter());
    PREF_SIZE = createAppPropertyType("PREF_SIZE", Converters.getDoubleConverter());
    SEARCH_HISTORY = createAppPropertyType("SEARCH_HISTORY", SearchHistoryList.getConverter());
  }
}
