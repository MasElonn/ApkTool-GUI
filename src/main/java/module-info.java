module org.codex.apktoolgui.apktoolgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires java.logging;
    requires java.desktop;

    opens org.codex.apktoolgui to javafx.fxml;
    exports org.codex.apktoolgui;
}