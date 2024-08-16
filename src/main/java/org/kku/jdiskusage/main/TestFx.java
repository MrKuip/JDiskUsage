package org.kku.jdiskusage.main;

import org.kku.conf.Configuration;
import org.kku.conf.ConfigurationObjectMapper;
import org.kku.jdiskusage.util.preferences.DisplayMetric;
import com.fasterxml.jackson.databind.json.JsonMapper;
import javafx.application.Application;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;

public class TestFx
  extends Application
{
  @Override
  public void start(Stage stage)
  {
    JsonMapper mapper;
    TestConfiguration p;
    TestConfiguration p2;
    String text;

    p = new TestConfiguration();

    mapper = new ConfigurationObjectMapper().createMapper();
    try
    {
      text = mapper.writeValueAsString(p);
      System.out.println(text);
      p2 = mapper.readValue(text.getBytes(), TestConfiguration.class);
      p2.print();
      text = """
          { "double" : 10.13,
            "integer" : 11,
            "string" : "Hallo daar",
            "displayMetric" : "FILE_SIZE"
          }
          """;
      p2 = mapper.readValue(text.getBytes(), TestConfiguration.class);
      p2.print();

      System.exit(1);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public static class TestConfiguration
    extends Configuration
  {
    private SimpleObjectProperty<Double> m_double = new SimpleObjectProperty<>(10.12);
    private SimpleObjectProperty<Integer> m_integer = new SimpleObjectProperty<>(10);
    private SimpleObjectProperty<String> m_string = new SimpleObjectProperty<>("Hallo");
    private SimpleObjectProperty<DisplayMetric> m_displayMetric = new SimpleObjectProperty<>(DisplayMetric.FILE_COUNT);

    public TestConfiguration()
    {
    }

    public void print()
    {
      System.out.println("double=" + m_double.get());
      System.out.println("double=" + m_double.get().getClass());
      System.out.println("double=" + m_double.get() + " (" + m_double.get().getClass() + ")");
      System.out.println("integer=" + m_integer.get() + " (" + m_integer.get().getClass() + ")");
      System.out.println("string=" + m_string.get() + " (" + m_string.get().getClass() + ")");
      System.out.println("displayMetric=" + m_displayMetric.get() + " (" + m_displayMetric.get().getClass() + ")");
    }
  }

  public static class TestConfiguration2
    extends Configuration
  {
    private SimpleObjectPropertyWithDefault<Double> m_double = new SimpleObjectPropertyWithDefault<>(10.12);
    private SimpleObjectPropertyWithDefault<Integer> m_integer = new SimpleObjectPropertyWithDefault<>(10);
    private SimpleObjectPropertyWithDefault<String> m_string = new SimpleObjectPropertyWithDefault<>("Hallo");
    private SimpleObjectPropertyWithDefault<DisplayMetric> m_displayMetric = new SimpleObjectPropertyWithDefault<>(
        DisplayMetric.FILE_COUNT);

    public TestConfiguration2()
    {
    }

    public void print()
    {
      System.out.println("double=" + m_double.get());
      System.out.println("integer=" + m_integer.get());
      System.out.println("string=" + m_string.get());
      System.out.println("displayMetric=" + m_displayMetric.get());
    }

    public void reset()
    {
      m_double.reset();
      m_integer.reset();
      m_string.reset();
      m_displayMetric.reset();
    }
  }

  public static class SimpleObjectPropertyWithDefault<T>
    extends SimpleObjectProperty<T>
  {
    private T m_defaultValue;

    public SimpleObjectPropertyWithDefault(T defaultValue)
    {
      super(defaultValue);
      m_defaultValue = defaultValue;
    }

    public void reset()
    {
      set(m_defaultValue);
    }
  }

  public static void main(String[] args)
  {
    launch();
  }
}