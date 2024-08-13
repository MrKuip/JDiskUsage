package org.kku.conf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.kku.test.TestUtil;

public class ConfigurationManagerTest
{
  @Test
  void testConvertFieldName()
  {
    Function<String, String> convertFieldName;

    convertFieldName = TestUtil.invokePrivateMethod(ConfigurationManager.getInstance(), "convertFieldName");

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
  void saveAndGetConfiguration()
  {
    TestConfiguration testConfiguration;
    TestConfiguration testConfiguration2;
    byte[] bytes;
    byte[] bytes2;

    testConfiguration = new TestConfiguration();
    testConfiguration.add(new TestConfigurationItem(1, "Test1"));
    testConfiguration.add(new TestConfigurationItem(2, "Test2"));

    bytes = ConfigurationManager.getInstance().saveToBytes(testConfiguration);

    testConfiguration2 = ConfigurationManager.getInstance().get(TestConfiguration.class, bytes);

    assertNotNull(testConfiguration2);
    assertEquals(testConfiguration.getList().size(), testConfiguration2.getList().size());
    assertEquals(testConfiguration.getList().get(0).getId(), testConfiguration2.getList().get(0).getId());
    assertEquals(testConfiguration.getList().get(0).getName(), testConfiguration2.getList().get(0).getName());
    assertEquals(testConfiguration.getList().get(1).getId(), testConfiguration2.getList().get(1).getId());
    assertEquals(testConfiguration.getList().get(1).getName(), testConfiguration2.getList().get(1).getName());

    bytes2 = ConfigurationManager.getInstance().saveToBytes(testConfiguration2);
    assertArrayEquals(bytes, bytes2);
  }

  public static class TestConfiguration
    extends Configuration
  {
    private List<TestConfigurationItem> m_testConfigurationItemList = new ArrayList<>();

    public TestConfiguration()
    {
    }

    public void add(TestConfigurationItem item)
    {
      m_testConfigurationItemList.add(item);
    }

    public List<TestConfigurationItem> getList()
    {
      return m_testConfigurationItemList;
    }
  }

  public static class TestConfigurationItem
    extends ConfigurationItem
  {
    private int mi_id;
    private String mi_name;

    public TestConfigurationItem()
    {
    }

    public TestConfigurationItem(int id, String name)
    {
      mi_id = id;
      mi_name = name;
    }

    public int getId()
    {
      return mi_id;
    }

    public String getName()
    {
      return mi_name;
    }
  }
}
