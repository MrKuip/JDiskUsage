package org.kku.jdiskusage.ui.util;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class TableUtils
{

  /**
   * Install the keyboard handler: + CTRL + C = copy to clipboard + CTRL + V =
   * paste to clipboard
   * 
   * @param table
   */
  public static void installCopyPasteHandler(TableView<?> table)
  {

    // install copy/paste keyboard handler
    table.setOnKeyPressed(new TableKeyEventHandler());

  }

  /**
   * Copy/Paste keyboard event handler. The handler uses the keyEvent's source for
   * the clipboard data. The source must be of type TableView.
   */
  public static class TableKeyEventHandler
      implements EventHandler<KeyEvent>
  {
    KeyCodeCombination copyKeyCodeCompination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);

    @Override
    public void handle(final KeyEvent keyEvent)
    {
      if (copyKeyCodeCompination.match(keyEvent))
      {
        if (keyEvent.getSource() instanceof TableView<?> tableView)
        {
          // copy to clipboard
          copySelectionToClipboard(tableView);

          // event is handled, consume it
          keyEvent.consume();
        }
      }
    }
  }

  /**
   * Get table selection and copy it to the clipboard.
   * 
   * @param table
   */
  public static void copySelectionToClipboard(TableView<?> table)
  {
    StringBuilder clipboardString;
    ObservableList<TablePosition> positionList;
    int prevRow;
    final ClipboardContent clipboardContent;

    clipboardString = new StringBuilder();
    positionList = table.getSelectionModel().getSelectedCells();

    prevRow = -1;
    for (TablePosition position : positionList)
    {
      int row;
      int col;
      String text;
      ObservableValue<?> observableValue;
      Object value;

      row = position.getRow();
      col = position.getColumn();

      // determine whether we advance in a row (tab) or a column
      // (newline).
      if (prevRow == row)
      {
        clipboardString.append('\t');
      }
      else if (prevRow != -1)
      {
        clipboardString.append('\n');
      }

      // create string from cell
      text = "";

      observableValue = table.getColumns().get(col).getCellObservableValue(row);
      if (observableValue == null)
      {
        text = "";
      }

      value = observableValue.getValue();
      text = (value == null) ? "" : value.toString();

      clipboardString.append(text);

      prevRow = row;
    }

    // create clipboard content
    clipboardContent = new ClipboardContent();
    clipboardContent.putString(clipboardString.toString());

    // set clipboard content
    Clipboard.getSystemClipboard().setContent(clipboardContent);
  }

}
