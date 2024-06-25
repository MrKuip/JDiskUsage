package org.kku.jdiskusage.util.preferences;

import java.util.Locale;
import org.kku.jdiskusage.util.AppSettings;
import org.kku.jdiskusage.util.Converters;

public class AppPreferences
  extends AppSettings
{
  private final static AppPreferences m_instance = new AppPreferences();

  public final static AppSetting<SizeSystem> sizeSystemPreference;
  public final static AppSetting<DisplayMetric> displayMetricPreference;
  public final static AppSetting<Sort> sortPreference;
  public final static AppSetting<Locale> localePreference;
  public final static AppSetting<Integer> searchMaxCountPreference;
  public final static AppSetting<Integer> searchMaxTimePreference;
  public final static AppSetting<Boolean> searchRegexPreference;

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
  }

  protected AppPreferences()
  {
    super("JDiskUsage.preferences");
  }
}
