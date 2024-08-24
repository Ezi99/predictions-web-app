package user.ui.screen.execution;

import com.google.gson.Gson;
import dto.definition.ActiveEnvironmentDTO;
import dto.definition.EntityInfoDTO;
import dto.definition.PropertyInfoDTO;
import dto.definition.WorldInfoDTO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import user.ui.body.BodyController;
import user.utils.Constants;

import java.io.IOException;
import java.util.Collection;

import static user.utils.http.HttpClientUtil.HTTP_CLIENT;

public class ExecutionController {

    @FXML
    private Button clearButton;
    @FXML
    private Button startButton;
    @FXML
    private TableView<EntityInfoDTO> entityTable;
    @FXML
    private TableColumn<EntityInfoDTO, String> entityColumn;
    @FXML
    private TableColumn<EntityInfoDTO, String> populationColumn;
    @FXML
    private TableView<PropertyInfoDTO> environmentTable;
    @FXML
    private TableColumn<PropertyInfoDTO, String> envNameColumn;
    @FXML
    private TableColumn<PropertyInfoDTO, String> envValueColumn;
    @FXML
    private Label envVariableLabel;
    @FXML
    private Label entityLabel;
    @FXML
    private Button cancelButton;
    @FXML
    private Button confirmButton;
    @FXML
    private Label finalMessageLabel;
    private BodyController bodyController;
    private WorldInfoDTO worldInfoDTO;
    private Collection<PropertyInfoDTO> envVariables;
    private int requestID;

    @FXML
    public void initialize() {
        disableConfirmationButtons(true);
    }

    public void setBodyController(BodyController bodyController) {
        this.bodyController = bodyController;
    }

    public void setExecutionInfo(WorldInfoDTO worldInfoDTO, int requestID) {
        this.envVariables = worldInfoDTO.getEnvVariables();
        this.worldInfoDTO = worldInfoDTO;
        this.requestID = requestID;
        entityTable.getItems().clear();
        environmentTable.getItems().clear();
        setEnvironmentTable(envVariables);
        setEntityTable(worldInfoDTO.getEntitiesInfoDTO());
    }

    public void setEnvironmentTable(Collection<PropertyInfoDTO> envVariablesInfo) {
        environmentTable.getItems().clear();
        environmentTable.setEditable(true);
        envValueColumn.setCellValueFactory(new PropertyValueFactory<>("Value"));
        envNameColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
        envValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        envValueColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<PropertyInfoDTO, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<PropertyInfoDTO, String> event) {
                envVariableLabel.setText("");
                PropertyInfoDTO propertyInfoDTO = event.getRowValue();
                propertyInfoDTO.setValue(event.getNewValue());
                environmentTable.refresh();
            }
        });

        for (PropertyInfoDTO propertyInfoDTO : envVariablesInfo) {
            environmentTable.getItems().add(propertyInfoDTO);
        }
    }

    public void setEntityTable(Collection<EntityInfoDTO> entitiesInfoDTO) {
        entityTable.setEditable(true);
        populationColumn.setCellValueFactory(new PropertyValueFactory<>("Population"));
        entityColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
        populationColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        populationColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<EntityInfoDTO, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<EntityInfoDTO, String> event) {
                entityLabel.setText("");
                EntityInfoDTO entityInfo = event.getRowValue();
                try {
                    int count = Integer.parseInt(event.getNewValue());
                    entityInfo.setPopulation(count);
                } catch (Exception e) {
                    entityLabel.setText(event.getNewValue() + " is not a number for population");
                }
                entityTable.refresh();
            }
        });

        for (EntityInfoDTO entityInfoDTO : entitiesInfoDTO) {
            entityTable.getItems().add(entityInfoDTO);
        }
    }

    @FXML
    void clearButtonListener(ActionEvent event) {
        disableConfirmationButtons(true);

        for (PropertyInfoDTO propertyInfoDTO : envVariables) {
            propertyInfoDTO.setValue(null);
        }

        for (EntityInfoDTO entityInfoDTO : worldInfoDTO.getEntitiesInfoDTO()) {
            entityInfoDTO.setPopulation(0);
        }

        entityTable.refresh();
        environmentTable.refresh();
    }

    @FXML
    void startButtonListener(ActionEvent event) {
        int requestedExecutions = bodyController.getRequestedExecutions(requestID);
        int activeExecutions = bodyController.getActiveExecutions(requestID);

        if (requestedExecutions != activeExecutions) {
            setActiveEnvironment(worldInfoDTO);
        } else {
            finalMessageLabel.setText("can't start since you're asking for more active executions than requested");
        }
    }

    @FXML
    void cancelListener(ActionEvent event) {
        disableConfirmationButtons(true);
    }

    @FXML
    void confirmListener(ActionEvent event) {
        bodyController.startSimulation(worldInfoDTO, requestID, environmentTable);
        disableConfirmationButtons(true);
    }

    private void setActiveEnvironment(WorldInfoDTO worldInfoDTO) {
        Gson gson = new Gson();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.ACTIVE_ENVIRONMENT)
                .newBuilder().addQueryParameter(Constants.REQUEST_ID, String.valueOf(requestID));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(gson.toJson(worldInfoDTO).getBytes()))
                .build();
        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        entityLabel.setText("something went wrong when starting simulation"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bodyString = response.body().string();

                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    ActiveEnvironmentDTO activeEnvironmentDTO = gson.fromJson(bodyString, ActiveEnvironmentDTO.class);
                    Platform.runLater(() -> askUserToConfirm(activeEnvironmentDTO));
                } else {
                    Platform.runLater(() -> entityLabel.setText(bodyString));
                }
            }
        });
    }

    private void askUserToConfirm(ActiveEnvironmentDTO activeEnvironmentDTO) {
        worldInfoDTO.setEnvVariables(activeEnvironmentDTO.getEnvVariables());
        worldInfoDTO.setEntityInfosDTO(activeEnvironmentDTO.getEntityInfoList());
        setExecutionInfo(worldInfoDTO, requestID);
        String msg = "do you wish to start " + worldInfoDTO.getSimulationName() + " simulation with this active environment";
        finalMessageLabel.setText(msg);
        disableConfirmationButtons(false);
    }

    private void disableConfirmationButtons(boolean status) {
        confirmButton.setDisable(status);
        cancelButton.setDisable(status);
    }

    public void printError(String message) {
        entityLabel.setText(message);
    }
}
