package hk.com.uatech.eticket.eticket.network;

public enum ResponseType {
    HOUSE_LIST("houseList"),
    CONFIRM_TICKET("confirmTicket"),
    CONFIRM_TICKET_4UAT("confirmTicket"),//TASK_COMPLETED_4_UAT no skip
    AUTH("staffAuth"),
    VERIFY_TICKET("verifyTicket"),
    LIST_ORDER("listOrder"),
    ORDER_ACTION("orderAction"),
    FB_ORDER_ACTION("orderAction"),
    ORDERACTION_4_UAT("orderAction"),//TASK_COMPLETED_4_UAT SKIP
    FB_ORDERACTION_4_UAT("orderAction"),//TASK_FB_ORDERACTION_4_UAT
    UPDATE_TICKET_TYPE("updateTicketType"),
    GET_DOMAINS("adDomainList"),

    // Boardway APIs
    GATE_ALL_HOUSE("GateGetAllHouse.php"),
    GATE_LOGIN("GateLogin.php")

    ;


    public String getQueryName() {
        return queryName;

    }

    public boolean isNeedToSkip() {
        return needToSkip;
    }

    private String queryName;
    private boolean needToSkip;

    ResponseType(String queryName) {
        this.queryName = queryName;
    }

    ResponseType(String queryName, boolean needToSkip) {
        this.queryName = queryName;
    }


}
