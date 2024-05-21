package org.kku.jdiskusage.util;

import org.kku.jdiskusage.util.ApplicationProperties.Props;

public interface ApplicationPropertyExtensionIF
{
  public enum Property implements CharSequence
  {
    INITIAL_DIRECTORY,
    RECENT_FILES,
    WIDTH,
    HEIGHT,
    X,
    Y,
    SPLIT_PANE_POSITION,
    PREF_SIZE;

    @Override
    public int length()
    {
      return name().length();
    }

    @Override
    public char charAt(int index)
    {
      return name().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end)
    {
      return name().subSequence(end, end);
    }
  }

  default public Props getProps()
  {
    return getProps(getClass().getSimpleName());
  }

  default public Props getProps(String subject)
  {
    return ApplicationProperties.getInstance().getProps(subject);
  }
}
