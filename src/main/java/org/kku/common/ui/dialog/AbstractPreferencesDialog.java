package org.kku.common.ui.dialog;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import org.kku.fx.ui.util.FxIconUtil;
import org.kku.fx.util.AppProperties.AppProperty;
import org.kku.jdiskusage.main.Main;
import org.kku.jdiskusage.ui.util.TranslateUtil;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;

abstract public class AbstractPreferencesDialog
{
  private Dialog<ButtonType> m_dialog;

  public AbstractPreferencesDialog()
  {
  }

  public void show()
  {
    TabPane content;

    content = new TabPane();
    initContent(content);

    m_dialog = new Dialog<>();
    m_dialog.setResizable(true);
    m_dialog.getDialogPane().setContent(content);
    m_dialog.initOwner(Main.getRootStage());
    m_dialog.initModality(Modality.APPLICATION_MODAL);
    m_dialog.setTitle("Preferences");
    TranslateUtil.bind(m_dialog.titleProperty());
    m_dialog.getDialogPane().getScene().getWindow().setOnCloseRequest((e) -> m_dialog.close());
    m_dialog.getDialogPane().setMinSize(600, 400);
    m_dialog.showAndWait();
  }

  abstract protected void initContent(TabPane content);

  protected String getCellConstraint(int x, int y)
  {
    return "cell " + x + " " + y + " ";
  }

  protected Tab createTab(String text, Node content)
  {
    Tab tab;

    tab = translate(new Tab(text));
    tab.setContent(content);
    tab.setClosable(false);

    return tab;
  }

  protected Node resetPreference(AppProperty<?> property)
  {
    Button button;

    button = translate(new Button("", FxIconUtil.createIconNode("restore")));
    button.setOnAction((ae) -> property.reset());

    return button;
  }
}
