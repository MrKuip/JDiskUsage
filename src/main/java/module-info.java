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
  requires jdk.xml.dom;
  requires com.fasterxml.jackson.databind;

  exports org.kku.jdiskusage.main;

  opens org.kku.jdiskusage.conf to com.fasterxml.jackson.databind;
}
