package org.kku.jdiskusage.ui.dialog;

import java.awt.Desktop;
import java.net.URI;
import org.kku.common.util.Log;
import org.kku.common.util.VersionUtil;
import org.kku.fx.ui.util.LogoUtil;
import org.kku.fx.ui.util.RootStage;
import org.kku.fx.ui.util.TranslateUtil;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Modality;

public class AboutDialog
{
  private Dialog<ButtonType> m_dialog;

  public AboutDialog()
  {
  }

  public void show()
  {
    m_dialog = new Dialog<>();
    m_dialog.setResizable(true);
    m_dialog.getDialogPane().setContent(getContent());
    m_dialog.getDialogPane().getStyleClass().add("about-dialog");
    m_dialog.initOwner(RootStage.get());
    m_dialog.initModality(Modality.APPLICATION_MODAL);
    m_dialog.setTitle("About");
    TranslateUtil.bind(m_dialog.titleProperty());
    m_dialog.getDialogPane().getScene().getWindow().setOnCloseRequest((_) -> m_dialog.close());
    m_dialog.getDialogPane().setMinSize(500, 250);
    m_dialog.showAndWait();
  }

  private Node getContent()
  {
    MigPane content;
    Hyperlink link;

    link = new Hyperlink("https://github.com/MrKuip/JDiskUsage");
    link.setFocusTraversable(false);
    link.setOnAction((_) -> {
      new Thread(() -> {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
        {
          try
          {
            System.out.println("link :" + link.getText());
            Desktop.getDesktop().browse(new URI(link.getText()));
          }
          catch (Exception e)
          {
            Log.log.error(e, "Failed to open link %s", link.getText());
          }
        }
      }).start();
    });

    content = new MigPane();
    content.add(new ImageView(LogoUtil.getLogo(200)), "span 2 4");
    content.add(styleNode(new Label("JDiskUsage"), "text1"), "split 2");
    content.add(styleNode(new Label("disk analyzer"), "text2"), "wrap");
    content.add(styleNode(new Label("Version " + VersionUtil.getInstance().getVersion()), "text3"), "wrap");
    content.add(styleNode(new Text("""
        JDiskUsage is an open source disk analyzer.
        Copyright © 2025 Kees Kuip
        Licensed under the GNU AGPLv3.
        """), "text4"), "wrap, top");
    content.add(styleNode(new Label("source:"), "text4"), "split 2");
    content.add(styleNode(link, "text4"), "wrap");

    return content;
  }

  private <T extends Node> T styleNode(T node, String... styleClasses)
  {
    node.getStyleClass().addAll(styleClasses);
    return node;
  }
}
