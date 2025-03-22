module org.kku.jdiskusage
{
  requires transitive javafx.controls;
  requires transitive javafx.graphics;
  requires java.desktop;
  requires java.logging;
  requires java.base;
  requires org.kku.fonticons;
  requires org.kku.common;
  requires java.prefs;
  requires com.miglayout.javafx;
  requires org.commonmark;
  requires javafx.web;
  requires jdk.xml.dom;
  requires com.fasterxml.jackson.databind;
  requires com.miglayout.core;

  exports org.kku.jdiskusage.main;

  opens org.kku.jdiskusage.conf to com.fasterxml.jackson.databind;
  opens org.kku.conf to com.fasterxml.jackson.databind;
  opens org.kku.jdiskusage.util.preferences to com.fasterxml.jackson.databind;
  opens org.kku.jdiskusage.main to com.fasterxml.jackson.databind;
}
