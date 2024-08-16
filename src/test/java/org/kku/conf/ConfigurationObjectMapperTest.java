package org.kku.conf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.kku.test.TestUtil;
import com.fasterxml.jackson.databind.json.JsonMapper;
import javafx.beans.property.SimpleObjectProperty;

public class ConfigurationObjectMapperTest
{
  @Test
  void testConvertFieldName()
  {
    Function<String, String> convertFieldName;

    convertFieldName = TestUtil.invokePrivateMethod(ConfigurationObjectMapper.class, "convertFieldName");

    assertEquals("test", convertFieldName.apply("test"));
    assertEquals("p_test", convertFieldName.apply("p_test"));
    assertEquals("test", convertFieldName.apply("m_test"));
    assertEquals("test", convertFieldName.apply("mi_test"));
    assertEquals("_test", convertFieldName.apply("mi__test"));
    assertEquals("_mi_test", convertFieldName.apply("_mi_test"));
    assertEquals("test", convertFieldName.apply("mii_test"));
    assertEquals("test", convertFieldName.apply("miii_test"));
    assertEquals("test", convertFieldName.apply("miiii_test"));
    assertEquals("test", convertFieldName.apply("miiiiiiiiiiiii_test"));
    assertEquals("miiip_test", convertFieldName.apply("miiip_test"));
  }

  @Test
  void saveAndGetConfiguration() throws Exception
  {
    TestConfiguration testConfiguration;
    TestConfiguration testConfiguration2;
    JsonMapper mapper;
    String text;
    String text2;

    testConfiguration = new TestConfiguration();
    testConfiguration.mi_int = 1;
    testConfiguration.mi_string = "String1";
    testConfiguration.mi_stringProperty.set("String2");
    testConfiguration.mi_intProperty.set(null);
    testConfiguration.mi_doubleProperty = null;

    mapper = ConfigurationObjectMapper.createMapper();
    text = mapper.writeValueAsString(testConfiguration);

    System.out.println(text);

    testConfiguration2 = mapper.readValue(text, TestConfiguration.class);

    assertNotNull(testConfiguration2);
    assertEquals(testConfiguration.mi_int, testConfiguration2.mi_int);
    assertEquals(testConfiguration.mi_string, testConfiguration2.mi_string);
    assertEquals(testConfiguration.mi_stringProperty.get(), testConfiguration2.mi_stringProperty.get());
    assertEquals(testConfiguration.mi_intProperty.get(), testConfiguration2.mi_intProperty.get());
    assertEquals(null, testConfiguration2.mi_intProperty.get());

    text2 = mapper.writeValueAsString(testConfiguration2);
    assertEquals(text, text2);
  }

  public static class TestConfiguration
    extends Configuration
  {
    private int mi_int;
    private String mi_string;
    private SimpleObjectProperty<String> mi_stringProperty = new SimpleObjectProperty<>();
    private SimpleObjectProperty<Integer> mi_intProperty = new SimpleObjectProperty<>(null);
    private SimpleObjectProperty<Double> mi_doubleProperty = null;
  }
}
