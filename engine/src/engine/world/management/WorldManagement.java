package engine.world.management;

import dto.definition.ActiveEnvironmentDTO;
import dto.definition.SimulationListDTO;
import dto.definition.TerminationDTO;
import dto.definition.WorldInfoDTO;
import dto.execution.SimulationDTO;
import dto.execution.start.StartSimulationDTO;
import dto.requests.AllocationRequestsDTO;
import dto.requests.RequestDTO;
import dto.requests.SubmitRequestDTO;
import dto.requests.UpdateRequestStatusDTO;
import engine.execution.SimulationState;
import engine.world.World;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public interface WorldManagement {

    WorldInfoDTO loadDataFile(InputStream in) throws FileNotFoundException;

    void setThreadPool(int count);

    SimulationListDTO getSimulationList();

    WorldInfoDTO getWorldInfo(String worldName);

    ActiveEnvironmentDTO getActiveEnvironmentAndPopulation(Integer worldID);

    void resumeSimulation(Integer worldID);

    ActiveEnvironmentDTO instantiateWorld(WorldInfoDTO worldInfoDTO, World worldInstance);

    SimulationDTO startSimulation(WorldInfoDTO worldInfoDTO, String userName, int requestNumber);

    SimulationDTO getSimulationWalkthrough(Integer worldID);

    SimulationDTO getSimulationEndResults(Integer worldID);

    SimulationState getSimulationState(int worldID);

    TerminationDTO getTerminationDTO(Integer worldID);

    void closeThreadPool();

    List<StartSimulationDTO> getSimulationsState();

    void pauseSimulation(Integer worldID);

    void stopSimulation(Integer worldID);

    void addAllocationRequest(SubmitRequestDTO submitRequestDTO);

    AllocationRequestsDTO getAllocationRequests();

    void updateRequestStatus(UpdateRequestStatusDTO update);

    List<RequestDTO> getUserRequests(String userName);

    void removeUserRequests(String usernameFromSession);

    List<StartSimulationDTO> getUserSimulationsState(String username);
}
