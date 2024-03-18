package org.kku.jdiskusage.util;

import java.nio.file.Path;

public interface ScanListenerIF
{
  public boolean progress(Path currentFile, int numberOfDirectoriesEvaluated, int numberOfFilesEvaluated,
      boolean scanReady);
}
