package org.apkutility.app.views.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apkutility.app.services.InjectDocService;
import org.apkutility.app.services.ZipAlignService;
import org.apkutility.app.utils.UiUtils;
import org.apkutility.app.views.MainView;

import java.io.File;

public class UtilitiesTab {
    private final MainView mainView;
    private final InjectDocService injectDocService;
    private final ZipAlignService zipAlignService;

    public UtilitiesTab(MainView mainView,InjectDocService injectDocService,ZipAlignService zipAlignService) {
        this.mainView = mainView;
        this.injectDocService = injectDocService;
        this.zipAlignService = zipAlignService;
    }

    public Node createContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root-container");

        // --- Zipalign Card ---
        VBox zipCard = new VBox(15);
        zipCard.getStyleClass().add("card");
        Label zipTitle = new Label("Zipalign APK");
        zipTitle.getStyleClass().add("card-title");

        GridPane zipGrid = new GridPane();
        zipGrid.setHgap(15); zipGrid.setVgap(15);

        Label zipInputLabel = new Label("Input APK");
        TextField zipInputField = new TextField();
        zipInputField.setPromptText("Select APK...");
        zipInputField.setPrefWidth(300);
        Label zipOutputLabel = new Label("Output APK");
        TextField zipOutputField = new TextField();
        zipOutputField.setPromptText("Default: [apkname]_aligned.apk");

        Button browseZipInput = new Button("Browse");
        browseZipInput.setOnAction(e -> {
            File file = UiUtils.fileChooser.showOpenDialog(null);
            if (file != null) {
                zipInputField.setText(file.getAbsolutePath());
                zipOutputField.setText(UiUtils.generateOutputFilePath(file.getAbsolutePath(), "_aligned"));
            }
        });

        Button browseZipOutput = new Button("Browse");
        browseZipOutput.setOnAction(e -> UiUtils.browseSaveFile(UiUtils.fileChooser, zipOutputField, "Save Aligned APK", "*.apk", "Select File"));

        zipGrid.add(zipInputLabel, 0, 0); zipGrid.add(zipInputField, 1, 0); zipGrid.add(browseZipInput, 2, 0);
        zipGrid.add(zipOutputLabel, 0, 1); zipGrid.add(zipOutputField, 1, 1); zipGrid.add(browseZipOutput, 2, 1);

        Button alignButton = new Button("Sign / Align"); // Text simplified
        alignButton.getStyleClass().add("button-primary");
        alignButton.setOnAction(e -> {
            String input = zipInputField.getText();
            if(input.isEmpty()) { mainView.showError("Select input APK"); return; }
            String output = zipOutputField.getText().isEmpty() ? UiUtils.generateOutputFilePath(input, "_aligned") : zipOutputField.getText();
            zipAlignService.alignApk(input, output);
        });

        zipCard.getChildren().addAll(zipTitle, zipGrid, alignButton);

        // --- Inject Doc Card ---
        VBox docCard = new VBox(15);
        docCard.getStyleClass().add("card");
        Label docTitle = new Label("Inject Document Provider");
        docTitle.getStyleClass().add("card-title");

        HBox docBox = new HBox(15);
        docBox.setAlignment(Pos.CENTER_LEFT);
        
        Label docLabel = new Label("Target APK");
        TextField docField = new TextField();
        docField.setPromptText("Select APK...");
        docField.setPrefWidth(300);
        Button browseDoc = new Button("Browse");
        browseDoc.setOnAction(e -> UiUtils.browseFile(UiUtils.fileChooser, docField, "Select APK", "*.apk", "Select File"));
        
        Button injectButton = new Button("Inject Provider");
        injectButton.getStyleClass().add("button-primary");
        injectButton.setOnAction(e -> {
             if(docField.getText().isEmpty()) { mainView.showError("Select APK"); return; }
             injectDocService.executeInjectDoc(docField.getText());
        });

        docBox.getChildren().addAll(docLabel, docField, browseDoc);
        docCard.getChildren().addAll(docTitle, docBox, injectButton);

        root.getChildren().addAll(zipCard, docCard);
        return root;
    }

}
