package org.kku.jdiskusage.util.preferences;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class JDiskUsagePreferences
{
  private final static JDiskUsagePreferences m_instance = new JDiskUsagePreferences();

  private ObjectProperty<SizeSystem> m_sizeSystemProperty = new SimpleObjectProperty<>(SizeSystem.BINARY);
  private ObjectProperty<DisplayMetric> m_displayMetricProperty = new SimpleObjectProperty<>(DisplayMetric.FILE_SIZE);
  private ObjectProperty<Sort> m_sortProperty = new SimpleObjectProperty<>(Sort.NUMERIC);

  private JDiskUsagePreferences()
  {
  }

  public JDiskUsagePreferences getInstance()
  {
    return m_instance;
  }

  public static SizeSystem getSizeSystem()
  {
    return m_instance.m_sizeSystemProperty.get();
  }

  public static ObjectProperty<SizeSystem> sizeSytemProperty()
  {
    return m_instance.m_sizeSystemProperty;
  }

  public static DisplayMetric getDisplayMetric()
  {
    return m_instance.m_displayMetricProperty.get();
  }

  public static ObjectProperty<DisplayMetric> displayMetricProperty()
  {
    return m_instance.m_displayMetricProperty;
  }

  public static Sort getSort()
  {
    return m_instance.m_sortProperty.get();
  }

  public static ObjectProperty<Sort> sortProperty()
  {
    return m_instance.m_sortProperty;
  }
}
