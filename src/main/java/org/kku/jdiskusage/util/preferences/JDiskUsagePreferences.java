package org.kku.jdiskusage.util.preferences;

public class JDiskUsagePreferences
{
  private final static JDiskUsagePreferences m_instance = new JDiskUsagePreferences();

  private SizeSystem m_sizeDefinition = SizeSystem.BINARY;
  private DisplayMetric m_displayMetric = DisplayMetric.FILE_SIZE;

  private JDiskUsagePreferences()
  {
  }

  public JDiskUsagePreferences getInstance()
  {
    return m_instance;
  }

  public static SizeSystem getSizeSystem()
  {
    return m_instance.m_sizeDefinition;
  }

  public static DisplayMetric getDisplayMetric()
  {
    return m_instance.m_displayMetric;
  }
}
