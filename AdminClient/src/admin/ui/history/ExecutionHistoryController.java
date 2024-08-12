package admin.ui.history;

import admin.ui.history.node.PopulationGraphNode;
import admin.ui.history.node.PropertyStatsNode;
import admin.ui.history.node.ResultTreeNode;
import admin.ui.tasks.history.ExecutionListManager;
import admin.ui.tasks.history.SimulationRunTask;
import dto.definition.EntityInfoDTO;
import dto.definition.PropertyInfoDTO;
import dto.execution.SimulationDTO;
import dto.execution.end.PopulationChartDTO;
import dto.execution.end.PropertyHistogramDTO;
import dto.execution.start.StartSimulationDTO;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExecutionHistoryController {
    private SimulationRunTask simulationRunTask;
    private ExecutionListManager executionListManager;
    private StartSimulationDTO chosenSimulation;
    @FXML
    private TableView<StartSimulationDTO> executionList;
    @FXML
    private TableColumn<StartSimulationDTO, String> idColumn;
    @FXML
    private TableColumn<StartSimulationDTO, String> requestIDColumn;
    @FXML
    private TableColumn<StartSimulationDTO, String> startDateColumn;
    @FXML
    private TableColumn<StartSimulationDTO, String> stateColumn;
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
    private Label currentIdLabel;

    public ExecutionHistoryController() {
        executionListManager = null;
    }

    @FXML
    public void initialize() {
        entityColumn.setCellValueFactory(new PropertyValueFactory<>("Name"));
        populationColumn.setCellValueFactory(new PropertyValueFactory<>("Population"));
        idColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        requestIDColumn.setCellValueFactory(new PropertyValueFactory<>("RequestID"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("Start"));
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("State"));
        resultsTree.setRoot(new TreeItem<>(new ResultTreeNode("Results")));
        setResultTreeHandler();
        setExecutionListHandler();
    }

    public void setActive() {
        createExecutionListUpdater();
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

    private void showPropertyHistogram(ResultTreeNode value) {
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

    private void showConsistency(ResultTreeNode value) {
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

    private void setExecutionListHandler() {
        EventHandler<MouseEvent> rowClickHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                StartSimulationDTO selectedSimulation = executionList.getSelectionModel().getSelectedItem();
                chosenSimulation = selectedSimulation;
                if (chosenSimulation != null) {
                    currentIdLabel.setText(selectedSimulation.getID().toString());
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

    private void launchSimulationRunTask(StartSimulationDTO selectedSimulation) {
        simulationRunTask = new SimulationRunTask(selectedSimulation.getID());
        simulationRunTask.bindMessage(exceptionLabel::setText);
        simulationRunTask.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.getSimulationEndDTO() != null) {
                resultsTree.getRoot().getChildren().clear();
                setEntityPopulation(newValue);
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

    private void setEntityPopulation(SimulationDTO simulationDTO) {
        List<EntityInfoDTO> entityInfoList = simulationDTO.getEntityInfoDTOS();
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
            }
        }

        executionList.refresh();
    }
}
