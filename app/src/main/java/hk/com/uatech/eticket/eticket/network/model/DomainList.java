package hk.com.uatech.eticket.eticket.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DomainList {
    @SerializedName("resultCode")
    @Expose
    private Integer resultCode;
    @SerializedName("resultMsg")
    @Expose
    private String resultMsg;
    @SerializedName("adDomainList")
    @Expose
    private List<Domain> adDomainList = null;

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public List<Domain> getAdDomainList() {
        return adDomainList;
    }

    public void setAdDomainList(List<Domain> adDomainList) {
        this.adDomainList = adDomainList;
    }
}
