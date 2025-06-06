package org.kku.jdiskusage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.kku.common.util.AppProperties;
import org.kku.common.util.AppProperties.AppProperty;
import org.kku.common.util.AppProperties.AppPropertyType;
import javafx.beans.property.SimpleStringProperty;

public class AppPropertiesTest
{
  public AppPropertiesTest()
  {
  }

  @Test
  void test() throws Exception
  {
    AppProperties appProperties;
    AppPropertyType<String> type;
    AppProperty<String> property;
    SimpleStringProperty stringProperty;
    String subject;
    String propertyKey;
    String propertyValue;
    String defaultValue;

    appProperties = AppProperties.get(getClass());
    appProperties.getStore().setSyncImmediately(true);
    appProperties.getStore().clear();

    propertyKey = "Test";
    propertyValue = "Test1";
    defaultValue = "defaultValue";
    subject = "TestSubject";

    type = appProperties.createAppPropertyType(propertyKey, Converters.getStringConverter());
    property = type.forSubject(subject, propertyValue);

    assertEquals(property.get(), propertyValue);

    propertyValue = "Test2";
    property.set(propertyValue);
    assertEquals(property.get(), propertyValue);
    assertTrue(checkFileContent(appProperties, subject + "_" + propertyKey + "=" + propertyValue));

    property = type.forSubject(subject, defaultValue);
    assertEquals(property.get(), propertyValue);
  }

  private boolean checkFileContent(AppProperties properties, String regex) throws IOException
  {
    Pattern pattern;

    pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

    return Files.readAllLines(properties.getStore().getFilePath()).stream().filter(pattern.asPredicate()).findFirst()
        .isPresent();
  }
}
