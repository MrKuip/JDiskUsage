package org.kku.jdiskusage.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class StyledText
{
  private List<StyledTextItem> mi_elements = new ArrayList<>();
  private boolean mi_isPlainText = true;
  private String mi_plainText;

  public StyledText()
  {
  }

  public void addItem(String text)
  {
    mi_plainText = null;
    mi_elements.add(new StyledTextItem(text, ""));
  }

  public void addItem(String text, String style)
  {
    mi_isPlainText = false;
    mi_plainText = null;
    mi_elements.add(new StyledTextItem(text, style));
  }

  public TextFlow getTextFlow()
  {
    TextFlow textFlow;

    textFlow = new TextFlow();

    mi_elements.forEach(item -> {
      Text text;

      text = new Text(item.mii_text);
      text.setSmooth(true);
      text.setFontSmoothingType(FontSmoothingType.LCD);
      text.setStyle(item.mii_style);

      textFlow.getChildren().add(text);
    });

    return textFlow;
  }

  public boolean isPlainText()
  {
    return mi_isPlainText;
  }

  public String getPlainText()
  {
    if (mi_plainText == null)
    {
      mi_plainText = mi_elements.stream().map(item -> item.mii_text).collect(Collectors.joining());
    }

    return mi_plainText;
  }

  private static class StyledTextItem
  {
    private final String mii_text;
    private final String mii_style;

    private StyledTextItem(String text, String style)
    {
      mii_text = text;
      mii_style = style == null ? "" : style;
    }
  }
}