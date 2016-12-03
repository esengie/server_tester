package ru.spbau.mit.GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import ru.spbau.mit.CLIApps.ClientLaunchArgs;
import ru.spbau.mit.Protocol.RemoteFile;
import ru.spbau.mit.TorrentClient.TorrentClient;
import ru.spbau.mit.TorrentClient.TorrentClientImpl;
import ru.spbau.mit.TorrentClient.TorrentFile.FileManager;
import ru.spbau.mit.TorrentClient.TorrentFile.TorrentFileLocal;

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
    private static FileManager fileManager;
    private static TorrentClient client;
    private static Thread lister;

    @FXML
    private Button uploadBtn;

    @FXML
    private VBox serverFiles;

    @FXML
    private VBox downloading;

    private List<RemoteFile> remoteLst;
    private Stage stage;

    public static void main(String[] args2) {
        String args[] = {"-port", "8902", "-stateDir", ".", "-tracker", "localhost"};
        try {
            CommandLine cmd = ClientLaunchArgs.parseArgs(args);

            Short port = Short.parseShort(cmd.getOptionValue(ClientLaunchArgs.PORT_ARG_NAME));

            fileManager = new FileManager(new File(
                    cmd.getOptionValue(ClientLaunchArgs.STATE_DIR_ARG_NAME)));
            client = new TorrentClientImpl(fileManager, port);
            client.connect(cmd.getOptionValue(ClientLaunchArgs.TRACKER_ADDR_ARG_NAME));
            launch(args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            ClientLaunchArgs.launchUsage();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
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
        List<RemoteFile> remotes = populateServersList();

        Set<Integer> ids = new HashSet<>(fileManager.getFileIds());

        remotes = remotes.stream()
                .filter(s -> ids.contains(s.id))
                .collect(Collectors.toList());

        for (RemoteFile rf : remotes) {
            try {
                addToDownloadingList(rf, fileManager.getTorrentFile(rf.id));
            } catch (IOException e) {
                showException(e);
                logger.log(Level.SEVERE, e.toString());
                break;
            }
        }

        lister = new Thread(new Lister());
        lister.start();
    }

    private List<RemoteFile> populateServersList() {
        try {
            remoteLst = client.executeList();
            serverFiles.getChildren().clear();
            for (int i = 0; i < remoteLst.size(); ++i) {
                serverFiles.getChildren()
                        .add(createServerTextField(i, format(remoteLst.get(i))));
            }
            return remoteLst;
        } catch (IOException e) {
            logger.log(Level.WARNING, e.toString());
            showException(e);
        }
        return new ArrayList<>();
    }

    private void setupUploadButton() {
        FileChooser fileChooser = new FileChooser();
        uploadBtn.setOnMouseClicked(mouseEvent -> {
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                upload(file);
            }
        });
    }

    private void upload(File f) {
        try {
            RemoteFile remoteFile = client.executeUpload(f);
            addToDownloadingList(remoteFile, fileManager.getTorrentFile(remoteFile.id));
        } catch (IOException e) {
            showException(e);
        }
    }

    private void getFile(RemoteFile remote, File dir) {
        try {
            TorrentFileLocal tf = fileManager.getTorrentFile(remote.id);
            if (tf == null) {
                addToDownloadingList(remote, client.executeGet(dir, remote));
            }
            if (tf != null && tf.getParts().size() != tf.totalParts()) {
                client.executeGet(dir, remote);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            showException(e);
        }
    }

    private void addToDownloadingList(RemoteFile remote, TorrentFileLocal file) throws IOException {
        TextField field = new TextField(format(remote));
        field.setEditable(false);
        HBox.setHgrow(field, Priority.ALWAYS);

        ProgressBar pb = new ProgressBar(file.percent());
        file.addObserver(new ProgressObserver(pb));

        HBox hb = new HBox(field, pb);
        downloading.getChildren().add(hb);
    }

    private static String format(RemoteFile remote) {
        return MessageFormat.format("{0}: {1}", remote.id, remote.name);
    }

    private TextField createServerTextField(int i, String text) {
        TextField tf = new TextField();
        tf.setText(text);
        tf.setEditable(false);

        DirectoryChooser directoryChooser = new DirectoryChooser();
        tf.setOnMouseClicked(actionEvent -> {
            File dir = directoryChooser.showDialog(stage);
            if (dir != null) {
                RemoteFile rf = remoteLst.get(i);
                getFile(rf, dir);
            }
        });

        return tf;
    }

    @Override
    public void stop() throws Exception {
        lister.interrupt();
        lister.join();
        client.disconnect();
        fileManager.saveToDisk();
        super.stop();
    }

    private class Lister implements Runnable {
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    Thread.sleep(1000);
                    Platform.runLater(ClientAppG.this::populateServersList);
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
