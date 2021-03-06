package hk.com.uatech.eticket.eticket.pojo;

import hk.com.uatech.eticket.eticket.ScanType;

public class SeatInfo {
    private String seatStatus = "Valid";
    private String seatId;
    private String ticketType;

    private boolean isChecked = false;
    private boolean isConcession = false;
    private ScanType action = ScanType.OUT;


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

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isConcession() {
        return isConcession;
    }

    public void setConcession(boolean concession) {
        isConcession = concession;
    }

    public ScanType getAction() {
        return action;
    }

    public void setAction(ScanType action) {
        this.action = action;
    }
}
