package admin;

import admin.ui.base.BaseController;
import admin.utils.http.HttpClientUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class AdminMain extends Application {

    private static final String APP_BASE_LOCATION = "ui/base/admin-base.fxml";
    private BaseController baseController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(APP_BASE_LOCATION);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            Scene scene = new Scene(root, 800, 550);
            baseController = fxmlLoader.getController();
            baseController.setPrimaryStage(primaryStage);
            primaryStage.setTitle("Predictions - Admin");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException | NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        baseController.close();
        HttpClientUtil.shutdown();
    }

}
