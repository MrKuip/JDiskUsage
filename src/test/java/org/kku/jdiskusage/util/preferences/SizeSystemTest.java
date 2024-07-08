package org.kku.jdiskusage.util.preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class SizeSystemTest
{

  @Test
  void getFileSize()
  {
    assertEquals("1000 bytes", SizeSystem.SI.getFileSize(1000));

    assertEquals("1.0 Kb", SizeSystem.SI.getFileSize(1001));
    assertEquals("1.0 Kb", SizeSystem.SI.getFileSize(1011));
    assertEquals("1.1 Kb", SizeSystem.SI.getFileSize(1111));
    assertEquals("1.1 Kb", SizeSystem.SI.getFileSize(1050));
    assertEquals("1.0 Kb", SizeSystem.SI.getFileSize(1049));
    assertEquals("11.0 Kb", SizeSystem.SI.getFileSize(11000));
    assertEquals("111.0 Kb", SizeSystem.SI.getFileSize(111000));

    assertEquals("1.1 Mb", SizeSystem.SI.getFileSize(1111000));
    assertEquals("1.1 Mb", SizeSystem.SI.getFileSize(1149999));
    assertEquals("1.2 Mb", SizeSystem.SI.getFileSize(1150000));
    assertEquals("11.2 Mb", SizeSystem.SI.getFileSize(11150000));
    assertEquals("11.1 Mb", SizeSystem.SI.getFileSize(11149999));
    assertEquals("111.2 Mb", SizeSystem.SI.getFileSize(111150000));

    assertEquals("1.2 Gb", SizeSystem.SI.getFileSize(1150000000));
    assertEquals("1.1 Gb", SizeSystem.SI.getFileSize(1149999999));

    assertEquals("1000 bytes", SizeSystem.BINARY.getFileSize(1000));
    assertEquals("1024 bytes", SizeSystem.BINARY.getFileSize(1024));
    assertEquals("1.0 Kb", SizeSystem.BINARY.getFileSize(1025));
    assertEquals("1.0 Kb", SizeSystem.BINARY.getFileSize(1075));
    assertEquals("1.1 Kb", SizeSystem.BINARY.getFileSize(1076));
    assertEquals("1024.0 Kb", SizeSystem.BINARY.getFileSize(1024 * 1024));

    assertEquals("1.0 Mb", SizeSystem.BINARY.getFileSize(1024 * 1024 + 1));
    assertEquals("1.1 Mb", SizeSystem.BINARY.getFileSize(1024 * 1024 * 1.1));
    assertEquals("1.0 Mb", SizeSystem.BINARY.getFileSize(1024 * 1024 * 1.049));
    assertEquals("1.1 Mb", SizeSystem.BINARY.getFileSize(1024 * 1024 * 1.050));
    assertEquals("1024.0 Mb", SizeSystem.BINARY.getFileSize(1024 * 1024 * 1024));

    assertEquals("1.0 Gb", SizeSystem.BINARY.getFileSize(1024 * 1024 * 1024 + 1));
    assertEquals("1.1 Gb", SizeSystem.BINARY.getFileSize(1024 * 1024 * 1024 * 1.1));
    assertEquals("1.0 Gb", SizeSystem.BINARY.getFileSize(1024 * 1024 * 1024 * 1.049));
    assertEquals("1.1 Gb", SizeSystem.BINARY.getFileSize(1024 * 1024 * 1024 * 1.050));
  }
}
