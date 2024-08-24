package engine.world;

import dto.definition.*;
import dto.execution.SimulationDTO;
import dto.execution.end.PopulationChartDTO;
import dto.execution.end.PropertyHistogramDTO;
import dto.execution.start.StartSimulationDTO;
import engine.definition.entity.EntityDefinition;
import engine.execution.SimulationState;
import engine.execution.Termination;
import engine.execution.instance.environment.api.ActiveEnvironment;
import engine.execution.space.Grid;
import engine.rule.Rule;
import engine.world.allocation.AllocationManager;
import resource.generated.PRDWorld;

import java.util.List;
import java.util.Map;

public interface World {
    void loadPRDWorld(PRDWorld prdWorld);

    WorldInfoDTO getWorldData();

    Grid getGrid();

    List<PropertyInfoDTO> getEnvVariablesInfoDTO();

    List<PropertyInfoDTO> CreateActiveEnvironment(List<PropertyInfoDTO> envVariablesInit);

    void validateEnvVariableValue(PropertyInfoDTO propertyInfoDTO, String newValue);

    void setPopulation(List<EntityInfoDTO> entityInfoDTOS);

    List<EntityInfoDTO> getEntitiesDefinitionInfo();

    List<EntityDefinition> getEntityDefinitionMap();

    List<Rule> getRules();

    Termination getTermination();

    ActiveEnvironment getActiveEnvironment();

    void setSimulationRun(SimulationRun simulationRun);

    boolean isSimulationOver();

    void setSimulationOver(boolean isOver);

    SimulationDTO getCurrentStats();

    SimulationState getSimulationState();

    void setSimulationState(SimulationState simulationState);

    void setAllocationManager(AllocationManager allocationManager);

    void setExitSimulation(boolean exit);
    Integer getID();

    StartSimulationDTO getSimulationStartInfo();

    void setExceptionMessage(String message);

    SimulationDTO getSimulationEndResults();

    void setPopulationChart(Map<String, PopulationChartDTO> populationChart);

    void setPropertyValueCounts(List<PropertyHistogramDTO> propertyValueCounts);

    void pauseSimulation();

    void resumeSimulation();

    ActiveEnvironmentDTO getActiveEnvironmentAndPopulation();

    TerminationDTO getTerminationDTO();

    void setConsistency(List<EntityInfoDTO> consistency);

    void setTermination(Termination termination);
}
