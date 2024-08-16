package org.kku.jdiskusage.main;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import javafx.beans.property.SimpleObjectProperty;

public class JacksonConfig
{

  public static ObjectMapper configureObjectMapper()
  {
    ObjectMapper mapper = new ObjectMapper();

    // Create and register the custom serializer
    SimpleModule module = new SimpleModule();
    module.addSerializer((Class<SimpleObjectProperty<?>>) (Class<?>) SimpleObjectProperty.class,
        new SimpleObjectPropertySerializer());

    mapper.registerModule(module);

    return mapper;
  }

  public static class SimpleObjectPropertySerializer
    extends JsonSerializer<SimpleObjectProperty<?>>
  {

    @Override
    public void serialize(SimpleObjectProperty<?> value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException
    {
      // Serialize the value held by the SimpleObjectProperty
      if (value == null || value.get() == null)
      {
        gen.writeNull();
      }
      else
      {
        // Use the default serializer for the value inside the property
        serializers.defaultSerializeValue(value.get(), gen);
      }
    }
  }

}
