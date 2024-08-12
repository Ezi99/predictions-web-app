package admin.ui.management.header;

import admin.ui.base.BaseController;
import admin.utils.Constants;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static admin.utils.http.HttpClientUtil.HTTP_CLIENT;

public class HeaderController {

    @FXML
    private Button loadFileButton;
    @FXML
    private Label loadedFileLabel;
    @FXML
    private Label pendingCount;
    @FXML
    private Label runningCount;
    @FXML
    private Label finishedCount;
    @FXML
    private Label fileLoadedLabel;
    @FXML
    private Label fileNotLoadedLabel;
    @FXML
    private Button setThreadButton;
    @FXML
    private TextField threadCountField;
    @FXML
    private Label threadCountLabel;
    private final SimpleIntegerProperty threadCountProperty;

    private BaseController baseController;

    public HeaderController() {
        threadCountProperty = new SimpleIntegerProperty(0);
    }

    public void setBaseController(BaseController baseController) {
        this.baseController = baseController;
        fileNotLoadedLabel.textProperty().bind(baseController.logInMessageProperty());
        setThreadButton.disableProperty().bind(baseController.isAdminValidProperty().not());
        loadFileButton.disableProperty().bind(baseController.isAdminValidProperty().not());
        threadCountLabel.textProperty().bind(threadCountProperty.asString());
    }

    @FXML
    void loadFileListener(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("choose an xml file for simulation");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(baseController.getPrimaryStage());
        if (selectedFile == null) {
            return;
        }

        baseController.loadSimulationInfo(selectedFile);
    }

    @FXML
    void threadCountListener(ActionEvent event) {
        if(!pendingCount.getText().equals("0") || !runningCount.getText().equals("0")){
            printErrorMessage("can't set thread count when there's still simulations in process");
        } else {
            try{
                int count = Integer.parseInt(threadCountField.getText());
                setThreadCount(count);
            } catch (Exception e){
                printErrorMessage("invalid thread count");
            }
        }
    }

    private void setThreadCount(int count) {
        Request request = new Request.Builder()
                .url(Constants.SET_THREAD_POOL)
                .put(RequestBody.create(String.valueOf(count).getBytes()))
                .build();

        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        printErrorMessage("something went wrong when updating thread pool"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bodyString = response.body().string();
                if(response.isSuccessful()){
                    Platform.runLater(() -> {
                                printGoodMessage(bodyString);
                                threadCountProperty.set(count);
                            });
                } else {
                    Platform.runLater(() ->
                            printErrorMessage(bodyString));
                }
            }
        });
    }

    public void bindFileStatus(ReadOnlyStringProperty readOnlyStringProperty, ReadOnlyObjectProperty<Throwable> throwableReadOnlyObjectProperty) {
        unBindFileStatus();
        fileLoadedLabel.textProperty().bind(readOnlyStringProperty);
        fileNotLoadedLabel.textProperty().bind(throwableReadOnlyObjectProperty.asString());
    }

    private void unBindFileStatus() {
        fileLoadedLabel.textProperty().unbind();
        fileNotLoadedLabel.textProperty().unbind();
    }

    public void printErrorMessage(String message){
        unBindFileStatus();
        fileLoadedLabel.setText("");
        fileNotLoadedLabel.setText(message);
    }

    public void printGoodMessage(String message){
        unBindFileStatus();
        fileLoadedLabel.setText(message);
        fileNotLoadedLabel.setText("");
    }

    public void onLoadSuccess(String path) {
        unBindFileStatus();
        loadedFileLabel.setText(path);
        fileNotLoadedLabel.setText("");
    }

    public SimpleIntegerProperty getThreadCountProperty(){
        return threadCountProperty;
    }
}
