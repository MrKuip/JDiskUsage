package org.kku.jdiskusage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.kku.jdiskusage.util.AppProperties2.AppProperty;
import org.kku.jdiskusage.util.AppProperties2.AppPropertyType;
import javafx.beans.property.SimpleStringProperty;

public class AppProperties2Test
{
  public AppProperties2Test()
  {
  }

  @Test
  void test() throws Exception
  {
    AppPropertyType<String> type;
    AppProperty<String> property;
    SimpleStringProperty stringProperty;
    TestProperties properties;
    String subject;
    String propertyKey;
    String propertyValue;
    String defaultValue;

    properties = new TestProperties();
    properties.getStore().clear();

    propertyKey = "Test";
    propertyValue = "Test2";
    defaultValue = "defaultValue";
    subject = "TestSubject";

    type = properties.createType(propertyKey, Converters.getStringConverter());
    property = type.forSubject(subject, propertyValue);

    assertEquals(property.get(), propertyValue);

    propertyValue = "Test1";
    property.set(propertyValue);
    assertEquals(property.get(), propertyValue);
    assertTrue(checkFileContent(properties, subject + "_" + propertyKey + "=" + propertyValue));

    property = type.forSubject(subject, defaultValue);
    assertEquals(property.get(), propertyValue);

    stringProperty = new SimpleStringProperty("Hallo");
    property.property().bindBidirectional(stringProperty);

    propertyValue = "Hallo2";
    stringProperty.set(propertyValue);
    assertEquals(property.get(), propertyValue);
    assertTrue(checkFileContent(properties, subject + "_" + propertyKey + "=" + propertyValue));
  }

  @Test
  void testList() throws Exception
  {
    AppPropertyType<List<String>> type;
    AppProperty<List<String>> property;
    SimpleStringProperty stringProperty;
    TestProperties properties;
    String subject;
    String propertyKey;
    String propertyValue;
    String defaultValue;
    List<String> list;
    /*
    
    properties = new TestProperties();
    properties.getStore().clear();
    
    propertyKey = "Test";
    propertyValue = "Test2";
    defaultValue = "defaultValue";
    subject = "TestSubject";
    
    type = properties.createListType(propertyKey, Converters.getStringConverter(), 20);
    property = type.forSubject("subject");
    
    list = property.get();
    property.set(list);
    */

  }

  @Test
  void testArray() throws Exception
  {
  }

  private boolean checkFileContent(TestProperties properties, String regex) throws IOException
  {
    Pattern pattern;

    pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

    return Files.readAllLines(properties.getStore().getFilePath()).stream().filter(pattern.asPredicate()).findFirst()
        .isPresent();
  }

  public static class TestProperties
    extends AppProperties2
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
