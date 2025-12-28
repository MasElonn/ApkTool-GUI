module org.apkutility.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires java.logging;
    requires java.desktop;

    opens org.apkutility.app to javafx.fxml;
    exports org.apkutility.app;
    exports org.apkutility.app.views;
    opens org.apkutility.app.views to javafx.fxml;
    exports org.apkutility.app.services;
    opens org.apkutility.app.services to javafx.fxml;
    exports org.apkutility.app.utils;
    opens org.apkutility.app.utils to javafx.fxml;
    exports org.apkutility.app.views.tabs;
    opens org.apkutility.app.views.tabs to javafx.fxml;
    exports org.apkutility.app.config;
    opens org.apkutility.app.config to javafx.fxml;
}