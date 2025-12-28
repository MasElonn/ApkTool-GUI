package org.apkutility.app.views.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.apkutility.app.services.AaptService;
import org.apkutility.app.services.ApkSignerService;
import org.apkutility.app.services.LogOutput;
import org.apkutility.app.services.UserNotifier;
import org.apkutility.app.utils.UiUtils;

import java.io.File;

/**
 * APK Signer Tab - Signing, Verification, and AAPT operations
 */
public class ApkSignerTab {
    
    private final LogOutput logOutput;
    private final UserNotifier userNotifier;
    private final ApkSignerService apkSignerService;
    private final AaptService aaptService;
    
    public ApkSignerTab(LogOutput logOutput, UserNotifier userNotifier, 
                        ApkSignerService apkSignerService, AaptService aaptService) {
        this.logOutput = logOutput;
        this.userNotifier = userNotifier;
        this.apkSignerService = apkSignerService;
        this.aaptService = aaptService;
    }
    
    public Node createContent() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("tab-content");
        
        // Header
        Label titleLabel = new Label("🔐 APK Signer & AAPT");
        titleLabel.getStyleClass().add("section-title");
        
        // Create sections
        Node signingSection = createSigningSection();
        Node verifySection = createVerifySection();
        Node aaptSection = createAaptSection();
        
        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox(20, titleLabel, signingSection, verifySection, aaptSection);
        content.setPadding(new Insets(10));
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("edge-to-edge");
        
