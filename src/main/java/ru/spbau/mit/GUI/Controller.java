package ru.spbau.mit.GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.spbau.mit.CreationAndConfigs.IntervalWithStep;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.CreationAndConfigs.UserConfig;
import ru.spbau.mit.CreationAndConfigs.VaryingParameter;
import ru.spbau.mit.Tester.ArchTester;
import ru.spbau.mit.Tester.ResultWriter.ResultWriter;
import ru.spbau.mit.Tester.Timing.RunResults;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller extends Application {
    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    private static String hostName = "localhost";

    private ArchTester tester;
    private UserConfig config;
    private IntervalWithStep step;
    private File dir = new File("results");
    private ResultWriter wr;

    @FXML
    private Button startBtn;

    @FXML
    private ChoiceBox architectureChoice;
    @FXML
    private TextField reqsPerClient;
    @FXML
    private TextField arraySize;
    @FXML
    private TextField clientsSize;
    @FXML
    private TextField nextReqDelta;

    @FXML
    private ChoiceBox varyingChoice;
    @FXML
    private TextField varyingFrom;
    @FXML
    private TextField varyingTo;
    @FXML
    private TextField varyingStep;

    public static void main(String[] args) {
        if (args.length != 0) {
            hostName = args[0];
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            VBox page = FXMLLoader.load(this.getClass().getResource("/ui.fxml"));
            Scene scene = new Scene(page);
            Stage stage = primaryStage;
            primaryStage.setScene(scene);
            primaryStage.setTitle("Network performance");
            primaryStage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            showException(e);
        }
    }

    @FXML
    private void initialize() {
        initWriter();

        architectureChoice.setItems(FXCollections
                .observableArrayList(ServerType.validValues()));
        architectureChoice.getSelectionModel().selectFirst();

        varyingChoice.setItems(FXCollections
                .observableArrayList(VaryingParameter.values()));
        varyingChoice.getSelectionModel().selectFirst();

        setupUploadButton();
    }

    private void initWriter() {
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            wr = new ResultWriter(dir);
        } catch (IOException e) {
            logger.log(Level.WARNING, e.toString());
            showException(e);
        }
    }

    private void setupUploadButton() {
        startBtn.setOnMouseClicked(mouseEvent -> {
            gatherClientInput();
            showBusy();
            try {
                gatherServerData();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error writing logs", e);
                showException(e);
            }
            closeBusy();
        });
    }

    private void gatherClientInput() {
        config = UserConfig.builder()
                .serverType((ServerType) architectureChoice.getValue())
                .requestsPerClient(getInt(reqsPerClient))
                .arraySize(getInt(arraySize))
                .clientsSize(getInt(clientsSize))
                .nextReqDelta(getInt(nextReqDelta))
                .varyingParameter((VaryingParameter) varyingChoice.getValue())
                .build();

        step = IntervalWithStep.builder()
                .start(getInt(varyingFrom))
                .end(getInt(varyingTo))
                .step(getInt(varyingStep))
                .build();

        tester = new ArchTester(config, hostName);
    }

    private int getInt(TextField field) {
        if (field.getText().equals("")) {
            return Integer.parseInt(field.getPromptText());
        }
        return Integer.parseInt(field.getText());
    }

    private void gatherServerData() throws IOException {
        List<RunResults> results = new ArrayList<>();
        for (int i = step.getStart(); i < step.getEnd(); i += step.getStep()) {
            config.setVarying(i);
            try {
                results.add(tester.testOnce());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Exception in test runner " + config, e);
            }
        }
        wr.writeResults(config, step, results);
    }

    private void showBusy() {
        startBtn.setDisable(true);
    }

    private void closeBusy() {
        startBtn.setDisable(false);
    }

    private Alert alert = null;

    private void showException(Exception ex) {
        if (alert == null) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Exception");
            alert.setHeaderText("An exception has occured");
            alert.setContentText(ex.getMessage());

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            String exceptionText = sw.toString();

            Label label = new Label("The exception stacktrace was:");

            TextArea textArea = new TextArea(exceptionText);
            textArea.setEditable(false);
            textArea.setWrapText(true);

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);

            alert.setOnCloseRequest(it -> Platform.exit());
            alert.showAndWait();
        }
    }

}
