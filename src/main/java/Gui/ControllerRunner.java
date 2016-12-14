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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.IOException;

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
    private Label tfProgress;

    @FXML
    private Button btRun;

    void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public void handleRun(ActionEvent actionEvent) throws IOException {
        btRun.setDisable(true);
        ClientRunner clientRunner = new ClientRunner("localhost",
                44444, 55555, Constants.ClientType.TCP_PERMANENT, parameters.getServerType());
        XYChart.Series<Number, Number> timePerRequestServer = new XYChart.Series();
        XYChart.Series<Number, Number> timePerClientServer = new XYChart.Series();
        XYChart.Series<Number, Number> timePerClient = new XYChart.Series();

        lchTimeRequestServer.getData().add(timePerRequestServer);
        lchTimeClientServer.getData().add(timePerClientServer);
        lchTimeClient.getData().add(timePerClient);

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
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
                            clientRunner.resetServer();
                            updateChartsAndProgress(i, numOfIteration, statistics, d);
                        }
                        clientRunner.exitServer();
                        break;
                }
                return null;
            }
        };
        pbProgress.setProgress(0D);
        new Thread(task).start();
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
}
