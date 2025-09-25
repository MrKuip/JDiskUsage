package org.kku.jdiskusage.util;

import static org.kku.fx.ui.util.TranslateUtil.translate;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kku.common.util.AppProperties.AppProperty;
import org.kku.common.util.StringUtils;
import org.kku.fx.scene.control.BreadCrumbBar;
import org.kku.fx.ui.dialog.FxDialog;
import org.kku.iconify.ui.FxIcon;
import org.kku.iconify.ui.FxIcon.IconColor;
import org.kku.iconify.ui.FxIcon.IconSize;
import org.kku.jdiskusage.javafx.scene.control.MyTableColumn;
import org.kku.jdiskusage.javafx.scene.control.MyTableView;
import org.kku.jdiskusage.util.DirectoryList.Directory;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.Window;

public class DirectoryChooser
{
  private final ObjectProperty<MyPath> m_directory = new SimpleObjectProperty<>();
  private final ObjectProperty<SelectionMode> m_selectionMode = new SimpleObjectProperty<>(SelectionMode.SINGLE);
  private final StringProperty m_searchText = new SimpleStringProperty();

  private FxDialog<PathList> m_dialog;
  private final FavoriteDirectoryNodes m_favoriteDirectoryNodes = new FavoriteDirectoryNodes();
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

    content = new MigPane("", "[grow]", "[grow]");
    content.getStyleClass().add("jdiskusage-directory-chooser");
    content.add(m_toolBarPane, "dock north");
    content.add(new Separator(), "dock north");
    content.add(m_sidePane, "dock west");
    content.add(new Separator(Orientation.VERTICAL), "dock west");
    content.add(m_breadCrumbPane, "dock north");
    content.add(m_directoryPane, "grow");

    content.addEventFilter(KeyEvent.KEY_PRESSED, m_toolBarPane::onKeyTyped);
    content.addEventFilter(KeyEvent.KEY_RELEASED, m_toolBarPane::onKeyTyped);
    content.addEventFilter(KeyEvent.KEY_TYPED, m_toolBarPane::onKeyTyped);

    m_dialog = new FxDialog<>(content, PathList.empty());
    m_dialog.showAndWait();

