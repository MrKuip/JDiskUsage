module org.kku.jdiskusage
{
  requires transitive javafx.controls;
  requires transitive javafx.graphics;
  requires java.desktop;
  requires java.base;
  requires org.kku.fonticons;
  requires org.kku.common;
  requires org.kku.fx;
  requires java.prefs;
  requires com.miglayout.javafx;
  requires org.commonmark;
  requires javafx.web;
  requires jdk.xml.dom;
  requires com.miglayout.core;

  exports org.kku.jdiskusage.main;

  provides org.kku.common.util.ResourceProviderIF with org.kku.jdiskusage.util.ResourceProvider;

  //opens org.kku.jdiskusage.util;

  //opens org.kku.jdiskusage.conf to com.fasterxml.jackson.databind;
  //opens org.kku.jdiskusage.util.preferences to com.fasterxml.jackson.databind;
  //opens org.kku.jdiskusage.main to com.faster
}
