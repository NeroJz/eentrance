package hk.com.uatech.eticket.eticket.pojo;

public class TransInfo {

    private String transId;
    private String movieTitle;
    private String movieCategory;
    private String houseNo;
    private String houseName;
    private String cinemaName;
    private String showDate;
    private String showTime;


    public TransInfo(String transId, String movieTitle, String movieCategory,
                     String houseNo, String houseName, String cinemaName,
                     String showDate, String showTime) {

        this.transId = transId;
        this.movieTitle = movieTitle;
        this.movieCategory = movieCategory;
        this.houseNo = houseNo;
        this.houseName = houseName;
        this.cinemaName = cinemaName;
        this.showDate = showDate;
        this.showTime = showTime;

    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMovieCategory() {
        return movieCategory;
    }

    public void setMovieCategory(String movieCategory) {
        this.movieCategory = movieCategory;
    }

    public String getHouseNo() {
        return houseNo;
    }

    public void setHouseNo(String houseNo) {
        this.houseNo = houseNo;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }

    public String getCinemaName() {
        return cinemaName;
    }

    public void setCinemaName(String cinemaName) {
        this.cinemaName = cinemaName;
    }

    public String getShowDate() {
        return showDate;
    }

    public void setShowDate(String showDate) {
        this.showDate = showDate;
    }

    public String getShowTime() {
        return showTime;
    }

    public void setShowTime(String showTime) {
        this.showTime = showTime;
    }
}
