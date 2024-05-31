package org.kku.jdiskusage.ui;

import static org.kku.jdiskusage.ui.util.TranslateUtil.translate;
import java.io.File;
import java.util.Optional;
import org.kku.fonticons.ui.FxIcon.IconSize;
import org.kku.jdiskusage.ui.util.IconUtil;
import org.kku.jdiskusage.util.ApplicationPropertyExtensionIF;
import org.kku.jdiskusage.util.FileTree;
import org.kku.jdiskusage.util.FileTree.DirNode;
import javafx.application.Platform;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ScanFileTreeDialog
    implements ApplicationPropertyExtensionIF
{
  private Dialog<ButtonType> m_dialog;
  private Label m_currentDirectoryLabel;
  private Label m_currentFileCountLabel;
  private Label m_filteredFileCountLabel;
  private Label m_elapsedTimeLabel;
  private ProgressBar m_progressLabel;

  public ScanFileTreeDialog()
  {
  }

  public DirNode chooseDirectory(Stage stage)
  {
    DirectoryChooser directoryChooser;
    File directory;
    File initialDirectory;

    initialDirectory = getProps().getFile(Property.INITIAL_DIRECTORY);
    directoryChooser = new DirectoryChooser();
    if (initialDirectory != null)
    {
      directoryChooser.setInitialDirectory(initialDirectory);
    }

    directory = directoryChooser.showDialog(stage);
    if (directory == null)
    {
      return null;
    }

    getProps().set(Property.INITIAL_DIRECTORY, directory.getParentFile());

    return scanDirectory(directory);
  }

  public DirNode scanDirectory(File directory)
  {
    Optional<ButtonType> scanDialogResult;
    GridPane grid;
    Label currentDirectory;
    Label currentCount;
    Label filteredCount;
    Label elapsedTime;
    Scan scan;

    m_dialog = new Dialog<>();
    m_dialog.setTitle(translate("Scan directory"));
    m_dialog.setHeaderText(translate("Scan") + " " + directory.getPath());
    m_dialog.getDialogPane().getButtonTypes().addAll(new ButtonType(translate("Cancel"), ButtonData.CANCEL_CLOSE));
    m_dialog.setGraphic(IconUtil.createImageView("file-search", IconSize.LARGE));

    grid = new GridPane();
    grid.setMinSize(1000, 200);
    grid.setHgap(10);
    grid.setVgap(10);

    currentDirectory = translate(new Label("Current directory"));
    currentDirectory.setMinWidth(currentDirectory.getPrefWidth());
    m_currentDirectoryLabel = new Label();
    m_currentDirectoryLabel.setMaxWidth(Double.MAX_VALUE);
    currentCount = translate(new Label("Scanned"));
    m_currentFileCountLabel = new Label();
    m_currentFileCountLabel.setMaxWidth(Double.MAX_VALUE);
    filteredCount = translate(new Label("Filtered"));
    m_filteredFileCountLabel = new Label();
    m_filteredFileCountLabel.setMaxWidth(Double.MAX_VALUE);
    elapsedTime = translate(new Label("Elapsed time"));
    m_elapsedTimeLabel = new Label();
    m_elapsedTimeLabel.setMaxWidth(Double.MAX_VALUE);
    m_progressLabel = new ProgressBar();
    m_progressLabel.setMaxWidth(Double.MAX_VALUE);

    ColumnConstraints column1 = new ColumnConstraints();
    column1.setPrefWidth(Region.USE_COMPUTED_SIZE);
    column1.setMinWidth(Region.USE_PREF_SIZE);
    column1.setMaxWidth(Region.USE_PREF_SIZE);
    grid.getColumnConstraints().addAll(column1, new ColumnConstraints());

    grid.add(currentDirectory, 0, 0);
    grid.add(m_currentDirectoryLabel, 1, 0);
    grid.add(currentCount, 0, 1);
    grid.add(m_currentFileCountLabel, 1, 1);
    grid.add(filteredCount, 0, 2);
    grid.add(m_filteredFileCountLabel, 1, 2);
    grid.add(elapsedTime, 0, 3);
    grid.add(m_elapsedTimeLabel, 1, 3);
    grid.add(m_progressLabel, 0, 4, 2, 1);

    GridPane.setHgrow(m_currentDirectoryLabel, Priority.SOMETIMES);
    GridPane.setHgrow(m_currentFileCountLabel, Priority.SOMETIMES);
    GridPane.setHgrow(m_filteredFileCountLabel, Priority.SOMETIMES);
    GridPane.setHgrow(m_elapsedTimeLabel, Priority.SOMETIMES);
    GridPane.setHgrow(m_progressLabel, Priority.SOMETIMES);

    m_dialog.getDialogPane().setContent(grid);

    scan = new Scan(directory);
    new Thread(scan).start();

    scanDialogResult = m_dialog.showAndWait();
    if (scanDialogResult.get() == ButtonType.CANCEL)
    {
      scan.cancel();
    }

    return scan.getResult();
  }

  private class Scan
      implements Runnable
  {
    private boolean mi_cancel;
    private File mi_directory;
    private long mi_startTime;
    private long mi_previousTime;
    private boolean mi_runLaterActive;
    private DirNode mi_result;

    private Scan(File directory)
    {
      mi_directory = directory;
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
      tree = new FileTree(mi_directory);
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
          m_elapsedTimeLabel.setText((int) ((currentTimeMillis - mi_startTime) / 1000) + " " + translate("seconds"));
          m_currentDirectoryLabel.setText(currentPath != null ? currentPath.toString() : "Ready");
          m_currentFileCountLabel.setText(
              numberOfDirectories + " " + translate("directories") + ", " + numberOfFiles + " " + translate("files"));
          m_filteredFileCountLabel.setText(
              numberOfDirectories + " " + translate("directories") + ", " + numberOfFiles + " " + translate("files"));
          mi_runLaterActive = false;
          if (scanReady)
          {
            m_dialog.close();
          }
        });

        return mi_cancel;
      });
      mi_result = tree.scan();
    }
  }
}
