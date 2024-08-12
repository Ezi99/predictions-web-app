package dto.execution;

import dto.definition.EntityInfoDTO;
import dto.execution.end.SimulationEndDTO;
import dto.execution.start.StartSimulationDTO;

import java.util.ArrayList;
import java.util.List;

public class SimulationDTO {
    private StartSimulationDTO startSimulationDTO;
    private Integer currentTick;
    private Integer currentSecond;
    private List<EntityInfoDTO> entityInfoDTOS;
    private String exceptionMessage;
    private SimulationEndDTO simulationEndDTO;


    public SimulationDTO() {
        exceptionMessage = "";
        currentSecond = 0;
        currentTick = 0;
    }

    public void setSimulationEndDTO(SimulationEndDTO simulationEndDTO) {
        this.simulationEndDTO = simulationEndDTO;
    }

    public SimulationEndDTO getSimulationEndDTO() {
        return simulationEndDTO;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public StartSimulationDTO getStartSimulationDTO() {
        return startSimulationDTO;
    }

    public void setStartSimulationDTO(StartSimulationDTO startSimulationDTO) {
        this.startSimulationDTO = startSimulationDTO;
    }

    public Integer getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(Integer currentTick) {
        this.currentTick = currentTick;
    }

    public Integer getCurrentSecond() {
        return currentSecond;
    }

    public void setCurrentSecond(Integer currentSecond) {
        this.currentSecond = currentSecond;
    }

    public List<EntityInfoDTO> getEntityInfoDTOS() {
        return entityInfoDTOS;
    }

    public void setEntityInfoDTOS(List<EntityInfoDTO> entityInfoDTOS) {
        this.entityInfoDTOS = entityInfoDTOS;
    }
}
