package org.kku.jdiskusage.ui.dialog;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import org.kku.common.conf.Language;
import org.kku.common.conf.LanguageConfiguration;
import org.kku.common.ui.dialog.AbstractPreferencesDialog;
import org.kku.fx.ui.util.FxIconUtil;
import org.kku.fx.ui.util.FxLanguageUtil;
import org.kku.fx.ui.util.FxUtil;
import org.kku.jdiskusage.javafx.scene.control.NumericTextField;
import org.kku.jdiskusage.ui.util.ColorPalette;
import org.kku.jdiskusage.ui.util.ColorPalette.ChartColor;
import org.kku.jdiskusage.util.preferences.AppPreferences;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class PreferencesDialog
  extends AbstractPreferencesDialog
{
  private Dialog<ButtonType> m_dialog;

  public PreferencesDialog()
  {
  }

  @Override
  protected final void initContent(TabPane content)
  {
    content.getTabs().addAll(getGeneralTab(), getChartingTab(), getColorsTab());
  }

  private Tab getGeneralTab()
  {
    MigPane pane;
    CheckBox autoExpandCheckBox;
    CheckBox autoCollapseCheckBox;
    CheckBox showProgressInCellCheckBox;
    ComboBox<Language> languageComboBox;
    NumericTextField<Integer> maxNumberInTopRankingField;
    Button resetAllButton;

    autoExpandCheckBox = translate(new CheckBox("Auto expand selected tree node"));
    autoExpandCheckBox.selectedProperty().bindBidirectional(AppPreferences.autoExpandTreeNode.property());

    autoCollapseCheckBox = translate(new CheckBox("Auto collapse deselected tree node"));
    autoCollapseCheckBox.selectedProperty().bindBidirectional(AppPreferences.autoCollapseTreeNode.property());

    showProgressInCellCheckBox = translate(new CheckBox("Show percent graphic in table"));
    showProgressInCellCheckBox.selectedProperty().bindBidirectional(AppPreferences.showProgressInTable.property());

    languageComboBox = new ComboBox<>();
    languageComboBox.getItems().addAll(LanguageConfiguration.getInstance().getList());
    languageComboBox.setCellFactory(FxUtil.getCellFactoryWithImage(Language::getName, FxLanguageUtil::getFlagImage));
    languageComboBox.setButtonCell(FxUtil.getListCellWithImage(Language::getName, FxLanguageUtil::getFlagImage));
    languageComboBox.valueProperty().bindBidirectional(AppPreferences.languagePreference.property());

    maxNumberInTopRankingField = NumericTextField.integerField();
    maxNumberInTopRankingField.setPrefWidth(80.0);
    maxNumberInTopRankingField.valueProperty().bindBidirectional(AppPreferences.maxNumberInTopRanking.property());

    resetAllButton = translate(new Button("Reset all to default", FxIconUtil.createIconNode("restore")));
    resetAllButton.setOnAction((ae) -> {
      AppPreferences.autoExpandTreeNode.reset();
      AppPreferences.autoCollapseTreeNode.reset();
      AppPreferences.languagePreference.reset();
      AppPreferences.maxNumberInTopRanking.reset();
    });

    pane = new MigPane("wrap 3", "[][]push[align right]", "[][][][][]push[]");

    pane.add(autoExpandCheckBox, "spanx 2");
    pane.add(resetPreference(AppPreferences.autoExpandTreeNode));
    pane.add(autoCollapseCheckBox, "spanx 2");
    pane.add(resetPreference(AppPreferences.autoCollapseTreeNode));
    pane.add(showProgressInCellCheckBox, "spanx 2");
    pane.add(resetPreference(AppPreferences.showProgressInTable));
    pane.add(translate(new Label("Language")));
    pane.add(languageComboBox);
    pane.add(resetPreference(AppPreferences.languagePreference));
    pane.add(translate(new Label("Max number in top Ranking")));
    pane.add(maxNumberInTopRankingField);
    pane.add(resetPreference(AppPreferences.maxNumberInTopRanking));
    pane.add(resetAllButton, "spanx, align right");

    return createTab("General", pane);
  }

  private Tab getChartingTab()
  {
    MigPane pane;
    NumericTextField<Integer> maxNumberOfElementsField;
    NumericTextField<Double> minPercentageElementField;
    NumericTextField<Integer> maxNumberOfLevelsSunburstChartField;
    Button restoreButton;

    maxNumberOfElementsField = NumericTextField.integerField();
    maxNumberOfElementsField.setPrefWidth(80.0);
    maxNumberOfElementsField.valueProperty().bindBidirectional(AppPreferences.maxNumberOfChartElements.property());

    minPercentageElementField = NumericTextField.doubleField();
    minPercentageElementField.setPrefWidth(80.0);
    minPercentageElementField.valueProperty().bindBidirectional(AppPreferences.minPercentageChartElement.property());

    maxNumberOfLevelsSunburstChartField = NumericTextField.integerField();
    maxNumberOfLevelsSunburstChartField.setPrefWidth(80.0);
    maxNumberOfLevelsSunburstChartField.valueProperty()
        .bindBidirectional(AppPreferences.maxNumberOfElementsInSunburstChart.property());

    minPercentageElementField = NumericTextField.doubleField();

    restoreButton = translate(new Button("Reset all to default", FxIconUtil.createIconNode("restore")));
    restoreButton.setOnAction((ae) -> {
      AppPreferences.maxNumberOfChartElements.reset();
      AppPreferences.minPercentageChartElement.reset();
    });

    pane = new MigPane("wrap 4", "[][][]push[align right]", "[][][]push[]");

    pane.add(translate(new Label("Chart shows at most")));
    pane.add(maxNumberOfElementsField, "");
    pane.add(translate(new Label("elements")), "");
    pane.add(resetPreference(AppPreferences.maxNumberOfChartElements));

    pane.add(translate(new Label("Show elements larger than")));
    pane.add(minPercentageElementField, "");
    pane.add(new Label("%"), "");
    pane.add(resetPreference(AppPreferences.minPercentageChartElement));

    pane.add(translate(new Label("Sunburst chart has at most")));
    pane.add(maxNumberOfLevelsSunburstChartField, "");
    pane.add(translate(new Label("levels")));
    pane.add(resetPreference(AppPreferences.maxNumberOfElementsInSunburstChart));

    pane.add(restoreButton, "spanx, align right");

    return createTab("Charting", pane);
  }

  private Tab getColorsTab()
  {
    MigPane pane;
    int colorSize;
    int colorSizePerColumn;
    int x;
    int y;

    pane = new MigPane("", "[][][][]30[][][][]", "[][][][][][][][][][][]");
    colorSize = ColorPalette.getColorList().size();
    colorSizePerColumn = colorSize / 2;
    x = 0;
    y = 0;
    for (int index = 0; index < colorSize; index++)
    {
      Label colorLabel;
      ChartColor color;
      ColorPicker colorPicker;
      Button restoreButton;

      color = ColorPalette.getColorList().get(index);

      colorLabel = new Label();
      colorLabel.setMinWidth(80.0);
      colorLabel.styleProperty().bind(color.backgroundCssProperty());

      colorPicker = new ColorPicker();
      colorPicker.setValue(color.getColor());
      color.colorProperty().addListener((obs, oldValue, newValue) -> {
        colorPicker.setValue(newValue);
      });
      colorPicker.setOnAction((ae2) -> {
        color.setColor(colorPicker.getValue());
      });

      restoreButton = translate(new Button("", FxIconUtil.createIconNode("restore")));
      restoreButton.setOnAction((ae) -> {
        color.reset();
      });

      pane.add(new Label(String.valueOf(index + 1)), getCellConstraint(x++, y));
      pane.add(colorLabel, "growy, growx, " + getCellConstraint(x++, y));
      pane.add(colorPicker, getCellConstraint(x++, y));
      pane.add(restoreButton, "align right, " + getCellConstraint(x++, y));

      x = index < colorSizePerColumn - 1 ? 0 : 4;
      y = (y == colorSizePerColumn - 1) ? 0 : y + 1;
    }

    Button restoreButton;

    restoreButton = translate(new Button("Reset all to default", FxIconUtil.createIconNode("restore")));
    restoreButton.setOnAction((ae) -> {
      ColorPalette.getColorList().forEach(ChartColor::reset);
    });

    pane.add(restoreButton, "spanx, align right, " + getCellConstraint(0, colorSizePerColumn));

    return createTab("Colors", pane);
  }
}
