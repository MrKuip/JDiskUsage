package org.kku.jdiskusage.ui.common;

import java.util.stream.Stream;
import org.kku.jdiskusage.ui.util.IconUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class CollapsableButtonPane
  extends BorderPane
{
  private BooleanProperty mi_expandedProperty = new SimpleBooleanProperty(true);

  public CollapsableButtonPane(String text, Node graphic)
  {
    HBox collapsableButtonPane;
    Button filterButton;

    // Start in collapsed state
    toggleExpanded();

    collapsableButtonPane = new HBox();
    collapsableButtonPane.setPadding(new Insets(5, 10, 5, 10));
    collapsableButtonPane.setPadding(new Insets(0, 10, 0, 10));
    collapsableButtonPane.setAlignment(Pos.CENTER);

    filterButton = new Button(null, IconUtil.createIconNode("filter-menu"));
    filterButton.setOnAction((ae) -> toggleExpanded());

    collapsableButtonPane.getChildren().add(filterButton);

    setLeft(collapsableButtonPane);

    rightProperty().addListener((o, oldValue, newValue) -> evaluateExpanded());
    topProperty().addListener((o, oldValue, newValue) -> evaluateExpanded());
    bottomProperty().addListener((o, oldValue, newValue) -> evaluateExpanded());
    centerProperty().addListener((o, oldValue, newValue) -> evaluateExpanded());
  }

  public BooleanProperty expandedProperty()
  {
    return mi_expandedProperty;
  }

  private void toggleExpanded()
  {
    mi_expandedProperty.set(!mi_expandedProperty.get());
    HBox.setHgrow(this, mi_expandedProperty.get() ? Priority.ALWAYS : Priority.NEVER);
    evaluateExpanded();
  }

  private void evaluateExpanded()
  {
    Stream.of(getRight(), getTop(), getBottom(), getCenter()).forEach(node -> {
      if (node != null)
      {
        node.setManaged(mi_expandedProperty.get());
        node.setVisible(mi_expandedProperty.get());
      }
    });
  }
}