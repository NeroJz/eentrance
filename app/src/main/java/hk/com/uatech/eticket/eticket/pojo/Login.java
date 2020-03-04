package hk.com.uatech.eticket.eticket.pojo;

public class Login {

    private String resultCode;
    private String resultMsg;
    private int isManager;
    private int staff_id;

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

    public int getIsManager() {
        return isManager;
    }

    public boolean isManager() {
        return isManager == 1 ? true : false;
    }

    public void setIsManager(int isManager) {
        this.isManager = isManager;
    }

    public int getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(int staff_id) {
        this.staff_id = staff_id;
    }
}
