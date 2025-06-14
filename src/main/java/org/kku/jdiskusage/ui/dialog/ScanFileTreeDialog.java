package org.kku.jdiskusage.ui.dialog;

import static org.kku.fx.ui.util.TranslateUtil.translate;
import static org.kku.fx.ui.util.TranslateUtil.translatedTextProperty;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.kku.common.util.AppProperties.AppProperty;
import org.kku.common.util.CommonUtil;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.fx.ui.util.FxIconUtil;
import org.kku.fx.ui.util.Notifications;
import org.kku.fx.ui.util.RootStage;
import org.kku.jdiskusage.ui.util.ConcurrentUtil;
import org.kku.jdiskusage.util.AppSettings;
import org.kku.jdiskusage.util.DirectoryChooser;
import org.kku.jdiskusage.util.FileTree;
import org.kku.jdiskusage.util.FileTree.DirNode;
import org.kku.jdiskusage.util.PathList;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ScanFileTreeDialog
{
  private Dialog<ButtonType> m_dialog;
  private Label m_currentDirectoryLabel;
  private Label m_currentFileCountLabel;
  private Label m_elapsedTimeLabel;
  private ProgressBar m_progressLabel;

  public ScanFileTreeDialog()
  {
  }

  public ScanResult chooseDirectory(Stage stage)
  {
    DirectoryChooser directoryChooser;
    PathList dirPathList;
    Path initialDirectory;

    initialDirectory = getInitialDirectoryProperty().get();
    directoryChooser = new DirectoryChooser();
    if (initialDirectory != null)
    {
      directoryChooser.setInitialDirectory(initialDirectory);
    }

    dirPathList = directoryChooser.showOpenMultipleDialog(stage);
    if (dirPathList == null || dirPathList.isEmpty())
    {
      return null;
    }

    return scanDirectory(dirPathList);
  }

  public static class ScanResult
  {
    private final PathList mi_directoryList;
    private final DirNode mi_result;

    private ScanResult(PathList directoryList, DirNode result)
    {
      mi_directoryList = directoryList;
      mi_result = result;
    }

    public PathList getDirectoryList()
    {
      return mi_directoryList;
    }

    public DirNode getResult()
    {
      return mi_result;
    }

    public boolean hasResult()
    {
      return getResult() != null;
    }

  }

  public ScanResult scanDirectory(PathList directoryList)
  {
    Optional<ButtonType> scanDialogResult;
    GridPane content;
    Label currentDirectory;
    Label currentCount;
    Label elapsedTime;
    Scan scan;
    ButtonType cancelButtonType;

    scan = new Scan(directoryList.getPathList());

    content = new GridPane();
    content.setMinSize(1000, 200);
    content.setHgap(10);
    content.setVgap(10);

    currentDirectory = translate(new Label("Current directory"));
    currentDirectory.setMinWidth(currentDirectory.getPrefWidth());
    m_currentDirectoryLabel = new Label();
    m_currentDirectoryLabel.setMaxWidth(Double.MAX_VALUE);
    currentCount = translate(new Label("Scanned"));
    m_currentFileCountLabel = new Label();
    m_currentFileCountLabel.setMaxWidth(Double.MAX_VALUE);
    elapsedTime = translate(new Label("Elapsed time"));
    m_elapsedTimeLabel = new Label();
    m_elapsedTimeLabel.setMaxWidth(Double.MAX_VALUE);
    m_progressLabel = new ProgressBar();
    m_progressLabel.setMaxWidth(Double.MAX_VALUE);

    ColumnConstraints column1 = new ColumnConstraints();
    column1.setPrefWidth(Region.USE_COMPUTED_SIZE);
    column1.setMinWidth(Region.USE_PREF_SIZE);
    column1.setMaxWidth(Region.USE_PREF_SIZE);
    content.getColumnConstraints().addAll(column1, new ColumnConstraints());

    content.add(currentDirectory, 0, 0);
    content.add(m_currentDirectoryLabel, 1, 0);
    content.add(currentCount, 0, 1);
    content.add(m_currentFileCountLabel, 1, 1);
    content.add(elapsedTime, 0, 2);
    content.add(m_elapsedTimeLabel, 1, 2);
    content.add(m_progressLabel, 0, 3, 2, 1);

    GridPane.setHgrow(m_currentDirectoryLabel, Priority.SOMETIMES);
    GridPane.setHgrow(m_currentFileCountLabel, Priority.SOMETIMES);
    GridPane.setHgrow(m_elapsedTimeLabel, Priority.SOMETIMES);
    GridPane.setHgrow(m_progressLabel, Priority.SOMETIMES);

    cancelButtonType = new ButtonType(translate("Cancel"), ButtonData.CANCEL_CLOSE);

    m_dialog = new Dialog<>();
    m_dialog.getDialogPane().setContent(content);
    m_dialog.getDialogPane().getStyleClass().add("undecorated-dialog");
    m_dialog.initOwner(RootStage.get());
    m_dialog.initModality(Modality.APPLICATION_MODAL);
    m_dialog.initStyle(StageStyle.UNDECORATED);
    m_dialog.titleProperty().bind(translatedTextProperty("Scan directory"));
    m_dialog.headerTextProperty().bind(translatedTextProperty("Scan").concat(" ").concat(scan.getRootDirectory()));
    m_dialog.getDialogPane().getButtonTypes().addAll(cancelButtonType);
    m_dialog.setGraphic(FxIconUtil.createIconNode("file-search", IconSize.LARGE));

    ConcurrentUtil.getInstance().getDefaultExecutor().submit(scan);

    scanDialogResult = m_dialog.showAndWait();
    if (scanDialogResult.get() == cancelButtonType)
    {
      scan.cancel();
    }

    return new ScanResult(directoryList, scan.getResult());
  }

  private class Scan
      implements Runnable
  {
    private List<Path> mi_directoryList;
    private Path mi_rootDirectory;
    private boolean mi_cancel;
    private long mi_startTime;
    private long mi_previousTime;
    private boolean mi_runLaterActive;
    private DirNode mi_result;

    private Scan(List<Path> directoryList)
    {
      mi_directoryList = directoryList;
      mi_rootDirectory = directoryList.get(0);
      if (directoryList.size() > 1)
      {
        mi_rootDirectory = mi_rootDirectory.getParent();
      }

      //getProps().set(AppProperties.INITIAL_DIRECTORY, mi_rootDirectory);
      getInitialDirectoryProperty().set(mi_rootDirectory);
    }

    public Path getRootDirectory()
    {
      return mi_rootDirectory;
    }

    public DirNode getResult()
    {
      return mi_result;
    }

    public void cancel()
    {
      mi_cancel = true;
    }

    @Override
    public void run()
    {
      FileTree tree;

      mi_startTime = System.currentTimeMillis();
      mi_previousTime = mi_startTime;
      tree = new FileTree(mi_directoryList);
      tree.setScanListener((currentPath, numberOfDirectories, numberOfFiles, scanReady) -> {
        long currentTimeMillis;

        currentTimeMillis = System.currentTimeMillis();
        if (!scanReady && !mi_runLaterActive && mi_previousTime + 100 > currentTimeMillis)
        {
          return mi_cancel;
        }

        mi_previousTime = currentTimeMillis;
        mi_runLaterActive = true;
        Platform.runLater(() -> {
          m_elapsedTimeLabel.textProperty().bind(Bindings.format("%,d %s",
              (int) ((currentTimeMillis - mi_startTime) / 1000), translatedTextProperty("seconds")));
          m_currentDirectoryLabel.setText(currentPath != null ? currentPath.toString() : translate("Ready"));
          m_currentFileCountLabel.textProperty().bind(Bindings.format("%,d %s %,d %s", numberOfDirectories,
              translatedTextProperty("directories"), numberOfFiles, translatedTextProperty("files")));

          mi_runLaterActive = false;
          if (scanReady)
          {
            StringExpression textExpression;

            textExpression = Bindings.format("%,d %s , %,d %s %s %d %s", numberOfDirectories,
                translatedTextProperty("directories"), numberOfFiles, translatedTextProperty("files"),
                translatedTextProperty("in"), (int) ((currentTimeMillis - mi_startTime) / 1000), translate("seconds"));

            Notifications.showMessage(translatedTextProperty("Scan ready"), textExpression);
          }
        });

        return mi_cancel;
      });
      mi_result = tree.scan();

      // Give the dialog a minimal time (200 ms) to show itself. Otherwise (on Linux) the application looses its
      //   focus and the only way to get it back is to click outside the app and then click inside the app.
      //   When there is no focus the menu's on the menubar will not show. 
      CommonUtil.sleep(200 - (System.currentTimeMillis() - mi_startTime));
      Platform.runLater(() -> m_dialog.close());
    }
  }

  private AppProperty<Path> getInitialDirectoryProperty()
  {
    return AppSettings.INITIAL_DIRECTORY.forSubject(this);
  }
}
