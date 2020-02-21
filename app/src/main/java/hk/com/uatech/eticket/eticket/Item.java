package hk.com.uatech.eticket.eticket;

import java.io.Serializable;

@SuppressWarnings("serial")

/**
 * Created by alex_ on 08/09/2017.
 */

public class Item implements Serializable{    //介面為物件序列化,可將物件變成二進位串流,就可儲存成檔案,也可以直接將物件丟進輸出入串流做傳送
    private long id;
    private String refNo;
    private String seatId;
    private String seatStatus;
    private String ticketType;

    //private Colors color;

    public Item(){
        refNo = "";
        seatId = "";
        seatStatus = "";
        ticketType = "";
        //color = Colors.LIGHTGREY;
    }
    public Item(long id,String refNo,String seatId,String ticketType, String seatStatus){
        this.id=id;
        this.refNo=refNo;
        this.seatId=seatId;
        this.ticketType = ticketType;
        this.seatStatus=seatStatus;

    }
    public void setId(long id){
        this.id=id;
    }
    public long getId(){
        return this.id;
    }
    public void setRefNo(String refNo){
        this.refNo=refNo;
    }
    public String getRefNo(){
        return this.refNo;
    }
    public void setSeatStatus(String seatStatus){
        this.seatStatus=seatStatus;
    }
    public String getSeatStatus(){
        return this.seatStatus;
    }
    public void setSeatId(String seatId){
        this.seatId=seatId;
    }
    public String getSeatId(){
        return this.seatId;
    }

    public void setTicketType(String ticketType){
        this.ticketType=ticketType;
    }
    public String getTicketType(){
        return this.ticketType;
    }

}
