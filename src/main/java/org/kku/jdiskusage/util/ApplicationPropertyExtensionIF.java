package org.kku.jdiskusage.util;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import org.controlsfx.control.Notifications;
import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.util.ApplicationProperties.Props;
import javafx.application.Platform;
import javafx.util.Duration;

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
    PREF_SIZE,
    SELECTED_ID;

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

  default public void showInformationNotification(String title, String text)
  {
    Platform.runLater(() -> Notifications.create().title(translate(title)).text(translate(text))
        .graphic(new FxIcon("information").size(IconSize.LARGE).getImageView()).hideAfter(Duration.seconds(10l))
        .show());
  }

}
