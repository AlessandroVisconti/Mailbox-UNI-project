package serverposta;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerPosta extends Application {

    FXMLServerController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLServer.fxml"));
        try {
            stage.setScene(new Scene(loader.load()));
        } catch (IOException ex) {
            ex.toString();
        }
        controller = loader.<FXMLServerController>getController();
        stage.show();
    }

    @Override
    public void stop() {
        controller.finish();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
