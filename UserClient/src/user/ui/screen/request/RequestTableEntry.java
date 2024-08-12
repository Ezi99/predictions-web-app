package user.ui.screen.request;
import dto.requests.RequestStatus;

public class RequestTableEntry {
    private int requestID;
    private String simulationName;
    private int requestedExecutions;
    private RequestStatus status;
    private int runningExecutions;
    private int doneExecutions;

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public String getSimulationName() {
        return simulationName;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    public int getRequestedExecutions() {
        return requestedExecutions;
    }

    public void setRequestedExecutions(int requestedExecutions) {
        this.requestedExecutions = requestedExecutions;
    }


    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public int getRunningExecutions() {
        return runningExecutions;
    }

    public void setRunningExecutions(int runningExecutions) {
        this.runningExecutions = runningExecutions;
    }

    public int getDoneExecutions() {
        return doneExecutions;
    }

    public void setDoneExecutions(int doneExecutions) {
        this.doneExecutions = doneExecutions;
    }
}
