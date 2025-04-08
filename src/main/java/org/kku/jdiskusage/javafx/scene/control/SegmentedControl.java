package org.kku.jdiskusage.javafx.scene.control;

import static org.kku.fx.ui.util.TranslateUtil.translate;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

public class SegmentedControl
{
  private HBox m_segmentedControl = new HBox();
  private String LEFT_PILL_STYLE = "left-pill";
  private String CENTER_PILL_STYLE = "center-pill";
  private String RIGHT_PILL_STYLE = "right-pill";
  private ToggleGroup m_toggleGroup = new ToggleGroup();

  public SegmentedControl()
  {
  }

  public ToggleButton addToggle(String text)
  {
    return add(translate(new ToggleButton(text)));
  }

  public ToggleButton addToggle(Node iconNode)
  {
    return add(translate(new ToggleButton(null, iconNode)));
  }

  public ToggleButton add(ToggleButton button)
  {
    button.setToggleGroup(m_toggleGroup);

    m_segmentedControl.getChildren().add(button);
    evaluateStyles();

    return button;
  }

  public Node getNode()
  {
    return m_segmentedControl;
  }

  private void evaluateStyles()
  {
    List<Node> children;

    children = m_segmentedControl.getChildren();
    children.forEach(child -> {
      child.getStyleClass().removeAll(LEFT_PILL_STYLE, CENTER_PILL_STYLE, RIGHT_PILL_STYLE);
    });

    if (children.size() > 1)
    {
      children.get(0).getStyleClass().add(LEFT_PILL_STYLE);
      children.get(children.size() - 1).getStyleClass().add(RIGHT_PILL_STYLE);

      if (children.size() > 2)
      {
        children.subList(1, children.size() - 1).forEach(child -> {
          child.getStyleClass().add(CENTER_PILL_STYLE);
        });
      }
    }
  }
}