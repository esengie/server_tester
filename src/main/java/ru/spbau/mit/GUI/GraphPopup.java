package ru.spbau.mit.GUI;

import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.spbau.mit.CreationAndConfigs.IntervalWithStep;
import ru.spbau.mit.CreationAndConfigs.ServerType;
import ru.spbau.mit.CreationAndConfigs.VaryingParameter;
import ru.spbau.mit.Tester.Timing.RunResults;

import java.util.ArrayList;
import java.util.List;

public class GraphPopup {
    private final VaryingParameter varying;
    private final String title;
    private List<RunResults> res;
    private IntervalWithStep step;

    public GraphPopup(List<RunResults> res, String title,
                      IntervalWithStep step, VaryingParameter varying) {
        this.res = res;
        this.title = title;
        this.varying = varying;
        this.step = step;
    }

    private LineChart<Number, Number> getChart(int i, String xlab) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xlab);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(ylabels[i]);

        LineChart<Number, Number> chart =
                new LineChart<>(xAxis, yAxis);
        chart.setTitle(titles[i]);
        chart.setLegendVisible(false);
        return chart;
    }

    String[] titles = {"Per sort", "Per request", "Per client"};
    String[] ylabels = {"Time, mcs", "Time, mcs", "Time, ms"};

    public void display() {
        List<XYChart.Series<Number, Number>> series = new ArrayList<>();
        List<LineChart<Number, Number>> charts = new ArrayList<>();
        for (int i = 0; i < 3; ++i){
            series.add(new XYChart.Series<>());
            charts.add(getChart(i, varying.toString()));
            charts.get(i).getData().add(series.get(i));
        }

        for (int i = 0; i < res.size(); ++i) {
            RunResults r = res.get(i);
            int curStep = step.getStart() + step.getStep() * i;
            series.get(0).getData().add(new XYChart.Data<>(curStep, r.perSort / 1000));
            series.get(1).getData().add(new XYChart.Data<>(curStep, r.perRequest / 1000));
            series.get(2).getData().add(new XYChart.Data<>(curStep, r.perClient / 1000000));
        }

        HBox hb = new HBox(charts.get(0), charts.get(1));
        VBox vb = new VBox(charts.get(2), hb);

        Scene scene = new Scene(vb, 1024, 768);
        Stage st = new Stage();
        st.setScene(scene);
        st.setTitle(title);
        st.show();
    }
}
