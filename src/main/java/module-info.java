module org.kku.jdiskusage
{
  requires javafx.controls;
  requires transitive javafx.graphics;
  requires java.desktop;
  requires java.logging;
  requires java.base;
  requires org.kku.fonticons;
  requires org.controlsfx.controls;
  requires java.prefs;
  requires com.miglayout.swing;
  requires com.miglayout.javafx;
  requires org.commonmark;
  requires javafx.web;

  exports org.kku.jdiskusage.main;
}
