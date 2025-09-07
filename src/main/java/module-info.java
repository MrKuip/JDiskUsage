module org.kku.jdiskusage
{
  requires transitive javafx.controls;
  requires transitive javafx.graphics;
  requires java.desktop;
  requires java.base;
  requires org.kku.iconify4j;
  requires org.kku.common;
  requires org.kku.fx;
  requires java.prefs;
  requires org.commonmark;
  requires javafx.web;
  requires jdk.xml.dom;

  exports org.kku.jdiskusage.main;
}
