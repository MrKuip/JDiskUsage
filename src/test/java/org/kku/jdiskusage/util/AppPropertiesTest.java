package org.kku.jdiskusage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.kku.jdiskusage.util.AppSettings.AppSetting;
import org.kku.jdiskusage.util.AppSettings.AppSettingType;
import javafx.beans.property.SimpleStringProperty;

class AppPropertiesTest
{
  public AppPropertiesTest()
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
    properties.reset();

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

  @Test
  void testArray() throws Exception
  {
    AppSettingType<String> type;
    AppSetting<String> property;
    List<String> list;
    TestProperties properties;

    properties = TestProperties.getInstance();
    properties.reset();

    type = properties.createAppSettingType("Test", Converters.getStringConverter(), 10);
    property = type.forSubject("Test");

    assertEquals(Collections.emptyList(), property.getList());

    list = new ArrayList<>();
    for (int i = 0; i < 4; i++)
    {
      list.add("String" + i);
    }

    property.setList(list);
    assertEquals(Arrays.asList("String0", "String1", "String2", "String3"), property.getList());
  }

  public static class TestProperties
    extends AppSettings
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

    public void reset() throws Exception
    {
      if (Files.exists(getSettingPath()))
      {
        Files.delete(getSettingPath());
      }
    }
  }
}
