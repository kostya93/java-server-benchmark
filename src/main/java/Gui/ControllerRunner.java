package Gui;

import Client.ClientRunner;
import Client.Constants;
import Client.Statistics;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

;

/**
 * Created by kostya on 13.12.2016.
 */
public class ControllerRunner {
    private Parameters parameters;

    @FXML
    private LineChart<Number, Number> lchTimeRequestServer;

    @FXML
    private LineChart<Number, Number> lchTimeClientServer;

    @FXML
    private LineChart<Number, Number> lchTimeClient;

    @FXML
    private ProgressBar pbProgress;

    @FXML
    private Button btRun;

    @FXML
    public Label lbParams;

    void setParameters(Parameters parameters) {
        this.parameters = parameters;
        lbParams.setText(parameters.toString());
    }

    public void handleRun(ActionEvent actionEvent) throws IOException {
        btRun.setDisable(true);
        ClientRunner clientRunner = new ClientRunner("localhost",
                44444, 55555, getClientType(parameters.getServerType()), parameters.getServerType());
        XYChart.Series<Number, Number> timePerRequestServer = new XYChart.Series();
        XYChart.Series<Number, Number> timePerClientServer = new XYChart.Series();
        XYChart.Series<Number, Number> timePerClient = new XYChart.Series();

        lchTimeRequestServer.getData().add(timePerRequestServer);
        lchTimeClientServer.getData().add(timePerClientServer);
        lchTimeClient.getData().add(timePerClient);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Statistics> results = new ArrayList<>();
                double numOfIteration = (parameters.getMax()-parameters.getMin())/parameters.getStep() + 1;
                switch (parameters.getVariableParameter()) {
                    case "N":
                        setXAxisLabels("N, количество элементов");
                        clientRunner.startServer();
                        for (int n = parameters.getMin(), i = 1; n <= parameters.getMax(); n += parameters.getStep(), i++) {
                            Statistics statistics = clientRunner.run(
                                    n,
                                    parameters.getNumOfClients(),
                                    parameters.getDelta(),
                                    parameters.getNumOfRequests()
                            );
                            results.add(statistics);
                            clientRunner.resetServer();
                            updateChartsAndProgress(i, numOfIteration, statistics, n);
                        }
                        clientRunner.exitServer();
                        break;
                    case "M":
                        setXAxisLabels("M, количество клиентов");
                        clientRunner.startServer();
                        for (int m = parameters.getMin(), i = 1; m <= parameters.getMax(); m += parameters.getStep(), i++) {
                            Statistics statistics = clientRunner.run(
                                    parameters.getNumOfElements(),
                                    m,
                                    parameters.getDelta(),
                                    parameters.getNumOfRequests()
                            );
                            results.add(statistics);
                            clientRunner.resetServer();
                            updateChartsAndProgress(i, numOfIteration, statistics, m);
                        }
                        clientRunner.exitServer();
                        break;
                    case "D":
                        setXAxisLabels("Δ, пауза между запросами");
                        clientRunner.startServer();
                        for (int d = parameters.getMin(), i = 1; d <= parameters.getMax(); d += parameters.getStep(), i++) {
                            Statistics statistics = clientRunner.run(
                                    parameters.getNumOfElements(),
                                    parameters.getNumOfClients(),
                                    d,
                                    parameters.getNumOfRequests()
                            );
                            results.add(statistics);
                            clientRunner.resetServer();
                            updateChartsAndProgress(i, numOfIteration, statistics, d);
                        }
                        clientRunner.exitServer();
                        break;
                }
                saveResults(results);
                return null;
            }
        };
        pbProgress.setProgress(0D);
        task.setOnSucceeded(e -> {
            showInformationDialog("Done. Results in files:\n" +
                    "TimeRequestServer.txt\n" +
                    "TimeClientServer.txt\n" +
                    "TimeClient.txt\n" +
                    "Parameters.txt");
        });
        new Thread(task).start();
    }

    private Constants.ClientType getClientType(int serverType) {
        switch (serverType) {
            case Common.Constants.ServerType.TCP_ASYNC:
            case Common.Constants.ServerType.TCP_CACHED_THREAD_POOL:
            case Common.Constants.ServerType.TCP_NON_BLOCKING:
            case Common.Constants.ServerType.TCP_THREAD:
                return Constants.ClientType.TCP_PERMANENT;

            case Common.Constants.ServerType.TCP_ONE_THREAD_SEQUENTIAL:
                return Constants.ClientType.TCP_NON_PERMANENT;

            case Common.Constants.ServerType.UDP_THREAD:
            case Common.Constants.ServerType.UDP_TREAD_POOL:
                return Constants.ClientType.UDP;

            default:
                throw new NotImplementedException();
        }
    }

    private void saveResults(List<Statistics> results) throws IOException {
        try (PrintWriter printWriter = new PrintWriter("TimeRequestServer.txt")) {
            printWriter.println(results.size());
            for (Statistics st : results) {
                printWriter.println(st.getTimePerRequestServer());
            }
        }

        try (PrintWriter printWriter = new PrintWriter("TimeClientServer.txt")) {
            printWriter.println(results.size());
            for (Statistics st : results) {
                printWriter.println(st.getTimePerClientServer());
            }
        }

        try (PrintWriter printWriter = new PrintWriter("TimeClient.txt")) {
            printWriter.println(results.size());
            for (Statistics st : results) {
                printWriter.println(st.getTimePerClient());
            }
        }

        try (PrintWriter printWriter = new PrintWriter("Parameters.txt")) {
            printWriter.println(parameters.toString());
        }
    }

    private void setXAxisLabels(String value) {
        Platform.runLater(() -> {
            lchTimeRequestServer.getXAxis().setLabel(value);
            lchTimeClientServer.getXAxis().setLabel(value);
            lchTimeClient.getXAxis().setLabel(value);
        });
    }

    private void updateChartsAndProgress(double curIter, double numOfIteration, Statistics statistics, int xValue) {
        Platform.runLater(() -> {
            lchTimeRequestServer.getData().get(0).getData().add(new XYChart.Data(xValue, statistics.getTimePerRequestServer()));
            lchTimeClientServer.getData().get(0).getData().add(new XYChart.Data(xValue, statistics.getTimePerClientServer()));
            lchTimeClient.getData().get(0).getData().add(new XYChart.Data(xValue, statistics.getTimePerClient()));
            pbProgress.setProgress(curIter /numOfIteration);
        });
    }

    private void showInformationDialog(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
        alert.showAndWait();
    }
}
