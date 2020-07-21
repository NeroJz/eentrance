package hk.com.uatech.eticket.eticket.pojo;

public class GateSeatInfo {

    private String seat_no;
    private String isRefunded;
    private String isScannedIn;
    private Price price;
    private Ticket ticket;
    private Log log;
    private String is_concession;

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

    public String getIs_concession() {
        return is_concession;
    }

    public void setIs_concession(String is_concession) {
        this.is_concession = is_concession;
    }

    public boolean isConcession() {
        return is_concession.equals("1");
    }

    public void setLog(Log log) {
        this.log = log;
    }
}
