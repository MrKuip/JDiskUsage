package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.FxIcon.IconColor;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.CollapsableButtonPane;
import org.kku.jdiskusage.ui.common.Filter;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

class FilterPane
{
  private final DiskUsageData mi_diskUsageData;
  private final Set<Filter> mi_filterSet = new LinkedHashSet<>();
  private final Map<String, Set<Filter>> mi_filterByTypeMap = new HashMap<>();
  private final Map<String, Pane> mi_filterTypePaneByTypeMap = new HashMap<>();
  private final HBox mi_filterActivationPane = new HBox();
  private final HBox mi_filterPane = new HBox();
  private final BorderPane contentPane;
  private Button mi_activateFilterButton;
  private Button mi_cancelFilterButton;
  private Button mi_clearFilterButton;

  FilterPane(DiskUsageData diskUsageData)
  {
    HBox filterTextPane;

    mi_diskUsageData = diskUsageData;

    contentPane = new CollapsableButtonPane("Filter", IconUtil.createIconNode("filter", IconSize.SMALLER));

    filterTextPane = new HBox();
    filterTextPane.setPadding(new Insets(5, 10, 5, 10));
    filterTextPane.setAlignment(Pos.CENTER);
    filterTextPane.getChildren()
        .add(translate(new Label("Filter", IconUtil.createIconNode("filter", IconSize.SMALLER))));

    mi_filterPane.setId("filterPane");

    mi_filterActivationPane.setPadding(new Insets(0, 10, 0, 10));
    mi_filterActivationPane.setAlignment(Pos.CENTER);
    mi_filterActivationPane.setSpacing(2);

    //contentPane.setLeft(filterTextPane);
    contentPane.setCenter(mi_filterPane);
    contentPane.setRight(mi_filterActivationPane);

    updateFilterActivationPane();
  }

  public Node getNode()
  {
    return contentPane;
  }

  private Set<Filter> getFilterSet()
  {
    return mi_filterSet;
  }

  public void addFilter(Filter filter, boolean activateFilterImmediately)
  {
    mi_filterSet.add(filter);

    reInitFilterPanel();

    if (activateFilterImmediately && mi_activateFilterButton != null)
    {
      mi_activateFilterButton.fire();
    }
  }

  private void reInitFilterPanel()
  {
    mi_filterPane.getChildren().clear();
    mi_filterActivationPane.getChildren().clear();
    mi_filterByTypeMap.clear();
    mi_filterTypePaneByTypeMap.clear();

    mi_filterSet.forEach(filter -> {
      if (mi_filterByTypeMap.computeIfAbsent(filter.getFilterType(), (k) -> new HashSet<>()).add(filter))
      {
        Pane filterTypePane;
        Node filterValueNode;

        filterValueNode = createFilterValueNode(filter);

        filterTypePane = mi_filterTypePaneByTypeMap.computeIfAbsent(filter.getFilterType(),
            filterType -> createFilterTypePane(filter));
        if (mi_filterByTypeMap.get(filter.getFilterType()).size() > 1)
        {
          filterTypePane.getChildren().add(getFilterTypePaneText("or"));
        }
        filterTypePane.getChildren().add(filterValueNode);
      }
    });

    updateFilterActivationPane();
  }

  private Label getFilterTypePaneText(String text)
  {
    Label label;

    label = translate(new Label(text));
    label.setId("filterTypePaneText");

    return label;
  }

  private Pane createFilterTypePane(Filter filter)
  {
    HBox filterPane;

    filterPane = new HBox();
    filterPane.setId("filterTypePane");

    filterPane.getChildren()
        .add(getFilterTypePaneText(translate(filter.getFilterType()) + " " + translate("is") + " "));

    mi_filterPane.getChildren().add(filterPane);

    return filterPane;
  }

  private Node createFilterValueNode(Filter filter)
  {
    Label filterNode;
    Node closeNode;

    // Wrap in HBox because adding a mouselistener to the ImageView does not work!
    // (Bug in JavaFX!)
    closeNode = new HBox(new FxIcon("close").size(IconSize.SMALLER).fillColor(IconColor.RED).getImageView());
    closeNode.setStyle("-fx-padding:2px 0px 1px 0px;");

    filterNode = translate(new Label(filter.getFilterValue(), closeNode));
    filterNode.setId("filterButton");
    filterNode.setContentDisplay(ContentDisplay.RIGHT);
    filterNode.setUserData(filter);
    filterNode.disableProperty().bind(filter.disabledProperty());

    closeNode.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
      removeFilters(filter);
    });

    return filterNode;
  }

  public void removeFilters(Filter... filters)
  {
    Stream.of(filters).forEach(filter -> {
      mi_filterSet.remove(filter);
    });

    reInitFilterPanel();
  }

  public boolean accept(FileNodeIF fileNode)
  {
    Set<Entry<String, Set<Filter>>> entries;

    entries = mi_filterByTypeMap.entrySet();
    for (Entry<String, Set<Filter>> entry : entries)
    {
      if (entry.getValue().stream().filter(filter -> filter.accept(fileNode)).findFirst().isPresent())
      {
        continue;
      }

      return false;
    }

    return true;
  }

  private void updateFilterActivationPane()
  {
    if (!mi_filterPane.getChildren().isEmpty())
    {
      boolean disabled;

      if (mi_filterActivationPane.getChildren().isEmpty())
      {
        mi_activateFilterButton = translate(
            new Button("Activate filter", IconUtil.createIconNode("filter-plus", IconSize.SMALL)));
        mi_activateFilterButton.setOnAction((ae) -> getFilterSet().forEach(filter -> {
          filter.disable(false);
          mi_diskUsageData.getTreePaneData().setFilter((fn) -> accept(fn));
          updateFilterActivationPane();
        }));
        mi_filterActivationPane.getChildren().add(mi_activateFilterButton);

        mi_cancelFilterButton = translate(
            new Button("Cancel filter", IconUtil.createIconNode("filter-minus", IconSize.SMALL)));
        mi_cancelFilterButton.setOnAction((ae) -> {
          removeFilters(getFilterSet().stream().filter(Filter::isDisabled).toArray(Filter[]::new));
        });
        mi_filterActivationPane.getChildren().add(mi_cancelFilterButton);

        mi_clearFilterButton = translate(
            new Button("Clear filter", IconUtil.createIconNode("filter-remove", IconSize.SMALL)));
        mi_clearFilterButton.setOnAction((ae) -> {
          removeFilters(getFilterSet().stream().toArray(Filter[]::new));
          mi_diskUsageData.getTreePaneData().setFilter((fn) -> accept(fn));
        });
        mi_filterActivationPane.getChildren().add(mi_clearFilterButton);
      }

      disabled = getFilterSet().stream().filter(filter -> filter.isDisabled()).findFirst().isPresent();
      mi_activateFilterButton.setDisable(!disabled);
      mi_cancelFilterButton.setDisable(!disabled);
    }
    else
    {
      if (!mi_filterActivationPane.getChildren().isEmpty())
      {
        mi_filterActivationPane.getChildren().clear();
        mi_activateFilterButton = null;
        mi_cancelFilterButton = null;
      }
    }
  }
}