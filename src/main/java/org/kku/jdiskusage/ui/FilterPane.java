package org.kku.jdiskusage.ui;

import static org.kku.fx.ui.util.TranslateUtil.translate;
import static org.kku.fx.ui.util.TranslateUtil.translatedTextProperty;
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
import org.kku.fx.ui.util.FxIconUtil;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.Filter;
import org.kku.jdiskusage.util.FileTree.FileNodeIF;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

class FilterPane
{
  private final DiskUsageData m_diskUsageData;
  private final Set<Filter> m_filterSet = new LinkedHashSet<>();
  private final Map<String, Set<Filter>> m_filterByTypeMap = new HashMap<>();
  private final Map<String, Pane> m_filterTypePaneByTypeMap = new HashMap<>();
  private final HBox m_filterActivationPane = new HBox();
  private final FlowPane m_filterPane = new FlowPane();
  private final MigPane m_contentPane;
  private Button m_activateFilterButton;
  private Button m_clearFilterButton;

  FilterPane(DiskUsageData diskUsageData)
  {
    m_diskUsageData = diskUsageData;

    m_contentPane = new MigPane("ins 0", "[][grow][]", "[grow]");

    m_filterPane.setId("filterPane");

    m_contentPane.add(FxIconUtil.createIconNode("filter", IconSize.SMALL), "aligny baseline");
    m_contentPane.add(m_filterPane, "grow");
    m_contentPane.add(m_filterActivationPane, "top");

    updateFilterActivationPane();
  }

  public Node getNode()
  {
    return m_contentPane;
  }

  private Set<Filter> getFilterSet()
  {
    return m_filterSet;
  }

  public void addFilter(Filter filter, boolean activateFilterImmediately)
  {
    m_filterSet.add(filter);

    reInitFilterPanel();

    if (activateFilterImmediately && m_activateFilterButton != null)
    {
      m_activateFilterButton.fire();
    }
  }

  private void reInitFilterPanel()
  {
    m_filterPane.getChildren().clear();
    m_filterActivationPane.getChildren().clear();
    m_filterByTypeMap.clear();
    m_filterTypePaneByTypeMap.clear();

    m_filterSet.forEach(filter -> {
      if (m_filterByTypeMap.computeIfAbsent(filter.getFilterKey(), (k) -> new HashSet<>()).add(filter))
      {
        Pane filterTypePane;
        Node filterValueNode;

        filterValueNode = createFilterValueNode(filter);

        filterTypePane = m_filterTypePaneByTypeMap.computeIfAbsent(filter.getFilterKey(),
            filterKey -> createFilterTypePane(filter));
        if (m_filterByTypeMap.get(filter.getFilterKey()).size() > 1)
        {
          filterTypePane.getChildren().add(getFilterTypePaneText(translatedTextProperty("or")));
        }
        filterTypePane.getChildren().add(filterValueNode);
      }
    });

    updateFilterActivationPane();
  }

  private Label getFilterTypePaneText(StringExpression textExpression)
  {
    Label label;

    label = new Label();
    label.textProperty().bind(textExpression);
    label.setId("filterTypePaneText");

    return label;
  }

  private Pane createFilterTypePane(Filter filter)
  {
    HBox filterPane;

    filterPane = new HBox();
    filterPane.setId("filterTypePane");

    filterPane.getChildren().add(getFilterTypePaneText(Bindings.format("%s %s ",
        translatedTextProperty(filter.getFilterType()), translatedTextProperty(filter.getFilterOperator()))));

    m_filterPane.getChildren().add(filterPane);

    return filterPane;
  }

  private Node createFilterValueNode(Filter filter)
  {
    Label filterNode;
    Node closeNode;

    // Wrap in HBox because adding a mouselistener to the ImageView does not work!
    // (Bug in JavaFX!)
    closeNode = new HBox(new FxIcon("close").size(IconSize.SMALLER).fillColor(IconColor.RED).getIconLabel());
    closeNode.setStyle("-fx-padding:2px 0px 1px 0px;");

    filterNode = translate(new Label(filter.getFilterValue(), closeNode));
    filterNode.setId("filterButton");
    filterNode.setContentDisplay(ContentDisplay.RIGHT);
    filterNode.setUserData(filter);
    filterNode.disableProperty().bind(filter.disabledProperty());

    closeNode.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
      removeFilters(filter);
      activateFilter();
    });

    return filterNode;
  }

  public void removeFilters(Filter... filters)
  {
    Stream.of(filters).forEach(filter -> {
      m_filterSet.remove(filter);
    });

    reInitFilterPanel();
  }

  public boolean accept(FileNodeIF fileNode)
  {
    Set<Entry<String, Set<Filter>>> entries;

    entries = m_filterByTypeMap.entrySet();
    for (Entry<String, Set<Filter>> entry : entries)
    {
      if (!(entry.getValue().stream().anyMatch(filter -> filter.accept(fileNode))))
      {
        return false;
      }
    }

    return true;
  }

  private void updateFilterActivationPane()
  {
    if (!m_filterPane.getChildren().isEmpty())
    {
      boolean disabled;

      if (m_filterActivationPane.getChildren().isEmpty())
      {
        m_activateFilterButton = translate(
            new Button("Activate filter", FxIconUtil.createIconNode("filter-check", IconSize.SMALL)));
        m_activateFilterButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        m_activateFilterButton.setOnAction((ae) -> {
          activateFilter();
        });
        m_filterActivationPane.getChildren().add(m_activateFilterButton);

        m_clearFilterButton = translate(
            new Button("Clear filter", FxIconUtil.createIconNode("filter-remove", IconSize.SMALL)));
        m_clearFilterButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        m_clearFilterButton.setOnAction((ae) -> {
          Filter[] filters;

          filters = getFilterSet().stream().filter(Filter::isDisabled).toArray(Filter[]::new);
          if (filters == null || filters.length == 0)
          {
            filters = getFilterSet().stream().toArray(Filter[]::new);
          }

          if (filters != null)
          {
            removeFilters(filters);
            m_diskUsageData.getTreePaneData().setFilter((fn) -> accept(fn));
          }

          activateFilter();
        });
        m_filterActivationPane.getChildren().add(m_clearFilterButton);
      }

      disabled = getFilterSet().stream().anyMatch(filter -> filter.isDisabled());
      m_activateFilterButton.setDisable(!disabled);
    }
    else
    {
      if (!m_filterActivationPane.getChildren().isEmpty())
      {
        m_filterActivationPane.getChildren().clear();
        m_activateFilterButton = null;
      }
    }
  }

  private void activateFilter()
  {
    TreeItem<FileNodeIF> selectedTreeItem;

    getFilterSet().forEach(filter -> { filter.disable(false); });

    selectedTreeItem = m_diskUsageData.getSelectedTreeItem();
    m_diskUsageData.getTreePaneData().setFilter((fn) -> accept(fn));
    updateFilterActivationPane();

    // try to navigate to the selected tree item (due to filtering it is possible it is no longer a node in the tree!)
    Platform.runLater(() -> {
      m_diskUsageData.getTreePaneData().navigateTo(selectedTreeItem);
    });
  }
}