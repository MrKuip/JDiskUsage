package org.kku.jdiskusage.util;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.controlsfx.control.BreadCrumbBar;
import org.kku.fonticons.ui.FxIcon;
import org.kku.fonticons.ui.FxIcon.IconColor;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.ui.util.FxUtil;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
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

  private Dialog<List<Path>> m_dialog;
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
  public Path showDialog(final Window ownerWindow)
  {
    List<Path> pathList;

    m_selectionMode.set(SelectionMode.SINGLE);
    pathList = showAndWait();
    if (!pathList.isEmpty())
    {
      return pathList.get(0);
    }
    return null;
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
  public List<Path> showOpenMultipleDialog(final Window ownerWindow)
  {
    m_selectionMode.set(SelectionMode.MULTIPLE);
    return showAndWait();
  }

  private List<Path> showAndWait()
  {
    MigPane content;

    content = new MigPane("insets 6", "[grow]", "[grow]");
    content.setId("directory-chooser");

    content.add(m_toolBarPane, "dock north");
    content.add(new Separator(), "dock north");
    content.add(m_breadCrumbPane, "dock north");
    content.add(m_sidePane, "dock west");
    content.add(m_directoryPane, "grow");

    m_dialog = new Dialog<>();
    m_dialog.initModality(Modality.APPLICATION_MODAL);
    m_dialog.initStyle(StageStyle.UNDECORATED);
    m_dialog.getDialogPane().setContent(content);
    m_dialog.showAndWait();

    return m_dialog.getResult();
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
      cancelButton.setOnAction((ae) -> {
        m_dialog.setResult(Collections.emptyList());
        m_dialog.close();
      });

      spacer = FxUtil.createHorizontalFiller();
      openButton = new Button(translate("Open"), new FxIcon("open-in-new").size(IconSize.SMALL).getImageView());
      openButton.setOnAction((ae) -> {
        List<Path> result;

        result = new ArrayList<>();
        result.addAll(m_directoryPane.getSelectedPaths());
        result.sort(Comparator.comparing(Path::toString));
        m_dialog.setResult(result);

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
      super("insets 0 0 0 0", "[pref, fill]", "[]");
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
      Stream.of(File.listRoots()).map(DirectoryNode::new).forEach(rootNodes::add);

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
      setId("side-pane");
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
      MyTableColumn<Path, Long> fileSizeColumn;
      MyTableColumn<Path, Long> typeColumn;
      MyTableColumn<Path, Date> lastModifiedColumn;

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
      pathTypeColumn.initPersistentPrefWidth(20.0);
      pathTypeColumn.setCellValueGetter(this::pathToImage);

      nameColumn = tableView.addColumn("Name");
      nameColumn.initPersistentPrefWidth(200.0);
      nameColumn.setCellValueGetter((path) -> path.getFileName().toString());

      /*
       * fileSizeColumn = table.addColumn("File size");
       * fileSizeColumn.initPersistentPrefWidth(100.0);
       * fileSizeColumn.setCellValueFormatter(FormatterFactory.
       * createStringFormatFormatter("%,d"));
       * fileSizeColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
       * fileSizeColumn.setCellValueGetter((owi) -> owi.getObject().getSize());
       * 
       * lastModifiedColumn = table.addColumn("Last modified");
       * lastModifiedColumn.initPersistentPrefWidth(200.0);
       * lastModifiedColumn.setCellValueGetter((owi) -> new
       * Date(owi.getObject().getLastModifiedTime()));
       * lastModifiedColumn.setCellValueFormatter(FormatterFactory.
       * createSimpleDateFormatter("dd/MM/yyyy HH:mm:ss"));
       * lastModifiedColumn.setCellValueAlignment(Pos.CENTER_RIGHT);
       */

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
        System.out.println(e);
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

    private DirectoryNode(File file)
    {
      this(file.getPath(), "folder-outline", file.toPath());
    }

    private DirectoryNode(String name, String iconName, Path path)
    {
      m_name = name;
      m_iconName = iconName;
      m_path = path;

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

}
