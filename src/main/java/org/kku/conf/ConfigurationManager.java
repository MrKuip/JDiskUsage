package org.kku.conf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kku.jdiskusage.util.Log;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.json.JsonMapper.Builder;

public class ConfigurationManager
{
  private final static ConfigurationManager m_instance = new ConfigurationManager();
  private final static int CONFIGURATION_MAX_BYTES = 1000000;

  private final Map<Class<? extends Configuration>, Configuration> configurationByClassMap = new HashMap<>();
  private final JsonMapper m_objectMapper = createMapper();
  private final Function<String, String> m_convertFieldName = convertFieldName();

  private ConfigurationManager()
  {
  }

  public static ConfigurationManager getInstance()
  {
    return m_instance;
  }

  @SuppressWarnings("unchecked")
  public <T extends Configuration> T get(Class<T> clazz)
  {
    return (T) configurationByClassMap.computeIfAbsent(clazz, this::loadConfiguration);
  }

  @SuppressWarnings("unchecked")
  public <T extends Configuration> T get(Class<T> clazz, byte[] bytes)
  {
    return (T) configurationByClassMap.computeIfAbsent(clazz, c -> loadConfiguration(c, bytes));
  }

  private Configuration loadConfiguration(Class<? extends Configuration> clazz)
  {
    String configurationName;

    configurationName = "/" + clazz.getSimpleName() + ".json";
    try (InputStream is = ConfigurationManager.class.getResourceAsStream(configurationName))
    {
      byte[] bytes;

      if (is == null)
      {
        throw new ConfigurationException("Configuration %s inputstream is null", configurationName);
      }

      bytes = is.readNBytes(CONFIGURATION_MAX_BYTES);
      return loadConfiguration(clazz, bytes);
    }
    catch (Exception ex)
    {
      throw new ConfigurationException(ex, "Configuration %s inputstream failure", configurationName);
    }
  }

  private Configuration loadConfiguration(Class<? extends Configuration> clazz, byte[] bytes)
  {
    String configurationName;
    Object value;

    configurationName = "/" + clazz.getSimpleName() + ".json";

    if (bytes.length == CONFIGURATION_MAX_BYTES)
    {
      throw new ConfigurationException("Refused to load: Configuration %s exceeds %s bytes", configurationName,
          CONFIGURATION_MAX_BYTES);
    }

    if (bytes.length == 0)
    {
      try
      {
        return clazz.getConstructor().newInstance();
      }
      catch (Throwable ex)
      {
        Log.log.error("Exception while loading: Configuration %s", configurationName);
        throw new ConfigurationException(ex, "Exception while loading: Configuration %s", configurationName);
      }
    }

    try
    {
      value = getMapper().readValue(bytes, clazz);

      return (Configuration) value;
    }
    catch (Throwable ex)
    {
      Log.log.error("Exception while reading value: Configuration %s", configurationName);
      throw new ConfigurationException(ex, "Exception while reading value: Configuration %s", configurationName);
    }
  }

  public <T extends Configuration> byte[] saveToBytes(T configuration)
  {
    ByteArrayOutputStream os;

    os = new ByteArrayOutputStream(1000);
    saveConfiguration(configuration, os);

    return os.toByteArray();
  }

  public <T extends Configuration> void saveToStdOut(T configuration)
  {
    saveConfiguration(configuration, System.out);
  }

  private void saveConfiguration(Configuration configuration, OutputStream os)
  {
    configurationByClassMap.remove(configuration.getClass());
    try
    {
      getMapper().writeValue(os, configuration);
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private JsonMapper getMapper()
  {
    return m_objectMapper;
  }

  private JsonMapper createMapper()
  {
    Builder builder;
    JsonMapper mapper;

    builder = JsonMapper.builder();
    builder.enable(SerializationFeature.INDENT_OUTPUT);
    builder.enable(MapperFeature.AUTO_DETECT_FIELDS);
    builder.disable(MapperFeature.AUTO_DETECT_GETTERS);
    builder.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
    builder.disable(MapperFeature.AUTO_DETECT_SETTERS);
    builder.disable(MapperFeature.AUTO_DETECT_CREATORS);

    mapper = builder.build();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    mapper.setPropertyNamingStrategy(createNamingStrategy());

    return mapper;
  }

  /**
   * Function that converts a fieldName to the name in the json structure.
   * 
   * In the source code EVERY field is prefixed with m_ (for every inner class level an 'i' is inserted between the 'm' and the '_'.
   * 
   * @return the converted field name
   */
  private Function<String, String> convertFieldName()
  {
    Pattern p;
    Matcher m;

    p = Pattern.compile("m[i]*_(.*)");
    m = p.matcher("");

    return fieldName -> {

      m.reset(fieldName);
      if (m.matches())
      {
        return m.group(1);
      }

      return fieldName;
    };
  }

  private PropertyNamingStrategy createNamingStrategy()
  {
    return new PropertyNamingStrategy()
    {
      @Override
      public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName)
      {
        return m_convertFieldName.apply(field.getName());
      }
    };
  }
}
