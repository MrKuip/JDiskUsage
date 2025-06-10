package org.kku.jdiskusage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.kku.common.conf.Language;
import org.kku.common.conf.LanguageConfiguration;
import org.kku.common.util.preferences.SizeSystem;
import org.kku.fx.util.Converters;

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

    assertEquals(value, PathList.getConverter().fromString(stringValue));
    assertEquals(stringValue, PathList.getConverter().toString(value));
  }

  @Test
  void testRecentScanListConverter()
  {
    RecentScanList value;
    PathList pathList1;
    PathList pathList2;
    String stringValue;

    pathList1 = new PathList(Arrays.asList(Path.of("dir", "file"), Path.of("dir2", "file2")));
    pathList2 = new PathList(Arrays.asList(Path.of("dir3", "file3"), Path.of("dir4", "file4")));
    value = new RecentScanList(Arrays.asList(pathList1, pathList2));
    stringValue = "dir" + File.separator + "file,dir2" + File.separator + "file2" + "###" + "dir3" + File.separator
        + "file3,dir4" + File.separator + "file4";

    assertEquals(value, RecentScanList.getConverter().fromString(stringValue));
    assertEquals(stringValue, RecentScanList.getConverter().toString(value));
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
  void testLanguageConverter()
  {
    Language value;
    String stringValue;

    value = LanguageConfiguration.getInstance().getDefault();
    stringValue = value.getName();

    assertEquals(value, Converters.getLanguageConverter().fromString(stringValue));
    assertEquals(stringValue, Converters.getLanguageConverter().toString(value));
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
