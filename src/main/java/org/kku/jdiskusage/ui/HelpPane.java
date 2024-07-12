package org.kku.jdiskusage.ui;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.AbstractTabContentPane;
import org.tbee.javafx.scene.layout.MigPane;
import javafx.concurrent.Worker.State;
import javafx.scene.Node;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class HelpPane
  extends AbstractTabContentPane
{
  private WebView m_contentWebView;

  public HelpPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("HELP", "Help", "help", this::getHelpNode);

    init();
  }

  Node getHelpNode()
  {
    MigPane pane;

    pane = new MigPane("debug", "[200:200:200][grow, fill]", "[grow]");
    pane.add(getTableOfContentsWebView(), "growy");
    pane.add(getContentsWebView(), "growy");

    return pane;
  }

  Node getTableOfContentsWebView()
  {
    Parser parser;
    HtmlRenderer renderer;
    String html;
    WebView webView;
    WebEngine webEngine;

    parser = Parser.builder().build();
    renderer = HtmlRenderer.builder().build();

    html = readLink(parser, renderer, "/help/table_of_contents.md");

    webView = new WebView();
    webEngine = webView.getEngine();
    webEngine.loadContent(html);
    webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
      System.out.println("Clicked hyperlink: " + newValue);
      if (webEngine.getLoadWorker().cancel())
      {
      }

      String prefix;

      prefix = "file://";
      if (newValue.startsWith(prefix))
      {
        String link;
        link = newValue.substring(prefix.length());
        m_contentWebView.getEngine().loadContent(readLink(parser, renderer, link));
      }
      else
      {
        m_contentWebView.getEngine().load(newValue);
      }
    });
    webEngine.getLoadWorker().stateProperty().addListener((a, oldValue, newValue) -> {
      System.out.println(a + " " + newValue);
      if (newValue == State.FAILED)
      {
      }
    });

    return webView;

  }

  private String readLink(Parser parser, HtmlRenderer renderer, String link)
  {
    String html;

    try (Reader reader = new InputStreamReader(getClass().getResourceAsStream(link)))
    {
      html = renderer.render(parser.parseReader(reader));
    }
    catch (IOException e)
    {
      String markdown;

      e.printStackTrace();
      markdown = """
          # Exception!
          """;
      html = renderer.render(parser.parse(markdown));
    }

    return html;
  }

  Node getContentsWebView()
  {
    Parser parser;
    HtmlRenderer renderer;
    String html;
    WebView webView;

    parser = Parser.builder().build();
    renderer = HtmlRenderer.builder().build();
    try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/help/main.md")))
    {
      html = renderer.render(parser.parseReader(reader));
    }
    catch (IOException e)
    {
      String markdown;

      e.printStackTrace();
      markdown = """
          # Exception!
          """;
      html = renderer.render(parser.parse(markdown));
    }

    webView = new WebView();
    webView.getEngine().loadContent(html);

    m_contentWebView = webView;

    return webView;
  }
}