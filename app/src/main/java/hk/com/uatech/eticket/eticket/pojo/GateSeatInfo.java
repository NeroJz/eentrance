package hk.com.uatech.eticket.eticket.pojo;

public class GateSeatInfo {

    private String seat_no;
    private String isRefunded;
    private String isScannedIn;
    private Price price;
    private Ticket ticket;
    private Log log;

    public String getSeat_no() {
        return seat_no;
    }

    public void setSeat_no(String seat_no) {
        this.seat_no = seat_no;
    }

    public String getIsRefunded() {
        return isRefunded;
    }

    public void setIsRefunded(String isRefunded) {
        this.isRefunded = isRefunded;
    }

    public String getIsScannedIn() {
        return isScannedIn;
    }

    public void setIsScannedIn(String isScannedIn) {
        this.isScannedIn = isScannedIn;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }
}
