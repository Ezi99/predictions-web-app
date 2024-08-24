package engine.world;

import dto.definition.EntityInfoDTO;
import dto.definition.PropertyInfoDTO;
import dto.execution.end.PopulationChartDTO;
import dto.execution.end.PropertyHistogramDTO;
import engine.action.api.Action;
import engine.definition.entity.EntityDefinition;
import engine.definition.property.api.PropertyDefinition;
import engine.definition.property.api.PropertyType;
import engine.execution.SimulationState;
import engine.execution.Termination;
import engine.execution.context.Context;
import engine.execution.context.ContextImpl;
import engine.execution.instance.entity.EntityInstance;
import engine.execution.instance.entity.manager.EntityInstanceManager;
import engine.execution.instance.entity.manager.EntityInstanceManagerImpl;
import engine.execution.instance.environment.api.ActiveEnvironment;
import engine.execution.instance.property.PropertyInstance;
import engine.execution.space.Coordinates;
import engine.execution.space.Direction;
import engine.execution.space.Grid;
import engine.rule.Rule;

import java.util.*;
import java.util.stream.Collectors;

public class SimulationRun implements Runnable {

    private final World world;
    private final Map<String, EntityInstanceManager> entityInstanceManagers;
    private int POPULATION_UPDATE_THRESHOLD = 2;
    private Integer ticks;
    private Integer seconds;
    private boolean exit;
    private boolean pause;

    public SimulationRun(World world) {
        this.world = world;
        ticks = 0;
        seconds = 0;
        exit = false;
        entityInstanceManagers = new HashMap<>();
    }

    @Override
    public void run() {

        world.setSimulationState(SimulationState.RUNNING);
        System.out.println("here 1");
        Map<String, PopulationChartDTO> populationMap = new HashMap<>();

        for (EntityDefinition entityDefinition : world.getEntityDefinitionMap()) {
            EntityInstanceManager entityInstanceManager = new EntityInstanceManagerImpl(entityDefinition);
            entityInstanceManagers.put(entityInstanceManager.getEntityDefinition().getName(), entityInstanceManager);
            PopulationChartDTO populationChartDTO = new PopulationChartDTO();
            populationChartDTO.setEntityName(entityDefinition.getName());
            populationMap.put(entityDefinition.getName(), populationChartDTO);
        }

        if (runSimulation(populationMap)) {
            updateChart(populationMap);
            updateEndResults(populationMap, createConsistency());
        }

    }

    private List<EntityInfoDTO> createConsistency() {
        List<EntityInfoDTO> entitiesInfo = new ArrayList<>();

        for (EntityInstanceManager entityInstanceManager : entityInstanceManagers.values()) {
            if (entityInstanceManager.getInstanceCount() != 0) {
                EntityInfoDTO entityInfo = new EntityInfoDTO();
                entityInfo.setName(entityInstanceManager.getEntityDefinition().getName());
                for (PropertyDefinition propertyDefinition : entityInstanceManager.getEntityDefinition().getPropertiesDefinition()) {
                    entityInfo.addProperty(createPropertyConsistency(entityInstanceManager, propertyDefinition));
                }
                entitiesInfo.add(entityInfo);
            }
        }

        return entitiesInfo;
    }

    private PropertyInfoDTO createPropertyConsistency(EntityInstanceManager entityInstanceManager, PropertyDefinition propertyDefinition) {
        float averages = 0;

        for (EntityInstance entityInstance : entityInstanceManager.getInstances()) {
            PropertyInstance propertyInstance = entityInstance.getPropertyInstanceByName(propertyDefinition.getName());
            if (propertyDefinition.getType() == PropertyType.FLOAT) {
                averages += (float) propertyInstance.getValue();
            } else {
                if (propertyInstance.getTicksUntilUpdate().size() != 0) {
                    averages += propertyInstance.getConsistency();
                } else {
                    averages += ticks;
                }
            }
        }

        PropertyInfoDTO propertyInfoDTO = new PropertyInfoDTO();
        propertyInfoDTO.setName(propertyDefinition.getName());
        propertyInfoDTO.setType(propertyDefinition.getType().toString());
        propertyInfoDTO.setConsistency(averages / entityInstanceManager.getInstanceCount());

        return propertyInfoDTO;
    }

