package org.codex.apktoolgui;

import javafx.application.Application;
import javafx.stage.Stage;
import org.codex.apktoolgui.views.MainView;

public class App extends Application {
    private MainView controller;

    @Override
    public void start(Stage primaryStage) {
        controller = new MainView(primaryStage);
        controller.initialize(primaryStage);
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}