package org.kku.jdiskusage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kku.jdiskusage.util.AppProperties.AppProperty;
import org.kku.jdiskusage.util.AppProperties.AppPropertyType;

import javafx.beans.property.SimpleStringProperty;

class AppPropertiesTest
{
  public AppPropertiesTest()
  {
  }

  @Test
  void test()
  {
    AppPropertyType<String> type;
    AppProperty<String> property;
    SimpleStringProperty stringProperty;

    initAppProperties("Test.properties");

    type = new AppPropertyType<>("Test", Converters.getStringConverter());
    property = type.forSubject("Test");

    assertEquals(property.get("Test2"), "Test2");

    property.set("Test1");
    assertEquals(property.get("Test2"), "Test1");

    property = type.forSubject("Test");
    assertEquals(property.get("Test2"), "Test1");

    stringProperty = new SimpleStringProperty("Hallo");
    stringProperty.addListener(property.getChangeListener());

    stringProperty.set("Hallo2");
    assertEquals(property.get("Test2"), "Hallo2");

    type = new AppPropertyType<>("Test", Converters.getStringConverter());
    property = type.forSubject("Test");
  }

  @Test
  void testArray()
  {
    AppPropertyType<String> type;
    AppProperty<String> property;
    List<String> list;

    initAppProperties("Test.properties");

    type = new AppPropertyType<>("Test", Converters.getStringConverter(), 10);
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

  private void initAppProperties(String propertyFileName)
  {
    AppProperties.getInstance().setPropertyFileName(propertyFileName);
    try
    {
      Files.delete(AppProperties.getInstance().getPropertyPath());
    }
    catch (IOException e)
    {
    }
  }
}
