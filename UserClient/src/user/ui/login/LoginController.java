package user.ui.login;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import user.ui.base.BaseController;
import user.utils.http.HttpClientUtil;

import java.io.Closeable;
import java.io.IOException;
import user.utils.Constants;

public class LoginController implements Closeable {
    @FXML
    public TextField userNameTextField;
    @FXML
    public Label errorMessageLabel;
    private BaseController baseController;
    private final StringProperty errorMessageProperty = new SimpleStringProperty("");

    @FXML
    public void initialize() {
        errorMessageLabel.textProperty().bind(errorMessageProperty);
    }

    @FXML
    private void loginButtonClicked(ActionEvent event) {
        String userName = userNameTextField.getText();

        if (userName.isEmpty()) {
            errorMessageProperty.set("User name is empty. You can't login with empty user name");
            return;
        }

        String finalUrl = HttpUrl
                .parse(Constants.LOGIN)
                .newBuilder()
                .addQueryParameter(Constants.USERNAME, userName)
                .build()
                .toString();


        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorMessageProperty.set("Something went wrong: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Platform.runLater(() ->
                            errorMessageProperty.set(responseBody)
                    );
                } else {
                    response.close();
                    Platform.runLater(() -> {
                        baseController.updateUserName(userName);
                        baseController.switchToBody();
                    });
                }
            }
        });
    }

    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        errorMessageProperty.set("");
    }

    @FXML
    private void quitButtonClicked(ActionEvent e) {
        Platform.exit();
    }

    public void setBaseController(BaseController chatAppMainController) {
        this.baseController = chatAppMainController;
    }

    @Override
    public void close() throws IOException {
        String finalUrl = HttpUrl
                .parse(Constants.LOGOUT)
                .newBuilder()
                .build()
                .toString();
        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("something went wrong when logging out");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    System.out.println("user logged out");
                } else {
                    System.out.println("something went wrong when logging out");
                }
                response.close();
            }
        });

    }
}
