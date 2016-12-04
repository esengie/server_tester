package ru.spbau.mit.GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.spbau.mit.ClientSide.Clients;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClientAppG extends Application {
    private static final Logger logger = Logger.getLogger(ClientAppG.class.getName());
    private static Clients client;
    private static Thread lister;

    @FXML
    private Button uploadBtn;

    @FXML
    private VBox serverFiles;

    @FXML
    private VBox downloading;

    private Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            SplitPane page = FXMLLoader.load(this.getClass().getResource("/ui.fxml"));
            Scene scene = new Scene(page);
            stage = primaryStage;
            primaryStage.setScene(scene);
            primaryStage.setTitle("Torrent Client");
            primaryStage.show();
        } catch (IOException e) {
            logger.log(Level.WARNING, e.toString());
            showException(e);
        }
    }

    @FXML
    private void initialize() {
        setupUploadButton();

        Set<Integer> ids = new HashSet<>();

        lister = new Thread(new Lister());
        lister.start();
    }

//    private List<RemoteFile> populateServersList() {
//        try {
//            remoteLst = client.executeList();
//            serverFiles.getChildren().clear();
//            for (int i = 0; i < remoteLst.size(); ++i) {
//                serverFiles.getChildren()
//                        .add(createServerTextField(i, format(remoteLst.get(i))));
//            }
//            return remoteLst;
//        } catch (IOException e) {
//            logger.log(Level.WARNING, e.toString());
//            showException(e);
//        }
//        return new ArrayList<>();
//    }

    private void setupUploadButton() {
        FileChooser fileChooser = new FileChooser();
        uploadBtn.setOnMouseClicked(mouseEvent -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
//                upload(file);
            }
        });
    }


    @Override
    public void stop() throws Exception {
        lister.interrupt();
        lister.join();
        client.disconnect();
        super.stop();
    }

    private class Lister implements Runnable {
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    Thread.sleep(1000);
//                    Platform.runLater(ClientAppG.this::populateServersList);
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void showException(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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

        alert.showAndWait();
    }

}
