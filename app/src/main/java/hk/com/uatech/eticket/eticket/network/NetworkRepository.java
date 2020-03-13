package hk.com.uatech.eticket.eticket.network;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import hk.com.uatech.eticket.eticket.preferences.PreferencesController;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class NetworkRepository {
    private static NetworkRepository instance;
    private ApiService service;

    public static NetworkRepository getInstance() {
        if (instance == null) {
            synchronized (NetworkRepository.class) {
                if (instance == null) {
                    instance = new NetworkRepository();
                }
            }
        }
        return instance;
    }

    private NetworkRepository() {
        service = new Retrofit.Builder()
                .baseUrl("https://www.google.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(ApiService.class);
    }

    public void getHouseList(String parameters, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrlWithAuth(
                        getCommonQuery(ResponseType.HOUSE_LIST),
                        getToken(),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.HOUSE_LIST, callback));
    }

    public void confirmTicket(String parameters, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrlWithAuth(
                        getCommonQuery(ResponseType.CONFIRM_TICKET),
                        getToken(),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.CONFIRM_TICKET, callback));
    }

    public void confirmTicket4UAT(String parameters, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrlWithAuth(
                        getCommonQuery(ResponseType.CONFIRM_TICKET_4UAT),
                        getToken(),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.CONFIRM_TICKET_4UAT, callback));
    }

    public void auth(String parameters, final QueryCallback callback) {
//        Call<String> stringCall = service
//                .callUrl(
//                        getCommonQuery(ResponseType.AUTH),
//                        parameters);

        Call<String> stringCall = service
                .callUrl(
                        getCommonQuery(ResponseType.GATE_LOGIN),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.AUTH, callback));
    }

    public void verifyTicket(String parameters, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrlWithAuth(
                        getCommonQuery(ResponseType.VERIFY_TICKET),
                        getToken(),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.VERIFY_TICKET, callback));
    }

    public void listOrder(String parameters, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrlWithAuth(
                        getCommonQuery(ResponseType.LIST_ORDER),
                        getToken(),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.LIST_ORDER, callback));
    }

    public void orderAction(String parameters, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrlWithAuth(
                        getCommonQuery(ResponseType.ORDER_ACTION),
                        getToken(),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.ORDER_ACTION, callback));
    }

    public void orderActionFb(String parameters, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrlWithAuth(
                        getCommonQuery(ResponseType.FB_ORDER_ACTION),
                        getToken(),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.FB_ORDER_ACTION, callback));
    }

    public void orderAction4UAT(String parameters, boolean skip, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrlWithAuth(
                        getCommonQuery(ResponseType.ORDERACTION_4_UAT),
                        getToken(),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.ORDERACTION_4_UAT, callback));
    }

    public void orderActionFb4UAT(String parameters,boolean skip, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrlWithAuth(
                        getCommonQuery(ResponseType.FB_ORDERACTION_4_UAT),
                        getToken(),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.FB_ORDERACTION_4_UAT, callback));
    }

    public void updateTicketType(String parameters,final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrlWithAuth(
                        getCommonQuery(ResponseType.UPDATE_TICKET_TYPE),
                        getToken(),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.UPDATE_TICKET_TYPE, callback));
    }



    public void getDomains(QueryCallback callback) {
        Call<String> stringCall = service
                .callUrl(
                        getCommonQuery(ResponseType.GET_DOMAINS));
        stringCall.enqueue(new BaseCallback(ResponseType.GET_DOMAINS, callback));
    }


    private String getCommonQuery(ResponseType responseType) {
        return getEndPoint()
                + responseType.getQueryName();
    }

    private String getEndPoint() {
        return PreferencesController.getInstance().getServerIpAddress()
                + PreferencesController.getInstance().getEntrance();
    }

    private String getToken() {
        return "Bearer " + PreferencesController.getInstance().getAccessToken();
    }

    public interface QueryCallback {
        void onResponse(ResponseType responseType, String result);
    }

    public interface ObservableCallback {
        void onResponse(ResponseType responseType, String result);
        void handleResults(ResponseType responseType,List<String> result);
        void handleError(Throwable t);
    }


    /**
     * The following contains API for Turnstile
     * Author: Jz
     * Date: 05-03-2020
     * Version: 0.0.1
     */

    /**
     * Get All Houses
     * @param callback
     */
    public void getGateAllHouse(QueryCallback callback) {
        Call<String> stringCall = service
                .callUrl(
                        getCommonQuery(ResponseType.GATE_ALL_HOUSE)
                );
        stringCall.enqueue(new BaseCallback(ResponseType.GATE_ALL_HOUSE, callback));
    }

    /**
     * Get shows list of a day
     * @param parameters
     * @param callback
     */
    public void getGateShowList(String parameters, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrl(
                        getCommonQuery(ResponseType.GATE_SHOW_LIST),
                        parameters);

        stringCall.enqueue(new BaseCallback(ResponseType.GATE_SHOW_LIST, callback));
    }


    /**
     * Admit/Exit a seat
     * @param parameters
     * @param callback
     */
    public void getGateValidateTicket(String parameters, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrl(
                        getCommonQuery(ResponseType.GATE_VALIDATE_TICKET),
                        parameters);

        stringCall.enqueue(new BaseCallback(ResponseType.GATE_VALIDATE_TICKET, callback));
    }


    public void multipleValidateTicket(List<JSONObject> params, final ObservableCallback callback) {
        try{
            List<ObservableSource<String>> requests = new ArrayList<>();

            for(JSONObject jsonVal : params) {
                Log.d(NetworkRepository.class.toString(), jsonVal.toString());

                requests.add(service.multipleCall(
                        getCommonQuery(ResponseType.GATE_VALIDATE_TICKET),
                        jsonVal.toString()));
            }

            Observable<List<String>> output = Observable.zip(
                    requests,
                    new Function<Object[], List<String>>() {
                        @Override
                        public List<String> apply(Object[] objects) throws Exception {
                            List<String> output = new ArrayList<>();

                            for(Object obj : objects){
                                output.add(obj.toString());
                            }

                            return output;
                        }
                    }
            );

            output.subscribe(new Observer<List<String>>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(List<String> o) {
                    callback.handleResults(ResponseType.GATE_VALIDATE_TICKET, o);
                }

                @Override
                public void onError(Throwable e) {
                    callback.handleError(e);
                }

                @Override
                public void onComplete() {

                }
            });

            /*
            Observable.zip(
                    requests,
                    new Function<Object[], List<String>>() {
                        @Override
                        public List<String> apply(Object[] objects) throws Exception {
                            List<String> output = new ArrayList<>();

                            for(Object obj : objects){
                                Log.d(NetworkRepository.class.toString(), obj.toString());
                                output.add(obj.toString());
                            }

                            return output;
                        }
                    })
                    .subscribe(
                            new Consumer<Object>() {
                                @Override
                                public void accept(Object o) throws Exception {
                                    Log.d(NetworkRepository.class.toString() + " Accept ", o.toString());
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    callback.handleError(throwable);
                                }
                            }
                    );

             */
        } catch (Exception e) {
            Log.d(NetworkRepository.class.toString(), e.getMessage());
            throw e;
        }

    }

    /**
     * Import the entrance log (after Internet resumes)
     * @param parameters
     * @param callback
     */
    public void getGateImportEntranceLog(String parameters, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrl(
                        getCommonQuery(ResponseType.GATE_IMPORT_ENTRANCE_LOG),
                        parameters);

        stringCall.enqueue(new BaseCallback(ResponseType.GATE_IMPORT_ENTRANCE_LOG, callback));
    }


    /**
     * Get the transaction info before admit/exit
     * call after scanning
     * @param parameters
     * @param callback
     */
    public void getGateGetTransactionInfo(String parameters, final QueryCallback callback) {
        Call<String> stringCall = service
                .callUrl(
                        getCommonQuery(ResponseType.GATE_GET_TRANSACTION_INFO),
                        parameters);
        stringCall.enqueue(new BaseCallback(ResponseType.GATE_GET_TRANSACTION_INFO, callback));
    }

}
