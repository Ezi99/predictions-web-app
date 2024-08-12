package user.ui.base;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import user.ui.body.BodyController;
import user.ui.login.LoginController;
import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import user.utils.Constants;

public class BaseController implements Closeable {

    @FXML
    private ScrollPane scrollPaneBase;
    private GridPane loginComponent;
    private LoginController loginController;
    private BorderPane bodyComponent;
    private BodyController bodyController;

    @FXML
    public void initialize() {
        loadLoginPage();
        loadPredictionsPage();
    }

    private void loadLoginPage() {
        URL loginPageUrl = getClass().getResource(Constants.LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            loginController = fxmlLoader.getController();
            loginController.setBaseController(this);
            loginComponent.prefWidthProperty().bind(scrollPaneBase.widthProperty());
            loginComponent.prefHeightProperty().bind(scrollPaneBase.heightProperty());
            scrollPaneBase.setContent(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPredictionsPage() {
        URL predictionsPageUrl = getClass().getResource(Constants.CHAT_ROOM_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(predictionsPageUrl);
            bodyComponent = fxmlLoader.load();
            bodyController = fxmlLoader.getController();
            bodyComponent.prefWidthProperty().bind(scrollPaneBase.widthProperty());
            bodyComponent.prefHeightProperty().bind(scrollPaneBase.heightProperty());
            bodyController.setBaseController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchToBody() {
        scrollPaneBase.setContent(bodyComponent);
        bodyController.setActive();
    }

    @Override
    public void close() throws IOException {
        loginController.close();
        bodyController.close();
    }

    public void updateUserName(String userName) {
        bodyController.setUserName(userName);
    }
}
