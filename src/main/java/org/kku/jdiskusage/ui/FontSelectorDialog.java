package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.kku.common.util.StringUtils;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.util.Callback;

public class FontSelectorDialog
  extends Dialog<Font>
{
  private List<FontFamily> mi_familyList;
  private ListView<FontFamily> m_familyListView;
  private ListView<FontStyle> m_styleListView;
  private ListView<Double> m_sizeListView;
  private SimpleObjectProperty<Font> m_selectedFont = new SimpleObjectProperty<>();

  public FontSelectorDialog(Font initialFont)
  {
    MigPane pane;
    Label sampleText;
    FontFamily initialFontFamily;
    FontStyle initialFontStyle;

    assert initialFont != null;
    m_selectedFont.set(initialFont);

    pane = new MigPane("", "[70%, grow, fill][20%, grow, fill][10%, grow, fill]", "[][fill][80px:80px:80px]");

    sampleText = new Label("The quick brown fox jumps over the lazy dog");
    sampleText.fontProperty().bind(m_selectedFont);

    initialFont.getFamily();
    initialFontFamily = getFontFamilyList().stream().filter(ff -> ff.getFamilyName().equals(initialFont.getFamily()))
        .findFirst().orElseGet(null);
    initialFontStyle = initialFontFamily == null ? null : initialFontFamily.getFontStyle(initialFont.getStyle());

    m_familyListView = new ListView<>();
    initFontListView(m_familyListView);
    m_familyListView.getItems().addAll(getFontFamilyList());
    m_familyListView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> updateSelectedFont());

    m_styleListView = new ListView<>();
    m_familyListView.getSelectionModel().selectedItemProperty().addListener((a, oldValue, newValue) -> {
      FontStyle selectedItem = m_styleListView.getSelectionModel().getSelectedItem();
      m_styleListView.getItems().setAll(newValue.getFontStyleList());
      if (selectedItem != null)
      {
        m_styleListView.getSelectionModel().select(selectedItem);
      }
      if (m_styleListView.getSelectionModel().getSelectedIndex() == -1)
      {
        m_styleListView.getSelectionModel().select(0);
      }
    });
    m_styleListView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> updateSelectedFont());

    m_sizeListView = new ListView<>();
    m_sizeListView.getItems().addAll(8d, 9d, 11d, 12d, 13d, 14d, 16d, 18d, 20d, 22d, 24d, 26d, 28d, 36d, 48d, 72d);
    m_sizeListView.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> updateSelectedFont());

    Platform.runLater(() -> {
      m_familyListView.getSelectionModel().select(initialFontFamily);
      m_familyListView.scrollTo(m_familyListView.getSelectionModel().getSelectedIndex());
      m_styleListView.getSelectionModel().select(initialFontStyle);
      m_styleListView.scrollTo(m_familyListView.getSelectionModel().getSelectedIndex());
      m_sizeListView.getSelectionModel().select(initialFont.getSize());
      m_sizeListView.scrollTo(m_familyListView.getSelectionModel().getSelectedIndex());
    });

    pane.add(translate(new Label("Family")));
    pane.add(translate(new Label("Style")));
    pane.add(translate(new Label("Size")), "wrap");
    pane.add(m_familyListView, "");
    pane.add(m_styleListView, "");
    pane.add(m_sizeListView, " wrap");
    pane.add(translate(sampleText), "spanx");
    pane.setMinSize(600, 400);

    setResultConverter(
        dialogButton -> dialogButton == ButtonType.OK ? m_familyListView.getSelectionModel().getSelectedItem().getFont()
            : null);

    getDialogPane().setHeaderText(translate("Select font"));
    getDialogPane().setGraphic(IconUtil.createIconNode("help-circle-outline", IconSize.VERY_LARGE));
    getDialogPane().setContent(pane);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
  }

  private void updateSelectedFont()
  {
    FontFamily family;
    FontStyle style;
    Double size;

    if (m_familyListView != null && m_styleListView != null && m_sizeListView != null)
    {
      family = m_familyListView.getSelectionModel().getSelectedItem();
      style = m_styleListView.getSelectionModel().getSelectedItem();
      size = m_sizeListView.getSelectionModel().getSelectedItem();

      System.out.println("familie=" + family.getFamilyName() + "<");

      if (family != null && style != null && size != null)
      {
        m_selectedFont.set(style.getFont(size));
      }
    }
  }

  private void initFontListView(ListView<FontFamily> fontListView)
  {
    fontListView.setCellFactory(new Callback<ListView<FontFamily>, ListCell<FontFamily>>()
    {
      @Override
      public ListCell<FontFamily> call(ListView<FontFamily> listview)
      {
        return new ListCell<FontFamily>()
        {
          @Override
          protected void updateItem(FontFamily family, boolean empty)
          {
            super.updateItem(family, empty);

            if (!empty)
            {
              setFont(family.getFont());
              setText(family.getFamilyName());
            }
            else
            {
              setText(null);
            }
          }
        };
      }
    });
  }

  private List<FontFamily> getFontFamilyList()
  {
    if (mi_familyList == null)
    {
      mi_familyList = Font.getFamilies().stream().filter(Predicate.not(StringUtils::isEmpty))
          .map(ff -> new FontFamily(ff)).toList();
    }

    return mi_familyList;
  }

  private static class FontFamily
  {
    private Font mi_font;
    private static Map<String, List<FontStyle>> mi_FontStyleListByFamilyNameMap = new HashMap<>();

    private FontFamily(String fontFamily)
    {
      mi_font = Font.font(fontFamily);
    }

    public String getFamilyName()
    {
      return getFont().getFamily();
    }

    public List<FontStyle> getFontStyleList()
    {
      return mi_FontStyleListByFamilyNameMap.computeIfAbsent(getFamilyName(), (key) -> {
        return Font.getFontNames(key).stream().map(Font::font).map(FontStyle::new)
            .sorted(Comparator.comparing(FontStyle::getStyle)).toList();
      });
    }

    public FontStyle getFontStyle(String style)
    {
      return getFontStyleList().stream().filter(fs -> style.equals(fs.getStyle())).findFirst().orElseGet(null);
    }

    public Font getFont()
    {
      return mi_font;
    }

    @Override
    public String toString()
    {
      return getFamilyName();
    }
  }

  private static class FontStyle
  {
    private final Font mi_font;
    private final Map<Double, Font> mi_fontBySize = new HashMap<>();

    private FontStyle(Font font)
    {
      mi_font = font;
    }

    public Font getFont(Double size)
    {
      return mi_fontBySize.computeIfAbsent(size, key -> {
        return new Font(mi_font.getName(), key);
      });
    }

    public String getStyle()
    {
      return mi_font.getStyle();
    }

    @Override
    public String toString()
    {
      return mi_font.getStyle();
    }
  }
}