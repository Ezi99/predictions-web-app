package user.ui.screen.request;

import com.google.gson.Gson;
import dto.definition.SimulationListDTO;
import dto.requests.RequestDTO;
import dto.requests.SubmitRequestDTO;
import dto.requests.RequestStatus;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import user.ui.body.BodyController;
import user.ui.tasks.request.RequestRefresher;
import user.utils.Constants;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Timer;

import static user.utils.http.HttpClientUtil.HTTP_CLIENT;

public class RequestsController implements Closeable {

    @FXML
    private Label selectedSimulationLabel;
    @FXML
    private TextField executionCountField;
    @FXML
    private TextField ticksToEndField;
    @FXML
    private TextField secondsToEndField;
    @FXML
    private TreeView<String> simulationTree;
    @FXML
    private TableView<RequestTableEntry> requestsTable;
    @FXML
    private Button executeButton;
    @FXML
    private CheckBox checkBox;
    @FXML
    private Label messageLabel;
    @FXML
    private TableColumn<RequestTableEntry, String> requestIDColumn;
    @FXML
    private TableColumn<RequestTableEntry, String> simulationColumn;
    @FXML
    private TableColumn<RequestTableEntry, Integer> requestedExecColumn;
    @FXML
    private TableColumn<RequestTableEntry, RequestStatus> statusColumn;
    @FXML
    private TableColumn<RequestTableEntry, Integer> doneColumn;
    @FXML
    private TableColumn<RequestTableEntry, Integer> runningColumn;
    private BodyController bodyController;
    private RequestTableEntry chosenEntry;
    private RequestRefresher tableRefresher;
    private Timer timer;


    @FXML
    public void initialize() {
        simulationTree.setRoot(new TreeItem<>("Simulations"));
        setSimulationTreeListener();
        setRequestsTable();
        setRequestTableHandler();
    }

    private void setRequestsTable() {
        requestIDColumn.setCellValueFactory(new PropertyValueFactory<>("RequestID"));
        simulationColumn.setCellValueFactory(new PropertyValueFactory<>("SimulationName"));
        requestedExecColumn.setCellValueFactory(new PropertyValueFactory<>("RequestedExecutions"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("Status"));
        doneColumn.setCellValueFactory(new PropertyValueFactory<>("DoneExecutions"));
        runningColumn.setCellValueFactory(new PropertyValueFactory<>("RunningExecutions"));
    }

    private void setRequestTableHandler() {
        EventHandler<MouseEvent> rowClickHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                chosenEntry = requestsTable.getSelectionModel().getSelectedItem();
                if (chosenEntry != null) {
                    executeButton.setDisable(chosenEntry.getStatus() != RequestStatus.APPROVED);
                }
            }
        };

        requestsTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<RequestTableEntry> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(rowClickHandler);
            return row;
        });
    }


    public void setBodyController(BodyController bodyController) {
        this.bodyController = bodyController;
    }

    @FXML
    void checkBoxListener(ActionEvent event) {
        ticksToEndField.setText("");
        secondsToEndField.setText("");
    }

    @FXML
    void executionCountListener(ActionEvent event) {
        messageLabel.setText("");
    }

    @FXML
    void secondsToEndListener(ActionEvent event) {
        checkBox.setSelected(false);
        messageLabel.setText("");
    }

    @FXML
    void ticksToEndListener(ActionEvent event) {
        checkBox.setSelected(false);
        messageLabel.setText("");
    }

    @FXML
    void submitButtonListener(ActionEvent event) {
        if (validateRequest()) {
            SubmitRequestDTO submitRequestDTO = new SubmitRequestDTO();

            submitRequestDTO.setUserName(bodyController.getUserName());
            submitRequestDTO.setSelectedSimulation(selectedSimulationLabel.getText());
            submitRequestDTO.setNumberOfExecutions(Integer.parseInt(executionCountField.getText()));
            if (!ticksToEndField.getText().isEmpty()) {
                submitRequestDTO.setNumberOfTicks(Integer.parseInt(ticksToEndField.getText()));
            }
            if (!secondsToEndField.getText().isEmpty()) {
                submitRequestDTO.setNumberOfSeconds(Integer.parseInt(secondsToEndField.getText()));
            }
            if (checkBox.isSelected()) {
                submitRequestDTO.setByUser(true);
            }

            sendRequest(submitRequestDTO);
        }
    }

    private void sendRequest(SubmitRequestDTO submitRequestDTO) {
        Gson gson = new Gson();
        Request request = new Request.Builder()
                .url(Constants.REQUEST)
                .post(RequestBody.create(gson.toJson(submitRequestDTO).getBytes()))
                .build();
        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        messageLabel.setText("something went wrong when submitting request"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Platform.runLater(() -> {
                    messageLabel.setText("request submitted successfully !");
                });
                response.close();
            }
        });
    }

    @FXML
    void executeListener(ActionEvent event) {
        if (chosenEntry != null && chosenEntry.getStatus() == RequestStatus.APPROVED) {
            bodyController.execute(chosenEntry);
        }
    }

    private boolean validateRequest() {

        if (selectedSimulationLabel.getText() == null || selectedSimulationLabel.getText().isEmpty()) {
            messageLabel.setText("make sure to select a simulation");
            return false;
        }

        if (executionCountField.getText().isEmpty()) {
            messageLabel.setText("make sure to set number of executions");
            return false;
        }

        if (ticksToEndField.getText().isEmpty() && secondsToEndField.getText().isEmpty() &&
                !checkBox.isSelected()) {
            messageLabel.setText("make sure to select an end condition");
            return false;
        }

        if ((!ticksToEndField.getText().isEmpty() || !secondsToEndField.getText().isEmpty())
                && checkBox.isSelected()) {
            messageLabel.setText("invalid end conditions");
            return false;
        }

        return validateIntValue(ticksToEndField.getText())
                && validateIntValue(secondsToEndField.getText())
                && validateIntValue(executionCountField.getText());
    }

    private boolean validateIntValue(String value) {

        try {
            if (!value.isEmpty()) {
                Integer.parseInt(value);
            }
        } catch (Exception e) {
            messageLabel.setText(value + "is invalid to insert here, get serious please :(");
            return false;
        }

        return true;
    }

    private void setSimulationTreeListener() {
        MultipleSelectionModel<TreeItem<String>> tvSelModel = simulationTree.getSelectionModel();
        tvSelModel.selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            public void changed(ObservableValue<? extends TreeItem<String>> changed, TreeItem<String> oldVal,
                                TreeItem<String> newVal) {
                if (newVal != null && newVal.getValue() != null && !newVal.getValue().equals("Simulations")) {
                    selectedSimulationLabel.setText(newVal.getValue());
                }
            }
        });
    }

    public void startSimulationListRefresher() {
        tableRefresher = new RequestRefresher(
                this::updateRequestTable,
                bodyController.getUserName(),
                messageLabel::setText);

        timer = new Timer();
        timer.schedule(tableRefresher, Constants.START_RATE, Constants.REFRESH_RATE);
    }

    private void updateRequestTable(List<RequestDTO> requestDTOs) {
        requestsTable.getItems().clear();

        if(requestDTOs != null) {
            for (RequestDTO requestDTO : requestDTOs) {
                RequestTableEntry requestTableEntry = new RequestTableEntry();
                SubmitRequestDTO submitRequestDTO = requestDTO.getSubmitRequestDTO();

                requestTableEntry.setRequestID(requestDTO.getRequestID());
                requestTableEntry.setSimulationName(submitRequestDTO.getSelectedSimulation());
                requestTableEntry.setRequestedExecutions(submitRequestDTO.getNumberOfExecutions());
                requestTableEntry.setStatus(requestDTO.getRequestStatus());
                requestTableEntry.setRunningExecutions(requestDTO.getRunning());
                requestTableEntry.setDoneExecutions(requestDTO.getDone());
                requestsTable.getItems().add(requestTableEntry);
            }
        }

        int i = 0;
        for (RequestTableEntry requestTableEntry : requestsTable.getItems()) {
            if (chosenEntry != null && chosenEntry.getRequestID() == requestTableEntry.getRequestID()) {
                requestsTable.getSelectionModel().select(i);
                chosenEntry = requestTableEntry;
                executeButton.setDisable(chosenEntry.getStatus() != RequestStatus.APPROVED);
                break;
            }
            i++;
        }
    }

    public void updateSimulationList(SimulationListDTO simulations) {
        simulationTree.getRoot().getChildren().clear();
        for (String simulation : simulations.getSimulationsName()) {
            simulationTree.getRoot().getChildren().add(new TreeItem<>(simulation));
        }
    }

    public void setActive() {
        startSimulationListRefresher();
    }

    @Override
    public void close() {
        if (tableRefresher != null && timer != null) {
            tableRefresher.cancel();
            timer.cancel();
        }
    }
}
