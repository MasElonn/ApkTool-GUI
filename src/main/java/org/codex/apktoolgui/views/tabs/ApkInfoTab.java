package org.codex.apktoolgui.views.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.codex.apktoolgui.models.*;
import org.codex.apktoolgui.views.MainView;
import org.codex.apktoolgui.services.ApkEditorService;
import org.codex.apktoolgui.utils.UiUtils;

import java.io.File;

public class ApkInfoTab {
    private final MainView mainView;
    private final ApkEditorService apkEditorService;

    public ApkInfoTab(MainView mainView, ApkEditorService apkEditorService) {
        this.mainView = mainView;
        this.apkEditorService = apkEditorService;
    }
    public Tab createApkInfoTab(){
        Tab apkInfoTab = new Tab("APKINFO");
        apkInfoTab.setClosable(false);
        apkInfoTab.setGraphic(mainView.createIcon("⚡"));

        GridPane fileGrid = new GridPane();
        fileGrid.setHgap(10);
        fileGrid.setVgap(10);

        Label apkLabel = new Label("APK File:");
        apkLabel.setAlignment(Pos.CENTER_LEFT);
        TextField apkPathField = new TextField();
        apkPathField.setPromptText("Select APK file...");
        apkPathField.setPrefWidth(300);

        Button browseApkButton = mainView.createStyledButton("Browse", "primary");
        browseApkButton.setOnAction(e -> UiUtils.browseFile(mainView.fileChooser, apkPathField, "Select APK", "*.apk", "Select File"));

        fileGrid.add(apkLabel, 0, 0);
        fileGrid.add(apkPathField, 1, 0);
        fileGrid.add(browseApkButton, 2, 0);

        HBox upperBox = new HBox(15);
        upperBox.setPadding(new Insets(10));
        upperBox.getStyleClass().add("dark-container");
        upperBox.getChildren().addAll(fileGrid,apkLabel,apkPathField,browseApkButton);

        Button getInfoButton = mainView.createStyledButton("GetInfo", "primary");
        getInfoButton.setPrefWidth(180);
        getInfoButton.setOnAction(e -> {
                    String apkPath = apkPathField.getText();
                    if (apkPath.isEmpty()) {
                        UiUtils.showAlert("Error", "Please select an APK file first.");
                        return;
                    }
                    apkEditorService.executeGetBasicInfo(apkPath,true);
                });

        TextField infoTextField = new TextField();
        infoTextField.setPromptText("APK Info");
        infoTextField.setPrefWidth(300);

        VBox btnAndTextBox = new VBox();
        btnAndTextBox.setPadding(new Insets(10));
        btnAndTextBox.getStyleClass().add("dark-container");
        btnAndTextBox.getChildren().addAll(getInfoButton);

        VBox mainBox = new VBox();
        mainBox.setPadding(new Insets(10));
        mainBox.getStyleClass().add("dark-container");
        mainBox.getChildren().addAll(upperBox,btnAndTextBox);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPadding(new Insets(0));

        apkInfoTab.setContent(scrollPane);
        return apkInfoTab;
    }
}
