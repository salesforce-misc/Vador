module vader {
  requires io.vavr;
  requires static lombok;
  requires org.jetbrains.annotations;
  requires kotlin.stdlib;
  requires reflection.util;
  requires net.jodah.typetools;
  requires org.hamcrest;

  exports org.revcloud.vader.config;
  exports org.revcloud.vader.config.container;
}
