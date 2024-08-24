package engine.world.management;

import dto.definition.*;
import dto.execution.SimulationDTO;
import dto.execution.start.StartSimulationDTO;
import dto.requests.AllocationRequestsDTO;
import dto.requests.RequestDTO;
import dto.requests.SubmitRequestDTO;
import dto.requests.UpdateRequestStatusDTO;
import engine.execution.SimulationState;
import engine.world.SimulationRun;
import engine.world.World;
import engine.world.WorldImpl;
import engine.world.allocation.AllocationManager;
import resource.generated.PRDWorld;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorldManagementImpl implements WorldManagement {
    public static final String PRD_CLASSES_LOCATION = "resource.generated";
    private final Map<Integer, World> worldMap;
    private final Map<String, PRDWorld> worldDefinitionsMap;
    private final AllocationManager allocationManager;
    private int worldID;
    private int requestID;
    private int threadCount;
    private ExecutorService threadPool;

    public WorldManagementImpl() {
        worldMap = new HashMap<>();
        worldDefinitionsMap = new HashMap<>();
        allocationManager = new AllocationManager();
        worldID = 0;
        requestID = 0;
        threadCount = 0;
    }

    @Override
    public WorldInfoDTO loadDataFile(InputStream in) throws FileNotFoundException {
        World world = new WorldImpl(-1, null, -1);
        PRDWorld tempPRDWorld = getPRDWorldFromFile(in);
        String simulationName = tempPRDWorld.getName().trim();

        if (worldDefinitionsMap.containsKey(simulationName)) {
            throw new IllegalArgumentException(
                    "engine already got definition for " + simulationName + " simulation settings");
        }

        world.loadPRDWorld(tempPRDWorld);
        worldDefinitionsMap.put(tempPRDWorld.getName().trim(), tempPRDWorld);
        return world.getWorldData();
    }

    @Override
    public void setThreadPool(int count) {

        for (World world : worldMap.values()) {
            if (world.getSimulationState() != SimulationState.FINISHED) {
                String message = "cannot set threadPool while there's simulation still in process";
                throw new IllegalArgumentException(message);
            }
        }

        closeThreadPool();
        threadCount = count;
        threadPool = Executors.newFixedThreadPool(count);
    }

    @Override
    public SimulationListDTO getSimulationList() {
        return new SimulationListDTO(new ArrayList<>(worldDefinitionsMap.keySet()));
    }

    @Override
    public WorldInfoDTO getWorldInfo(String worldName) {
        World world = new WorldImpl(-1, null, -1);
        PRDWorld prdWorld = worldDefinitionsMap.get(worldName);
        if (prdWorld != null) {
            world.loadPRDWorld(worldDefinitionsMap.get(worldName));
        } else {
            throw new IllegalArgumentException("invalid simulation name");
        }

        return world.getWorldData();
    }


    ////////////// run simulation process
    @Override
    public SimulationDTO getSimulationWalkthrough(Integer worldID) {
        return worldMap.get(worldID).getCurrentStats();
    }

    @Override
    public synchronized SimulationDTO getSimulationEndResults(Integer worldID) {
        return worldMap.get(worldID).getSimulationEndResults();
    }

    @Override
    public ActiveEnvironmentDTO instantiateWorld(WorldInfoDTO worldInfoDTO, World worldInstance) {
        worldInstance.loadPRDWorld(worldDefinitionsMap.get(worldInfoDTO.getSimulationName()));
        worldInstance.setPopulation(worldInfoDTO.getEntitiesInfoDTO());
        worldInstance.CreateActiveEnvironment(worldInfoDTO.getEnvVariables());
        return worldInstance.getActiveEnvironmentAndPopulation();
    }

    @Override
    public SimulationDTO startSimulation(WorldInfoDTO worldInfoDTO, String userName, int requestID) {
        SimulationDTO simulationDTO;

        if(!allocationManager.isExecutionRequestValid(userName, requestID)){
            simulationDTO = new SimulationDTO();
            simulationDTO.setExceptionMessage("invalid execution request");
            return simulationDTO;
        }

        World worldInstance = new WorldImpl(worldID, userName, requestID);
        worldInstance.setAllocationManager(allocationManager);
        ActiveEnvironmentDTO activeEnvDTO = instantiateWorld(worldInfoDTO, worldInstance);
        simulationDTO = createStartingSimulationDTO(worldInstance, activeEnvDTO.getEnvVariables());
        SimulationRun simulationRun = new SimulationRun(worldInstance);

        worldInstance.setSimulationRun(simulationRun);
        worldInstance.setTermination(allocationManager.getTermination(userName, requestID));
        allocationManager.addWorldIDToRequest(userName, requestID, worldID);
        worldMap.put(worldID, worldInstance);
        worldID++;

        if (threadPool == null || threadCount == 0) {
            throw new IllegalArgumentException("thread pool has not been set yet by an admin");
        } else {
            threadPool.execute(simulationRun);
        }

        return simulationDTO;
    }

    private SimulationDTO createStartingSimulationDTO(World worldInstance, Collection<PropertyInfoDTO> envVariables) {
        StartSimulationDTO startSimulationDTO = new StartSimulationDTO();
        SimulationDTO simulationDTO = new SimulationDTO();

        startSimulationDTO.setID(worldID);
        startSimulationDTO.setState(SimulationState.PENDING.name());
        startSimulationDTO.setEntityInfoList(worldInstance.getEntitiesDefinitionInfo());
        startSimulationDTO.setEnvVariables(new ArrayList<>(envVariables));
        simulationDTO.setStartSimulationDTO(startSimulationDTO);

        return simulationDTO;
    }

    @Override
    public SimulationState getSimulationState(int worldID) {
        return worldMap.get(worldID).getSimulationState();
    }

    @Override
    public List<StartSimulationDTO> getSimulationsState() {
        List<StartSimulationDTO> startSimulationList = new ArrayList<>();
        for (World world : worldMap.values()) {
            startSimulationList.add(world.getSimulationStartInfo());
        }
        return startSimulationList;
    }

    @Override
    public void pauseSimulation(Integer worldID) {
        worldMap.get(worldID).pauseSimulation();
    }

    @Override
    public void stopSimulation(Integer worldID) {
        worldMap.get(worldID).setExitSimulation(true);
    }

    @Override
    public ActiveEnvironmentDTO getActiveEnvironmentAndPopulation(Integer worldID) {
        return worldMap.get(worldID).getActiveEnvironmentAndPopulation();
    }

    @Override
    public void resumeSimulation(Integer worldID) {
        worldMap.get(worldID).resumeSimulation();
    }

    ///////////////////////////////////////

    @Override
    public void addAllocationRequest(SubmitRequestDTO submitRequestDTO) {
        allocationManager.addRequest(submitRequestDTO, requestID);
        requestID++;
    }

    @Override
    public AllocationRequestsDTO getAllocationRequests() {
        return allocationManager.getAllocationsDTO();
    }

    @Override
    public void updateRequestStatus(UpdateRequestStatusDTO update) {
        allocationManager.updateRequestStatus(update);
    }

    @Override
    public List<RequestDTO> getUserRequests(String userName) {
        return allocationManager.getUserRequests(userName);
    }

    @Override
    public void removeUserRequests(String username) {
        allocationManager.removeUserRequests(username);
    }

    @Override
    public List<StartSimulationDTO> getUserSimulationsState(String username) {
        List<StartSimulationDTO> startSimulationDTOS = new ArrayList<>();
        List<Integer> userAllocations = allocationManager.getUserAllocations(username);

        for (Integer worldID : userAllocations) {
            startSimulationDTOS.add(worldMap.get(worldID).getSimulationStartInfo());
        }

        return startSimulationDTOS;
    }

    @Override
    public TerminationDTO getTerminationDTO(Integer worldID) {
        return worldMap.get(worldID).getTerminationDTO();
    }

    @Override
    public void closeThreadPool() {
        for (World world : worldMap.values()) {
            world.setExitSimulation(true);
        }
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }

    public PRDWorld getPRDWorldFromFile(InputStream in) throws FileNotFoundException {
        PRDWorld res;

        try {
            JAXBContext jc = JAXBContext.newInstance(PRD_CLASSES_LOCATION, this.getClass().getClassLoader());
            Unmarshaller u = jc.createUnmarshaller();
            res = (PRDWorld) u.unmarshal(in);
        } catch (JAXBException e) {
            throw new FileNotFoundException(e.getMessage());
        }

        return res;
    }
}
