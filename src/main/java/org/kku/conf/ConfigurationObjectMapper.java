package org.kku.conf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.json.JsonMapper.Builder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import javafx.beans.property.SimpleObjectProperty;

public class ConfigurationObjectMapper
{
  public static JsonMapper createMapper()
  {
    Builder builder;
    JsonMapper mapper;

    builder = JsonMapper.builder();
    builder.enable(SerializationFeature.INDENT_OUTPUT);

    builder.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    builder.disable(MapperFeature.AUTO_DETECT_CREATORS);
    builder.enable(MapperFeature.AUTO_DETECT_FIELDS);
    builder.disable(MapperFeature.AUTO_DETECT_GETTERS);
    builder.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
    builder.disable(MapperFeature.AUTO_DETECT_SETTERS);

    mapper = builder.build();
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    mapper.setPropertyNamingStrategy(createNamingStrategy());
    mapper.registerModule(getConfigurationModule());

    return mapper;
  }

  /**
   * Function that converts a fieldName to the name in the json structure.
   * 
   * In the source code EVERY field is prefixed with m_ (for every inner class level an 'i' is inserted between the 'm' and the '_'.
   * 
   * @return the converted field name
   */
  static private Function<String, String> convertFieldName()
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

  /**
   * Create a naming strategy that converts the field name according the method {@link #convertFieldName()}
   * 
   * @return a property naming strategy
   */
  private static PropertyNamingStrategy createNamingStrategy()
  {
    return new PropertyNamingStrategy()
    {
      private Function<String, String> mi_convertFieldName = convertFieldName();

      @Override
      public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName)
      {
        return mi_convertFieldName.apply(field.getName());
      }
    };
  }

  public static Module getConfigurationModule()
  {
    SimpleModule module;

    module = new SimpleModule("Configuration");
    module.addSerializer(new SimpleObjectPropertySerializer());
    module.addDeserializer(SimpleObjectProperty.class, new SimpleObjectPropertyDeserializer());

    return module;
  }

  /**
   * A deserializer that converts a value to a SimpleObjectProperty
   */
  private static class SimpleObjectPropertyDeserializer
    extends JsonDeserializer<SimpleObjectProperty<?>>
      implements ContextualDeserializer
  {
    private static Map<JavaType, TypeDeserializer> mi_serializerByTypeMap = new HashMap<>();

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
    {
      return mi_serializerByTypeMap.computeIfAbsent(property.getType().containedType(0), TypeDeserializer::new);
    }

    @Override
    public SimpleObjectProperty<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException
    {
      return null;
    }

    private static class TypeDeserializer
      extends JsonDeserializer<SimpleObjectProperty<?>>
    {
      private JavaType type;

      private TypeDeserializer(JavaType type)
      {
        this.type = type;
      }

      @Override
      public SimpleObjectProperty<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
          throws IOException
      {
        SimpleObjectProperty<?> property = new SimpleObjectProperty<>();
        property.setValue(deserializationContext.readValue(jsonParser, type));
        return property;
      }

      @Override
      public SimpleObjectProperty<?> getNullValue(DeserializationContext ctxt)
      {
        SimpleObjectProperty<?> property = new SimpleObjectProperty<>();
        property.set(null);
        return property;
      }
    }
  }

  /**
   * A serializer that converts a SimpleObjectProperty to its value.
   */

  private static class SimpleObjectPropertySerializer
    extends StdSerializer<SimpleObjectProperty<?>>
      implements ContextualSerializer
  {
    private static Map<JavaType, TypeSerializer> mi_serializerByTypeMap = new HashMap<>();

    private SimpleObjectPropertySerializer()
    {
      super(TypeFactory.defaultInstance().constructType(SimpleObjectProperty.class));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
        throws JsonMappingException
    {
      return mi_serializerByTypeMap.computeIfAbsent(property.getType().containedType(0), TypeSerializer::new);
    }

    @Override
    public void serialize(SimpleObjectProperty<?> value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException
    {
    }

    private static class TypeSerializer
      extends StdSerializer<SimpleObjectProperty<?>>
    {
      private JavaType mi_type;

      private TypeSerializer(JavaType type)
      {
        super(TypeFactory.defaultInstance().constructType(SimpleObjectProperty.class));

        mi_type = type;
      }

      @Override
      public void serialize(SimpleObjectProperty<?> value, JsonGenerator jgen, SerializerProvider provider)
          throws IOException, JsonProcessingException
      {
        if (value == null || value.get() == null)
        {
          jgen.writeNull();
        }
        else
        {
          provider.findTypedValueSerializer(mi_type, true, null).serialize(value.get(), jgen, provider);
        }
      }
    }
  }
}