    return m_dialog.getResult();
  }

  private class ToolBarPane
    extends MigPane
  {
    private TextField mi_searchField;

    private ToolBarPane()
    {
      super("insets 0 0 6 0", "[]push[]push[][]", "[pref:pref:pref]");
      init();
    }

    private void init()
    {
      Button cancelButton;
      Button searchButton;
      Button openButton;

      cancelButton = translate(
          new Button("Cancel", new FxIcon("mdi-close").size(IconSize.REGULAR).color(IconColor.RED).getNode()));
      cancelButton.setAlignment(Pos.BASELINE_LEFT);
      cancelButton.setOnAction((_) -> {
        m_dialog.close();
      });

      mi_searchField = new TextField();
      mi_searchField.setText("");
      mi_searchField.setVisible(false);
      m_searchText.bind(mi_searchField.textProperty());

      searchButton = translate(new Button("", new FxIcon("mdi-magnify").size(IconSize.REGULAR).getNode()));
      searchButton.setOnAction((_) -> {
        mi_searchField.setVisible(true);
        mi_searchField.requestFocus();
      });
      searchButton.setAlignment(Pos.BASELINE_LEFT);

      openButton = translate(new Button("Open", new FxIcon("mdi-open-in-new").size(IconSize.REGULAR).getNode()));
      openButton.setAlignment(Pos.BASELINE_LEFT);
      openButton.setOnAction((_) -> {
        List<MyPath> result;

        result = new ArrayList<>();
        result.addAll(getSelectedPaths());
        result.sort(Comparator.comparing(MyPath::toString));
        m_dialog.setResult(new PathList(result.stream().map(MyPath::getPath).toList()));
        m_dialog.close();
      });

      add(cancelButton, "tag cancel, sg buttons");
      add(mi_searchField, "");
      add(searchButton, "");
      add(openButton, "tag ok, sg buttons");
    }

    private List<MyPath> getSelectedPaths()
    {
      List<MyPath> result;

      result = new ArrayList<>();
      if (m_directoryPane.getSelectedPaths().isEmpty())
      {
        result.add(getDirectory());
      }
      else
      {
        result.addAll(m_directoryPane.getSelectedPaths());
      }

      return result;
    }

    public void onKeyTyped(KeyEvent ke)
    {
      if (ke.getCode() == KeyCode.UP || ke.getCode() == KeyCode.DOWN || ke.getCode() == KeyCode.ENTER)
      {
        if (ke.getTarget() != m_directoryPane.getTableView())
        {
          KeyEvent ke2;

          ke2 = ke.copyFor(m_directoryPane.getTableView(), m_directoryPane.getTableView());
          m_directoryPane.getTableView().fireEvent(ke2);
          m_directoryPane.getTableView().requestFocus();
          ke.consume();
        }

        return;
      }
      else if (ke.getTarget() != mi_searchField)
      {
        KeyEvent ke2;

        mi_searchField.setVisible(true);
        mi_searchField.requestFocus();
        mi_searchField.forward();

        ke2 = ke.copyFor(mi_searchField, mi_searchField);
        mi_searchField.fireEvent(ke2);
        ke.consume();
      }
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

    public void reInit()
    {
      getChildren().clear();
      init();
    }

    private void init()
    {
      getRootNodes().forEach(rootNode -> {
        add(rootNode);
      });
      add(new Separator(), "wrap");
      add(getHomeNode());
      getFavoriteNodes().forEach(rootNode -> {
        add(rootNode);
      });
    }

    private void add(DirectoryNode directoryNode)
    {
      directoryNode.setId("button");
      add(directoryNode, "grow, width 180px, height 30px, wrap");
    }

    private DirectoryNode getHomeNode()
    {
      return new DirectoryNode(translate("Home"), "mdi-home", new MyPath(Path.of(System.getProperty("user.home"))));
    }

    private List<DirectoryNode> getFavoriteNodes()
    {
      return m_favoriteDirectoryNodes.getDirectoryList().stream().map(directory -> {
        DirectoryNode node;
        ContextMenu menu;
        MenuItem removeMenuItem;
        MenuItem renameMenuItem;

        menu = new ContextMenu();

        node = new DirectoryNode(directory.toString(), "mdi-star", new MyPath(directory.getPath()));
        node.setTooltip(new Tooltip(directory.toString()));
        node.setContextMenu(menu);

        removeMenuItem = translate(new MenuItem("Remove favorite"));
        removeMenuItem.setOnAction((_) -> {
          m_favoriteDirectoryNodes.removeFavorite(directory);
        });
        renameMenuItem = translate(new MenuItem("Rename"));
        renameMenuItem.setOnAction((_) -> {
          FxDialog<String> dialog;
          TextField nameTextField;
          Bounds screenBounds;

          screenBounds = node.localToScreen(node.getBoundsInLocal());

          nameTextField = new TextField(directory.getName());
          dialog = new FxDialog<>(nameTextField, "");

          nameTextField.setOnAction((_) -> {
            String directoryName;

            directoryName = nameTextField.getText();
            if (!StringUtils.isEmpty(directoryName))
            {
              directory.setName(directoryName);
              m_favoriteDirectoryNodes.renameFavorite(directory);
            }
            dialog.close();
          });
          dialog.setLocation(screenBounds.getMinX(), screenBounds.getMinY());
          dialog.closeOnEscape();
          dialog.setOnShown((_) -> {
            nameTextField.requestFocus();
          });
          dialog.showAndWait();
        });

        menu.getItems().addAll(removeMenuItem, renameMenuItem);
        return node;
      }).toList();
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
      selectedTreeItem().addListener((_, _, newValue) -> {
        setDirectory(newValue.getValue());
      });

      m_directory.addListener((_, _, newDirectory) -> {
        treeItem().setValue(convertToTreeItem(newDirectory));
      });
    }

    private TreeItem<MyPath> convertToTreeItem(MyPath path)
    {
      List<TreeItem<MyPath>> mi_treePathList;
      List<MyPath> pathList;

      pathList = Stream.iterate(path, Objects::nonNull, MyPath::getParent).collect(Collectors.toList());
      Collections.reverse(pathList);

      mi_treePathList = new ArrayList<>();

      pathList.forEach(p -> {
        TreeItem<MyPath> treeItem;
        int parentIndex;

        treeItem = p.createTreeItem();
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
    private MyTableView<MyPath> mi_tableView;

    private DirectoryPane()
    {
      super("", "[grow, fill]", "[grow, fill]");
      init();
    }

    public MyTableView<MyPath> getTableView()
    {
      return mi_tableView;
    }

    public List<MyPath> getSelectedPaths()
    {
      return mi_tableView.getSelectionModel().getSelectedItems();
    }

    private void init()
    {
      mi_tableView = createTableView();

      add(mi_tableView);

      m_searchText.addListener((_, _, _) -> {
        fillTableView();
      });

      m_directory.addListener((_, _, _) -> {
        fillTableView();
      });
    }

    private MyTableView<MyPath> createTableView()
    {
      MyTableView<MyPath> tableView;
      MyTableColumn<MyPath, Node> pathTypeColumn;
      MyTableColumn<MyPath, String> nameColumn;
      ContextMenu menu;
      MenuItem menuItem;

      tableView = new MyTableView<>("Directories");
      tableView.getSelectionModel().selectionModeProperty().bind(m_selectionMode);
      tableView.getSelectionModel().setCellSelectionEnabled(false);
      tableView.setEditable(false);
      tableView.setOnKeyReleased((ke) -> {
        if (ke.getCode() == KeyCode.ENTER)
        {
          selectDirectory();
        }
      });
      tableView.setOnMousePressed((me) -> {
        if (me.isPrimaryButtonDown() && me.getClickCount() == 2)
        {
          selectDirectory();
        }
      });

      menu = new ContextMenu();
      menuItem = translate(new MenuItem("Add to favorites"));
      menuItem.setOnAction((_) -> {
        MyPath path;
        String name;

        path = tableView.getSelectionModel().getSelectedItem();
        name = path.getName();

        m_favoriteDirectoryNodes.addFavorite(new Directory(name, path.getPath()));
      });
      menu.getItems().add(menuItem);
      tableView.setContextMenu(menu);

      pathTypeColumn = tableView.addColumn("");
      pathTypeColumn.setColumnCount(3);
      pathTypeColumn.setCellValueGetter(this::pathToImage);

      nameColumn = tableView.addColumn("Name");
      nameColumn.setColumnCount(40);
      nameColumn.setCellValueGetter((path) -> path.getName());

      return tableView;
    }

    private void selectDirectory()
    {
      setDirectory(mi_tableView.getSelectionModel().getSelectedItem());
      m_toolBarPane.mi_searchField.setText("");
      m_toolBarPane.mi_searchField.setVisible(false);

    }

    private void fillTableView()
    {
      mi_tableView.getItems().clear();

      //try (Stream<MyPath> stream = Files.list(getDirectory()))
      try (Stream<MyPath> stream = getDirectory().getChildren())
      {
        ObservableList<MyPath> list;
        String searchText;

        searchText = m_searchText.get();

        list = stream.filter(MyPath::isDirectory) // Filter only directories
            .filter(path -> StringUtils.isEmpty(searchText)
                || path.toString().toLowerCase().contains(searchText.toLowerCase())) // Search filter
            .sorted(
                Comparator.comparing((MyPath path) -> path.getName().toLowerCase().startsWith(searchText.toLowerCase()))
                    .reversed() // Sort paths where the name starts with `searchText` first
                    .thenComparing(path -> path.getName().toLowerCase()) // Then sort alphabetically by file name
            ).collect(Collectors.toCollection(FXCollections::observableArrayList));

        mi_tableView.setItems(list);
      }
      catch (IOException e)
      {
        return;
      }
    }

    private Node pathToImage(MyPath path)
    {
      if (path == null)
      {
        return null;
      }

      if (Files.isDirectory(path.getPath()))
      {
        return new FxIcon("mdi-folder-outline").size(IconSize.REGULAR).getNode();
      }

      if (Files.isRegularFile(path.getPath()))
      {
        return new FxIcon("mdi-file-outline").size(IconSize.REGULAR).getNode();
      }

      return new Region();
    }
  }

  private class MyPath
  {
    private MyPath mi_parent;
    private final Path mi_path;
    private final String mi_name;
    private final boolean mi_isRootNode;

    private MyPath()
    {
      mi_path = null;
      mi_name = "<Root>";
      mi_isRootNode = true;
    }

    public TreeItem<MyPath> createTreeItem()
    {
      TreeItem<MyPath> treeItem;

      treeItem = new TreeItem<>(this);
      if (isRootNode())
      {
        treeItem.setGraphic(new FxIcon("mdi-arrow-right-circle").size(IconSize.MEDIUM).getNode());
      }

      return treeItem;
    }

    private MyPath(Path path)
    {
      mi_path = path;
      mi_isRootNode = false;

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

    public boolean isRootNode()
    {
      return mi_isRootNode;
    }

    public MyPath getParent()
    {
      if (mi_isRootNode)
      {
        return null;
      }

      if (mi_parent == null)
      {
        if (mi_path.getParent() != null)
        {
          mi_parent = new MyPath(mi_path.getParent());
        }
        else
        {
          mi_parent = new MyPath();
        }
      }

      return mi_parent;
    }

    public Stream<MyPath> getChildren() throws IOException
    {
      if (mi_path == null)
      {
        return getRootNodes().stream().map(DirectoryNode::getPath);
      }
      else
      {
        return Files.list(mi_path).map(MyPath::new);
      }
    }

    public String getName()
    {
      return mi_name;
    }

    public Path getPath()
    {
      return mi_path;
    }

    public boolean isDirectory()
    {
      return mi_path != null ? Files.isDirectory(mi_path) : false;
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
    private final MyPath m_path;

    private DirectoryNode(MyPath path)
    {
      this(path.toString(), "mdi-folder-outline", path);
    }

    private DirectoryNode(String name, String iconName, MyPath path)
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
        setGraphic(new FxIcon(m_iconName).size(IconSize.REGULAR).getNode());
      }
      setOnAction((_) -> {
        setDirectory(m_path);
      });
    }

    public MyPath getPath()
    {
      return m_path;
    }
  }

  private class FavoriteDirectoryNodes
  {
    private void addFavorite(Directory directory)
    {
      setFavoriteDirectoryList(new DirectoryList(
          Stream.concat(getFavoriteDirectoryList().getDirectoryList().stream(), Stream.of(directory)).toList()));
      m_sidePane.reInit();
    }

    private void removeFavorite(Directory directory)
    {
      setFavoriteDirectoryList(new DirectoryList(
          getFavoriteDirectoryList().getDirectoryList().stream().filter(d -> !Objects.equals(d, directory)).toList()));
      m_sidePane.reInit();
    }

    private void renameFavorite(Directory directory)
    {
      setFavoriteDirectoryList(new DirectoryList(getFavoriteDirectoryList().getDirectoryList().stream().map(d -> {
        return Objects.equals(d.getPath(), directory.getPath()) ? directory : d;
      }).toList()));
      m_sidePane.reInit();
    }

    public List<Directory> getDirectoryList()
    {
      return getFavoriteDirectoryList().getDirectoryList();
    }

    private void setFavoriteDirectoryList(DirectoryList pathList)
    {
      getFavoriteDirectoriesProperty().set(pathList);
    }

    private DirectoryList getFavoriteDirectoryList()
    {
      return getFavoriteDirectoriesProperty().get(DirectoryList.empty());
    }

    private AppProperty<DirectoryList> getFavoriteDirectoriesProperty()
    {
      return AppSettings.FAVORITE_DIRECTORIES.forSubject(this);
    }
  }

  public void setInitialDirectory(Path directory)
  {
    setDirectory(new MyPath(directory));
  }

  /**
   * The current directory for the displayed dialog.
   */

  public final void setDirectory(MyPath value)
  {
    Platform.runLater(() -> directoryProperty().set(value));
  }

  public final MyPath getDirectory()
  {
    return (m_directory != null) ? m_directory.get() : null;
  }

  public final ObjectProperty<MyPath> directoryProperty()
  {
    return m_directory;
  }

  private List<DirectoryNode> getRootNodes()
  {
    List<DirectoryNode> rootNodeList;

    rootNodeList = new ArrayList<>();
    FileSystems.getDefault().getRootDirectories()
        .forEach(path -> rootNodeList.add(new DirectoryNode(new MyPath(path))));

    return rootNodeList;
  }

}
