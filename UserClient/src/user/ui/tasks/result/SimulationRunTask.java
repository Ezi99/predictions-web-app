package user.ui.tasks.result;

import com.google.gson.Gson;
import dto.definition.TerminationDTO;
import dto.execution.SimulationDTO;
import dto.execution.SimulationState;
import javafx.application.Platform;
import javafx.concurrent.Task;
import okhttp3.*;
import user.utils.Constants;
import user.utils.http.HttpClientUtil;
import java.util.function.Consumer;

public class SimulationRunTask extends Task<SimulationDTO> {
    private final TerminationDTO terminationDTO;
    private final Integer worldID;
    private boolean exit;
    private Consumer<Integer> totalTicks;
    private Consumer<Integer> totalSeconds;
    private Consumer<SimulationDTO> entityStatus;
    private Consumer<Double> ticksProgress;
    private Consumer<Double> secondProgress;
    private Consumer<String> message;

    public SimulationRunTask(Integer worldID) {
        this.worldID = worldID;
        terminationDTO = getTerminationDTO();
        exit = false;
    }

    @Override
    protected SimulationDTO call() throws Exception {
        SimulationDTO simulationDTO;

        while (getSimulationState() == SimulationState.PENDING) {
            Thread.sleep(200);
        }

        while (getSimulationState() != SimulationState.FINISHED && !exit) {
            updateWalkthrough(getSimulationWalkthrough());
            Thread.sleep(300);
        }

        simulationDTO = getSimulationWalkthrough();
        updateWalkthrough(simulationDTO);

        if (getSimulationState() == SimulationState.FINISHED) {
            if (!simulationDTO.getExceptionMessage().isEmpty()) {
                SimulationDTO finalSimulationDTO = simulationDTO;
                Platform.runLater(() -> message.accept(finalSimulationDTO.getExceptionMessage()));
                simulationDTO = null;
            } else {
                simulationDTO = getSimulationEndResults();
            }
        }

        return simulationDTO;
    }

    public void exit() {
        exit = true;
    }

    public void bindTimeLabels(Consumer<Integer> totalTicks, Consumer<Integer> totalSeconds) {
        this.totalTicks = totalTicks;
        this.totalSeconds = totalSeconds;
    }

    public void bindEntitiesStatus(Consumer<SimulationDTO> entityStatus) {
        this.entityStatus = entityStatus;
    }

    public Integer getWorldID() {
        return worldID;
    }

    private void updateWalkthrough(SimulationDTO simulationDTO) {
        Platform.runLater(() -> totalSeconds.accept(simulationDTO.getCurrentSecond()));
        Platform.runLater(() -> totalTicks.accept(simulationDTO.getCurrentTick()));
        Platform.runLater(() -> entityStatus.accept(simulationDTO));
        if (terminationDTO.getTicksToEnd() != null) {
            Platform.runLater(() -> ticksProgress.accept(
                    simulationDTO.getCurrentTick() / terminationDTO.getTicksToEnd()));
        }
        if (terminationDTO.getSecondToEnd() != null) {
            Platform.runLater(() -> secondProgress.accept(
                    simulationDTO.getCurrentSecond() / terminationDTO.getSecondToEnd()));
        }
    }

    public void bindProgressBar(Consumer<Double> ticksProgress, Consumer<Double> secondProgress) {
        this.ticksProgress = ticksProgress;
        this.secondProgress = secondProgress;
    }

    public void bindMessage(Consumer<String> message){
        this.message = message;
    }

    private SimulationState getSimulationState() {
        String finalUrl = HttpUrl
                .parse(Constants.SIMULATION_STATE)
                .newBuilder()
                .addQueryParameter(Constants.WORLD_ID, worldID.toString())
                .build()
                .toString();
        Request request = new Request.Builder().url(finalUrl).build();
        SimulationState res = null;

        try {
            Gson gson = new Gson();
            Response response = HttpClientUtil.execute(request);
            String state = response.body().string();
            if(response.isSuccessful()){
               res = SimulationState.convert(state);
            } else {
                System.out.println("something went wrong when getting simulation state");
            }
        } catch (Exception e) {
            System.out.println("something went wrong when getting simulation state");
        }

        return res;
    }

    private TerminationDTO getTerminationDTO() {
        String finalUrl = HttpUrl
                .parse(Constants.TERMINATION)
                .newBuilder()
                .addQueryParameter(Constants.WORLD_ID, worldID.toString())
                .build()
                .toString();
        Request request = new Request.Builder().url(finalUrl).build();
        TerminationDTO res = null;

        try {
            Gson gson = new Gson();
            Response response = HttpClientUtil.execute(request);
            String bodyString = response.body().string();
            if(response.isSuccessful()){
                res =  gson.fromJson(bodyString, TerminationDTO.class);
            } else {
                System.out.println("something went wrong when getting terminationDTO");
            }
        } catch (Exception ignored) {
            System.out.println("something went wrong when getting terminationDTO");
        }

        return res;
    }

    private SimulationDTO getSimulationWalkthrough(){
        String finalUrl = HttpUrl
                .parse(Constants.SIMULATION_RUN)
                .newBuilder()
                .addQueryParameter(Constants.WORLD_ID, worldID.toString())
                .addQueryParameter(Constants.WALKTHROUGH_TYPE, "runningResults")
                .build()
                .toString();

        return getSimulationDTO(finalUrl);
    }

    private SimulationDTO getSimulationEndResults() {
        String finalUrl = HttpUrl
                .parse(Constants.SIMULATION_RUN)
                .newBuilder()
                .addQueryParameter(Constants.WORLD_ID, worldID.toString())
                .addQueryParameter(Constants.WALKTHROUGH_TYPE, "endResults")
                .build()
                .toString();

        return getSimulationDTO(finalUrl);

    }

    private SimulationDTO getSimulationDTO(String finalUrl){
        Request request = new Request.Builder().url(finalUrl).build();
        SimulationDTO res = null;

        try {
            Gson gson = new Gson();
            Response response = HttpClientUtil.execute(request);
            String bodyString = response.body().string();
            if(response.isSuccessful()){
                res =  gson.fromJson(bodyString, SimulationDTO.class);
            } else {
                System.out.println("something went wrong when getting SimulationDTO");
            }
        } catch (Exception e) {
            System.out.println("something went wrong when getting SimulationDTO");
        }

        return res;
    }
}

