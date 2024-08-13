package org.kku.jdiskusage.conf;

import java.util.Locale;
import org.kku.conf.ConfigurationItem;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Language
  extends ConfigurationItem
{
  private String mi_name;
  private String mi_language;
  private String mi_flag;
  private boolean mi_default;
  @JsonIgnore
  private Locale mi_locale;

  public Language()
  {
  }

  public void setName(String name)
  {
    mi_name = name;
  }

  public String getName()
  {
    return mi_name;
  }

  public String getLanguage()
  {
    return mi_language;
  }

  public void setLanguage(String language)
  {
    mi_language = language;
  }

  public void setFlag(String flag)
  {
    mi_flag = flag;
  }

  public String getFlag()
  {
    return mi_flag;
  }

  public void setDefault(boolean isDefault)
  {
    mi_default = isDefault;
  }

  public boolean isDefault()
  {
    return mi_default;
  }

  public Locale getLocale()
  {
    if (mi_locale == null)
    {
      mi_locale = new Locale(mi_language);
    }

    return mi_locale;
  }

  @Override
  public String toString()
  {
    return mi_name;
  }
}