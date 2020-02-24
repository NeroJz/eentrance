package hk.com.uatech.eticket.eticket.pojo;

public class Show {
    private String date;

    private int no;

    private Movie movie;

    private TicketGroup ticket_group;

    private String id;

    private Cinema cinema;

    private House house;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public TicketGroup getTicket_group() {
        return ticket_group;
    }

    public void setTicket_group(TicketGroup ticket_group) {
        this.ticket_group = ticket_group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Cinema getCinema() {
        return cinema;
    }

    public void setCinema(Cinema cinema) {
        this.cinema = cinema;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }
}
