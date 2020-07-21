package hk.com.uatech.eticket.eticket.pojo;

public class TicketInfo {

    // SHOW DETAILS
    private String show_id;
    private String show_date;
    private String show_no;

    // HOUSE DETAILS
    private String house_id;
    private String house_ename;
    private String house_cname;

    // CINEMA DETAILS
    private String cinema_id;
    private String cinema_ename;
    private String cinema_cname;

    // MOVIE DETAILS
    private String movie_id;
    private String movie_ename;
    private String movie_cname;
    private String duration;
    private String mv_attributes;

    // TICKET GROUP DETAILS
    private String tg_code;
    private String tg_ename;
    private String tg_cname;

    private String created_date;

    private String trans_id = "";
    private String movie_ctg = "";
    private String seat = "";
    private String full_seat = "";
    private String ticket_type = "";

    public TicketInfo(String show_id, String show_date, String show_no,
                      String house_id, String house_ename, String house_cname,
                      String cinema_id, String cinema_ename, String cinema_cname,
                      String movie_id, String movie_ename, String movie_cname,
                      String duration, String mv_attributes, String tg_code,
                      String tg_ename, String tg_cname, String created_date) {

        this.show_id = show_id;
        this.show_date = show_date;
        this.show_no = show_no;
        this.house_id = house_id;
        this.house_ename = house_ename;
        this.house_cname = house_cname;
        this.cinema_id = cinema_id;
        this.cinema_ename = cinema_ename;
        this.cinema_cname = cinema_cname;
        this.movie_id = movie_id;
        this.movie_ename = movie_ename;
        this.movie_cname = movie_cname;
        this.duration = duration;
        this.mv_attributes = mv_attributes;
        this.tg_code = tg_code;
        this.tg_ename = tg_ename;
        this.tg_cname = tg_cname;
        this.created_date = created_date;

    }


    public TicketInfo(String trans_id,
                      String show_date,
                      String house_id,
                      String house_ename,
                      String cinema_id,
                      String cinema_ename,
                      String movie_ename,
                      String movie_cname,
                      String mv_attributes,
                      String tg_ename,
                      String seat,
                      String full_seat) {

        this.trans_id = trans_id;
        this.show_date = show_date;
        this.house_id = house_id;
        this.house_ename = house_ename;
        this.cinema_id = cinema_id;
        this.cinema_ename = cinema_ename;
        this.movie_ename = movie_ename;
        this.movie_cname = movie_cname;
        this.mv_attributes = mv_attributes;
        this.tg_ename = tg_ename;
        this.seat = seat;
        this.full_seat = full_seat;
    }


    public TicketInfo(String trans_id,
                      String show_date,
                      String house_id,
                      String house_ename,
                      String cinema_id,
                      String cinema_ename,
                      String movie_ename,
                      String movie_cname,
                      String mv_attributes,
                      String tg_ename,
                      String seat) {

        this.trans_id = trans_id;
        this.show_date = show_date;
        this.house_id = house_id;
        this.house_ename = house_ename;
        this.cinema_id = cinema_id;
        this.cinema_ename = cinema_ename;
        this.movie_ename = movie_ename;
        this.movie_cname = movie_cname;
        this.mv_attributes = mv_attributes;
        this.tg_ename = tg_ename;
        this.seat = seat;
    }


    public String getShow_id() {
        return show_id;
    }

    public void setShow_id(String show_id) {
        this.show_id = show_id;
    }

    public String getShow_date() {
        return show_date;
    }

    public void setShow_date(String show_date) {
        this.show_date = show_date;
    }

    public String getShow_no() {
        return show_no;
    }

    public void setShow_no(String show_no) {
        this.show_no = show_no;
    }

    public String getHouse_id() {
        return house_id;
    }

    public void setHouse_id(String house_id) {
        this.house_id = house_id;
    }

    public String getHouse_ename() {
        return house_ename;
    }

    public void setHouse_ename(String house_ename) {
        this.house_ename = house_ename;
    }

    public String getHouse_cname() {
        return house_cname;
    }

    public void setHouse_cname(String house_cname) {
        this.house_cname = house_cname;
    }

    public String getCinema_id() {
        return cinema_id;
    }

    public void setCinema_id(String cinema_id) {
        this.cinema_id = cinema_id;
    }

    public String getCinema_ename() {
        return cinema_ename;
    }

    public void setCinema_ename(String cinema_ename) {
        this.cinema_ename = cinema_ename;
    }

    public String getCinema_cname() {
        return cinema_cname;
    }

    public void setCinema_cname(String cinema_cname) {
        this.cinema_cname = cinema_cname;
    }

    public String getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(String movie_id) {
        this.movie_id = movie_id;
    }

    public String getMovie_ename() {
        return movie_ename;
    }

    public void setMovie_ename(String movie_ename) {
        this.movie_ename = movie_ename;
    }

    public String getMovie_cname() {
        return movie_cname;
    }

    public void setMovie_cname(String movie_cname) {
        this.movie_cname = movie_cname;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getMv_attributes() {
        return mv_attributes;
    }

    public void setMv_attributes(String mv_attributes) {
        this.mv_attributes = mv_attributes;
    }

    public String getTg_code() {
        return tg_code;
    }

    public void setTg_code(String tg_code) {
        this.tg_code = tg_code;
    }

    public String getTg_ename() {
        return tg_ename;
    }

    public void setTg_ename(String tg_ename) {
        this.tg_ename = tg_ename;
    }

    public String getTg_cname() {
        return tg_cname;
    }

    public void setTg_cname(String tg_cname) {
        this.tg_cname = tg_cname;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getTrans_id() {
        return trans_id;
    }

    public void setTrans_id(String trans_id) {
        this.trans_id = trans_id;
    }

    public String getMovie_ctg() {
        return movie_ctg;
    }

    public void setMovie_ctg(String movie_ctg) {
        this.movie_ctg = movie_ctg;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public String getTicket_type() {
        return ticket_type;
    }

    public void setTicket_type(String ticket_type) {
        this.ticket_type = ticket_type;
    }
}
