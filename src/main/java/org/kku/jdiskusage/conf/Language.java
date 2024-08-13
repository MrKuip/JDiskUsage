package org.kku.jdiskusage.conf;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.kku.conf.ConfigurationItem;
import org.kku.jdiskusage.util.Log;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.scene.image.Image;

public class Language
  extends ConfigurationItem
{
  @JsonIgnore
  private static Map<String, Image> iconByNameMap = new HashMap<>();
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

  public Image getFlagImage()
  {
    return iconByNameMap.computeIfAbsent(getFlag(), flag -> {
      String flagName;

      flagName = "/flags/" + flag + ".png";
      try (InputStream is = Language.class.getResourceAsStream(flagName))
      {
        return new Image(is);
      }
      catch (Exception e)
      {
        Log.log.error(e, "Cannot load image " + flagName);
        throw new RuntimeException(e);
      }
    });
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