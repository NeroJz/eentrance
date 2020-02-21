package hk.com.uatech.eticket.eticket.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Domain {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("domain")
    @Expose
    private String domain;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
