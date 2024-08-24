package engine.world;

import dto.definition.*;
import dto.execution.SimulationDTO;
import dto.execution.end.PopulationChartDTO;
import dto.execution.end.PropertyHistogramDTO;
import dto.execution.end.SimulationEndDTO;
import dto.execution.start.StartSimulationDTO;
import engine.action.api.Action;
import engine.definition.entity.EntityDefinition;
import engine.definition.environment.api.EnvVariablesManager;
import engine.definition.property.api.PropertyDefinition;
import engine.definition.property.api.PropertyType;
import engine.definition.property.impl.FloatPropertyDefinition;
import engine.execution.SimulationState;
import engine.execution.Termination;
import engine.execution.instance.environment.api.ActiveEnvironment;
import engine.execution.instance.property.PropertyInstance;
import engine.execution.instance.property.PropertyInstanceImpl;
import engine.execution.space.Grid;
import engine.expression.Expression;
import engine.expression.ExpressionImpl;
import engine.rule.Rule;
import engine.world.allocation.AllocationManager;
import engine.world.loader.PRDWorldLoader;
import resource.generated.PRDWorld;

import java.text.SimpleDateFormat;
import java.util.*;

public class WorldImpl implements World {
    private final String userName;
    private final int requestID;
    private final int worldID;
    private boolean firstRun;
    private AllocationManager allocationManager;
    private String name;
    private Map<String, EntityDefinition> entityDefinitionMap;
    private EnvVariablesManager envPropertiesDefinitions;
    private List<Rule> rules;
    private Termination termination;
    private ActiveEnvironment activeEnvironment;
    private Map<String, PopulationChartDTO> populationChart;
    private List<PropertyHistogramDTO> propertyValueCounts;
    private List<EntityInfoDTO> consistency;
    private SimulationRun simulationRun;
    private SimulationState simulationState;
    private String exceptionMessage;
    private Grid grid;


    public WorldImpl(Integer worldID, String userName, int requestID) {
        this.worldID = worldID;
        this.userName = userName;
        this.requestID = requestID;
        activeEnvironment = null;
        simulationState = SimulationState.PENDING;
        entityDefinitionMap = new HashMap<>();
        firstRun = true;
    }

    @Override
    public void setAllocationManager(AllocationManager allocationManager) {
        this.allocationManager = allocationManager;
    }

    @Override // command 1
    public void loadPRDWorld(PRDWorld prdWorld) {
        PRDWorldLoader prdWorldLoader = new PRDWorldLoader();
        envPropertiesDefinitions = prdWorldLoader.loadEnvVariables(prdWorld);
        entityDefinitionMap = prdWorldLoader.loadEntities(prdWorld);
        rules = prdWorldLoader.loadRules(prdWorld);
        name = prdWorld.getName().trim();
        grid = prdWorldLoader.loadGrid(prdWorld);
    }

    @Override
    public WorldInfoDTO getWorldData() {
        List<EntityInfoDTO> entitiesInfoDTO = new ArrayList<>();

        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            EntityInfoDTO entityInfoDTO = new EntityInfoDTO();
            entityInfoDTO.setName(entityDefinition.getName());
            entityInfoDTO.setPopulation(entityDefinition.getPopulation());
            entityInfoDTO.setPropertyInfoDTOSet(createPropertiesInfoDTO(entityDefinition.getPropertiesDefinition()));
            entitiesInfoDTO.add(entityInfoDTO);
        }

        Map<String, RuleInfoDTO> ruleInfoDTOs = createRuleInfoDTOs();
        WorldInfoDTO worldInfoDTO = new WorldInfoDTO(entitiesInfoDTO, ruleInfoDTOs);

