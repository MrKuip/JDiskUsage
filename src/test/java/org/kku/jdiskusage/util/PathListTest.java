package org.kku.jdiskusage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class PathListTest
{
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
}
