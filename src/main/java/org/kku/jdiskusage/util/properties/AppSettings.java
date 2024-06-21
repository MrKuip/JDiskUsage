package org.kku.jdiskusage.util.properties;

import java.nio.file.Path;

public class AppSettings extends AppProperties
{
  public final static AppProperty<Path> initialDirectory = new AppProperty<>();

  protected AppSettings()
  {
    super("JDiskUsageSettings.properties");
  }
}
