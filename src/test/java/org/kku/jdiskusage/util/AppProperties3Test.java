package org.kku.jdiskusage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.kku.jdiskusage.util.AppProperties.AppSetting;
import org.kku.jdiskusage.util.AppProperties.AppSettingType;
import javafx.beans.property.SimpleStringProperty;

class AppProperties3Test
{
  public AppProperties3Test()
  {
  }

  @Test
  void test() throws Exception
  {
    AppSettingType<String> type;
    AppSetting<String> property;
    SimpleStringProperty stringProperty;
    TestProperties properties;

    properties = new TestProperties();
    properties.getStore().clear();

    type = properties.createAppSettingType("Test", Converters.getStringConverter());
    property = type.forSubject("Test", "Test2");

    assertEquals(property.get(), "Test2");

    property.set("Test1");
    assertEquals(property.get(), "Test1");

    property = type.forSubject("Test", "Test2");
    assertEquals(property.get(), "Test1");

    stringProperty = new SimpleStringProperty("Hallo");
    stringProperty.addListener(property.getChangeListener());

    stringProperty.set("Hallo2");
    assertEquals(property.get("Test2"), "Hallo2");
  }

  public static class TestProperties
    extends AppProperties
  {
    private final static TestProperties mi_instance = new TestProperties();

    private TestProperties()
    {
      super("JDiskUsageTest.properties");
    }

    public static TestProperties getInstance()
    {
      return mi_instance;
    }
  }
}
