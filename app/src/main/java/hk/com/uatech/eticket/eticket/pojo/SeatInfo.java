package hk.com.uatech.eticket.eticket.pojo;

public class SeatInfo {
    private String seatStatus = "Valid";
    private String seatId;
    private String ticketType;


    public SeatInfo(String seatId, String ticketType) {
        this.seatId = seatId;
        this.ticketType = ticketType;
    }


    public String getSeatStatus() {
        return seatStatus;
    }

    public void setSeatStatus(String seatStatus) {
        this.seatStatus = seatStatus;
    }

    public String getSeatId() {
        return seatId;
    }

    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }
}
