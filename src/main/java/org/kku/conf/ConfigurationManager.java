package org.kku.conf;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  public <T extends Configuration> void save(T configuration)
  {
    configurationByClassMap.remove(configuration.getClass());
    saveConfiguration(configuration);
  }

  private Configuration loadConfiguration(Class<? extends Configuration> clazz)
  {
    String configurationName;

    configurationName = "/" + clazz.getSimpleName() + ".json";
    try (InputStream is = getClass().getResourceAsStream(configurationName))
    {
      byte[] bytes;
      Object value;

      bytes = is.readNBytes(CONFIGURATION_MAX_BYTES);
      if (bytes.length == CONFIGURATION_MAX_BYTES)
      {
        Log.log.error("Refused to load: Configuration %s exceeds %s bytes", configurationName, CONFIGURATION_MAX_BYTES);
        return null;
      }

      if (bytes.length == 0)
      {
        try
        {
          return clazz.getConstructor().newInstance();
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
            | NoSuchMethodException | SecurityException e)
        {
          Log.log.error("Exception while loading: Configuration %s", configurationName);
          return null;
        }
      }

      value = getMapper().readValue(bytes, clazz);
      if (value instanceof Configuration configuration)
      {
        return configuration;
      }
    }
    catch (Exception e)
    {
      Log.log.error(e, "Configuration %s failed to load", configurationName);
    }

    return null;
  }

  private void saveConfiguration(Configuration configuration)
  {
    try
    {
      getMapper().writeValue(System.out, configuration);
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

  private PropertyNamingStrategy createNamingStrategy()
  {
    return new PropertyNamingStrategy()
    {
      static final List<String> prefixes = Arrays.asList("m_", "mi_", "mii_", "miii_");

      @Override
      public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName)
      {
        String name;

        name = field.getName();
        if (name.startsWith("m"))
        {
          Optional<String> p = prefixes.stream().filter(prefix -> name.startsWith(prefix)).findFirst();
          if (p.isPresent())
          {
            return name.substring(p.get().length());
          }
        }

        return name;
      }
    };
  }

}
