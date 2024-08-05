package org.kku.jdiskusage.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.kku.jdiskusage.ui.DiskUsageView.DiskUsageData;
import org.kku.jdiskusage.ui.common.AbstractFormPane;
import org.kku.jdiskusage.ui.util.ConcurrentUtil;
import org.kku.jdiskusage.util.AppSettings2;
import org.kku.jdiskusage.util.AppSettings.AppSetting;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class HelpFormPane
  extends AbstractFormPane
{
  public static final String EVENT_TYPE_CLICK = "click";
  public static final String FILE_PREFIX = "file://";

  private WebBrowser m_contentWebBrowser;

  public HelpFormPane(DiskUsageData diskUsageData)
  {
    super(diskUsageData);

    createPaneType("HELP", "Help", "help", this::getHelpNode);

    init();
  }

  Node getHelpNode()
  {
    SplitPane splitPane;
    Node tableOfContentsView;
    Node contentsView;

    splitPane = new SplitPane();

    tableOfContentsView = getTableOfContentsWebBrowser().getWebView();
    contentsView = getContentsWebBrowser().getWebView();

    splitPane.getItems().addAll(tableOfContentsView, contentsView);
    splitPane.getDividers().get(0).positionProperty().addListener(getSplitPaneProperty().getChangeListener());
    SplitPane.setResizableWithParent(tableOfContentsView, false);
    SplitPane.setResizableWithParent(contentsView, false);
    splitPane.getDividers().get(0).setPosition(getSplitPaneProperty().get(0.25));

    return splitPane;
  }

  WebBrowser getTableOfContentsWebBrowser()
  {
    WebBrowser webBrowser;

    webBrowser = new WebBrowser();
    webBrowser.loadLocalContent("/help/table_of_contents.md");
    webBrowser.setContentWebBrowser(getContentsWebBrowser());

    return webBrowser;
  }

  WebBrowser getContentsWebBrowser()
  {
    WebBrowser webBrowser;

    if (m_contentWebBrowser == null)
    {
      webBrowser = new WebBrowser();
      webBrowser.loadLocalContent("/help/main.md");
      m_contentWebBrowser = webBrowser;
    }

    return m_contentWebBrowser;
  }

  private AppSetting<Double> getSplitPaneProperty()
  {
    return AppSettings2.SPLIT_PANE_POSITION.forSubject(this);
  }

  private static class WebBrowser
      implements ChangeListener<State>
  {
    private final WebView mi_webView;
    private final WebEngine mi_webEngine;
    private final Parser mi_parser;
    private final HtmlRenderer mi_renderer;
    private WebBrowser mi_contentWebBrowser;

    private WebBrowser()
    {
      mi_webView = new WebView();
      mi_webView.setFontScale(0.8);
      mi_webEngine = mi_webView.getEngine();
      mi_webEngine.getLoadWorker().stateProperty().addListener(this);
      mi_parser = Parser.builder().build();
      mi_renderer = HtmlRenderer.builder().build();
    }

    public WebView getWebView()
    {
      return mi_webView;
    }

    public void setContentWebBrowser(WebBrowser contentWebBrowser)
    {
      mi_contentWebBrowser = contentWebBrowser;
    }

    private WebBrowser getContentWebView()
    {
      return mi_contentWebBrowser == null ? this : mi_contentWebBrowser;
    }

    public void loadLocalContent(String link)
    {
      String html;

      try (Reader reader = new InputStreamReader(HelpFormPane.class.getResourceAsStream(link), StandardCharsets.UTF_8))
      {
        html = mi_renderer.render(mi_parser.parseReader(reader));
      }
      catch (IOException e)
      {
        String markdown;

        e.printStackTrace();
        markdown = """
            # Exception!
            """;
        html = mi_renderer.render(mi_parser.parse(markdown));
      }

      mi_webEngine.loadContent(html);
    }

    @Override
    public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue)
    {
      if (newValue == Worker.State.SUCCEEDED)
      {
        Document doc;
        NodeList nodeList;

        doc = mi_webView.getEngine().getDocument();
        nodeList = doc.getElementsByTagName("a");
        for (int i = 0; i < nodeList.getLength(); i++)
        {
          ((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, event -> {
            if (event.getCurrentTarget() instanceof HTMLAnchorElement anchorElement)
            {
              event.preventDefault();
              if (anchorElement.getHref().startsWith(FILE_PREFIX))
              {
                getContentWebView().loadLocalContent(anchorElement.getHref().substring(FILE_PREFIX.length()));
              }
              else
              {
                ConcurrentUtil.getInstance().getDefaultExecutor().submit(() -> {
                  if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                  {
                    try
                    {
                      Desktop.getDesktop().browse(new URI(anchorElement.getHref()));
                    }
                    catch (IOException | URISyntaxException e)
                    {
                      e.printStackTrace();
                    }
                  }
                });
              }
            }
          }, false);
        }
      }
    }
  }
}