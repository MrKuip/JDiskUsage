package org.kku.jdiskusage.util.preferences;

import org.kku.common.util.Converters.Converter;
import org.kku.fx.util.AppProperties;
import org.kku.jdiskusage.conf.Language;
import org.kku.jdiskusage.conf.LanguageConfiguration;
import org.kku.jdiskusage.util.Converters;

public class AppPreferences
  extends AppProperties
{
  private final static AppPreferences m_instance = new AppPreferences();

  public final static AppProperty<SizeSystem> sizeSystemPreference;
  public final static AppProperty<DisplayMetric> displayMetricPreference;
  public final static AppProperty<Sort> sortPreference;
  public final static AppProperty<Language> languagePreference;
  public final static AppProperty<Integer> searchMaxCountPreference;
  public final static AppProperty<Integer> searchMaxTimePreference;
  public final static AppProperty<Boolean> searchRegexPreference;
  public final static AppProperty<Boolean> autoExpandTreeNode;
  public final static AppProperty<Boolean> autoCollapseTreeNode;
  public final static AppProperty<Boolean> showProgressInTable;
  public final static AppProperty<Integer> maxNumberOfChartElements;
  public final static AppProperty<Double> minPercentageChartElement;
  public final static AppProperty<Integer> maxNumberInTopRanking;
  public final static AppProperty<Integer> maxNumberOfElementsInSunburstChart;

  static
  {
    sizeSystemPreference = createPreference("Size system", Converters.getEnumConverter(SizeSystem.class),
        SizeSystem.BINARY);
    displayMetricPreference = createPreference("Display metric", Converters.getEnumConverter(DisplayMetric.class),
        DisplayMetric.FILE_SIZE);
    sortPreference = createPreference("Sort", Converters.getEnumConverter(Sort.class), Sort.NUMERIC);
    languagePreference = createPreference("Language", Converters.getLanguageConverter(),
        LanguageConfiguration.getInstance().getDefault());
    searchMaxCountPreference = createPreference("Max count", Converters.getIntegerConverter(), 100);
    searchMaxTimePreference = createPreference("Max time", Converters.getIntegerConverter(), 10);
    searchRegexPreference = createPreference("Use regex search", Converters.getBooleanConverter(), Boolean.FALSE);
    autoExpandTreeNode = createPreference("Auto expand tree node", Converters.getBooleanConverter(), Boolean.FALSE);
    autoCollapseTreeNode = createPreference("Auto collapse tree node", Converters.getBooleanConverter(), Boolean.FALSE);
    showProgressInTable = createPreference("Show progress in table", Converters.getBooleanConverter(), Boolean.TRUE);
    maxNumberOfChartElements = createPreference("Max number of elements", Converters.getIntegerConverter(), 11);
    minPercentageChartElement = createPreference("Min percentage element", Converters.getDoubleConverter(), 5.0);
    maxNumberInTopRanking = createPreference("Max number in top ranking", Converters.getIntegerConverter(), 50);
    maxNumberOfElementsInSunburstChart = createPreference("Max number of levels in sunburst chart",
        Converters.getIntegerConverter(), 5);
  }

  static public <T> AppProperty<T> createPreference(String name, Converter<T> converter, T defaultValue)
  {
    return m_instance.createAppPropertyType(name, converter).forSubject(m_instance, defaultValue);
  }

  protected AppPreferences()
  {
    super("JDiskUsage.preferences");
  }
}