        root.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        return root;
    }
    
    private Node createSigningSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label sectionTitle = new Label("✍️ Sign APK");
        sectionTitle.getStyleClass().add("subsection-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        // Input APK
        Label inputLabel = new Label("Input APK:");
        TextField inputField = new TextField();
        inputField.setPromptText("Select APK to sign...");
        inputField.setPrefWidth(350);
        Button inputBrowse = new Button("📂");
        inputBrowse.getStyleClass().add("button-icon");
        inputBrowse.setOnAction(e -> browseFile(inputField, "Select APK", "*.apk"));
        
        // Output APK
        Label outputLabel = new Label("Output APK:");
        TextField outputField = new TextField();
        outputField.setPromptText("Default: [name]_signed.apk");
        Button outputBrowse = new Button("📂");
        outputBrowse.getStyleClass().add("button-icon");
        outputBrowse.setOnAction(e -> browseSaveFile(outputField, "Save Signed APK"));
        
        // Use Test Key checkbox
        CheckBox useTestKeyCheck = new CheckBox("Use Test Key (debug signing)");
        useTestKeyCheck.setSelected(true);
        
        // Keystore path
        Label ksLabel = new Label("Keystore:");
        TextField ksField = new TextField();
        ksField.setPromptText("Select keystore file...");
        ksField.setDisable(true);
        Button ksBrowse = new Button("📂");
        ksBrowse.getStyleClass().add("button-icon");
        ksBrowse.setDisable(true);
        ksBrowse.setOnAction(e -> browseFile(ksField, "Select Keystore", "*.jks", "*.keystore"));
        
        // Keystore password
        Label ksPwdLabel = new Label("Keystore Password:");
        PasswordField ksPwdField = new PasswordField();
        ksPwdField.setPromptText("Enter keystore password");
        ksPwdField.setDisable(true);
        
        // Key alias
        Label aliasLabel = new Label("Key Alias:");
        TextField aliasField = new TextField();
        aliasField.setPromptText("Enter key alias");
        aliasField.setDisable(true);
        
        // Key password
        Label keyPwdLabel = new Label("Key Password:");
        PasswordField keyPwdField = new PasswordField();
        keyPwdField.setPromptText("Enter key password");
        keyPwdField.setDisable(true);
        
        // Toggle keystore fields based on test key checkbox
        useTestKeyCheck.setOnAction(e -> {
            boolean useTestKey = useTestKeyCheck.isSelected();
            ksField.setDisable(useTestKey);
            ksBrowse.setDisable(useTestKey);
            ksPwdField.setDisable(useTestKey);
            aliasField.setDisable(useTestKey);
            keyPwdField.setDisable(useTestKey);
        });
        
        // Signature schemes
        Label schemesLabel = new Label("Signature Schemes:");
        HBox schemesBox = new HBox(15);
        CheckBox v1Check = new CheckBox("v1 (JAR)");
        v1Check.setSelected(true);
        CheckBox v2Check = new CheckBox("v2");
        v2Check.setSelected(true);
        CheckBox v3Check = new CheckBox("v3");
        v3Check.setSelected(true);
        CheckBox v4Check = new CheckBox("v4");
        schemesBox.getChildren().addAll(v1Check, v2Check, v3Check, v4Check);
        
        // Layout
        int row = 0;
        grid.add(inputLabel, 0, row);
        grid.add(inputField, 1, row);
        grid.add(inputBrowse, 2, row);
        
        row++;
        grid.add(outputLabel, 0, row);
        grid.add(outputField, 1, row);
        grid.add(outputBrowse, 2, row);
        
        row++;
        grid.add(useTestKeyCheck, 0, row, 3, 1);
        
        row++;
        grid.add(ksLabel, 0, row);
        grid.add(ksField, 1, row);
        grid.add(ksBrowse, 2, row);
        
        row++;
        grid.add(ksPwdLabel, 0, row);
        grid.add(ksPwdField, 1, row);
        
        row++;
        grid.add(aliasLabel, 0, row);
        grid.add(aliasField, 1, row);
        
        row++;
        grid.add(keyPwdLabel, 0, row);
        grid.add(keyPwdField, 1, row);
        
        row++;
        grid.add(schemesLabel, 0, row);
        grid.add(schemesBox, 1, row, 2, 1);
        
        // Buttons
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(10, 0, 0, 0));
        
        Button signBtn = new Button("🔏 Sign APK");
        signBtn.getStyleClass().add("button-primary");
        signBtn.setOnAction(e -> {
            apkSignerService.signApk(
                inputField.getText(),
                outputField.getText(),
                ksField.getText(),
                ksPwdField.getText(),
                aliasField.getText(),
                keyPwdField.getText(),
                v1Check.isSelected(),
                v2Check.isSelected(),
                v3Check.isSelected(),
                v4Check.isSelected(),
                useTestKeyCheck.isSelected()
            );
        });
        
        Button quickSignBtn = new Button("⚡ Quick Sign (Test Key)");
        quickSignBtn.getStyleClass().add("button-secondary");
        quickSignBtn.setOnAction(e -> {
            apkSignerService.quickSignWithTestKey(inputField.getText(), outputField.getText());
        });
        
        buttons.getChildren().addAll(signBtn, quickSignBtn);
        
        section.getChildren().addAll(sectionTitle, grid, buttons);
        return section;
    }
    
    private Node createVerifySection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label sectionTitle = new Label("✅ Verify Signature");
        sectionTitle.getStyleClass().add("subsection-title");
        
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label apkLabel = new Label("APK File:");
        TextField apkField = new TextField();
        apkField.setPromptText("Select APK to verify...");
        apkField.setPrefWidth(400);
        Button apkBrowse = new Button("📂");
        apkBrowse.getStyleClass().add("button-icon");
        apkBrowse.setOnAction(e -> browseFile(apkField, "Select APK", "*.apk"));
        
        Button verifyBtn = new Button("🔍 Verify");
        verifyBtn.getStyleClass().add("button-primary");
        verifyBtn.setOnAction(e -> showVerifyDialog(apkField.getText()));
        
        row.getChildren().addAll(apkLabel, apkField, apkBrowse, verifyBtn);
        
        section.getChildren().addAll(sectionTitle, row);
        return section;
    }
    
    private void showVerifyDialog(String apkPath) {
        if (apkPath == null || apkPath.trim().isEmpty()) {
            userNotifier.showError("Please select an APK file to verify.");
            return;
        }
        
        // Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Verify APK Signature");
        dialog.setHeaderText("Verification Options");
        
        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label apkLabel = new Label("APK: " + apkPath);
        apkLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8b949e;");
        
        CheckBox verboseCheck = new CheckBox("Verbose output");
        verboseCheck.setSelected(true);
        
        CheckBox printCertsCheck = new CheckBox("Print certificate information");
        printCertsCheck.setSelected(true);
        
        content.getChildren().addAll(apkLabel, verboseCheck, printCertsCheck);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Style the dialog
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/org.apkutility.app/dark-theme.css").toExternalForm()
        );
        
        // Handle result
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                apkSignerService.verifyApk(apkPath, verboseCheck.isSelected(), printCertsCheck.isSelected());
            }
        });
    }
    
    private Node createAaptSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(20));
        
        Label sectionTitle = new Label("📦 AAPT / AAPT2 Operations");
        sectionTitle.getStyleClass().add("subsection-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        // APK File
        Label apkLabel = new Label("APK File:");
        TextField apkField = new TextField();
        apkField.setPromptText("Select APK...");
        apkField.setPrefWidth(350);
        Button apkBrowse = new Button("📂");
        apkBrowse.getStyleClass().add("button-icon");
        apkBrowse.setOnAction(e -> browseFile(apkField, "Select APK", "*.apk"));
        
        // Operation selector
        Label opLabel = new Label("Operation:");
        ComboBox<String> opCombo = new ComboBox<>();
        opCombo.getItems().addAll(
            "Dump Badging",
            "Dump Permissions",
            "Dump Resources",
            "Dump Configurations",
            "Dump XML Tree",
            "Dump Strings (AAPT2)",
            "List Contents",
            "Get Version"
        );
        opCombo.setValue("Dump Badging");
        opCombo.setPrefWidth(200);
        
        // AAPT version toggle
        Label versionLabel = new Label("Tool:");
        ToggleGroup aaptToggle = new ToggleGroup();
        RadioButton aaptRadio = new RadioButton("AAPT");
        aaptRadio.setToggleGroup(aaptToggle);
        RadioButton aapt2Radio = new RadioButton("AAPT2");
        aapt2Radio.setToggleGroup(aaptToggle);
        aapt2Radio.setSelected(true);
        HBox toggleBox = new HBox(10, aaptRadio, aapt2Radio);
        
        // XML asset path (for XML tree dump)
        Label xmlLabel = new Label("XML Asset:");
        TextField xmlField = new TextField();
        xmlField.setPromptText("AndroidManifest.xml");
        xmlField.setPrefWidth(200);
        
        // Layout
        grid.add(apkLabel, 0, 0);
        grid.add(apkField, 1, 0);
        grid.add(apkBrowse, 2, 0);
        
        grid.add(opLabel, 0, 1);
        grid.add(opCombo, 1, 1);
        
        grid.add(versionLabel, 0, 2);
        grid.add(toggleBox, 1, 2);
        
        grid.add(xmlLabel, 0, 3);
        grid.add(xmlField, 1, 3);
        
        // Execute button
        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(10, 0, 0, 0));
        
        Button executeBtn = new Button("▶️ Execute");
        executeBtn.getStyleClass().add("button-primary");
        executeBtn.setOnAction(e -> executeAaptOperation(
            apkField.getText(),
            opCombo.getValue(),
            aapt2Radio.isSelected(),
            xmlField.getText()
        ));
        
        buttons.getChildren().add(executeBtn);
        
        section.getChildren().addAll(sectionTitle, grid, buttons);
        return section;
    }
    
    private void executeAaptOperation(String apkPath, String operation, boolean useAapt2, String xmlAsset) {
        if (operation == null) return;
        
        switch (operation) {
            case "Dump Badging":
                if (useAapt2) aaptService.aapt2DumpBadging(apkPath);
                else aaptService.dumpBadging(apkPath);
                break;
            case "Dump Permissions":
                if (useAapt2) aaptService.aapt2DumpPermissions(apkPath);
                else aaptService.dumpPermissions(apkPath);
                break;
            case "Dump Resources":
                if (useAapt2) aaptService.aapt2DumpResources(apkPath);
                else aaptService.dumpResources(apkPath);
                break;
            case "Dump Configurations":
                if (useAapt2) aaptService.aapt2DumpConfigurations(apkPath);
                else aaptService.dumpConfigurations(apkPath);
                break;
            case "Dump XML Tree":
                if (useAapt2) aaptService.aapt2DumpXmlTree(apkPath, xmlAsset);
                else aaptService.dumpXmlTree(apkPath, xmlAsset);
                break;
            case "Dump Strings (AAPT2)":
                aaptService.aapt2DumpStrings(apkPath);
                break;
            case "List Contents":
                aaptService.listContents(apkPath, true);
                break;
            case "Get Version":
                if (useAapt2) aaptService.getAapt2Version();
                else aaptService.getVersion();
                break;
        }
    }
    
    private void browseFile(TextField field, String title, String... extensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        for (String ext : extensions) {
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Files", ext)
            );
        }
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            field.setText(file.getAbsolutePath());
        }
    }
    
    private void browseSaveFile(TextField field, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("APK Files", "*.apk")
        );
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            field.setText(file.getAbsolutePath());
        }
    }
}
