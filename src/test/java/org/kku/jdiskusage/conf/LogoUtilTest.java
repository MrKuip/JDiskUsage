package org.kku.jdiskusage.conf;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.kku.fx.ui.util.LogoUtil;
import javafx.scene.image.Image;

class LogoUtilTest
{
  @Test
  void testDefault()
  {
    List<Image> list;

    list = LogoUtil.getLogoList();
    assertTrue(list.size() >= 4, "There should be more than 4 logo's. There are " + list.size() + " logo's");
  }
}
