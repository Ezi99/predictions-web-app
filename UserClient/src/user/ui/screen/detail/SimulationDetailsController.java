package user.ui.screen.detail;

import com.google.gson.Gson;
import dto.definition.*;
import dto.definition.action.ActionInfoDTO;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.FlowPane;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import user.ui.body.BodyController;
import user.ui.tasks.detail.SimulationListRefresher;
import user.utils.http.HttpClientUtil;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

import user.utils.Constants;

public class SimulationDetailsController implements Closeable {
    private final Map<String, WorldInfoDTO> worldInfoMap;
    private final TreeItem<String> entityRoot;
    private final TreeItem<RuleTreeNode> rulesRoot;
    private final TreeItem<PropertyInfoDTO> envVariablesRoot;
    @FXML
    private TreeView<String> simulationTree;
    @FXML
    private TreeView<PropertyInfoDTO> envVariablesTree;
    @FXML
    private TreeView<String> entitiesTree;
    @FXML
    private TreeView<RuleTreeNode> rulesTree;
    @FXML
    private TreeView<String> generalTree;
    @FXML
    private Label selectedItemLabel;
    @FXML
    private FlowPane flowPane;
    private SimulationListRefresher listRefresher;
    private Timer timer;
    private WorldInfoDTO chosenWorldInfo;
    private BodyController bodyController;


    public SimulationDetailsController() {
        worldInfoMap = new HashMap<>();
        entityRoot = new TreeItem<>("Entities");
        rulesRoot = new TreeItem<>(new RuleTreeNode("Rules"));
        PropertyInfoDTO propertyInfoDTO = new PropertyInfoDTO();
        propertyInfoDTO.setName("Environment Variables");
        envVariablesRoot = new TreeItem<>(propertyInfoDTO);
    }

    @FXML
    public void initialize() {
        simulationTree.setRoot(new TreeItem<>("Simulations"));
        entitiesTree.setRoot(entityRoot);
        rulesTree.setRoot(rulesRoot);
        envVariablesTree.setRoot(envVariablesRoot);
        generalTree.setRoot(new TreeItem<>("General"));
        setSimulationTreeListener();
    }

    public void setBodyController(BodyController bodyController) {
        this.bodyController = bodyController;
    }

    public void addWorldInfoDTO(WorldInfoDTO worldInfoDTO) {
        worldInfoMap.put(worldInfoDTO.getSimulationName(), worldInfoDTO);
    }

