package engine.world.allocation;

import dto.requests.AllocationRequestsDTO;
import dto.requests.RequestDTO;
import dto.requests.SubmitRequestDTO;
import dto.requests.UpdateRequestStatusDTO;
import engine.execution.SimulationState;
import engine.execution.Termination;

import java.util.*;

public class AllocationManager {
    private final Map<String, Map<Integer, SimulationRequest>> usersRequestsMap;

    public AllocationManager() {
        usersRequestsMap = new LinkedHashMap<>();
    }

    public void addRequest(SubmitRequestDTO submitRequestDTO, int requestID) {
        String userName = submitRequestDTO.getUserName();

        if (!usersRequestsMap.containsKey(userName)) {
            usersRequestsMap.put(userName, new LinkedHashMap<>());
        }

        Map<Integer, SimulationRequest> simulationRequestMap = usersRequestsMap.get(userName);
        SimulationRequest simulationRequest = new SimulationRequest(requestID,
                userName,
                submitRequestDTO.getSelectedSimulation());

        simulationRequest.setRequestedExecutions(submitRequestDTO.getNumberOfExecutions());
        simulationRequest.setTerminationDefinition(submitRequestDTO);
        simulationRequestMap.put(requestID, simulationRequest);
    }

    public AllocationRequestsDTO getAllocationsDTO() {
        AllocationRequestsDTO allocationRequestsDTO = new AllocationRequestsDTO();
        Map<String, Map<Integer, RequestDTO>> requestsMapDTO = new HashMap<>();

        for (String userName : usersRequestsMap.keySet()) {
            requestsMapDTO.put(userName, new HashMap<>());
            Map<Integer, SimulationRequest> map = usersRequestsMap.get(userName);
            for (Integer requestID : map.keySet()) {
                requestsMapDTO.get(userName).put(requestID, map.get(requestID).getRequestDTO());
            }
        }

        allocationRequestsDTO.setRequests(requestsMapDTO);
        return allocationRequestsDTO;
    }

    public void updateRequestStatus(UpdateRequestStatusDTO update) {
        Map<Integer, SimulationRequest> simulationRequestMap = usersRequestsMap.get(update.getUserName());
        if (simulationRequestMap != null) {
            simulationRequestMap.get(update.getRequestID()).setRequestStatus(update.getRequestStatus());
        }
    }

    public List<RequestDTO> getUserRequests(String userName) {
        List<RequestDTO> requestList = null;
        Map<Integer, SimulationRequest> simulationRequestMap = usersRequestsMap.get(userName);

        if (simulationRequestMap != null) {
            requestList = new ArrayList<>();
            for (SimulationRequest simulationRequest : simulationRequestMap.values()) {
                requestList.add(simulationRequest.getRequestDTO());
            }
        }

        return requestList;
    }

    public void removeUserRequests(String username) {
        usersRequestsMap.remove(username);
    }

    public void addWorldIDToRequest(String userName, int requestID, int worldID) {
        usersRequestsMap.get(userName).get(requestID).addWorldID(worldID);
    }

    public List<Integer> getUserAllocations(String userName) {
        List<Integer> res = new ArrayList<>();
        Map<Integer, SimulationRequest> userRequestMap = usersRequestsMap.get(userName);

        for (SimulationRequest simulationRequest : userRequestMap.values()) {
            res.addAll(simulationRequest.getWorldIDs());
        }

        return res;
    }

    public Termination getTermination(String userName, int requestNumber) {
        return usersRequestsMap.get(userName).get(requestNumber).getTermination();
    }

    public void updateExecutionStatus(String userName, int requestID, SimulationState simulationState) {
        usersRequestsMap.get(userName).get(requestID).updateExecutingSimulations(simulationState);
    }

    public boolean isExecutionValid(String userName, int requestID){
        return usersRequestsMap.get(userName).get(requestID).isExecutionValid();
    }
}
