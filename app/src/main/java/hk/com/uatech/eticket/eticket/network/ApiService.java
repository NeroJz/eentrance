package hk.com.uatech.eticket.eticket.network;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface ApiService {
    @POST
    @Headers("Content-Type:application/json")
    Call<String> callUrlWithAuth(
            @Url String url,
            @Header("Authorization") String token,
            @Body String testParameters);

    @POST
    @Headers("Content-Type:application/json")
    Call<String> callUrl(
            @Url String url,
            @Body String testParameters);

    @POST
    @Headers("Content-Type:application/json")
    Call<String> callUrl(
            @Url String url);

    @POST
    @Headers("Content-Type:application/json")
    Observable<String> multipleCall(
            @Url String url,
            @Body String params);
}
