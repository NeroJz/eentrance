package hk.com.uatech.eticket.eticket.network;

import androidx.annotation.NonNull;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseCallback implements Callback<String> {
    private final static String FULL_ERROR = "ERROR (%1$s) : %2$s";
    private final static String SHORT_ERROR = "ERROR (%1$s)";
    private final static String NETWORK_ERROR = "ERROR (402) : Cannot connect to API";
    private ResponseType type;
    private NetworkRepository.QueryCallback callback;
    private boolean skip;


    public static String getNetworkError (){
        return NETWORK_ERROR;
    }


    public BaseCallback(ResponseType type, NetworkRepository.QueryCallback callback) {
        this.type = type;
        this.callback = callback;
    }

    public BaseCallback(ResponseType type, boolean skip, NetworkRepository.QueryCallback callback) {
        this.type = type;
        this.callback = callback;
        this.skip = skip;
    }

    @Override
    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
        if (response.isSuccessful()) {
            callback.onResponse(type, response.body());
        } else {
            handleErrorCase(response);
        }
    }

    @Override
    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
        handleErrorCase(NETWORK_ERROR);
    }

    private void handleErrorCase(Response<String> response) {
        try {
            handleErrorCase(String.format(FULL_ERROR, response.code(), response.errorBody().string()));
        } catch (IOException | NullPointerException e) {
            handleErrorCase(String.format(SHORT_ERROR, response.code()));

        }
    }

    private void handleErrorCase(String message) {
        if (skip) {
            callback.onResponse(type, "");
        } else {
            callback.onResponse(type, message);
        }
    }
}
