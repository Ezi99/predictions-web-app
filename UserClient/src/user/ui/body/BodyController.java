package user.ui.body;

import com.google.gson.Gson;
import dto.definition.ActiveEnvironmentDTO;
import dto.definition.PropertyInfoDTO;
import dto.definition.SimulationListDTO;
import dto.definition.WorldInfoDTO;
import dto.execution.SimulationDTO;
import dto.execution.start.StartSimulationDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import user.ui.base.BaseController;
import user.ui.screen.detail.SimulationDetailsController;
import user.ui.screen.execution.ExecutionController;
import user.ui.screen.request.RequestTableEntry;
import user.ui.screen.request.RequestsController;
import user.ui.screen.result.ResultsController;
import user.utils.Constants;
import user.utils.http.HttpClientUtil;

import java.io.Closeable;
import java.io.IOException;

import static user.utils.http.HttpClientUtil.HTTP_CLIENT;

public class BodyController implements Closeable {
    @FXML
    private Label userNameLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private ScrollPane simulationDetailsComponent;
    @FXML
    private SimulationDetailsController simulationDetailsComponentController;
    @FXML
    private ScrollPane requestsComponent;
    @FXML
    private RequestsController requestsComponentController;
    @FXML
    private ScrollPane executionComponent;
    @FXML
    private ExecutionController executionComponentController;
    @FXML
    private ScrollPane resultsComponent;
    @FXML
    private ResultsController resultsComponentController;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab executionTab;
    @FXML
    private Tab resultsTab;

    private BaseController baseController;

    @FXML
    public void initialize() {
        simulationDetailsComponentController.setBodyController(this);
        requestsComponentController.setBodyController(this);
        executionComponentController.setBodyController(this);
        resultsComponentController.setBodyController(this);
    }

    public void setBaseController(BaseController baseController) {
        this.baseController = baseController;
    }

    public String getUserName() {
        return userNameLabel.getText();
    }

    public void setUserName(String userName) {
        userNameLabel.setText(userName);
    }

    public void printMessage(String message) {
        messageLabel.setText(message);
    }

    public void setActive() {
        simulationDetailsComponentController.setActive();
        requestsComponentController.setActive();
    }

    @Override
    public void close() throws IOException {
        simulationDetailsComponentController.close();
        requestsComponentController.close();
    }

    public void updateSimulationList(SimulationListDTO simulations) {
        requestsComponentController.updateSimulationList(simulations);
    }

    public void execute(RequestTableEntry chosenEntry) {
        String finalUrl = HttpUrl
                .parse(Constants.WORLD_INFO)
                .newBuilder()
                .addQueryParameter("simulationName", chosenEntry.getSimulationName())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    printMessage("something went wrong when loading " + chosenEntry.getSimulationName());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bodyString = response.body().string();
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    WorldInfoDTO worldInfo = gson.fromJson(bodyString, WorldInfoDTO.class);
                    Platform.runLater(() -> {
                        printMessage("");
                        executionComponentController.setExecutionInfo(worldInfo, chosenEntry.getRequestID());
                        tabPane.getSelectionModel().select(executionTab);
                    });
                } else {
                    Platform.runLater(() -> printMessage(bodyString));
                }
            }
        });
    }

    public void startSimulation(WorldInfoDTO worldInfoDTO, int requestID, TableView<PropertyInfoDTO> environmentTable) {
        Gson gson = new Gson();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.SIMULATION_RUN)
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
                        printMessage("something went wrong when starting simulation"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bodyString = response.body().string();

                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    SimulationDTO simulationDTO = gson.fromJson(bodyString, SimulationDTO.class);
                    Platform.runLater(() -> {
                        printMessage("");
                        StartSimulationDTO startSimulationDTO = simulationDTO.getStartSimulationDTO();
                        resultsComponentController.showEnvVariableOnStart(startSimulationDTO);
                        resultsComponentController.addSimulationStart(simulationDTO);
                        tabPane.getSelectionModel().select(resultsTab);
                    });
                } else {
                    Platform.runLater(() -> printMessage(bodyString));
                }
            }
        });

    }

    public void getActiveEnvironment(int worldID, int requestID) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.ACTIVE_ENVIRONMENT)
                .newBuilder().addQueryParameter(Constants.WORLD_ID, String.valueOf(worldID));
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();
        Call call = HTTP_CLIENT.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    String message = "something went wrong when executing reRun request";
                    executionComponentController.printError(message);
                    tabPane.getSelectionModel().select(executionTab);
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bodyString = response.body().string();
                Gson gson = new Gson();

                if (response.isSuccessful()) {
                    ActiveEnvironmentDTO activeEnv = gson.fromJson(bodyString, ActiveEnvironmentDTO.class);
                    Platform.runLater(() -> reRunSimulation(activeEnv, requestID));
                } else {
                    Platform.runLater(() -> {
                        executionComponentController.printError(bodyString);
                        tabPane.getSelectionModel().select(executionTab);
                    });
                }
            }
        });
    }

    public void reRunSimulation(ActiveEnvironmentDTO activeEnv, int requestID) {
        WorldInfoDTO worldInfoDTO = new WorldInfoDTO();
        worldInfoDTO.setSimulationName(activeEnv.getSimulationName());
        worldInfoDTO.setEnvVariables(activeEnv.getEnvVariables());
        worldInfoDTO.setEntityInfosDTO(activeEnv.getEntityInfoList());
        executionComponentController.setExecutionInfo(worldInfoDTO, requestID);
        tabPane.getSelectionModel().select(executionTab);
    }

    public int getRequestedExecutions(int requestID){
        return requestsComponentController.getRequestedExecutions(requestID);
    }

    public int getActiveExecutions(int requestID){
        return resultsComponentController.getActiveExecutions(requestID);
    }

}
