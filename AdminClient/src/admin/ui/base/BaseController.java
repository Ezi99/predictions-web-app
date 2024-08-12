package admin.ui.base;

import admin.ui.allocation.AllocationsController;
import admin.ui.history.ExecutionHistoryController;
import admin.ui.management.ManagementController;
import admin.ui.tasks.management.DetailsTask;
import admin.utils.http.HttpClientUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import admin.utils.Constants;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class BaseController implements Closeable {

    private final SimpleStringProperty logInMessage;
    private final SimpleBooleanProperty isAdminValid;
    private final SimpleIntegerProperty threadCount;
    @FXML
    private Tab allocationTab;
    @FXML
    private Tab executionHistoryTab;
    @FXML
    private Tab managementTab;
    @FXML
    private TabPane tabPane;
    @FXML
    private ScrollPane managementComponent;
    @FXML
    private ManagementController managementComponentController;
    @FXML
    private ScrollPane allocationComponent;
    @FXML
    private AllocationsController allocationComponentController;
    @FXML
    private ScrollPane historyComponent;
    @FXML
    private ExecutionHistoryController historyComponentController;

    private Stage primaryStage;

    public BaseController() {
        logInMessage = new SimpleStringProperty("");
        isAdminValid = new SimpleBooleanProperty(false);
        threadCount = new SimpleIntegerProperty(0);
    }

    @FXML
    public void initialize() {
        logIn();
        managementComponentController.setBaseController(this);
        allocationComponentController.setBaseController(this);
        threadCount.bind(managementComponentController.getHeaderController().getThreadCountProperty());
    }

    private void logIn() {
        String finalUrl = HttpUrl
                .parse(Constants.ADMIN_LOGIN)
                .newBuilder()
                .build()
                .toString();
        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    logInMessage.set("unable to log in");
                    allocationTab.setDisable(true);
                    executionHistoryTab.setDisable(true);
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBodyString = response.body().string();

                if (!response.isSuccessful()) {
                    Platform.runLater(() -> {
                        allocationTab.setDisable(true);
                        executionHistoryTab.setDisable(true);
                    });
                } else {
                    Platform.runLater(() -> {
                        isAdminValid.set(true);
                        managementComponentController.setActive();
                        allocationComponentController.setActive();
                        historyComponentController.setActive();
                    });
                }

                Platform.runLater(() -> logInMessage.set(responseBodyString));
            }
        });

    }

    private void logOut() {
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
                System.out.println("logged out");
                response.close();
            }
        });
    }

    public SimpleStringProperty logInMessageProperty() {
        return logInMessage;
    }

    public SimpleBooleanProperty isAdminValidProperty() {
        return isAdminValid;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void loadSimulationInfo(File selectedFile) {
        DetailsTask detailsTask = new DetailsTask(selectedFile);
        managementComponentController.bindFileStatus(detailsTask.messageProperty(), detailsTask.exceptionProperty());
        detailsTask.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            managementComponentController.addWorldInfoDTO(detailsTask.valueProperty().get());
            managementComponentController.getHeaderController().onLoadSuccess(selectedFile.getAbsolutePath());
        });
        new Thread(detailsTask).start();
    }

    public int getThreadCount(){
        return threadCount.get();
    }

    @Override
    public void close() throws IOException {
        logOut();
        managementComponentController.close();
        allocationComponentController.close();
    }
}