    private void setSimulationTreeListener() {
        MultipleSelectionModel<TreeItem<String>> tvSelModel = simulationTree.getSelectionModel();
        tvSelModel.selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            public void changed(ObservableValue<? extends TreeItem<String>> changed, TreeItem<String> oldVal,
                                TreeItem<String> newVal) {

                if (newVal != null && newVal.getValue() != null && !newVal.getValue().equals("Simulations")) {
                    String simulationName = newVal.getValue();
                    if (worldInfoMap.containsKey(simulationName)) {
                        if (chosenWorldInfo == null || !simulationName.equals(chosenWorldInfo.getSimulationName())) {
                            setChosenSimulationDetails(simulationName);
                        }
                    } else {
                        getSimulationDetailsFromServer(simulationName);
                    }

                }
            }
        });
    }

    private void getSimulationDetailsFromServer(String simulationName) {
        String finalUrl = HttpUrl
                .parse(Constants.WORLD_INFO)
                .newBuilder()
                .addQueryParameter("simulationName", simulationName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    String message = "something went wrong when loading " + simulationName;
                    bodyController.printMessage(message);
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bodyString = response.body().string();
                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    WorldInfoDTO worldInfo = gson.fromJson(bodyString, WorldInfoDTO.class);
                    Platform.runLater(() -> {
                        addWorldInfoDTO(worldInfo);
                        setChosenSimulationDetails(worldInfo.getSimulationName());
                        bodyController.printMessage("");
                    });
                } else {
                    Platform.runLater(() -> bodyController.printMessage(bodyString));
                }
            }
        });
    }

    private void setChosenSimulationDetails(String simulationName) {
        resetRoots();
        chosenWorldInfo = worldInfoMap.get(simulationName);
        setEntityTree(chosenWorldInfo.getEntitiesInfoDTO());
        setRulesTree(chosenWorldInfo.getRulesInfoDTOS());
        setEnvVariablesTree(chosenWorldInfo.getEnvVariables());
        setGeneralTree();
    }

    private void resetRoots() {
        entityRoot.getChildren().clear();
        rulesRoot.getChildren().clear();
        envVariablesRoot.getChildren().clear();
        generalTree.getRoot().getChildren().clear();
    }


    private void setGeneralTree() {
        TreeItem<String> gridRoot = new TreeItem<>("Grid");
        generalTree.getRoot().getChildren().add(gridRoot);
        setGeneralTreeListener();
    }


    private void setGeneralTreeListener() {
        MultipleSelectionModel<TreeItem<String>> tvSelModel = generalTree.getSelectionModel();
        tvSelModel.selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            public void changed(ObservableValue<? extends TreeItem<String>> changed, TreeItem<String> oldVal,
                                TreeItem<String> newVal) {
                resetView();
                String res = "";

                if (newVal != null && newVal.getValue() != null) {
                    if (newVal.getValue().equals("Grid")) {
                        res = String.format("Rows size: " + chosenWorldInfo.getRows() + "\n") +
                                String.format("Columns size: " + chosenWorldInfo.getColumns() + "\n");
                    }

                    selectedItemLabel.setText(res);
                }
            }
        });
    }

    private void setEnvVariablesTree(Collection<PropertyInfoDTO> envVariables) {
        for (PropertyInfoDTO propertyInfoDTO : envVariables) {
            TreeItem<PropertyInfoDTO> property = new TreeItem<>(propertyInfoDTO);
            envVariablesRoot.getChildren().add(property);
        }

        setEnvVariablesTreeListener();
    }

    private void setEnvVariablesTreeListener() {
        MultipleSelectionModel<TreeItem<PropertyInfoDTO>> tvSelModel = envVariablesTree.getSelectionModel();
        tvSelModel.selectedItemProperty().addListener(new ChangeListener<TreeItem<PropertyInfoDTO>>() {
            public void changed(ObservableValue<? extends TreeItem<PropertyInfoDTO>> changed, TreeItem<PropertyInfoDTO> oldVal,
                                TreeItem<PropertyInfoDTO> newVal) {
                resetView();
                if (newVal != null && newVal.getValue() != null && newVal.isLeaf()) {
                    PropertyInfoDTO propertyInfo = newVal.getValue();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(String.format("Name: " + propertyInfo.getName() + "\n"));
                    stringBuilder.append(String.format("Type: " + propertyInfo.getType() + "\n"));
                    if (propertyInfo.isHasRange()) {
                        stringBuilder.append(String.format("Range: " + propertyInfo.getBottomBoundary() +
                                "-" + propertyInfo.getUpperBoundary() + "\n"));
                    } else {
                        stringBuilder.append("property has no range\n");
                    }

                    selectedItemLabel.setText(stringBuilder.toString());
                }
            }
        });
    }

    private void setRulesTree(Collection<RuleInfoDTO> rulesInfoDTOS) {

        for (RuleInfoDTO ruleInfoDTO : rulesInfoDTOS) {
            TreeItem<RuleTreeNode> actions = new TreeItem<>(new RuleTreeNode());
            for (ActionInfoDTO actionInfoDTO : ruleInfoDTO.getActions()) {
                TreeItem<RuleTreeNode> treeItem = new TreeItem<>(new RuleTreeNode(actionInfoDTO));
                actions.getChildren().add(treeItem);
            }
            TreeItem<RuleTreeNode> activation = new TreeItem<>(new RuleTreeNode(ruleInfoDTO.getProbabilityToActivate(), ruleInfoDTO.getTicksToActivate()));
            TreeItem<RuleTreeNode> rule = new TreeItem<>(new RuleTreeNode(ruleInfoDTO.getName()));
            rule.getChildren().add(actions);
            rule.getChildren().add(activation);
            rulesRoot.getChildren().add(rule);
        }

        setRulesTreeListener();
    }

    private void setRulesTreeListener() {
        MultipleSelectionModel<TreeItem<RuleTreeNode>> tvSelModel = rulesTree.getSelectionModel();
        tvSelModel.selectedItemProperty().addListener(new ChangeListener<TreeItem<RuleTreeNode>>() {
            public void changed(ObservableValue<? extends TreeItem<RuleTreeNode>> changed, TreeItem<RuleTreeNode> oldVal,
                                TreeItem<RuleTreeNode> newVal) {
                resetView();
                if (newVal != null && newVal.getValue() != null) {
                    if (newVal.getValue().isActivation()) {
                        String res = String.format("ticks to activate - " + newVal.getValue().getTicksToActivate()) + "\n" +
                                String.format("probability to activate - " + newVal.getValue().getProbabilityToActivate()) + "\n";
                        selectedItemLabel.setText(res);
                    } else if (newVal.isLeaf()) {
                        handleActionSelection(newVal);
                    }
                }
            }
        });

    }

    private void handleActionSelection(TreeItem<RuleTreeNode> newVal) {
        if (newVal != null && newVal.getValue() != null && newVal.getValue().getActionInfoDTO() != null) {
            ActionInfoDTO actionInfoDTO = newVal.getValue().getActionInfoDTO();
            TreeItem<String> treeRoot = new TreeItem<>("Full " + actionInfoDTO.getType() + " Details");
            TreeItem<String> mainEntityTitle = createActionDetailNode("primary Entity to work on", actionInfoDTO.getMainEntity());
            treeRoot.getChildren().add(mainEntityTitle);
            if (actionInfoDTO.getSecondaryEntity() != null) {
                TreeItem<String> secondaryEntityTitle = createActionDetailNode("secondary Entity to work on", actionInfoDTO.getSecondaryEntity());
                treeRoot.getChildren().add(secondaryEntityTitle);
            }

            if (actionInfoDTO.getType().equalsIgnoreCase("increase")
                    || actionInfoDTO.getType().equalsIgnoreCase("decrease")) {
                createTypicalActionTree(treeRoot, actionInfoDTO, actionInfoDTO.getType().toLowerCase() + " by", "affected property");
            } else if (actionInfoDTO.getType().equalsIgnoreCase("condition")) {
                createConditionActionTree(treeRoot, actionInfoDTO);
            } else if (actionInfoDTO.getType().equalsIgnoreCase("set")) {
                createTypicalActionTree(treeRoot, actionInfoDTO, "set to", "property");
            } else if (actionInfoDTO.getType().equalsIgnoreCase("calculation")) {
                createCalculationActionTree(treeRoot, actionInfoDTO);
            } else if (actionInfoDTO.getType().equalsIgnoreCase("proximity")) {
                createProximityActionTree(treeRoot, actionInfoDTO);
            } else if (actionInfoDTO.getType().equalsIgnoreCase("replace")) {
                createReplaceActionTree(treeRoot, actionInfoDTO);
            }

            TreeView<String> treeView = new TreeView<>();
            treeView.setRoot(treeRoot);
            flowPane.getChildren().add(treeView);
        }
    }

    private void createReplaceActionTree(TreeItem<String> treeRoot, ActionInfoDTO actionInfoDTO) {

        TreeItem<String> killTitle = createActionDetailNode("entity to kill", actionInfoDTO.getMainEntity());
        TreeItem<String> createTitle = createActionDetailNode("entity to create", actionInfoDTO.getCreate());
        TreeItem<String> modeTitle = createActionDetailNode("mode", actionInfoDTO.getMode());

        treeRoot.getChildren().add(killTitle);
        treeRoot.getChildren().add(createTitle);
        treeRoot.getChildren().add(modeTitle);
    }

    private void createProximityActionTree(TreeItem<String> treeRoot, ActionInfoDTO actionInfoDTO) {

        TreeItem<String> sourceTitle = createActionDetailNode("source entity", actionInfoDTO.getMainEntity());
        TreeItem<String> targetTitle = createActionDetailNode("target entity", actionInfoDTO.getTargetEntity());
        TreeItem<String> depthTitle = createActionDetailNode("depth", actionInfoDTO.getDepth());
        TreeItem<String> actionCountTitle = createActionDetailNode("number of actions", Integer.toString(actionInfoDTO.getActionsCount()));

        treeRoot.getChildren().add(sourceTitle);
        treeRoot.getChildren().add(targetTitle);
        treeRoot.getChildren().add(depthTitle);
        treeRoot.getChildren().add(actionCountTitle);
    }

    private void createCalculationActionTree(TreeItem<String> treeRoot, ActionInfoDTO actionInfoDTO) {
        TreeItem<String> arg1Title = createActionDetailNode("argument 1", actionInfoDTO.getArg1());
        TreeItem<String> arg2Title = createActionDetailNode("argument 2", actionInfoDTO.getArg2());
        TreeItem<String> calcTypeTitle = createActionDetailNode("calculation operator", actionInfoDTO.getCalcType());
        TreeItem<String> propertyTitle = createActionDetailNode("affected property", actionInfoDTO.getProperty());
        String calcSymbol = actionInfoDTO.getCalcType().equalsIgnoreCase("multiply") ? "*" : "/";
        String fullCalc = actionInfoDTO.getProperty() + " = " + actionInfoDTO.getArg1() + " "
                + calcSymbol + " " + actionInfoDTO.getArg2();
        TreeItem<String> fullCalcTitle = createActionDetailNode("full calculation", fullCalc);
        treeRoot.getChildren().add(propertyTitle);
        treeRoot.getChildren().add(arg1Title);
        treeRoot.getChildren().add(arg2Title);
        treeRoot.getChildren().add(calcTypeTitle);
        treeRoot.getChildren().add(fullCalcTitle);
    }

    private void createTypicalActionTree(TreeItem<String> treeRoot, ActionInfoDTO actionInfoDTO, String exp, String property) {
        TreeItem<String> byTitle = createActionDetailNode(exp, actionInfoDTO.getExpression());
        TreeItem<String> propertyTitle = createActionDetailNode(property, actionInfoDTO.getProperty());
        treeRoot.getChildren().add(propertyTitle);
        treeRoot.getChildren().add(byTitle);
    }

    private void createConditionActionTree(TreeItem<String> treeRoot, ActionInfoDTO actionInfoDTO) {

        TreeItem<String> conditionTypeTitle = new TreeItem<>("Condition Type");
        TreeItem<String> conditionType;
        if (actionInfoDTO.getConditionsCount() > 1) {
            conditionType = new TreeItem<>("Multiple Conditions");
            treeRoot.getChildren().add(createActionDetailNode("logic operator", actionInfoDTO.getConditionOperator()));
            treeRoot.getChildren().add(createActionDetailNode("number of conditions", Integer.toString(actionInfoDTO.getConditionsCount())));
        } else {
            conditionType = new TreeItem<>("Single Condition");
            treeRoot.getChildren().add(createActionDetailNode("property that's being checked", actionInfoDTO.getConditionProperty()));
            treeRoot.getChildren().add(createActionDetailNode("operator", actionInfoDTO.getConditionOperator()));
            treeRoot.getChildren().add(createActionDetailNode("value", actionInfoDTO.getValue()));
        }
        conditionTypeTitle.getChildren().add(conditionType);
        treeRoot.getChildren().add(2, conditionTypeTitle);
        treeRoot.getChildren().add(createActionDetailNode("number of then actions", Integer.toString(actionInfoDTO.getThenActionCount())));
        if (actionInfoDTO.getElseActionCount() != 0) {
            treeRoot.getChildren().add(createActionDetailNode("number of else actions", Integer.toString(actionInfoDTO.getElseActionCount())));
        }
    }

    private TreeItem<String> createActionDetailNode(String title, String detail) {
        TreeItem<String> titleNode = new TreeItem<>(title);
        TreeItem<String> detailsNode = new TreeItem<>(detail);
        titleNode.getChildren().add(detailsNode);
        return titleNode;
    }

    private void setEntityTree(List<EntityInfoDTO> entitiesInfo) {

        for (EntityInfoDTO entityInfo : entitiesInfo) {
            TreeItem<String> entity = new TreeItem<>(entityInfo.getName());

            for (PropertyInfoDTO propertyInfoDTO : entityInfo.getPropertyInfoDTOSet()) {
                TreeItem<String> property = new TreeItem<>(propertyInfoDTO.getName());
                entity.getChildren().add(property);
            }

            entityRoot.getChildren().add(entity);
        }

        setEntityTreeListener();
    }

    private void setEntityTreeListener() {
        MultipleSelectionModel<TreeItem<String>> tvSelModel = entitiesTree.getSelectionModel();
        tvSelModel.selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
            public void changed(ObservableValue<? extends TreeItem<String>> changed, TreeItem<String> oldVal,
                                TreeItem<String> newVal) {
                resetView();
                if (newVal != null && newVal.getValue() != null && newVal.getParent() != null && newVal.isLeaf()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String EntityName = newVal.getParent().getValue();

                    for (EntityInfoDTO entityInfoDTO : chosenWorldInfo.getEntitiesInfoDTO()) {
                        if (entityInfoDTO.getName().equals(EntityName)) {
                            PropertyInfoDTO propertyInfoDTO = entityInfoDTO.getPropertyInfo(newVal.getValue());
                            stringBuilder.append(String.format("Type: " + propertyInfoDTO.getType() + "\n"));
                            stringBuilder.append(String.format("Is randomly initialized: "
                                    + propertyInfoDTO.isRandomlyInitialized() + "\n"));
                            if (propertyInfoDTO.isHasRange()) {
                                stringBuilder.append(String.format("Range: " +
                                        propertyInfoDTO.getBottomBoundary() + "-" +
                                        propertyInfoDTO.getUpperBoundary() + "\n"));
                            }
                            selectedItemLabel.setText(stringBuilder.toString());
                            break;
                        }
                    }
                }
            }
        });
    }

    public void startSimulationListRefresher() {
        listRefresher = new SimulationListRefresher(
                this::updateSimulationList,
                bodyController::printMessage
        );

        timer = new Timer();
        timer.schedule(listRefresher, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    private void updateSimulationList(SimulationListDTO simulations) {
        simulationTree.getRoot().getChildren().clear();
        for (String simulation : simulations.getSimulationsName()) {
            simulationTree.getRoot().getChildren().add(new TreeItem<>(simulation));
        }
        bodyController.updateSimulationList(simulations);
    }

    private void resetView() {
        selectedItemLabel.setText(" ");
        if (flowPane.getChildren().size() == 3) {
            flowPane.getChildren().remove(2);
        }
    }

    @Override
    public void close() {
        if (listRefresher != null && timer != null) {
            listRefresher.cancel();
            timer.cancel();
        }
    }

    public void setActive() {
        startSimulationListRefresher();
    }
}
