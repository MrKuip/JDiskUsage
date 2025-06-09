package org.kku.jdiskusage.main;

import org.kku.common.util.ResourceLoader;

public class Main5
{

  public static void main(String[] args)
  {
    try
    {
      //System.out.println(ResourceLoader.getInstance().getResources("flags/ME.png"));
      //System.out.println(ResourceLoader.getInstance().getResources("LanguageConfiguration.json"));
      //System.out.println(ResourceLoader.getInstance().getResourceAsStream("LanguageConfiguration.json"));
      //System.out.println(ResourceLoader.getInstance().getResources("logo/Logo-30px.png"));
      //System.out.println(ResourceLoader.getInstance().getResources("/lo-go/logo/Logo-30px.png"));
      //System.out.println(ResourceLoader.getInstance().getResources("/logo/Logo-30px.png"));
      //LogoUtil.getLogoList().forEach(logo -> { System.out.println(logo); });
      System.out.println(ResourceLoader.getInstance().getResources("flags"));

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
