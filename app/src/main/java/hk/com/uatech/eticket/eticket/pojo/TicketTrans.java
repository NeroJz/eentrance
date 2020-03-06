package hk.com.uatech.eticket.eticket.pojo;

public class TicketTrans {

    private String resultCode;
    private String resultMsg;
    private TransInfo[] transInfoList;
    private SeatInfo[] seatInfoList;
    private boolean isConcession = false;

    private String trans_id;


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

    public boolean isConcession() {
        return isConcession;
    }

    public void setConcession(boolean concession) {
        isConcession = concession;
    }

    public String getTrans_id() {
        return trans_id;
    }

    public void setTrans_id(String trans_id) {
        this.trans_id = trans_id;
    }
}
