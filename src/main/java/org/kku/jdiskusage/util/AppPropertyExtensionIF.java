package org.kku.jdiskusage.util;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;

import org.controlsfx.control.Notifications;
import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.main.Main;
import org.kku.jdiskusage.util.AppProperties.Props;

import javafx.application.Platform;
import javafx.util.Duration;

public interface AppPropertyExtensionIF
{

  default public Props getProps()
  {
    return getProps(getClass().getSimpleName());
  }

  default public Props getProps(String subject)
  {
    return AppProperties.getInstance().getProps(subject);
  }

  default public void showInformationNotification(String title, String text)
  {
    Platform.runLater(() -> Notifications.create().title(translate(title)).text(translate(text))
        .graphic(new FxIcon("information").size(IconSize.LARGE).getImageView()).hideAfter(Duration.seconds(10l))
        .owner(Main.getRootNode()).show());
  }

}