        worldInfoDTO.setSimulationName(name);
        grid.setGridInfo(worldInfoDTO);
        worldInfoDTO.setEnvVariables(getEnvVariablesInfoDTO());
        return worldInfoDTO;
    }

    @Override
    public Grid getGrid() {
        return grid;
    }

    @Override // command 3.1
    public List<PropertyInfoDTO> getEnvVariablesInfoDTO() {
        return createPropertiesInfoDTO(envPropertiesDefinitions.getEnvVariables());
    }

    @Override // command 3.2
    public List<PropertyInfoDTO> CreateActiveEnvironment(List<PropertyInfoDTO> envVariablesInit) {
        activeEnvironment = envPropertiesDefinitions.createActiveEnvironment();

        for (PropertyInfoDTO propertyInfoDTO : envVariablesInit) {
            PropertyDefinition propertyDefinition = envPropertiesDefinitions.getEnvVariable(propertyInfoDTO.getName());
            if (propertyInfoDTO.getValue() == null) {
                Object generatedValue = propertyDefinition.generateValue();
                propertyInfoDTO.setValue(generatedValue.toString());
                activeEnvironment.addPropertyInstance(new PropertyInstanceImpl(propertyDefinition, generatedValue));
            } else {
                validateEnvVariableValue(propertyInfoDTO, propertyInfoDTO.getValue());
            }
        }

        return envVariablesInit;
    }

    @Override
    public void validateEnvVariableValue(PropertyInfoDTO propertyInfoDTO, String newValue) {
        PropertyDefinition propertyDefinition = envPropertiesDefinitions.getEnvVariable(propertyInfoDTO.getName());
        Expression expression = new ExpressionImpl();
        switch (propertyInfoDTO.getType()) {
            case "FLOAT":
                Float floatValue = (Float) expression.getFreeValue(PropertyType.FLOAT, newValue);
                if (!((FloatPropertyDefinition) propertyDefinition).isInRange(floatValue)) {
                    throw new ArithmeticException(floatValue + " is not in range for " + propertyDefinition.getName());
                }
                if (activeEnvironment != null) {
                    activeEnvironment.addPropertyInstance(new PropertyInstanceImpl(propertyDefinition, floatValue));
                }
                break;
            case "STRING":
                if (activeEnvironment != null) {
                    activeEnvironment.addPropertyInstance(new PropertyInstanceImpl(propertyDefinition, newValue));
                }
                break;
            default:
                Boolean boolValue = expression.readBooleanExpression(newValue);
                if (activeEnvironment != null) {
                    activeEnvironment.addPropertyInstance(new PropertyInstanceImpl(propertyDefinition, boolValue));
                }
                break;
        }
    }

    @Override
    public void setPopulation(List<EntityInfoDTO> entityInfoDTOS) {
        int totalPopulation = 0;
        for (EntityInfoDTO entityInfoDTO : entityInfoDTOS) {
            int population = Integer.parseInt(entityInfoDTO.getPopulation());
            totalPopulation += population;
            if (totalPopulation > grid.getSize()) {
                throw new IllegalArgumentException("entities population bigger than grid size");
            }
            entityDefinitionMap.get(entityInfoDTO.getName()).setPopulation(population);
        }
    }

    @Override
    public List<EntityInfoDTO> getEntitiesDefinitionInfo() {
        List<EntityInfoDTO> entityInfoDTOS = new ArrayList<>();

        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            EntityInfoDTO entityInfoDTO = new EntityInfoDTO();
            entityInfoDTO.setPopulation(entityDefinition.getPopulation());
            entityInfoDTO.setName(entityDefinition.getName());
            entityInfoDTOS.add(entityInfoDTO);
        }

        return entityInfoDTOS;
    }

    @Override
    public List<EntityDefinition> getEntityDefinitionMap() {
        return new ArrayList<>(entityDefinitionMap.values());
    }

    @Override
    public List<Rule> getRules() {
        return rules;
    }

    @Override
    public Termination getTermination() {
        return termination;
    }

    @Override
    public void setTermination(Termination termination) {
        this.termination = termination;
    }

    @Override
    public ActiveEnvironment getActiveEnvironment() {
        return activeEnvironment;
    }

    @Override
    public void setSimulationRun(SimulationRun simulationRun) {
        this.simulationRun = simulationRun;
    }

    @Override
    public SimulationDTO getCurrentStats() {
        SimulationDTO simulationDTO = new SimulationDTO();
        simulationDTO.setCurrentSecond(simulationRun.getSeconds());
        simulationDTO.setCurrentTick(simulationRun.getTicks());
        simulationDTO.setEntityInfoDTOS(simulationRun.getEntitiesStatus());
        if (exceptionMessage != null) {
            simulationDTO.setExceptionMessage(exceptionMessage);
        }
        return simulationDTO;
    }

    @Override
    public SimulationState getSimulationState() {
        return simulationState;
    }

    @Override
    public void setSimulationState(SimulationState state) {
        if ((state != SimulationState.RUNNING || firstRun)) {
            allocationManager.updateExecutionStatus(userName, requestID, state);
        }

        firstRun = state == SimulationState.PAUSED;
        this.simulationState = state;
    }

    @Override
    public void setExitSimulation(boolean exit) {
        simulationRun.setExit(exit);
        if (simulationState == SimulationState.PAUSED) {
            simulationRun.resume();
        }
    }

    @Override
    public StartSimulationDTO getSimulationStartInfo() {
        StartSimulationDTO startSimulationDTO = new StartSimulationDTO();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy | HH.mm.ss");

        Date startDate = termination.getStartDate();
        startSimulationDTO.setStart(startDate != null ? dateFormat.format(startDate) : "");

        startSimulationDTO.setState(simulationState.name());
        startSimulationDTO.setID(worldID);
        startSimulationDTO.setRequestID(requestID);
        startSimulationDTO.setUserName(userName);

        return startSimulationDTO;
    }

    @Override
    public void setExceptionMessage(String message) {
        this.exceptionMessage = message;
    }

    @Override
    public SimulationDTO getSimulationEndResults() {
        SimulationDTO simulationDTO = new SimulationDTO();
        SimulationEndDTO simulationEndDTO = new SimulationEndDTO();

        if (exceptionMessage != null) {
            simulationDTO.setExceptionMessage(exceptionMessage);
        } else {
            simulationEndDTO.setConsistencies(consistency);
            simulationEndDTO.setPopulationChart(populationChart);
            simulationEndDTO.setPropertyHistogram(propertyValueCounts);
            simulationDTO.setEntityInfoDTOS(simulationRun.getEntitiesStatus());
            simulationDTO.setSimulationEndDTO(simulationEndDTO);
        }

        return simulationDTO;
    }

    @Override
    public void setPopulationChart(Map<String, PopulationChartDTO> populationChart) {
        this.populationChart = populationChart;
    }

    @Override
    public void setPropertyValueCounts(List<PropertyHistogramDTO> propertyValueCounts) {
        this.propertyValueCounts = propertyValueCounts;
    }

    @Override
    public void pauseSimulation() {
        simulationRun.pause();
    }

    @Override
    public void resumeSimulation() {
        if (simulationState == SimulationState.PAUSED) {
            simulationRun.resume();
        }
    }

    @Override
    public ActiveEnvironmentDTO getActiveEnvironmentAndPopulation() {
        ActiveEnvironmentDTO activeEnvironmentDTO = new ActiveEnvironmentDTO();
        List<EntityInfoDTO> EntityInfoDTOList = new ArrayList<>();
        List<PropertyInfoDTO> envPropertiesDTO = new ArrayList<>();
        List<PropertyInfoDTO> propertiesInfoDTO = createPropertiesInfoDTO(envPropertiesDefinitions.getEnvVariables());

        for (EntityDefinition entityDefinition : entityDefinitionMap.values()) {
            EntityInfoDTO entityInfoDTO = new EntityInfoDTO();
            entityInfoDTO.setPopulation(entityDefinition.getPopulation());
            entityInfoDTO.setName(entityDefinition.getName());
            EntityInfoDTOList.add(entityInfoDTO);
        }
        for (PropertyInfoDTO propertyInfoDTO : propertiesInfoDTO) {
            PropertyInstance propertyInstance = activeEnvironment.getProperty(propertyInfoDTO.getName());
            propertyInfoDTO.setValue(propertyInstance.getValue().toString());
            envPropertiesDTO.add(propertyInfoDTO);
        }

        activeEnvironmentDTO.setSimulationName(name);
        activeEnvironmentDTO.setEntityInfoList(EntityInfoDTOList);
        activeEnvironmentDTO.setEnvVariables(envPropertiesDTO);

        return activeEnvironmentDTO;
    }

    @Override
    public TerminationDTO getTerminationDTO() {
        Double seconds = termination.getSecondsToEnd() != null ? termination.getSecondsToEnd().doubleValue() : null;
        Double ticks = termination.getTicksToEnd() != null ? termination.getTicksToEnd().doubleValue() : null;
        return new TerminationDTO(seconds, ticks);
    }

    @Override
    public void setConsistency(List<EntityInfoDTO> consistency) {
        this.consistency = consistency;
    }

    private List<PropertyInfoDTO> createPropertiesInfoDTO(Collection<PropertyDefinition> propertyDefinitions) {
        List<PropertyInfoDTO> propertyInfoDTOSet = new ArrayList<>();

        for (PropertyDefinition propertyDefinition : propertyDefinitions) {
            PropertyInfoDTO propertyInfoDTO = new PropertyInfoDTO();
            propertyInfoDTO.setName(propertyDefinition.getName());
            propertyInfoDTO.setType(propertyDefinition.getType().toString());
            propertyInfoDTO.setRandomlyInitialized(propertyDefinition.isRandomlyInitialized());
            if (propertyDefinition instanceof FloatPropertyDefinition) {
                FloatPropertyDefinition floatPropertyDefinition = (FloatPropertyDefinition) propertyDefinition;

                if (floatPropertyDefinition.doesHaveBoundaries()) {
                    propertyInfoDTO.setBoundary(floatPropertyDefinition.getBottomBoundary(), floatPropertyDefinition.getUpperBoundary());
                }
            }

            propertyInfoDTOSet.add(propertyInfoDTO);
        }

        return propertyInfoDTOSet;
    }

    private Map<String, RuleInfoDTO> createRuleInfoDTOs() {
        Map<String, RuleInfoDTO> ruleInfoDTOs = new LinkedHashMap<>();

        for (Rule rule : rules) {
            RuleInfoDTO ruleInfoDTO = new RuleInfoDTO();
            ruleInfoDTO.setName(rule.getName());
            ruleInfoDTO.setProbabilityToActivate(rule.getActivation().getProbability());
            ruleInfoDTO.setTicksToActivate(rule.getActivation().getTicks());

            for (Action action : rule.getActionsToPerform()) {
                ruleInfoDTO.addAction(action.getActionInfo());
            }

            ruleInfoDTOs.put(ruleInfoDTO.getName(), ruleInfoDTO);
        }

        return ruleInfoDTOs;
    }
}
