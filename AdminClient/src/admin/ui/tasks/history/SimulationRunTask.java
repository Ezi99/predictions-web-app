package admin.ui.tasks.history;

import admin.utils.Constants;
import admin.utils.http.HttpClientUtil;
import com.google.gson.Gson;
import dto.execution.SimulationDTO;
import dto.execution.SimulationState;
import javafx.application.Platform;
import javafx.concurrent.Task;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.util.function.Consumer;

public class SimulationRunTask extends Task<SimulationDTO> {
    private final Integer worldID;
    private boolean exit;
    private Consumer<String> message;

    public SimulationRunTask(Integer worldID) {
        this.worldID = worldID;
        exit = false;
    }

    @Override
    protected SimulationDTO call() throws Exception {
        SimulationDTO simulationDTO;

        while (getSimulationState() != SimulationState.FINISHED && !exit) {
            Thread.sleep(500);
        }

        simulationDTO = getSimulationEndResults();

        if (getSimulationState() == SimulationState.FINISHED) {

            if (!simulationDTO.getExceptionMessage().isEmpty()) {
                SimulationDTO finalSimulationDTO = simulationDTO;
                Platform.runLater(() -> message.accept(finalSimulationDTO.getExceptionMessage()));
                simulationDTO = null;
            }
        }

        return simulationDTO;
    }

    public void exit() {
        exit = true;
    }

    public Integer getWorldID() {
        return worldID;
    }

    public void bindMessage(Consumer<String> message) {
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
            if (response.isSuccessful()) {
                res = SimulationState.convert(state);
            } else {
                System.out.println("something went wrong when getting simulation state");
            }
        } catch (Exception e) {
            System.out.println("something went wrong when getting simulation state");
        }

        return res;
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

    private SimulationDTO getSimulationDTO(String finalUrl) {
        Request request = new Request.Builder().url(finalUrl).get().build();
        SimulationDTO res = null;

        try {
            Gson gson = new Gson();
            Response response = HttpClientUtil.execute(request);
            String bodyString = response.body().string();
            if (response.isSuccessful()) {
                res = gson.fromJson(bodyString, SimulationDTO.class);
            } else {
                System.out.println("something went wrong when getting SimulationDTO");
            }
        } catch (Exception e) {
            System.out.println("something went wrong when getting SimulationDTO");
        }

        return res;
    }
}

