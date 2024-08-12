package dto.definition;

import java.util.List;
import java.util.Set;

public class ActiveEnvironmentDTO {
    private String simulationName;
    private  List<PropertyInfoDTO> envVariables;
    private  List<EntityInfoDTO> EntityInfoList;

    public String getSimulationName() {
        return simulationName;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    public void setEnvVariables(List<PropertyInfoDTO> envVariables) {
        this.envVariables = envVariables;
    }

    public List<PropertyInfoDTO> getEnvVariables() {
        return envVariables;
    }

    public void setEntityInfoList(List<EntityInfoDTO> EntityInfoList) {
        this.EntityInfoList = EntityInfoList;
    }

    public List<EntityInfoDTO> getEntityInfoList() {
        return EntityInfoList;
    }
}
