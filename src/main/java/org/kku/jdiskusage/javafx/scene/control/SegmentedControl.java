package org.kku.jdiskusage.javafx.scene.control;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

public class SegmentedControl
{
  private HBox m_segmentedControl = new HBox();
  private String SEGMENTED_CONTROL_STYLE = "segmented-control";
  private String FIRST_BUTTON_STYLE = "left-pill";
  private String CENTER_BUTTON_STYLE = "center-pill";
  private String LAST_BUTTON_STYLE = "right-pill";
  private ToggleGroup m_toggleGroup = new ToggleGroup();

  public SegmentedControl()
  {
    m_segmentedControl.getStyleClass().add(SEGMENTED_CONTROL_STYLE);
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
    //button.setFocusTraversable(false);
    button.setToggleGroup(m_toggleGroup);

    m_segmentedControl.getChildren().add(button);

    return button;
  }

  public Node getNode()
  {
    evaluateStyles();
    return m_segmentedControl;
  }

  private void evaluateStyles()
  {
    List<Node> children;

    children = m_segmentedControl.getChildren();
    children.forEach(child -> {
      child.getStyleClass().removeAll(FIRST_BUTTON_STYLE, CENTER_BUTTON_STYLE, LAST_BUTTON_STYLE);
    });

    if (children.size() > 1)
    {
      children.get(0).getStyleClass().add(FIRST_BUTTON_STYLE);
      children.get(children.size() - 1).getStyleClass().add(LAST_BUTTON_STYLE);

      if (children.size() > 2)
      {
        children.subList(1, children.size() - 1).forEach(child -> {
          child.getStyleClass().add(CENTER_BUTTON_STYLE);
        });
      }
    }
  }

}