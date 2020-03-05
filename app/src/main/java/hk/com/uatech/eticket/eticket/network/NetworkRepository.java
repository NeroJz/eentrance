package hk.com.uatech.eticket.eticket.network;

import hk.com.uatech.eticket.eticket.preferences.PreferencesController;
import retrofit2.Call;
import retrofit2.Retrofit;
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

}
