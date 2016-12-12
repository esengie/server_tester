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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.CreationAndConfigs.UserConfig;
import ru.spbau.mit.CreationAndConfigs.VaryingParameter;
import ru.spbau.mit.Tester.ArchTester;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller extends Application {
    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    private static String hostName = "localhost";

    private ArchTester tester;
    private UserConfig config;

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


    private Stage stage;

    public static void main(String[] args) {
        if (args.length != 0){
            hostName = args[0];
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            VBox page = FXMLLoader.load(this.getClass().getResource("/ui.fxml"));
            Scene scene = new Scene(page);
            stage = primaryStage;
            primaryStage.setScene(scene);
            primaryStage.setTitle("Network performance");
            primaryStage.show();
        } catch (IOException e) {
            logger.log(Level.WARNING, e.toString());
            showException(e);
        }
    }

    @FXML
    private void initialize() {
        architectureChoice.setItems(FXCollections
                .observableArrayList(ServerType.validValues()));
        architectureChoice.getSelectionModel().selectFirst();

        varyingChoice.setItems(FXCollections
                .observableArrayList(VaryingParameter.values()));
        varyingChoice.getSelectionModel().selectFirst();
    }

    private void setupUploadButton() {
        startBtn.setOnMouseClicked(mouseEvent -> {
            gatherClientInput();
            showBusy();
            gatherServerData();
            closeBusy();
        });
    }

    private void gatherClientInput() {

    }

    private void gatherServerData() {

    }

    private void showBusy() {
        
    }

    private void closeBusy() {

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
