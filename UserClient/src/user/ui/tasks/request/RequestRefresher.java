package user.ui.tasks.request;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.requests.RequestDTO;
import javafx.application.Platform;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import user.utils.Constants;
import user.utils.http.HttpClientUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class RequestRefresher extends TimerTask {

    private final Consumer<List<RequestDTO>> requestConsumer;
    private final Consumer<String> messageConsumer;
    private final String userName;

    public RequestRefresher(Consumer<List<RequestDTO>> requestConsumer, String userName, Consumer<String> messageConsumer) {
        this.requestConsumer = requestConsumer;
        this.messageConsumer = messageConsumer;
        this.userName = userName;

    }

    @Override
    public void run() {
        String finalUrl = HttpUrl
                .parse(Constants.REQUEST)
                .newBuilder()
                .addQueryParameter(Constants.USERNAME, userName)
                .build()
                .toString();


        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        messageConsumer.accept("something went wrong when updating request table"));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBodyString = response.body().string();

                if (response.isSuccessful()) {
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<RequestDTO>>() {}.getType();
                    List<RequestDTO> requests = gson.fromJson(responseBodyString, type);
                    Platform.runLater(() ->
                            requestConsumer.accept(requests));
                } else {
                    Platform.runLater(() ->
                            messageConsumer.accept(responseBodyString));
                }
            }
        });
    }
}
