package admin.ui.allocation.table;
import dto.requests.RequestStatus;

public class AllocationTableEntry {
    private int requestID;
    private String simulationName;
    private String userName;
    private Integer execCount;
    private Integer ticks;
    private Integer seconds;
    private Boolean byUser;
    private RequestStatus status;
    private Integer done;
    private Integer running;

    public String getSimulationName() {
        return simulationName;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getExecCount() {
        return execCount;
    }

    public void setExecCount(Integer execCount) {
        this.execCount = execCount;
    }

    public Integer getTicks() {
        return ticks;
    }

    public void setTicks(Integer ticks) {
        this.ticks = ticks;
    }

    public Integer getSeconds() {
        return seconds;
    }

    public void setSeconds(Integer seconds) {
        this.seconds = seconds;
    }

    public Boolean getByUser() {
        return byUser;
    }

    public void setByUser(Boolean byUser) {
        this.byUser = byUser;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Integer getDone() {
        return done;
    }

    public void setDone(Integer done) {
        this.done = done;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }
}
