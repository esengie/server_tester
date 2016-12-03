package ru.spbau.mit.GUI;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.util.Observable;
import java.util.Observer;

class ProgressObserver implements Observer {
    private final ProgressBar pb;

    ProgressObserver(ProgressBar pb) {
        this.pb = pb;
    }

    @Override
    public void update(Observable observable, Object o) {
        double value = (double) o;
        Platform.runLater(() -> pb.setProgress(value));
    }
}
