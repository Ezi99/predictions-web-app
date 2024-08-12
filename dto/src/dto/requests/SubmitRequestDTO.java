package dto.requests;

public class SubmitRequestDTO {
    private String userName;
    private String selectedSimulation;
    private int numberOfExecutions;
    private Integer numberOfTicks;
    private Integer numberOfSeconds;
    private boolean isByUser;

    public SubmitRequestDTO() {
        isByUser = false;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSelectedSimulation() {
        return selectedSimulation;
    }

    public void setSelectedSimulation(String selectedSimulation) {
        this.selectedSimulation = selectedSimulation;
    }

    public int getNumberOfExecutions() {
        return numberOfExecutions;
    }

    public void setNumberOfExecutions(int numberOfExecutions) {
        this.numberOfExecutions = numberOfExecutions;
    }

    public Integer getNumberOfTicks() {
        return numberOfTicks;
    }

    public void setNumberOfTicks(Integer numberOfTicks) {
        this.numberOfTicks = numberOfTicks;
    }

    public Integer getNumberOfSeconds() {
        return numberOfSeconds;
    }

    public void setNumberOfSeconds(Integer numberOfSeconds) {
        this.numberOfSeconds = numberOfSeconds;
    }

    public boolean isByUser() {
        return isByUser;
    }

    public void setByUser(boolean byUser) {
        isByUser = byUser;
    }
}
