package admin.ui.tasks.management;

import admin.utils.Constants;
import admin.utils.http.HttpClientUtil;
import com.google.gson.Gson;
import dto.definition.SimulationListDTO;
import javafx.application.Platform;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.util.TimerTask;
import java.util.function.Consumer;

public class SimulationListRefresher extends TimerTask {
    private final Consumer<SimulationListDTO> usersListConsumer;
    private final Consumer<String> message;

    public SimulationListRefresher(Consumer<SimulationListDTO> usersListConsumer, Consumer<String> message) {
        this.usersListConsumer = usersListConsumer;
        this.message = message;
    }

    @Override
    public void run() {
        String finalUrl = HttpUrl
                .parse(Constants.SIMULATION_LIST)
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> message.accept("something went wrong while updating simulation list"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBodyString = response.body().string();

                if(response.isSuccessful()) {
                    Gson gson = new Gson();
                    SimulationListDTO list = gson.fromJson(responseBodyString, SimulationListDTO.class);
                    Platform.runLater(() -> usersListConsumer.accept(list));
                } else {
                    Platform.runLater(() ->  message.accept(responseBodyString));
                }
            }
        });
    }
}
