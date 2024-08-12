package admin.ui.tasks.management;

import admin.utils.Constants;
import admin.utils.http.HttpClientUtil;
import com.google.gson.Gson;
import dto.definition.WorldInfoDTO;
import javafx.concurrent.Task;
import okhttp3.*;

import java.io.File;
import java.io.IOException;

public class DetailsTask extends Task<WorldInfoDTO> {
    private final File selectedFile;

    public DetailsTask(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    @Override
    protected WorldInfoDTO call() throws Exception {
        updateMessage("about to load World Details...");
        RequestBody body =
                new MultipartBody.Builder()
                        .addFormDataPart(Constants.FILE_NAME, selectedFile.getName(), RequestBody.create(selectedFile, MediaType.parse("text/plain")))
                        .build();

        Request request = new Request.Builder()
                .url(Constants.LOAD_FILE)
                .post(body)
                .build();

        WorldInfoDTO res;
        Gson gson = new Gson();

        try {
            Response response = HttpClientUtil.execute(request);
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                res = gson.fromJson(responseBody, WorldInfoDTO.class);
            } else {
                updateMessage("");
                throw new IllegalArgumentException(responseBody);
            }
        } catch (IOException | NullPointerException e) {
            updateMessage("");
            throw new IllegalArgumentException("something went wrong when uploading file");
        }

        updateMessage("World details loaded successfully !");
        return res;
    }

}
