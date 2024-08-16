package org.kku.jdiskusage.main;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleObjectProperty;

public class Main2
{
  public static void main(String[] args) throws JsonProcessingException
  {
    // Example object with a SimpleObjectProperty
    MyClass obj = new MyClass();
    obj.setProperty(new SimpleObjectProperty<>("Hello World"));

    // Use the custom ObjectMapper to serialize the object
    ObjectMapper mapper = JacksonConfig.configureObjectMapper();
    String json = mapper.writeValueAsString(obj);

    System.out.println(json);
  }
}

class MyClass
{
  private SimpleObjectProperty<String> property;

  public SimpleObjectProperty<String> getProperty()
  {
    return property;
  }

  public void setProperty(SimpleObjectProperty<String> property)
  {
    this.property = property;
  }
}
