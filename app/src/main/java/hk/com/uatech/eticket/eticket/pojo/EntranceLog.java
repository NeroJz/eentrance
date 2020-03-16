package hk.com.uatech.eticket.eticket.pojo;

public class EntranceLog {

    private int success_count;
    private int fialed_count;
    private Entrance[] success_list;
    private Entrance[] no_ticket_list;
    private Entrance[] insert_error_list;

    public int getSuccess_count() {
        return success_count;
    }

    public void setSuccess_count(int success_count) {
        this.success_count = success_count;
    }

    public int getFialed_count() {
        return fialed_count;
    }

    public void setFialed_count(int fialed_count) {
        this.fialed_count = fialed_count;
    }

    public Entrance[] getSuccess_list() {
        return success_list;
    }

    public void setSuccess_list(Entrance[] success_list) {
        this.success_list = success_list;
    }

    public Entrance[] getNo_ticket_list() {
        return no_ticket_list;
    }

    public void setNo_ticket_list(Entrance[] no_ticket_list) {
        this.no_ticket_list = no_ticket_list;
    }

    public Entrance[] getInsert_error_list() {
        return insert_error_list;
    }

    public void setInsert_error_list(Entrance[] insert_error_list) {
        this.insert_error_list = insert_error_list;
    }
}
