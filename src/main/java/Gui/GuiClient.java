package Gui;/**
 * Created by kostya on 13.12.2016.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class GuiClient extends Application {
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("server-type.fxml"));
        Parent root = loader.load();

        ((ControllerServerType)loader.getController()).setMainApp(this);

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Server Benchmark");
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));
        primaryStage.show();
    }

    Stage getPrimaryStage() {
        return primaryStage;
    }
}
