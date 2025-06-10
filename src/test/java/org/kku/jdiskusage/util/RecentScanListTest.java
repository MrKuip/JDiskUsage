package org.kku.jdiskusage.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class RecentScanListTest
{
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

}
