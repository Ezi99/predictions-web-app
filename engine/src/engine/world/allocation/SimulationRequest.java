package engine.world.allocation;

import dto.requests.RequestDTO;
import dto.requests.RequestStatus;
import dto.requests.SubmitRequestDTO;
import engine.execution.SimulationState;
import engine.execution.Termination;

import java.util.ArrayList;
import java.util.List;

public class SimulationRequest {
    private final Termination terminationDefinition;
    private final int requestNumber;
    private final String userName;
    private final String simulationName;
    private final List<Integer> worldIDs;
    private int requestedExecutions;
    private RequestStatus requestStatus;
    private int runningExecutions;
    private int doneExecutions;

    public SimulationRequest(int requestNumber, String userName, String simulationName) {
        this.userName = userName;
        this.requestNumber = requestNumber;
        this.simulationName = simulationName;
        terminationDefinition = new Termination();
        worldIDs = new ArrayList<>();
        requestStatus = RequestStatus.PENDING;
        runningExecutions = 0;
        doneExecutions = 0;
    }

    public void setRequestedExecutions(int requestedExecutions) {
        this.requestedExecutions = requestedExecutions;
    }

    public void updateExecutingSimulations(SimulationState simulationState){
        if(simulationState == SimulationState.RUNNING){
            runningExecutions++;
        } else {
            runningExecutions--;
            if(simulationState == SimulationState.FINISHED){
                doneExecutions++;
            }
        }
    }

    public RequestDTO getRequestDTO() {
        RequestDTO requestDTO = new RequestDTO();

        requestDTO.setSubmitRequestDTO(getSubmitRequestDTO());
        requestDTO.setRequestID(requestNumber);
        requestDTO.setRequestStatus(requestStatus);
        requestDTO.setRunning(runningExecutions);
        requestDTO.setDone(doneExecutions);

        return requestDTO;
    }

    private SubmitRequestDTO getSubmitRequestDTO() {
        SubmitRequestDTO submitRequestDTO = new SubmitRequestDTO();

        submitRequestDTO.setUserName(userName);
        submitRequestDTO.setNumberOfExecutions(requestedExecutions);
        submitRequestDTO.setSelectedSimulation(simulationName);
        submitRequestDTO.setNumberOfTicks(terminationDefinition.getTicksToEnd());
        submitRequestDTO.setNumberOfSeconds(terminationDefinition.getSecondsToEnd());
        if (terminationDefinition.getTicksToEnd() == null && terminationDefinition.getSecondsToEnd() == null) {
            submitRequestDTO.setByUser(true);
        }

        return submitRequestDTO;
    }

    public void setTerminationDefinition(SubmitRequestDTO submitRequestDTO) {
        terminationDefinition.setSecondsToEnd(submitRequestDTO.getNumberOfSeconds());
        terminationDefinition.setTicksToEnd(submitRequestDTO.getNumberOfTicks());
    }

    public Termination getTermination() {
        return new Termination(terminationDefinition);
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public void addWorldID(int worldID) {
        worldIDs.add(worldID);
    }

    public List<Integer> getWorldIDs() {
        return worldIDs;
    }

    public boolean isExecutionValid(){
        return (runningExecutions + doneExecutions) != requestedExecutions;
    }
}
