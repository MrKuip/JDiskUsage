package org.kku.jdiskusage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.kku.jdiskusage.util.AppProperties.AppProperty;
import org.kku.jdiskusage.util.AppProperties.AppPropertyType;
import org.kku.jdiskusage.util.DirectoryChooser.PathList;

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

    AppProperties.getInstance().setPropertyFileName("Test.properties");
    try
    {
      Files.delete(AppProperties.getInstance().getPropertyPath());
    }
    catch (IOException e)
    {
    }

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
    AppPropertyType<PathList> type;
    AppProperty<PathList> property;
    SimpleStringProperty stringProperty;

    AppProperties.getInstance().setPropertyFileName("Test.properties");

    type = new AppPropertyType<>("Test", Converters.getPathListConverter(), 10);
    property = type.forSubject("Test");

    assertEquals(Collections.emptyList(), property.getList());

  }
}
