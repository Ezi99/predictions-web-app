package user.ui.screen.result;

import com.google.gson.Gson;
import dto.SimulationControl;
import dto.definition.EntityInfoDTO;
import dto.definition.PropertyInfoDTO;
import dto.execution.SimulationDTO;
import dto.execution.SimulationState;
import dto.execution.end.PopulationChartDTO;
import dto.execution.end.PropertyHistogramDTO;
import dto.execution.start.StartSimulationDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import user.ui.body.BodyController;
import user.ui.screen.result.node.PopulationGraphNode;
import user.ui.screen.result.node.PropertyStatsNode;
import user.ui.screen.result.node.ResultTreeNode;
import user.ui.tasks.result.ExecutionListManager;
import user.ui.tasks.result.SimulationRunTask;
import user.utils.Constants;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static user.utils.http.HttpClientUtil.HTTP_CLIENT;

public class ResultsController {

    private final SimpleIntegerProperty totalTicks;
    private final SimpleIntegerProperty totalSeconds;
    private BodyController bodyController;
    private SimulationRunTask simulationRunTask;
    private ExecutionListManager executionListManager;
    private StartSimulationDTO chosenSimulation;
    @FXML
    private TableView<StartSimulationDTO> executionList;
    @FXML
    private TableColumn<StartSimulationDTO, String> idColumn;
    @FXML
    private TableColumn<StartSimulationDTO, String> startDateColumn;
    @FXML
    private TableColumn<StartSimulationDTO, String> stateColumn;
    @FXML
    private Label currentTickLabel;
    @FXML
    private Label currentSecondLabel;
    @FXML
    private TableView<EntityInfoDTO> entityTable;
    @FXML
    private TableColumn<EntityInfoDTO, String> entityColumn;
    @FXML
    private TableColumn<EntityInfoDTO, String> populationColumn;
    @FXML
    private Label exceptionLabel;
    @FXML
    private GridPane gridPane;
    @FXML
    private TreeView<ResultTreeNode> resultsTree;
    @FXML
    private BorderPane statsPane;
    @FXML
    private Button pauseButton;
    @FXML
    private Button resumeButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button reRunButton;
    @FXML
    private Label currentIdLabel;
    @FXML
    private ProgressBar ticksProgress;
    @FXML
    private ProgressBar secondsProgress;

    public ResultsController() {
        totalTicks = new SimpleIntegerProperty(0);
        totalSeconds = new SimpleIntegerProperty(0);
        executionListManager = null;
    }

