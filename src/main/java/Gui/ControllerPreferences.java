package Gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Created by kostya on 13.12.2016.
 */
public class ControllerPreferences {
    @FXML
    private TextField tfMin;

    @FXML
    private TextField tfMax;

    @FXML
    private TextField tfStep;

    @FXML
    private TextField tfNumOfRequests;

    @FXML
    private ToggleGroup tgVariableParameter;

    @FXML
    private TextField tfNumOfElements;

    @FXML
    private TextField tfNumOfClients;

    @FXML
    private TextField tfDelta;
    private GuiClient mainApp;
    private int serverType;

    public void handleNext(ActionEvent actionEvent) throws IOException {
        try {
            int min = Integer.parseInt(tfMin.getText());
            int max = Integer.parseInt(tfMax.getText());


            int step = Integer.parseInt(tfStep.getText());

            int numOfRequests = Integer.parseInt(tfNumOfRequests.getText());
            int numOfElements = 1;
            if (!tgVariableParameter.getSelectedToggle().getUserData().equals("N")) {
                numOfElements = Integer.parseInt(tfNumOfElements.getText());
            }

            int numOfClients = 1;
            if (!tgVariableParameter.getSelectedToggle().getUserData().equals("M")) {
                numOfClients = Integer.parseInt(tfNumOfClients.getText());
            }

            int delta = 0;
            if (!tgVariableParameter.getSelectedToggle().getUserData().equals("D")) {
                delta = Integer.parseInt(tfDelta.getText());
            }

            if (min > max || min < 1 || step < 1 || numOfRequests < 1 || numOfElements < 1 || numOfClients < 1 || delta < 0) {
                showInformationDialog("wrong parameters");
                return;
            }

            Parameters parameters = new Parameters(tgVariableParameter.getSelectedToggle().getUserData().toString(),
                    min, max, step, numOfRequests, numOfElements, numOfClients, delta, serverType);
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("runner.fxml"));
            Parent root = loader.load();

            ((ControllerRunner)loader.getController()).setParameters(parameters);

            mainApp.getPrimaryStage().setScene(new Scene(root));
            mainApp.getPrimaryStage().setTitle("Server Benchmark");
            mainApp.getPrimaryStage().getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
            mainApp.getPrimaryStage().show();

        } catch (NumberFormatException e) {
            showInformationDialog("wrong parameters");
        }
    }

    public void handleRadioButton(ActionEvent actionEvent) {
        String selectedRadioButton = tgVariableParameter.getSelectedToggle().getUserData().toString();
        switch (selectedRadioButton) {
            case "N":
                tfNumOfElements.setDisable(true);
                tfNumOfElements.setText("");
                tfNumOfClients.setDisable(false);
                tfDelta.setDisable(false);
                break;
            case "M":
                tfNumOfElements.setDisable(false);
                tfNumOfClients.setDisable(true);
                tfNumOfClients.setText("");
                tfDelta.setDisable(false);
                break;
            case "D":
                tfNumOfElements.setDisable(false);
                tfNumOfClients.setDisable(false);
                tfDelta.setDisable(true);
                tfDelta.setText("");
                break;
        }
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

    void setMainApp(GuiClient mainApp) {
        this.mainApp = mainApp;
    }

    void setServerType(int serverType) {
        this.serverType = serverType;
    }
}
