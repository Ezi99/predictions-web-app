package dto.definition;

import java.util.*;

public class WorldInfoDTO {
    private String simulationName;
    private List<EntityInfoDTO> entityInfosDTO;
    private Map<String, RuleInfoDTO> ruleInfoDTOMap;
    private List<PropertyInfoDTO> envVariables;
    private Integer secondsToEnd;
    private Integer ticksToEnd;
    private Integer rows;
    private Integer columns;

    public WorldInfoDTO() {
    }

    public String getSimulationName() {
        return simulationName;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    public WorldInfoDTO(List<EntityInfoDTO> entityInfosDTO, Map<String, RuleInfoDTO> ruleInfoDTOS) {
        this.entityInfosDTO = entityInfosDTO;
        ruleInfoDTOMap = ruleInfoDTOS;
        secondsToEnd = null;
        ticksToEnd = null;

    }

    public List<PropertyInfoDTO> getEnvVariables() {
        return envVariables;
    }

    public void setEnvVariables(List<PropertyInfoDTO> envVariables) {
        this.envVariables = envVariables;
    }

    public void setEntityInfosDTO(List<EntityInfoDTO> entityInfosDTO) {
        this.entityInfosDTO = entityInfosDTO;
    }

    public List<EntityInfoDTO> getEntitiesInfoDTO() {
        return entityInfosDTO;
    }

    public Collection<RuleInfoDTO> getRulesInfoDTOS() {
        return ruleInfoDTOMap.values();
    }

    public Integer getSecondsToEnd() {
        return secondsToEnd;
    }

    public void setSecondsToEnd(Integer secondsToEnd) {
        this.secondsToEnd = secondsToEnd;
    }

    public Integer getTicksToEnd() {
        return ticksToEnd;
    }

    public void setTicksToEnd(Integer ticksToEnd) {
        this.ticksToEnd = ticksToEnd;
    }

    public void setGridSize(Integer rows, Integer columns){
        this.rows = rows;
        this.columns = columns;
    }

    public Integer getRows() {
        return rows;
    }

    public Integer getColumns() {
        return columns;
    }
}
