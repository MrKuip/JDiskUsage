package org.kku.jdiskusage.ui.util;

public interface FormatterIF<T>
{
  public String getFormat();

  public String format(T t);
}