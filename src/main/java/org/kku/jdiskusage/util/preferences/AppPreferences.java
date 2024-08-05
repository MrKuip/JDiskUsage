package org.kku.jdiskusage.util.preferences;

import java.util.Locale;
import org.kku.jdiskusage.util.AppProperties;
import org.kku.jdiskusage.util.Converters;

public class AppPreferences
  extends AppProperties
{
  private final static AppPreferences m_instance = new AppPreferences();

  public final static AppSetting<SizeSystem> sizeSystemPreference;
  public final static AppSetting<DisplayMetric> displayMetricPreference;
  public final static AppSetting<Sort> sortPreference;
  public final static AppSetting<Locale> localePreference;
  public final static AppSetting<Integer> searchMaxCountPreference;
  public final static AppSetting<Integer> searchMaxTimePreference;
  public final static AppSetting<Boolean> searchRegexPreference;
  public final static AppSetting<Boolean> autoExpandTreeNode;
  public final static AppSetting<Boolean> autoCollapseTreeNode;
  public final static AppSetting<Integer> maxNumberOfChartElements;
  public final static AppSetting<Double> minPercentageChartElement;

  static
  {
    sizeSystemPreference = m_instance.createAppSettingType("Size system", Converters.getEnumConverter(SizeSystem.class))
        .forSubject(m_instance, SizeSystem.BINARY);
    displayMetricPreference = m_instance
        .createAppSettingType("Display metric", Converters.getEnumConverter(DisplayMetric.class))
        .forSubject(m_instance, DisplayMetric.FILE_SIZE);
    sortPreference = m_instance.createAppSettingType("Sort", Converters.getEnumConverter(Sort.class))
        .forSubject(m_instance, Sort.NUMERIC);
    localePreference = m_instance.createAppSettingType("Locale", Converters.getLocaleConverter()).forSubject(m_instance,
        new Locale("nl"));
    searchMaxCountPreference = m_instance.createAppSettingType("Max count", Converters.getIntegerConverter())
        .forSubject(m_instance, 100);
    searchMaxTimePreference = m_instance.createAppSettingType("Max time", Converters.getIntegerConverter())
        .forSubject(m_instance, 10);
    searchRegexPreference = m_instance.createAppSettingType("Use regex search", Converters.getBooleanConverter())
        .forSubject(m_instance, Boolean.FALSE);
    autoExpandTreeNode = m_instance.createAppSettingType("Auto expand tree node", Converters.getBooleanConverter())
        .forSubject(m_instance, Boolean.FALSE);
    autoCollapseTreeNode = m_instance.createAppSettingType("Auto collapse tree node", Converters.getBooleanConverter())
        .forSubject(m_instance, Boolean.FALSE);
    maxNumberOfChartElements = m_instance
        .createAppSettingType("Max number of elements", Converters.getIntegerConverter()).forSubject(m_instance, 11);
    minPercentageChartElement = m_instance
        .createAppSettingType("Min percentage element", Converters.getDoubleConverter()).forSubject(m_instance, 5.0);
  }

  protected AppPreferences()
  {
    super("JDiskUsage.preferences");
  }
}
