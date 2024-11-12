package org.kku.jdiskusage.ui.util;

public abstract class AbstractFormatter<T>
    implements FormatterIF<T>
{
  private final String m_format;

  public AbstractFormatter(String format)
  {
    m_format = format;
  }

  @Override
  public String getFormat()
  {
    return m_format;
  }

}