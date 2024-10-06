package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import org.kku.jdiskusage.conf.Language;
import org.kku.jdiskusage.conf.LanguageConfiguration;
import org.kku.jdiskusage.main.Main;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.ui.util.TranslateUtil;
import org.kku.jdiskusage.util.AppProperties.AppProperty;
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
    m_dialog.getDialogPane().setPrefSize(600, 400);
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
    ComboBox<Language> languageComboBox;
    NumericTextField<Integer> maxNumberInTopRankingField;
    Button resetAllButton;

    autoExpandCheckBox = new CheckBox("Auto expand selected tree node");
    autoExpandCheckBox.selectedProperty().bindBidirectional(AppPreferences.autoExpandTreeNode.property());

    autoCollapseCheckBox = new CheckBox("Auto collapse deselected tree node");
    autoCollapseCheckBox.selectedProperty().bindBidirectional(AppPreferences.autoCollapseTreeNode.property());

    languageComboBox = new ComboBox<>();
    languageComboBox.getItems().addAll(LanguageConfiguration.getInstance().getList());
    languageComboBox.setCellFactory(FxUtil.getCellFactoryWithImage(Language::getName, Language::getFlagImage));
    languageComboBox.setButtonCell(FxUtil.getListCellWithImage(Language::getName, Language::getFlagImage));
    languageComboBox.valueProperty().bindBidirectional(AppPreferences.languagePreference.property());

    maxNumberInTopRankingField = NumericTextField.integerField();
    maxNumberInTopRankingField.setPrefWidth(80.0);
    maxNumberInTopRankingField.valueProperty().bindBidirectional(AppPreferences.maxNumberInTopRanking.property());

    resetAllButton = translate(new Button("Reset all to default", IconUtil.createIconNode("restore")));
    resetAllButton.setOnAction((ae) -> {
      AppPreferences.autoExpandTreeNode.reset();
      AppPreferences.autoCollapseTreeNode.reset();
      AppPreferences.languagePreference.reset();
      AppPreferences.maxNumberInTopRanking.reset();
    });

    pane = new MigPane("wrap 3", "[][]push[align right]", "[][][][]push[]");

    pane.add(translate(autoExpandCheckBox), "spanx 2");
    pane.add(resetPreference(AppPreferences.autoExpandTreeNode));
    pane.add(translate(autoCollapseCheckBox), "spanx 2");
    pane.add(resetPreference(AppPreferences.autoCollapseTreeNode));
    pane.add(translate(new Label("Language")));
    pane.add(languageComboBox);
    pane.add(resetPreference(AppPreferences.languagePreference));
    pane.add(translate(new Label("Max number in top Ranking")));
    pane.add(maxNumberInTopRankingField);
    pane.add(resetPreference(AppPreferences.maxNumberInTopRanking));
    pane.add(resetAllButton, "spanx, align right");

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

    restoreButton = translate(new Button("Reset all to default", IconUtil.createIconNode("restore")));
    restoreButton.setOnAction((ae) -> {
      AppPreferences.maxNumberOfChartElements.reset();
      AppPreferences.minPercentageChartElement.reset();
    });

    pane = new MigPane("wrap 4", "[][][]push[align right]", "[][]push[]");

    pane.add(translate(new Label("Chart shows at most")));
    pane.add(maxNumberOfElementsField, "");
    pane.add(translate(new Label("elements")), "");
    pane.add(resetPreference(AppPreferences.maxNumberOfChartElements));
    pane.add(translate(new Label("Show elements larger than")));
    pane.add(minPercentageElementField, "");
    pane.add(new Label("%"), "");
    pane.add(resetPreference(AppPreferences.minPercentageChartElement));
    pane.add(restoreButton, "spanx, align right");

    tab = translate(new Tab("Charting"));
    tab.setContent(pane);

    return tab;
  }

  private Node resetPreference(AppProperty<?> property)
  {
    Button button;

    button = translate(new Button("", IconUtil.createIconNode("restore")));
    button.setOnAction((ae) -> property.reset());

    return button;
  }
}