    @FXML
    public void initialize() {
        entityColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
        populationColumn.setCellValueFactory(new PropertyValueFactory<>("Population"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("Start"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("State"));
        currentSecondLabel.textProperty().bind(Bindings.format("%,d", totalSeconds));
        currentTickLabel.textProperty().bind(Bindings.format("%,d", totalTicks));
        resultsTree.setRoot(new TreeItem<>(new ResultTreeNode("Results")));
        setResultTreeHandler();
        setExecutionListHandler();
    }

    @FXML
    void pauseListener(ActionEvent event) {
        if (chosenSimulation != null) {
            submitControl(SimulationControl.PAUSE);
            pauseButton.setDisable(true);
        }
    }

    @FXML
    void reRunListener(ActionEvent event) {
        int requestID = chosenSimulation.getRequestID();
        int requestedExecutions = bodyController.getRequestedExecutions(requestID);
        int activeExecutions = getActiveExecutions(requestID);

        if (chosenSimulation != null && activeExecutions != requestedExecutions) {
            bodyController.getActiveEnvironment(chosenSimulation.getID(), chosenSimulation.getRequestID());
            reRunButton.setDisable(true);
        } else {
            exceptionLabel.setText("cannot reRun simulation " + chosenSimulation.getID()
            + ", you're asking for more active executions than requested");
        }
    }

    public int getActiveExecutions(int requestID){
        int numOfExecutions = 0;

        for(StartSimulationDTO startSimulationDTO : executionList.getItems()){
            if(startSimulationDTO.getRequestID() == requestID){
                numOfExecutions++;
            }
        }

        return numOfExecutions;
    }

    @FXML
    void resumeListener(ActionEvent event) {
        if (chosenSimulation != null) {
            submitControl(SimulationControl.RESUME);
            resumeButton.setDisable(true);
        }
    }

    @FXML
    void stopListener(ActionEvent event) {
        if (chosenSimulation != null) {
            submitControl(SimulationControl.STOP);
            stopButton.setDisable(true);
            resumeButton.setDisable(true);
        }
    }

    private void submitControl(SimulationControl simulationControl){
        Gson gson = new Gson();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Constants.SIMULATION_CONTROL)
                .newBuilder()
                .addQueryParameter(Constants.WORLD_ID, chosenSimulation.getID().toString())
                .addQueryParameter(Constants.CONTROL, simulationControl.name());
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .put(RequestBody.create(gson.toJson(simulationControl).getBytes()))
                .build();
        Call call = HTTP_CLIENT.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        exceptionLabel.setText("something went wrong when controlling simulation"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bodyString = response.body().string();
                if(!response.isSuccessful()){
                    Platform.runLater(() ->
                            exceptionLabel.setText(bodyString));
                }
            }
        });
    }

    private void setResultTreeHandler() {
        MultipleSelectionModel<TreeItem<ResultTreeNode>> tvSelModel = resultsTree.getSelectionModel();
        tvSelModel.selectedItemProperty().addListener(new ChangeListener<TreeItem<ResultTreeNode>>() {
            public void changed(ObservableValue<? extends TreeItem<ResultTreeNode>> changed, TreeItem<ResultTreeNode> oldVal,
                                TreeItem<ResultTreeNode> newVal) {

                if (newVal != null && newVal.isLeaf()) {
                    ResultTreeNode value = newVal.getValue();
                    if (value.getPopulationGraphNode() != null) {
                        statsPane.setCenter(value.getPopulationGraphNode().getLineChart());
                    } else if (value.getPropertyNode() != null) {
                        if (value.getPropertyNode().getPropertyHistogram() != null) {
                           showPropertyHistogram(value);
                        } else {
                            showConsistency(value);
                        }
                    } else {
                        VBox vBox = new VBox();
                        Label label = new Label("no histogram to show since all instances are gone");
                        vBox.getChildren().add(label);
                        statsPane.setCenter(vBox);
                    }
                }
            }
        });

    }
    private void showPropertyHistogram(ResultTreeNode value){
        ScrollPane scrollPane = new ScrollPane();
        VBox vBox = new VBox();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        BorderPane.setAlignment(scrollPane, Pos.CENTER);
        PropertyStatsNode propertyStatsNode = value.getPropertyNode();
        Map<Object, Integer> propertyHistogram = propertyStatsNode.getPropertyHistogram();
        if (propertyHistogram != null) {
            for (Map.Entry<Object, Integer> entry : propertyHistogram.entrySet()) {
                String info = propertyStatsNode.getPropertyName() + ": " + entry.getKey() +
                        "  ||  Count: " + entry.getValue();
                Label label = new Label(info);
                vBox.getChildren().add(label);
            }
        }
        scrollPane.setContent(vBox);
        statsPane.setCenter(scrollPane);
    }

    private void showConsistency(ResultTreeNode value){
        VBox vBox = new VBox();
        Label name = new Label("name : " + value.getPropertyNode().getPropertyName());
        Label type = new Label("type : " + value.getPropertyNode().getType());
        Label consistency = new Label("consistency : " + value.getPropertyNode().getConsistency().toString());
        BorderPane.setAlignment(vBox, Pos.CENTER);
        vBox.getChildren().add(name);
        vBox.getChildren().add(type);
        vBox.getChildren().add(consistency);
        statsPane.setCenter(vBox);
    }

    public void setBodyController(BodyController bodyController) {
        this.bodyController = bodyController;
    }

    public synchronized void addSimulationStart(SimulationDTO simulationStart) {
        executionList.getItems().add(simulationStart.getStartSimulationDTO());

        if (executionListManager == null) {
            createExecutionListUpdater();
        }
    }

    private void setExecutionListHandler() {
        EventHandler<MouseEvent> rowClickHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                StartSimulationDTO selectedSimulation = executionList.getSelectionModel().getSelectedItem();
                chosenSimulation = selectedSimulation;
                if (chosenSimulation != null) {
                    currentIdLabel.setText(selectedSimulation.getID().toString());
                    updateButtonStatus();
                    if (simulationRunTask != null) {
                        if (selectedSimulation.getID().intValue() != simulationRunTask.getWorldID().intValue()) {
                            simulationRunTask.exit();
                            resultsTree.getRoot().getChildren().clear();
                        }
                    }

                    if (simulationRunTask == null || selectedSimulation.getID().intValue() != simulationRunTask.getWorldID().intValue()) {
                        statsPane.setCenter(null);
                        resultsTree.getRoot().getChildren().clear();
                        launchSimulationRunTask(selectedSimulation);
                    }
                }
            }
        };

        executionList.setRowFactory(tv -> {
            javafx.scene.control.TableRow<StartSimulationDTO> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(rowClickHandler);
            return row;
        });
    }

    private void updateButtonStatus() {
        if (chosenSimulation != null) {
            if (chosenSimulation.getState().equalsIgnoreCase(SimulationState.FINISHED.name())) {
                reRunButton.setDisable(false);
                pauseButton.setDisable(true);
                resumeButton.setDisable(true);
                stopButton.setDisable(true);
            } else {
                reRunButton.setDisable(true);
                if (chosenSimulation.getState().equalsIgnoreCase(SimulationState.PENDING.name())) {
                    pauseButton.setDisable(true);
                    resumeButton.setDisable(true);
                    stopButton.setDisable(true);
                } else if (chosenSimulation.getState().equalsIgnoreCase(SimulationState.RUNNING.name())) {
                    pauseButton.setDisable(false);
                    resumeButton.setDisable(true);
                    stopButton.setDisable(false);
                } else {
                    pauseButton.setDisable(true);
                    resumeButton.setDisable(false);
                    stopButton.setDisable(false);
                }
            }
        }
    }

    private void launchSimulationRunTask(StartSimulationDTO selectedSimulation) {
        simulationRunTask = new SimulationRunTask(selectedSimulation.getID());
        simulationRunTask.bindTimeLabels(totalTicks::set, totalSeconds::set);
        simulationRunTask.bindEntitiesStatus(this::setEntityPopulation);
        simulationRunTask.bindMessage(exceptionLabel::setText);
        simulationRunTask.bindProgressBar(ticksProgress::setProgress, secondsProgress::setProgress);
        simulationRunTask.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getSimulationEndDTO() != null) {
                resultsTree.getRoot().getChildren().clear();
                createChart(newValue.getSimulationEndDTO().getPopulationChart());
                createPropertyHistogram(newValue.getSimulationEndDTO().getPropertyHistogram());
                createConsistency(newValue.getSimulationEndDTO().getConsistencies());
            }
        });
        Thread taskThread = new Thread(simulationRunTask);
        taskThread.setDaemon(true);
        taskThread.start();
    }

    private void createConsistency(List<EntityInfoDTO> consistencies) {
        TreeItem<ResultTreeNode> exploreProperties = new TreeItem<>(
                new ResultTreeNode("Properties Consistencies"));

        for (EntityInfoDTO entityInfoDTO : consistencies) {
            TreeItem<ResultTreeNode> entityRoot = new TreeItem<>(
                    new ResultTreeNode(entityInfoDTO.getName()));

            for (PropertyInfoDTO propertyInfo : entityInfoDTO.getPropertyInfoDTOSet()) {
                PropertyStatsNode propertyStatsNode = new PropertyStatsNode();
                propertyStatsNode.setPropertyName(propertyInfo.getName());
                propertyStatsNode.setType(propertyInfo.getType());
                propertyStatsNode.setConsistency(propertyInfo.getConsistency());
                entityRoot.getChildren().add(new TreeItem<>(new ResultTreeNode(propertyStatsNode)));
            }

            exploreProperties.getChildren().add(entityRoot);
        }

        resultsTree.getRoot().getChildren().add(exploreProperties);
    }

    private void createPropertyHistogram(List<PropertyHistogramDTO> propertyHistograms) {
        TreeItem<ResultTreeNode> exploreProperties = new TreeItem<>(
                new ResultTreeNode("Properties Histograms"));

        for (PropertyHistogramDTO propertyHistogram : propertyHistograms) {
            if (propertyHistogram.getPropertyHistogram().size() != 0) {
                TreeItem<ResultTreeNode> entityRoot = new TreeItem<>(
                        new ResultTreeNode(propertyHistogram.getEntityName()));

                for (String propertyName : propertyHistogram.getPropertyHistogram().keySet()) {
                    PropertyStatsNode propertyStatsNode = new PropertyStatsNode();
                    propertyStatsNode.setPropertyName(propertyName);
                    propertyStatsNode.setPropertyHistogram(propertyHistogram.getPropertyHistogram().get(propertyName));
                    entityRoot.getChildren().add(new TreeItem<>(new ResultTreeNode(propertyStatsNode)));
                }

                exploreProperties.getChildren().add(entityRoot);
            }
        }
        resultsTree.getRoot().getChildren().add(exploreProperties);
    }

    private void createChart(Map<String, PopulationChartDTO> populationCharts) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Ticks");
        yAxis.setLabel("Population");
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        for (PopulationChartDTO populationChartDTO : populationCharts.values()) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(populationChartDTO.getEntityName());
            for (Integer tick : populationChartDTO.getPopulationTrack().keySet()) {
                series.getData().add((new XYChart.Data<>(tick, populationChartDTO.getPopulationInTick(tick))));
            }
            lineChart.getData().add(series);
        }
        ResultTreeNode resultTreeNode = new ResultTreeNode(new PopulationGraphNode(lineChart));
        resultsTree.getRoot().getChildren().add(new TreeItem<>(resultTreeNode));
    }

    private void setEntityPopulation(SimulationDTO simulationStart) {
        List<EntityInfoDTO> entityInfoList = simulationStart.getEntityInfoDTOS();
        entityTable.getItems().clear();
        for (EntityInfoDTO entityInfo : entityInfoList) {
            entityTable.getItems().add(entityInfo);
        }
        entityTable.refresh();
    }

    private void createExecutionListUpdater() {
        executionListManager = new ExecutionListManager();
        executionListManager.setStartSimulationConsumer(this::updateList);
        Thread thread = new Thread(executionListManager);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateList(Collection<StartSimulationDTO> list) {
        executionList.getItems().clear();

        for (StartSimulationDTO startSimulationDTO : list) {
            executionList.getItems().add(startSimulationDTO);

            if (chosenSimulation != null && Objects.equals(startSimulationDTO.getID(), chosenSimulation.getID())) {
                chosenSimulation.setState(startSimulationDTO.getState());
                updateButtonStatus();
            }
        }

        executionList.refresh();
    }

    public void showEnvVariableOnStart(StartSimulationDTO simulationDTO) {
        TableView<PropertyInfoDTO> envVariableTable = new TableView<>();
        TableView<EntityInfoDTO> entityTable = new TableView<>();
        TableColumn<PropertyInfoDTO, String> envNameColumn = new TableColumn<>();
        TableColumn<PropertyInfoDTO, String> envValueColumn = new TableColumn<>();
        TableColumn<EntityInfoDTO, String> entityNameColumn = new TableColumn<>();
        TableColumn<EntityInfoDTO, String> entityCountColumn = new TableColumn<>();
        VBox header = new VBox();
        Label label = new Label("Final active environment values");
        label.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        envNameColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
        envValueColumn.setCellValueFactory(new PropertyValueFactory<>("Value"));
        entityNameColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
        entityCountColumn.setCellValueFactory(new PropertyValueFactory<>("Population"));
        envNameColumn.setText("Name");
        envValueColumn.setText("Value");
        entityNameColumn.setText("Name");
        entityCountColumn.setText("Population");
        envVariableTable.getColumns().add(envNameColumn);
        envVariableTable.getColumns().add(envValueColumn);
        entityTable.getColumns().add(entityNameColumn);
        entityTable.getColumns().add(entityCountColumn);

        for (PropertyInfoDTO propertyInfoDTO : simulationDTO.getEnvVariables()) {
            envVariableTable.getItems().add(propertyInfoDTO);
        }
        for (EntityInfoDTO entityInfoDTO : simulationDTO.getEntityInfoList()) {
            entityTable.getItems().add(entityInfoDTO);
        }

        header.setAlignment(Pos.CENTER);
        envVariableTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        entityTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        header.getChildren().addAll(label, envVariableTable, entityTable);
        statsPane.setCenter(header);
    }
}
