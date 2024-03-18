package org.kku.jdiskusage.ui;

import java.io.File;
import java.util.Optional;
import org.kku.jdiskusage.util.FileTree;
import javafx.application.Platform;
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

public class ScanFileTreeUI
{
  private Dialog<ButtonType> m_dialog;
  private Label m_currentDirectoryLabel;
  private Label m_currentFileCountLabel;
  private Label m_elapsedTimeLabel;
  private ProgressBar m_progressLabel;

  public ScanFileTreeUI()
  {
  }

  public void execute(Stage stage)
  {
    DirectoryChooser directoryChooser;
    File directory;
    File initialDirectory;
    Optional<ButtonType> scanDialogResult;
    GridPane grid;
    Label currentDirectory;
    Label currentCount;
    Label elapsedTime;
    Scan scan;

    initialDirectory = DiskUsageProperties.INITIAL_DIRECTORY.getFile();
    directoryChooser = new DirectoryChooser();
    if (initialDirectory != null)
    {
      directoryChooser.setInitialDirectory(initialDirectory);
    }

    directory = directoryChooser.showDialog(stage);
    if (directory == null)
    {
      return;
    }

    DiskUsageProperties.INITIAL_DIRECTORY.set(directory.getParentFile());

    m_dialog = new Dialog<>();
    m_dialog.setTitle("Scan file tree");
    m_dialog.setHeaderText("Scan " + directory.getPath());
    m_dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

    grid = new GridPane();
    grid.setMinSize(1000, 200);
    grid.setHgap(10);
    grid.setVgap(10);

    currentDirectory = new Label("Current directory:");
    currentDirectory.setMinWidth(currentDirectory.getPrefWidth());
    m_currentDirectoryLabel = new Label();
    m_currentDirectoryLabel.setMaxWidth(Double.MAX_VALUE);
    currentCount = new Label("Scanned:");
    m_currentFileCountLabel = new Label();
    m_currentFileCountLabel.setMaxWidth(Double.MAX_VALUE);
    elapsedTime = new Label("Elapsed time:");
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
    grid.add(elapsedTime, 0, 2);
    grid.add(m_elapsedTimeLabel, 1, 2);
    grid.add(m_progressLabel, 0, 3, 2, 1);

    GridPane.setHgrow(m_currentDirectoryLabel, Priority.SOMETIMES);
    GridPane.setHgrow(m_currentFileCountLabel, Priority.SOMETIMES);
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

    System.out.println("Scan: " + directory);
    System.out.println("Result: " + scanDialogResult);
  }

  private class Scan
      implements Runnable
  {
    private boolean m_cancel;
    private File m_directory;
    private long m_startTime;
    private long m_previousTime;
    private boolean m_runLaterActive;

    private Scan(File directory)
    {
      m_directory = directory;
    }

    public void cancel()
    {
      m_cancel = true;
    }

    @Override
    public void run()
    {
      FileTree tree;

      m_startTime = System.currentTimeMillis();
      m_previousTime = m_startTime;
      tree = new FileTree(m_directory);
      tree.setScanListener((currentPath, numberOfDirectories, numberOfFiles, scanReady) -> {
        long currentTimeMillis;

        currentTimeMillis = System.currentTimeMillis();
        if (!m_runLaterActive && m_previousTime + 1000 > currentTimeMillis)
        {
          return m_cancel;
        }

        m_previousTime = currentTimeMillis;
        m_runLaterActive = true;
        Platform.runLater(() -> {
          m_elapsedTimeLabel.setText((int) ((currentTimeMillis - m_startTime) / 1000) + " seconds");
          m_currentDirectoryLabel.setText(currentPath != null ? currentPath.toString() : "Ready");
          m_currentFileCountLabel.setText(numberOfDirectories + " directories, " + numberOfFiles + " files");
          m_runLaterActive = false;
          if (scanReady)
          {
            m_dialog.close();
          }
        });

        return m_cancel;
      });
      tree.scan();
    }
  }
}
