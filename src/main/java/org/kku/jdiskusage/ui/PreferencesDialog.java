package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.kku.jdiskusage.main.Main;
import org.kku.jdiskusage.ui.util.TranslateUtil;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;

public class PreferencesDialog
{
  private Dialog<ButtonType> m_dialog;

  public PreferencesDialog()
  {
  }

  public void show()
  {
    m_dialog = new Dialog<>();
    m_dialog.setResizable(true);
    m_dialog.getDialogPane().setContent(getContent());
    m_dialog.initOwner(Main.getRootStage());
    m_dialog.initModality(Modality.APPLICATION_MODAL);
    m_dialog.setTitle(translate("Preferences"));
    m_dialog.showAndWait();
  }

  private Node getContent()
  {
    TabPane tabPane;

    tabPane = new TabPane();
    tabPane.getTabs().addAll(getGeneralTab(), getChartingTab());

    return tabPane;
  }

  private Tab getGeneralTab()
  {
    Tab tab;
    MigPane pane;
    CheckBox autoExpandCheckBox;
    CheckBox autoCollapseCheckBox;
    ComboBox<Language> languageComboBox;

    autoExpandCheckBox = new CheckBox("Auto expand selected tree node");
    autoCollapseCheckBox = new CheckBox("Auto collapse deselected tree nodes");

    languageComboBox = new ComboBox<>();
    languageComboBox.getItems().addAll(getLanguageList());

    pane = new MigPane();
    pane.add(translate(autoExpandCheckBox), "newline, spanx 2");
    pane.add(translate(autoCollapseCheckBox), "newline, spanx 2");
    pane.add(TranslateUtil.translate(new Label("Language")), "newline");
    pane.add(languageComboBox);

    tab = translate(new Tab("General"));
    tab.setContent(pane);

    return tab;
  }

  private Tab getChartingTab()
  {
    Tab tab;

    tab = translate(new Tab("Charting"));

    return tab;
  }

  private List<Language> getLanguageList()
  {
    Properties props;

    try
    {
      props = new Properties();
      props.load(getClass().getResourceAsStream("language.properties"));
      return props.entrySet().stream().map(entry -> new Language((String) entry.getKey(), (String) entry.getValue()))
          .toList();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  static class Language
  {
    private final String mi_name;
    private final String mi_bundlePostfix;

    public Language(String name, String bundlePostfix)
    {
      mi_name = name;
      mi_bundlePostfix = bundlePostfix;
    }

    @Override
    public String toString()
    {
      return mi_name;
    }
  }
}
