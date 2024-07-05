package org.kku.jdiskusage.util;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.controlsfx.control.BreadCrumbBar;
import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.FxIcon.IconColor;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.main.Main;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class DirectoryChooser
{
  private final ObjectProperty<Path> m_directory = new SimpleObjectProperty<>();
  private final ObjectProperty<SelectionMode> m_selectionMode = new SimpleObjectProperty<>(SelectionMode.SINGLE);

  private Dialog<PathList> m_dialog;
  private final ToolBarPane m_toolBarPane = new ToolBarPane();
  private final SidePane m_sidePane = new SidePane();
  private final DirectoryPane m_directoryPane = new DirectoryPane();
  private final BreadCrumbPane m_breadCrumbPane = new BreadCrumbPane();

  public DirectoryChooser()
  {
  }

  /**
   * Shows a new directory selection dialog. The method doesn't return until the
   * displayed dialog is dismissed. The return value specifies the directory
   * chosen by the user or {@code null} if no selection has been made. If the
   * owner window for the directory selection dialog is set, input to all windows
   * in the dialog's owner chain is blocked while the dialog is being shown.
   *
   * @param ownerWindow the owner window of the displayed dialog
   * @return the selected directory or {@code null} if no directory has been
   *         selected
   */
  public PathList showDialog(final Window ownerWindow)
  {
    m_selectionMode.set(SelectionMode.SINGLE);
    return showAndWait();
  }

  /**
   * Shows a new directory selection dialog. The method doesn't return until the
   * displayed dialog is dismissed. The return value specifies the directories
   * chosen by the user or an empty list if no selection has been made. If the
   * owner window for the directory selection dialog is set, input to all windows
   * in the dialog's owner chain is blocked while the dialog is being shown.
   *
   * @param ownerWindow the owner window of the displayed dialog
   * @return the selected directories or an empty list if no directory has been
   *         selected
   */
  public PathList showOpenMultipleDialog(final Window ownerWindow)
  {
    m_selectionMode.set(SelectionMode.MULTIPLE);
    return showAndWait();
  }

  private PathList showAndWait()
  {
    MigPane content;
    PathList result;

    content = new MigPane("", "[grow]", "[grow]");
    content.getStyleClass().add("undecorated-dialog");
    content.add(m_toolBarPane, "dock north");
    content.add(new Separator(), "dock north");
    content.add(m_sidePane, "dock west");
    content.add(new Separator(Orientation.VERTICAL), "dock west");
    content.add(m_breadCrumbPane, "dock north");
    content.add(m_directoryPane, "grow");

    m_dialog = new Dialog<>();
    m_dialog.getDialogPane().getStyleClass().remove("dialog-pane");
    m_dialog.getDialogPane().setContent(content);
    m_dialog.initOwner(Main.getRootStage());
    m_dialog.initModality(Modality.APPLICATION_MODAL);
    m_dialog.initStyle(StageStyle.UNDECORATED);
    m_dialog.showAndWait();

    result = m_dialog.getResult();

    return result;
  }

  private class ToolBarPane
    extends MigPane
  {
    private ToolBarPane()
    {
      super("insets 0 0 6 0", "[][grow][]", "[]");
      init();
    }

    private void init()
    {
      Button cancelButton;
      Button openButton;
      Region spacer;

      cancelButton = new Button(translate("Cancel"),
          new FxIcon("close").size(IconSize.SMALL).fillColor(IconColor.RED).getImageView());
      cancelButton.setAlignment(Pos.BASELINE_LEFT);
      cancelButton.setOnAction((ae) -> {
        m_dialog.setResult(PathList.empty());
        m_dialog.close();
      });

      spacer = FxUtil.createHorizontalFiller();
      openButton = new Button(translate("Open"), new FxIcon("open-in-new").size(IconSize.SMALL).getImageView());
      openButton.setAlignment(Pos.BASELINE_LEFT);
      openButton.setOnAction((ae) -> {
        List<Path> result;

        result = new ArrayList<>();
        result.addAll(m_directoryPane.getSelectedPaths());
        result.sort(Comparator.comparing(Path::toString));
        m_dialog.setResult(new PathList(result));
        m_dialog.close();
      });

      add(cancelButton, "tag cancel, sg buttons");
      add(spacer, "grow");
      add(openButton, "tag ok, sg buttons");
    }
  }

  private class SidePane
    extends MigPane
  {
    private SidePane()
    {
      super("insets 0 0 0 10", "[pref, fill]", "[]");
      setId("side-pane");
      init();
    }

    private void init()
    {
      getRootNodes().forEach(rootNode -> {
        add(rootNode);
      });
      add(new Separator(), "wrap");
      getFavoriteNodes().forEach(rootNode -> {
        add(rootNode);
      });
    }

    private void add(DirectoryNode directoryNode)
    {
      directoryNode.setId("button");
      add(directoryNode, "grow, width 120px, height 30px, wrap");
    }

    private List<DirectoryNode> getRootNodes()
    {
      List<DirectoryNode> rootNodes;

      rootNodes = new ArrayList<>();
      FileSystems.getDefault().getRootDirectories().forEach(path -> rootNodes.add(new DirectoryNode(path)));

      return rootNodes;
    }

    private List<DirectoryNode> getFavoriteNodes()
    {
      List<DirectoryNode> rootNodes;

      rootNodes = new ArrayList<>();
      rootNodes.add(new DirectoryNode("Home", "home", Path.of(System.getProperty("user.home"))));

      return rootNodes;
    }
  }

  private class BreadCrumbPane
    extends BreadCrumbBar<MyPath>
  {
    private BreadCrumbPane()
    {
      init();
      setId("breadcrumb");
    }

    private void init()
    {
      setOnCrumbAction((e) -> {
        setDirectory(e.getSelectedCrumb().getValue().getPath());
      });

      m_directory.addListener((a, b, newDirectory) -> {
        setSelectedCrumb(convertToTreeItem(newDirectory));
      });
    }

    private TreeItem<MyPath> convertToTreeItem(Path path)
    {
      List<TreeItem<MyPath>> mi_treePathList;
      List<MyPath> pathList;
      Path parent;

      parent = path;
      pathList = new ArrayList<>();
      while (parent != null)
      {
        pathList.add(new MyPath(parent));
        parent = parent.getParent();
      }

      Collections.reverse(pathList);

      mi_treePathList = new ArrayList<>();

      pathList.forEach(p -> {
        TreeItem<MyPath> treeItem;
        int parentIndex;

        treeItem = new TreeItem<>(p);
        parentIndex = mi_treePathList.size() - 1;
        if (parentIndex >= 0)
        {
          mi_treePathList.get(parentIndex).getChildren().add(treeItem);
        }

        mi_treePathList.add(treeItem);
      });

      if (mi_treePathList.isEmpty())
      {
        return null;
      }

      return mi_treePathList.get(mi_treePathList.size() - 1);
    }
  }

  private class DirectoryPane
    extends MigPane
  {
    private MyTableView<Path> mi_tableView;

    private DirectoryPane()
    {
      super("insets 0 0 0 0", "[fill]", "[pref][fill]");
      init();
    }

    public List<Path> getSelectedPaths()
    {
      return mi_tableView.getSelectionModel().getSelectedItems();
    }

    private void init()
    {
      mi_tableView = createTableView();

      add(mi_tableView, "dock center");

      m_directory.addListener((a, b, newDirectory) -> {
        fillTableView();
      });
    }

    private MyTableView<Path> createTableView()
    {
      MyTableView<Path> tableView;
      MyTableColumn<Path, Node> pathTypeColumn;
      MyTableColumn<Path, String> nameColumn;

      tableView = new MyTableView<>("Directories");
      tableView.getSelectionModel().selectionModeProperty().bind(m_selectionMode);
      tableView.getSelectionModel().setCellSelectionEnabled(false);
      tableView.setEditable(false);
      tableView.setOnMousePressed(new EventHandler<MouseEvent>()
      {
        @Override
        public void handle(MouseEvent event)
        {
          if (event.isPrimaryButtonDown() && event.getClickCount() == 2)
          {
            setDirectory(tableView.getSelectionModel().getSelectedItem());
          }
        }
      });

      pathTypeColumn = tableView.addColumn("");
      pathTypeColumn.setColumnCount(3);
      pathTypeColumn.setCellValueGetter(this::pathToImage);

      nameColumn = tableView.addColumn("Name");
      nameColumn.setColumnCount(40);
      nameColumn.setCellValueGetter((path) -> path.getFileName().toString());

      return tableView;
    }

    private void fillTableView()
    {
      mi_tableView.getItems().clear();

      try (Stream<Path> stream = Files.list(getDirectory()))
      {
        ObservableList<Path> list;

        list = stream.filter(path -> Files.isDirectory(path)).sorted()
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
        mi_tableView.setItems(list);
      }
      catch (IOException e)
      {
        return;
      }
    }

    private Node pathToImage(Path path)
    {
      if (Files.isDirectory(path))
      {
        return new FxIcon("folder-outline").size(IconSize.SMALLER).getImageView();
      }

      if (Files.isRegularFile(path))
      {
        return new FxIcon("file-outline").size(IconSize.SMALLER).getImageView();
      }

      return new Region();
    }
  }

  private class MyPath
  {
    private final Path mi_path;
    private final String mi_name;

    private MyPath(Path path)
    {
      mi_path = path;

      int nameIndex = mi_path.getNameCount() - 1;
      if (nameIndex >= 0)
      {
        mi_name = mi_path.getName(nameIndex).toString();
      }
      else
      {
        mi_name = mi_path.toString();
      }
    }

    public Path getPath()
    {
      return mi_path;
    }

    @Override
    public String toString()
    {
      return mi_name;
    }
  }

  private class DirectoryNode
    extends Button
  {
    private final String m_name;
    private final String m_iconName;
    private final Path m_path;

    private DirectoryNode(Path path)
    {
      this(path.toString(), "folder-outline", path);
    }

    private DirectoryNode(String name, String iconName, Path path)
    {
      m_name = name;
      m_iconName = iconName;
      m_path = path;

      setAlignment(Pos.BASELINE_LEFT);

      init();
    }

    private void init()
    {
      setText(m_name);
      if (m_iconName != null)
      {
        setGraphic(new FxIcon(m_iconName).size(IconSize.SMALL).getImageView());
      }
      setOnAction((ae) -> {
        setDirectory(m_path);
      });
    }
  }

  public void setInitialDirectory(Path directory)
  {
    setDirectory(directory);
  }

  /**
   * The current directory for the displayed dialog.
   */

  public final void setDirectory(Path value)
  {
    Platform.runLater(() -> directoryProperty().set(value));
  }

  public final Path getDirectory()
  {
    return (m_directory != null) ? m_directory.get() : null;
  }

  public final ObjectProperty<Path> directoryProperty()
  {
    return m_directory;
  }

  public static class PathList
  {
    private final List<Path> mi_directoryList;
    private final String mi_description;

    PathList(List<Path> directoryList)
    {
      mi_directoryList = directoryList;
      mi_description = initDescription();
    }

    public List<Path> getPathList()
    {
      return mi_directoryList;
    }

    public static PathList empty()
    {
      return new PathList(Collections.emptyList());
    }

    public static PathList of(Path path)
    {
      return new PathList(Arrays.asList(path));
    }

    public static PathList of(Path... pathArray)
    {
      return new PathList(Arrays.asList(pathArray));
    }

    public static PathList of(List<Path> pathList)
    {
      return new PathList(pathList);
    }

    public boolean isEmpty()
    {
      return getPathList().isEmpty();
    }

    private String initDescription()
    {
      List<Path> pathList;
      List<String> pathNameList;
      int commonPrefixIndex;
      int commonPrefixIndex2;
      boolean commonPrefixIndexFound;
      String shortestDirectoryName;
      StringBuilder description;

      pathList = getPathList();
      if (pathList.size() == 0)
      {
        return "";
      }

      if (pathList.size() == 1)
      {
        return pathList.get(0).toString();
      }

      pathNameList = pathList.stream().map(Path::toString).toList();

      shortestDirectoryName = pathNameList.stream().min(Comparator.comparing(String::length)).get();
      commonPrefixIndex = 0;
      commonPrefixIndexFound = false;

      for (int index = 0; index < shortestDirectoryName.length(); index++)
      {
        for (int dirIndex = 0; dirIndex < pathNameList.size(); dirIndex++)
        {
          if (shortestDirectoryName.charAt(index) != pathNameList.get(dirIndex).charAt(index))
          {
            commonPrefixIndex = index;
            commonPrefixIndexFound = true;
            break;
          }
        }

        if (commonPrefixIndexFound)
        {
          break;
        }
      }

      description = new StringBuilder();
      if (commonPrefixIndex > 0)
      {
        description.append(shortestDirectoryName.substring(0, commonPrefixIndex));
      }
      commonPrefixIndex2 = commonPrefixIndex;

      description.append(" -> ");
      description.append(pathNameList.stream().map(pn -> "'" + pn.substring(commonPrefixIndex2) + "'")
          .collect(Collectors.joining(" ")));

      return description.toString();
    }

    @Override
    public int hashCode()
    {
      return toString().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof PathList))
      {
        return false;
      }

      if (((PathList) o).toString().equals(toString()))
      {
        return true;
      }

      return super.equals(o);
    }

    @Override
    public String toString()
    {
      return mi_description;
    }
  }
}
