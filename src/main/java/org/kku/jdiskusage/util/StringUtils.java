package org.kku.jdiskusage.util;

public class StringUtils
{
  static public boolean isNotBlank(String text)
  {
    return text != null && !text.isBlank();
  }

  public static boolean isAllLetters(String type)
  {
    int length = type.length();

    for (int i = 0; i < length; i++)
    {
      if (!(Character.isLetter(type.charAt(i))))
      {
        return false;
      }
    }

    return true;
  }
}
