package org.kku.jdiskusage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.kku.jdiskusage.util.preferences.SizeSystem;

class ConvertersTest
{

  @Test
  void testDoubleConverter()
  {
    double value;
    String stringValue;

    value = 100.0;
    stringValue = "100.0";

    assertEquals(value, Converters.getDoubleConverter().fromString(stringValue));
    assertEquals(stringValue, Converters.getDoubleConverter().toString(value));
  }

  @Test
  void testIntegerConverter()
  {
    int value;
    String stringValue;

    value = 100;
    stringValue = "100";

    assertEquals(value, Converters.getIntegerConverter().fromString(stringValue));
    assertEquals(stringValue, Converters.getIntegerConverter().toString(value));
  }

  @Test
  void testLongConverter()
  {
    long value;
    String stringValue;

    value = 100l;
    stringValue = "100";

    assertEquals(value, Converters.getLongConverter().fromString(stringValue));
    assertEquals(stringValue, Converters.getLongConverter().toString(value));
  }

  @Test
  void testStringConverter()
  {
    String value;
    String stringValue;

    value = "100.0";
    stringValue = "100.0";

    assertEquals(value, Converters.getStringConverter().fromString(stringValue));
    assertEquals(stringValue, Converters.getStringConverter().toString(value));
  }

  @Test
  void testPathConverter()
  {
    Path value;
    String stringValue;

    value = Path.of("dir", "file");
    stringValue = "dir" + File.separator + "file";

    assertEquals(value, Converters.getPathConverter().fromString(stringValue));
    assertEquals(stringValue, Converters.getPathConverter().toString(value));
  }

  @Test
  void testPathListConverter()
  {
    PathList value;
    String stringValue;

    value = new PathList(Arrays.asList(Path.of("dir", "file"), Path.of("dir2", "file2")));
    stringValue = "dir" + File.separator + "file,dir2" + File.separator + "file2";

    assertEquals(value, Converters.getPathListConverter().fromString(stringValue));
    assertEquals(stringValue, Converters.getPathListConverter().toString(value));
  }

  @Test
  void testEnumConverter()
  {
    SizeSystem value;
    String stringValue;

    value = SizeSystem.BINARY;
    stringValue = "BINARY";

    assertEquals(value, Converters.getEnumConverter(SizeSystem.class).fromString(stringValue));
    assertEquals(stringValue, Converters.getEnumConverter(SizeSystem.class).toString(value));
  }

  @Test
  void testLocaleConverter()
  {
    Locale value;
    String stringValue;

    value = new Locale("nl");
    stringValue = "nl";

    assertEquals(value, Converters.getLocaleConverter().fromString(stringValue));
    assertEquals(stringValue, Converters.getLocaleConverter().toString(value));
  }

  @Test
  void testBooleanConverter()
  {
    Boolean value;
    String stringValue;

    value = Boolean.TRUE;
    stringValue = "true";

    assertEquals(value, Converters.getBooleanConverter().fromString(stringValue));
    assertEquals(stringValue, Converters.getBooleanConverter().toString(value));
  }
}
