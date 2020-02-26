package hk.com.uatech.eticket.eticket.pojo;

public class TicketTrans {

    private String resultCode;
    private String resultMsg;
    private TransInfo[] transInfoList;
    private SeatInfo[] seatInfoList;


    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public TransInfo[] getTransInfoList() {
        return transInfoList;
    }

    public void setTransInfoList(TransInfo[] transInfoList) {
        this.transInfoList = transInfoList;
    }

    public SeatInfo[] getSeatInfoList() {
        return seatInfoList;
    }

    public void setSeatInfoList(SeatInfo[] seatInfoList) {
        this.seatInfoList = seatInfoList;
    }

}