    private boolean runSimulation(Map<String, PopulationChartDTO> populationMap) {
        ActiveEnvironment activeEnvironment = world.getActiveEnvironment();
        Termination termination = world.getTermination();
        List<Rule> rules = world.getRules();
        Grid grid = world.getGrid();

        instantiateEntities(grid);

        ticks = 0;
        int counter = 0;
        Context context = new ContextImpl(activeEnvironment, grid, entityInstanceManagers);
        termination.setStartDate(new Date());
        termination.setLastChecked(new Date());
        updateChart(populationMap);

        while (!termination.isSecondsToEndFinished() && !termination.isTickToEndFinished(ticks) && !exit) {
            moveInstances(grid);
            List<Rule> activatedRules = new ArrayList<>();
            for (Rule rule : rules) {
                if (rule.getActivation().isActive()) {
                    activatedRules.add(rule);
                }
            }
            context.setCurrentTick(ticks);
            for (EntityInstanceManager entityInstanceManager : entityInstanceManagers.values()) {
                for (EntityInstance entityInstance : entityInstanceManager.getInstances()) {
                    if (entityInstance.isAlive()) {
                        for (Rule activatedRule : activatedRules) {
                            try {
                                performActions(activatedRule, entityInstance, context);
                            } catch (Exception e) {
                                world.setExceptionMessage(e.getMessage());
                                world.setSimulationState(SimulationState.FINISHED);
                                System.out.println("here 2");
                            }
                        }
                    }
                }
            }
            for (EntityInstanceManager entityInstanceManager : entityInstanceManagers.values()) {
                entityInstanceManager.updateInstances();
            }
            counter++;
            ticks++;
            if (counter == POPULATION_UPDATE_THRESHOLD) {
                updateChart(populationMap);
                POPULATION_UPDATE_THRESHOLD *= POPULATION_UPDATE_THRESHOLD;
                counter = 0;
            }

            seconds = termination.getCurrentSeconds();
            checkIfPaused(termination);
        }

        seconds = termination.getCurrentSeconds();
        return true;
    }

    private void performActions(Rule activatedRule, EntityInstance primaryInstance, Context context) {
        for (Action action : activatedRule.getActionsToPerform()) {
            if (action.getPrimaryEntityContext().getName().equals(primaryInstance.getEntityDefinition().getName())) {
                context.setPrimaryEntityInstance(primaryInstance);
                context.setPrimaryEntityInstanceManager(entityInstanceManagers.get(primaryInstance.getEntityDefinition().getName()));
                context.setSecondaryEntityDefinition(action.getSecondaryEntityContext());
                if (action.getSecondaryEntityContext() != null) {
                    actionWithSecondaryEntity(action, context);
                } else {
                    action.invoke(context);
                }
            }
        }
    }

    private void actionWithSecondaryEntity(Action action, Context context) {
        EntityInstanceManager instanceManager = entityInstanceManagers.get(action.getSecondaryEntityContext().getName());
        Collection<EntityInstance> Instances;
        boolean didInvoke = false;

        context.setSecondaryEntityInstanceManager(instanceManager);
        if (action.getConditionsToActivate() != null) {
            Instances = instanceManager.getRandomInstances(action.getSelectionCount());
        } else {
            Instances = instanceManager.getInstances();
        }

        for (EntityInstance secondaryInstance : Instances) {
            context.setSecondaryEntityInstance(secondaryInstance);
            if (action.isConditionMet(context)) {
                action.invoke(context);
                didInvoke = true;
            }
        }

        context.setSecondaryEntityInstance(null);
        context.setSecondaryEntityInstanceManager(null);
        if (!didInvoke) {
            action.invoke(context);
        }
    }

