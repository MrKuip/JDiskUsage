package org.kku.jdiskusage.ui.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatterFactory
{
  private FormatterFactory()
  {
  }

  static public FormatterIF<Long> createByteFormatter()
  {
    return new ByteFormatter();
  }

  static public <T> FormatterIF<T> createStringFormatFormatter(String format)
  {
    return new StringFormatFormatter<>(format);
  }

  static public FormatterIF<Date> createSimpleDateFormatter(String pattern)
  {
    return new DateFormatter(pattern);
  }

  static private class ByteFormatter
      implements FormatterIF<Long>
  {
    private ByteFormatter()
    {
    }

    @Override
    public String format(Long fileSize)
    {
      //String size = AppPreferences.sizeSystemPreference.get().getFileSize(fileSize);
      return "";
    }
  }

  static private class DateFormatter
      implements FormatterIF<Date>
  {
    private final SimpleDateFormat m_format;

    private DateFormatter(String pattern)
    {
      m_format = new SimpleDateFormat(pattern);
    }

    @Override
    public String format(Date date)
    {
      return m_format.format(date);
    }
  }

  static private class StringFormatFormatter<T>
      implements FormatterIF<T>
  {
    private final String m_format;

    private StringFormatFormatter(String format)
    {
      m_format = format;
    }

    @Override
    public String format(T object)
    {
      return String.format(m_format, object);
    }
  }
}
