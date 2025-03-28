package org.kku.jdiskusage.util;

import java.io.InputStream;
import org.kku.common.util.ResourceProviderIF;

public class ResourceProvider
    implements ResourceProviderIF
{
  @Override
  public InputStream getResourceAsStream(String configurationName)
  {
    return this.getClass().getResourceAsStream(configurationName);
  }
}
