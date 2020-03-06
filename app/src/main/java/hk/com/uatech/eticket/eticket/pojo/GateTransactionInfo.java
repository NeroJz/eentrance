package hk.com.uatech.eticket.eticket.pojo;

public class GateTransactionInfo {

    private GateSeatInfo[] seat;

    private Counter counter;

    public GateSeatInfo[] getSeat() {
        return seat;
    }

    public void setSeat(GateSeatInfo[] seat) {
        this.seat = seat;
    }

    public Counter getCounter() {
        return counter;
    }

    public void setCounter(Counter counter) {
        this.counter = counter;
    }
}
