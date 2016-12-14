package Gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;

import java.io.IOException;

/**
 * Created by kostya on 13.12.2016.
 */
public class ControllerServerType {
    @FXML
    private ToggleGroup tgServerType;

    private GuiClient mainApp;

    void setMainApp(GuiClient mainApp) {
        this.mainApp = mainApp;
    }

    public void handleNext(ActionEvent actionEvent) throws IOException {
        int serverType = Integer.parseInt(tgServerType.getSelectedToggle().getUserData().toString());
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("preferences.fxml"));
        Parent root = loader.load();

        ((ControllerPreferences)loader.getController()).setMainApp(mainApp);
        ((ControllerPreferences)loader.getController()).setServerType(serverType);

        mainApp.getPrimaryStage().setScene(new Scene(root));
        mainApp.getPrimaryStage().setTitle("Server Benchmark");
        mainApp.getPrimaryStage().getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
        mainApp.getPrimaryStage().show();
    }
}
