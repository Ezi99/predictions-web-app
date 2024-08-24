package user.ui.tasks.result;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.execution.start.StartSimulationDTO;
import javafx.application.Platform;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import user.utils.Constants;
import user.utils.http.HttpClientUtil;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

public class ExecutionListManager implements Runnable {
    private Consumer<List<StartSimulationDTO>> startSimulationConsumer;
    private boolean run;

    public ExecutionListManager() {
        run = true;
    }

    @Override
    public void run() {
        while (run) {
            updateSimulationsStatus();
            try {
                Thread.sleep(500);
            } catch (Exception ignore) {
            }
        }
    }

    private void updateSimulationsStatus() {
        List<StartSimulationDTO> startSimulationList = getSimulationsState();
        Platform.runLater(() -> startSimulationConsumer.accept(startSimulationList));
    }

    private List<StartSimulationDTO> getSimulationsState() {
        String finalUrl = HttpUrl
                .parse(Constants.SIMULATION_STATE)
                .newBuilder()
                .addQueryParameter(Constants.WORLD_ID, "all")
                .build()
                .toString();
        Request request = new Request.Builder().url(finalUrl).build();
        List<StartSimulationDTO> res = null;

        try {
            Gson gson = new Gson();
            Response response = HttpClientUtil.execute(request);
            String bodyString = response.body().string();
            if(response.isSuccessful()){
                Type foosmapType = new TypeToken<List<StartSimulationDTO>>() { }.getType();
                res = gson.fromJson(bodyString, foosmapType);
            } else {
                System.out.println("something went wrong when getting terminationDTO");
            }
        } catch (Exception ignored) {
            System.out.println("something went wrong when getting terminationDTO");
        }

        return res;
    }

    public void setStartSimulationConsumer(Consumer<List<StartSimulationDTO>> startSimulationConsumer) {
        this.startSimulationConsumer = startSimulationConsumer;
    }
}
