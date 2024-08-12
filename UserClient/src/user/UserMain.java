package user;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import user.ui.base.BaseController;
import user.utils.http.HttpClientUtil;
import java.io.IOException;
import java.net.URL;

public class UserMain extends Application {

    private static final String APP_BASE_LOCATION = "ui/base/user-base.fxml";
    private BaseController baseController;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(APP_BASE_LOCATION);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            baseController = fxmlLoader.getController();
            Scene scene = new Scene(root, 800, 550);
            primaryStage.setTitle("Predictions - User");
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

    public static void main(String[] args) {
        launch(args);
    }

}
