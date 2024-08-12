package admin.ui.allocation;

import admin.ui.allocation.table.AllocationTableEntry;
import admin.ui.tasks.allocation.AllocationTableRefresher;
import admin.ui.base.BaseController;
import admin.utils.Constants;
import com.google.gson.Gson;
import dto.requests.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;

import static admin.utils.http.HttpClientUtil.HTTP_CLIENT;

public class AllocationsController implements Closeable {

    @FXML
    private TableView<AllocationTableEntry> allocationTable;
    @FXML
    private TableColumn<AllocationTableEntry, String> simulationColumn;
    @FXML
    private TableColumn<AllocationTableEntry, String> userColumn;
    @FXML
    private TableColumn<AllocationTableEntry, Integer> execCountColumn;
    @FXML
    private TableColumn<AllocationTableEntry, Integer> ticksColumn;
    @FXML
    private TableColumn<AllocationTableEntry, Integer> secondsColumn;
    @FXML
    private TableColumn<AllocationTableEntry, Boolean> byUserColumn;
    @FXML
    private TableColumn<AllocationTableEntry, RequestStatus> stateColumn;
    @FXML
    private TableColumn<AllocationTableEntry, Integer> doneColumn;
    @FXML
    private TableColumn<AllocationTableEntry, Integer> runningColumn;
    @FXML
    private Button denyButton;
    @FXML
    private Button approveButton;
    @FXML
    private Label messageLabel;
    private AllocationTableEntry chosenRequest;
    private AllocationTableRefresher tableRefresher;
    private Timer timer;
    private BaseController baseController;


    @FXML
    public void initialize() {
        denyButton.setDisable(true);
        approveButton.setDisable(true);
        setAllocationTable();
        setAllocationTableHandler();
    }

    public void setBaseController(BaseController baseController) {
        this.baseController = baseController;
    }

    private void setAllocationTable() {
        simulationColumn.setCellValueFactory(new PropertyValueFactory<>("SimulationName"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("UserName"));
        execCountColumn.setCellValueFactory(new PropertyValueFactory<>("ExecCount"));
        ticksColumn.setCellValueFactory(new PropertyValueFactory<>("Ticks"));
        secondsColumn.setCellValueFactory(new PropertyValueFactory<>("Seconds"));
        byUserColumn.setCellValueFactory(new PropertyValueFactory<>("ByUser"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("Status"));
        doneColumn.setCellValueFactory(new PropertyValueFactory<>("Done"));
        runningColumn.setCellValueFactory(new PropertyValueFactory<>("Running"));
    }

    private void setAllocationTableHandler() {
        EventHandler<MouseEvent> rowClickHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                chosenRequest = allocationTable.getSelectionModel().getSelectedItem();
                if (chosenRequest != null) {
                    updateButtons();
                }
            }
        };

        allocationTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<AllocationTableEntry> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(rowClickHandler);
            return row;
        });
    }

    private void updateButtons() {
        if (chosenRequest.getStatus() != RequestStatus.PENDING) {
            approveButton.setDisable(true);
            denyButton.setDisable(true);
        } else {
            approveButton.setDisable(false);
            denyButton.setDisable(false);
        }
    }


    @FXML
    void approveListener(ActionEvent event) {
        if(baseController.getThreadCount() != 0){
            if (chosenRequest != null && chosenRequest.getStatus() == RequestStatus.PENDING) {
                updateRequestStatus(RequestStatus.APPROVED);
                updateButtons();
                messageLabel.setText("");
            }
        } else {
            messageLabel.setText("cannot approve before setting the thread pool");
        }
    }

    @FXML
    void denyListener(ActionEvent event) {
        if(baseController.getThreadCount() != 0){
            if (chosenRequest != null && chosenRequest.getStatus() == RequestStatus.PENDING) {
                updateRequestStatus(RequestStatus.DENIED);
                updateButtons();
                messageLabel.setText("");
            }
        } else {
            messageLabel.setText("cannot deny before setting the thread pool");
        }
    }



    private void updateRequestStatus(RequestStatus requestStatus){
        Gson gson = new Gson();
        UpdateRequestStatusDTO updateRequestStatusDTO = new UpdateRequestStatusDTO();
        chosenRequest.setStatus(requestStatus);
        updateRequestStatusDTO.setUserName(chosenRequest.getUserName());
        updateRequestStatusDTO.setRequestID(chosenRequest.getRequestID());
        updateRequestStatusDTO.setRequestStatus(requestStatus);
        Request request = new Request.Builder()
                .url(Constants.ADMIN_ALLOCATIONS)
                .put(RequestBody.create(gson.toJson(updateRequestStatusDTO).getBytes()))
                .build();
        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                    messageLabel.setText("something went wrong when updating request"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(!response.isSuccessful()){
                    Platform.runLater(() ->
                        messageLabel.setText("something went wrong when updating request"));
                }
                response.close();
            }
        });
    }

    public void startSimulationListRefresher() {
        tableRefresher = new AllocationTableRefresher(
                messageLabel::setText,
                this::updateAllocationTable
        );

        timer = new Timer();
        timer.schedule(tableRefresher, Constants.START_RATE, Constants.REFRESH_RATE);
    }

    private void updateAllocationTable(AllocationRequestsDTO allocationRequestsDTO) {
        allocationTable.getItems().clear();
        Map<String, Map<Integer, RequestDTO>> requests = allocationRequestsDTO.getRequests();

        for (String userName : requests.keySet()) {
            for (Integer requestID : requests.get(userName).keySet()) {
                RequestDTO requestDTO = requests.get(userName).get(requestID);
                allocationTable.getItems().add(createAllocationTableEntry(requestDTO));
            }
        }

        int i = 0;
        for (AllocationTableEntry allocationTableEntry : allocationTable.getItems()) {
            if (chosenRequest != null && chosenRequest.getRequestID() == allocationTableEntry.getRequestID()) {
                allocationTable.getSelectionModel().select(i);
                chosenRequest = allocationTableEntry;
                updateButtons();
                break;
            }
            i++;
        }

        allocationTable.refresh();
    }

    private AllocationTableEntry createAllocationTableEntry(RequestDTO requestDTO){
        AllocationTableEntry allocationTableEntry = new AllocationTableEntry();
        SubmitRequestDTO submitRequestDTO = requestDTO.getSubmitRequestDTO();

        allocationTableEntry.setRequestID(requestDTO.getRequestID());
        allocationTableEntry.setUserName(submitRequestDTO.getUserName());
        allocationTableEntry.setSimulationName(submitRequestDTO.getSelectedSimulation());
        allocationTableEntry.setExecCount(submitRequestDTO.getNumberOfExecutions());
        allocationTableEntry.setTicks(submitRequestDTO.getNumberOfTicks());
        allocationTableEntry.setSeconds(submitRequestDTO.getNumberOfSeconds());
        allocationTableEntry.setByUser(submitRequestDTO.isByUser());
        allocationTableEntry.setStatus(requestDTO.getRequestStatus());
        allocationTableEntry.setDone(requestDTO.getDone());
        allocationTableEntry.setRunning(requestDTO.getRunning());
        return allocationTableEntry;
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
