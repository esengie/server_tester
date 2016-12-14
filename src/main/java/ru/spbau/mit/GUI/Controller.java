package ru.spbau.mit.GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;
import ru.spbau.mit.CreationAndConfigs.IntervalWithStep;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.CreationAndConfigs.UserConfig;
import ru.spbau.mit.CreationAndConfigs.VaryingParameter;
import ru.spbau.mit.ProtoMessage.Messages;
import ru.spbau.mit.Tester.ArchTester;
import ru.spbau.mit.Tester.ResultWriter.ResultWriter;
import ru.spbau.mit.Tester.Timing.RunResults;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A fairly simple controller, forces ints inside TextFields, shows exceptions and
 * has a start button
 * <p>
 * Starts a singleThreadExecutor to handle actual server communication and test running
 */
public class Controller extends Application {
    private static final Logger logger = Logger.getLogger(Controller.class.getName());
    private static String hostName = "localhost";
    private static final ExecutorService pool = Executors.newSingleThreadExecutor();

    private ArchTester tester;
    private UserConfig config;
    private IntervalWithStep step;
    private final File dir = new File("results");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui.fxml"));
            VBox page = loader.load();
            Controller controller = loader.getController();
            controller.initWriter();

            Scene scene = new Scene(page);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Network performance");
            primaryStage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            ExceptionPopup.display(e);
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

        reqsPerClient.setTextFormatter(getPosIntFormatter());
        arraySize.setTextFormatter(getPosIntFormatter());
        clientsSize.setTextFormatter(getPosIntFormatter());
        nextReqDelta.setTextFormatter(getPosIntFormatter());

        varyingFrom.setTextFormatter(getPosIntFormatter());
        varyingTo.setTextFormatter(getPosIntFormatter());
        varyingStep.setTextFormatter(getPosIntFormatter());

        setupUploadButton();
    }

    private static TextFormatter<Number> getPosIntFormatter() {
        DecimalFormatSymbols sb = new DecimalFormatSymbols();
        sb.setMinusSign(' ');
        return new TextFormatter<>(
                new NumberStringConverter(new DecimalFormat("#", sb)));
    }

    private void initWriter() {
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            wr = new ResultWriter(dir);
        } catch (IOException e) {
            logger.log(Level.WARNING, e.toString());
            ExceptionPopup.display(e);
        }
    }

    private String prev;

    private void setupUploadButton() {
        startBtn.setOnMousePressed(mouseEvent -> {
            gatherClientInput();
            prev = showBusy();
        });

        startBtn.setOnMouseReleased(mouseEvent -> pool.submit(() -> {
            try {
            List<RunResults> res = gatherServerData();
            Platform.runLater(() -> closeBusy(prev));
            Platform.runLater(() -> drawResults(res));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error writing logs", e);
                ExceptionPopup.display(e);
            }
        }));
    }

    private void drawResults(List<RunResults> results) {
        String title = MessageFormat.format("{0}, requests per client: {1}",
                config.getServerType().toString(),
                Integer.toString(config.getRequestsPerClient()));
        GraphPopup popup = new GraphPopup(results, title, step,
                config.getVaryingParameter());
        popup.display();
    }

    @Override
    public void stop() throws Exception {
        pool.shutdown();
        super.stop();
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
                .step(getInt(varyingStep) > 0 ? getInt(varyingStep) : 1)
                .build();

        tester = new ArchTester(config, hostName);
    }

    private int getInt(TextField field) {
        if (field.getText().equals("")) {
            return Integer.parseInt(field.getPromptText());
        }
        return Integer.parseInt(field.getText());
    }

    private List<RunResults> gatherServerData() throws IOException {
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
        return results;
    }

    private String showBusy() {
        String res = startBtn.getText();
        startBtn.setDisable(true);
        startBtn.setText("Please wait");
        return res;
    }

    private void closeBusy(String prev) {
        startBtn.setDisable(false);
        startBtn.setText(prev);
    }
}
