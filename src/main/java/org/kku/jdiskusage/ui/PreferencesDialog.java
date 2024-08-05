package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import org.kku.jdiskusage.main.Main;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.ui.util.TranslateUtil;
import org.kku.jdiskusage.util.Translator;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
  private LanguagePreferences m_languagePreferences = new LanguagePreferences();

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
    m_dialog.setTitle("Preferences");
    TranslateUtil.bind(m_dialog.titleProperty());
    m_dialog.getDialogPane().getScene().getWindow().setOnCloseRequest((e) -> m_dialog.close());
    m_dialog.getDialogPane().setPrefSize(600, 200);
    m_dialog.getDialogPane().setMinSize(600, 400);
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
    ComboBox<LanguagePreferences.Language> languageComboBox;
    NumericTextField<Integer> maxNumberInTopRankingField;
    Button restoreButton;

    autoExpandCheckBox = new CheckBox("Auto expand selected tree node");
    autoExpandCheckBox.selectedProperty().bindBidirectional(AppPreferences.autoExpandTreeNode.property());

    autoCollapseCheckBox = new CheckBox("Auto collapse deselected tree nodes");
    autoCollapseCheckBox.selectedProperty().bindBidirectional(AppPreferences.autoCollapseTreeNode.property());

    languageComboBox = new ComboBox<>();
    languageComboBox.getItems().addAll(m_languagePreferences.getLanguageList());
    languageComboBox.getSelectionModel().select(m_languagePreferences.getCurrentLanguage());
    languageComboBox.setOnAction((ae) -> {
      Locale locale;

      locale = languageComboBox.getSelectionModel().getSelectedItem().getLocale();
      Translator.getInstance().changeLocale(locale);
      AppPreferences.localePreference.set(locale);
    });

    maxNumberInTopRankingField = NumericTextField.integerField();
    maxNumberInTopRankingField.setPrefWidth(80.0);
    maxNumberInTopRankingField.valueProperty().bindBidirectional(AppPreferences.maxNumberInTopRanking.property());

    restoreButton = translate(new Button("Reset all to default", IconUtil.createIconNode("restore")));
    restoreButton.setOnAction((ae) -> {
      AppPreferences.autoExpandTreeNode.reset();
      AppPreferences.autoCollapseTreeNode.reset();
      AppPreferences.localePreference.reset();
      AppPreferences.maxNumberInTopRanking.reset();
    });

    pane = new MigPane("debug");
    pane.add(translate(autoExpandCheckBox), "newline, spanx 2");
    pane.add(translate(autoCollapseCheckBox), "newline, spanx 2");
    pane.add(TranslateUtil.translate(new Label("Language")), "newline");
    pane.add(languageComboBox);
    pane.add(TranslateUtil.translate(new Label("Max number in top Ranking")), "newline");
    pane.add(maxNumberInTopRankingField);
    pane.add(restoreButton, "newline push");

    tab = translate(new Tab("General"));
    tab.setContent(pane);

    return tab;
  }

  private Tab getChartingTab()
  {
    Tab tab;
    MigPane pane;
    NumericTextField<Integer> maxNumberOfElementsField;
    NumericTextField<Double> minPercentageElementField;
    Button restoreButton;

    maxNumberOfElementsField = NumericTextField.integerField();
    maxNumberOfElementsField.setPrefWidth(80.0);
    maxNumberOfElementsField.valueProperty().bindBidirectional(AppPreferences.maxNumberOfChartElements.property());

    minPercentageElementField = NumericTextField.doubleField();
    minPercentageElementField.setPrefWidth(80.0);
    minPercentageElementField.valueProperty().bindBidirectional(AppPreferences.minPercentageChartElement.property());

    pane = new MigPane("", "[][][][]", "[][]push[]");

    pane.add(translate(new Label("Chart shows at most")), "");
    pane.add(maxNumberOfElementsField, "");
    pane.add(new Label(translate("elements")), "wrap");

    pane.add(translate(new Label("Show elements larger than")), "");
    pane.add(minPercentageElementField, "");
    pane.add(new Label("%"), "wrap");

    restoreButton = translate(new Button("Reset all to default", IconUtil.createIconNode("restore")));
    restoreButton.setOnAction((ae) -> {
      AppPreferences.maxNumberOfChartElements.reset();
      AppPreferences.minPercentageChartElement.reset();
    });
    pane.add(restoreButton);

    tab = translate(new Tab("Charting"));
    tab.setContent(pane);

    return tab;
  }

  static private class LanguagePreferences
  {

    private Language getCurrentLanguage()
    {
      Locale locale;

      locale = Locale.getDefault();

      return getLanguageList().stream().filter(l -> Objects.equals(l.getLanguage(), locale.getLanguage())).findFirst()
          .orElse(getLanguageList().get(0));
    }

    private List<Language> getLanguageList()
    {
      Properties props;
      List<Language> list;

      try
      {
        props = new Properties();
        list = new ArrayList<>();
        try (InputStream stream = getClass().getResourceAsStream("/language.properties"))
        {
          props.load(stream);
          props.entrySet().stream().map(entry -> new Language((String) entry.getKey(), (String) entry.getValue()))
              .collect(Collectors.toCollection(() -> list));
        }
        // Always add the default language at index 0
        list.add(0, new Language("English", ""));

        return list;
      }
      catch (IOException e)
      {
        e.printStackTrace();
        return Collections.emptyList();
      }
    }

    static private class Language
    {
      private final String mi_name;
      private final String mi_language;
      private final Locale mi_locale;

      private Language(String name, String language)
      {
        mi_name = name;
        mi_language = language;
        mi_locale = new Locale(language);
      }

      public String getLanguage()
      {
        return mi_language;
      }

      public Locale getLocale()
      {
        return mi_locale;
      }

      @Override
      public String toString()
      {
        return mi_name;
      }
    }
  }
}
