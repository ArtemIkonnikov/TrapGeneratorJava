package sample;

import java.io.File;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TextField;


public class Controller {

    static String rulesFormat;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button logFileButton;

    @FXML
    private Button rulesButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button executeButton;

    @FXML
    private CheckBox checkBox1;

    @FXML
    private CheckBox checkBox2;

    @FXML
    private Text invalidFileFormatLog;

    @FXML
    private Text invalidFileFormatRules;

    @FXML
    private Tooltip tooltipLog;

    @FXML
    private Tooltip tooltipRules;

    @FXML
    private Tooltip tooltipSave;

    @FXML
    private TextField logTextArea;

    @FXML
    private TextField rulesTextArea;

    @FXML
    private TextField saveTextArea;

    @FXML
    void initialize() {
        logFileButton.setOnAction(event -> {
            invalidFileFormatLog.setVisible(false);
            FileChooser fileChooser = new FileChooser();
            List<File> files = fileChooser.showOpenMultipleDialog(null);
            TrapGenerator.trapsReceivedPaths = new ArrayList<>();
            StringBuilder stringBuilder = new StringBuilder("invalid file format ");
            for (File file : files) {
                String fileName = file.getName();
                String format = fileName.substring(fileName.lastIndexOf(".") + 1);
                System.out.println(format);
                if (format.equals("txt") || format.equals("log")) {
                    TrapGenerator.trapsReceivedPaths.add(file.getAbsolutePath());
                } else {
                    stringBuilder.append("\"" + format + "\"");
                    invalidFileFormatLog.setText(stringBuilder.toString());
                    invalidFileFormatLog.setVisible(true);
                }
            }
            StringBuilder sb = new StringBuilder();
            for (String path : TrapGenerator.trapsReceivedPaths) {
                sb.append(path + " ; ");
            }
            logTextArea.setText(sb.toString());
            tooltipLog.setText(sb.toString());
        });

        rulesButton.setOnAction(event -> {
            invalidFileFormatRules.setVisible(false);
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            TrapGenerator.jsonRulesFilePath = file.getAbsolutePath();
            rulesTextArea.setText(TrapGenerator.jsonRulesFilePath);
            tooltipRules.setText(TrapGenerator.jsonRulesFilePath);
            String fileName = file.getName();
            rulesFormat = fileName.substring(fileName.lastIndexOf(".") + 1);
        });

        saveButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File dir = directoryChooser.showDialog(null);
            TrapGenerator.batFilePath = dir.getAbsolutePath();
            saveTextArea.setText(TrapGenerator.batFilePath);
            tooltipSave.setText(TrapGenerator.batFilePath);
        });

        executeButton.setOnAction(event -> {
            TrapGenerator.versionList = new ArrayList<>();
            if (checkBox1.isSelected()) {
                TrapGenerator.versionList.add("v1");
            }
            if (checkBox2.isSelected()) {
                TrapGenerator.versionList.add("v2");
            }
            TrapGenerator.inputDataParser();
            if (TrapGenerator.errorVisibility) {
                invalidFileFormatRules.setText("invalid file format \"" + rulesFormat + "\"");
                invalidFileFormatRules.setVisible(true);
            }
        });
    }
}
