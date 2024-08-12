package admin.ui.tasks.allocation;

import admin.utils.Constants;
import admin.utils.http.HttpClientUtil;
import com.google.gson.Gson;
import dto.requests.AllocationRequestsDTO;
import javafx.application.Platform;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.TimerTask;
import java.util.function.Consumer;

public class AllocationTableRefresher extends TimerTask {
    private final Consumer<String> messageConsumer;
    private final Consumer<AllocationRequestsDTO> allocationRequestsDTOConsumer;

    public AllocationTableRefresher(Consumer<String> messageConsumer,
                                    Consumer<AllocationRequestsDTO> allocationRequestsDTOConsumer) {
        this.messageConsumer = messageConsumer;
        this.allocationRequestsDTOConsumer = allocationRequestsDTOConsumer;
    }

    @Override
    public void run() {
        String finalUrl = HttpUrl
                .parse(Constants.ADMIN_ALLOCATIONS)
                .newBuilder()
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        messageConsumer.accept("something went wrong when getting requests from server"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bodyString = response.body().string();

                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    AllocationRequestsDTO res = gson.fromJson(bodyString, AllocationRequestsDTO.class);
                    Platform.runLater(() -> {
                        allocationRequestsDTOConsumer.accept(res);
                    });
                }
            }
        });
    }
}
