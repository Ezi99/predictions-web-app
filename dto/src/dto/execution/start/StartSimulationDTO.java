package dto.execution.start;

import dto.definition.EntityInfoDTO;
import dto.definition.PropertyInfoDTO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StartSimulationDTO {
    private String start;
    private Integer ID;
    private int requestID;
    private String state;
    private List<EntityInfoDTO> entityInfoList;
    private List<PropertyInfoDTO> envVariables;
    private String userName;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getState() {
        return state.toLowerCase();
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<EntityInfoDTO> getEntityInfoList() {
        return entityInfoList;
    }

    public void setEntityInfoList(List<EntityInfoDTO> entityInfoList) {
        this.entityInfoList = entityInfoList;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public void setEnvVariables(List<PropertyInfoDTO> envVariables) {
        this.envVariables = envVariables;
    }

    public List<PropertyInfoDTO> getEnvVariables() {
        return envVariables;
    }
}