    public void moveInstances(Grid grid) {
        for (EntityInstanceManager entityInstanceManager : entityInstanceManagers.values()) {
            for (EntityInstance entityInstance : entityInstanceManager.getInstances()) {
                if (entityInstance.isAlive()) {
                    Coordinates coordinates = entityInstance.getCoordinates();
                    while (!coordinates.didCheckAllDirections()) {
                        Direction direction = coordinates.drawDirection();
                        if (direction != null && grid.move(coordinates, direction)) {
                            break;
                        }
                    }
                    coordinates.resetCheckedDirections();
                }
            }
        }
    }

    public void instantiateEntities(Grid grid) {
        for (EntityInstanceManager entityInstanceManager : entityInstanceManagers.values()) {
            for (int i = 0; i < entityInstanceManager.getEntityDefinition().getPopulation(); i++) {
                EntityInstance entityInstance = entityInstanceManager.create(false, null, 0f);
                grid.placeEntityInstance(entityInstance);
            }
        }
    }

    public Integer getTicks() {
        return ticks;
    }

    public Integer getSeconds() {
        return seconds;
    }

    public List<EntityInfoDTO> getEntitiesStatus() {
        List<EntityInfoDTO> entityInfoList = new ArrayList<>();

        for (EntityInstanceManager entityInstanceManager : entityInstanceManagers.values()) {
            EntityInfoDTO entityInfoDTO = new EntityInfoDTO();
            entityInfoDTO.setName(entityInstanceManager.getEntityDefinition().getName());
            entityInfoDTO.setPopulation(entityInstanceManager.getInstanceCount());
            entityInfoList.add(entityInfoDTO);
        }

        return entityInfoList;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    private void updateChart(Map<String, PopulationChartDTO> populationChart) {
        for (EntityInstanceManager entityInstanceManager : entityInstanceManagers.values()) {
            String name = entityInstanceManager.getEntityDefinition().getName();
            populationChart.get(name).updatePopulation(ticks, entityInstanceManager.getInstanceCount());
        }
    }

    private void updateEndResults(Map<String, PopulationChartDTO> populationMap, List<EntityInfoDTO> consistency) {
        List<PropertyHistogramDTO> propertyHistogramDTOList = new ArrayList<>();

        for (EntityInstanceManager entityInstanceManager : entityInstanceManagers.values()) {
            String entityName = entityInstanceManager.getEntityDefinition().getName();
            Map<String, Map<Object, Integer>> propertyValueCounts = createPropertyValueCounts(entityName);
            PropertyHistogramDTO propertyHistogramDTO = new PropertyHistogramDTO();
            propertyHistogramDTO.setEntityName(entityName);
            propertyHistogramDTO.setPropertyHistogram(propertyValueCounts);
            propertyHistogramDTOList.add(propertyHistogramDTO);
        }

        world.setSimulationState(SimulationState.FINISHED);
        System.out.println("here 3");
        world.setConsistency(consistency);
        world.setPopulationChart(populationMap);
        world.setPropertyValueCounts(propertyHistogramDTOList);
    }

    private Map<String, Map<Object, Integer>> createPropertyValueCounts(String entityName) {

        return entityInstanceManagers.get(entityName).getInstances().stream()
                .filter(EntityInstance::isAlive)
                .flatMap(entity -> entity.getPropertiesInstances().stream())
                .collect(Collectors.groupingBy(
                        propertyInstance -> propertyInstance.getPropertyDefinition().getName(),
                        Collectors.groupingBy(
                                PropertyInstance::getValue,
                                Collectors.summingInt(e -> 1)
                        )
                ));

    }

    public void pause() {
        pause = true;
    }

    private synchronized void checkIfPaused(Termination termination) {
        while (pause) {
            try {
                world.setSimulationState(SimulationState.PAUSED);
                System.out.println("here 4");
                this.wait();
                termination.setLastChecked(new Date());
            } catch (Exception ignored) {
            }
        }

        world.setSimulationState(SimulationState.RUNNING);
        System.out.println("here 5");
    }

    public synchronized void resume() {
        pause = false;
        this.notifyAll();
    }
}