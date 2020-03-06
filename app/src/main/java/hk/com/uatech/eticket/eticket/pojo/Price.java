package hk.com.uatech.eticket.eticket.pojo;

public class Price {

    private String ticket_amount;
    private String lounge_price;
    private String discount;

    public String getTicket_amount() {
        return ticket_amount;
    }

    public void setTicket_amount(String ticket_amount) {
        this.ticket_amount = ticket_amount;
    }

    public String getLounge_price() {
        return lounge_price;
    }

    public void setLounge_price(String lounge_price) {
        this.lounge_price = lounge_price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}
