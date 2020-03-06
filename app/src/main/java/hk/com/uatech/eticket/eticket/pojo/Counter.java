package hk.com.uatech.eticket.eticket.pojo;

public class Counter {

    private int remain_ticket;
    private int total_scanned;
    private int total_ticket;

    public int getRemain_ticket() {
        return remain_ticket;
    }

    public void setRemain_ticket(int remain_ticket) {
        this.remain_ticket = remain_ticket;
    }

    public int getTotal_scanned() {
        return total_scanned;
    }

    public void setTotal_scanned(int total_scanned) {
        this.total_scanned = total_scanned;
    }

    public int getTotal_ticket() {
        return total_ticket;
    }

    public void setTotal_ticket(int total_ticket) {
        this.total_ticket = total_ticket;
    }
}
