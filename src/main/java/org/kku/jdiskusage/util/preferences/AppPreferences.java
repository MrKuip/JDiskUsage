package org.kku.jdiskusage.util.preferences;

import java.util.Locale;
import org.kku.jdiskusage.util.AppProperties;
import org.kku.jdiskusage.util.Converters;

public class AppPreferences
  extends AppProperties
{
  private final static AppPreferences m_instance = new AppPreferences();

  public final static AppProperty<SizeSystem> sizeSystemPreference;
  public final static AppProperty<DisplayMetric> displayMetricPreference;
  public final static AppProperty<Sort> sortPreference;
  public final static AppProperty<Locale> localePreference;
  public final static AppProperty<Integer> searchMaxCountPreference;
  public final static AppProperty<Integer> searchMaxTimePreference;
  public final static AppProperty<Boolean> searchRegexPreference;
  public final static AppProperty<Boolean> autoExpandTreeNode;
  public final static AppProperty<Boolean> autoCollapseTreeNode;
  public final static AppProperty<Integer> maxNumberOfChartElements;
  public final static AppProperty<Double> minPercentageChartElement;

  static
  {
    sizeSystemPreference = m_instance.createAppPropertyType("Size system", Converters.getEnumConverter(SizeSystem.class))
        .forSubject(m_instance, SizeSystem.BINARY);
    displayMetricPreference = m_instance
        .createAppPropertyType("Display metric", Converters.getEnumConverter(DisplayMetric.class))
        .forSubject(m_instance, DisplayMetric.FILE_SIZE);
    sortPreference = m_instance.createAppPropertyType("Sort", Converters.getEnumConverter(Sort.class))
        .forSubject(m_instance, Sort.NUMERIC);
    localePreference = m_instance.createAppPropertyType("Locale", Converters.getLocaleConverter()).forSubject(m_instance,
        new Locale("nl"));
    searchMaxCountPreference = m_instance.createAppPropertyType("Max count", Converters.getIntegerConverter())
        .forSubject(m_instance, 100);
    searchMaxTimePreference = m_instance.createAppPropertyType("Max time", Converters.getIntegerConverter())
        .forSubject(m_instance, 10);
    searchRegexPreference = m_instance.createAppPropertyType("Use regex search", Converters.getBooleanConverter())
        .forSubject(m_instance, Boolean.FALSE);
    autoExpandTreeNode = m_instance.createAppPropertyType("Auto expand tree node", Converters.getBooleanConverter())
        .forSubject(m_instance, Boolean.FALSE);
    autoCollapseTreeNode = m_instance.createAppPropertyType("Auto collapse tree node", Converters.getBooleanConverter())
        .forSubject(m_instance, Boolean.FALSE);
    maxNumberOfChartElements = m_instance
        .createAppPropertyType("Max number of elements", Converters.getIntegerConverter()).forSubject(m_instance, 11);
    minPercentageChartElement = m_instance
        .createAppPropertyType("Min percentage element", Converters.getDoubleConverter()).forSubject(m_instance, 5.0);
  }

  protected AppPreferences()
  {
    super("JDiskUsage.preferences");
  }
}
