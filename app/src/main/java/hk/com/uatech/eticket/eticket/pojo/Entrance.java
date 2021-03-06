/**
 * Entrance POJO for Entrance Model
 *
 * Author: JZ
 * Date: 02-03-2020
 * Version: 0.0.1
 */

package hk.com.uatech.eticket.eticket.pojo;

import org.json.JSONException;
import org.json.JSONObject;

public class Entrance {

    public String trans_id;
    public int is_concession = 0;
    public String inout_datetime;
    public String type;

    private final String TRANS_ID = "trans_id";
    private final String IS_CONCESSION = "is_concession";
    private final String INOUT_DATETIME = "inout_datetime";
    private final String TYPE = "type";

    public Entrance(String trans_id, int is_concession, String inout_datetime, String type) {
        this.trans_id = trans_id;
        this.is_concession = is_concession;
        this.inout_datetime = inout_datetime;
        this.type = type;
    }

    public String getTrans_id() {
        return trans_id;
    }

    public void setTrans_id(String trans_id) {
        this.trans_id = trans_id;
    }

    public int getIs_concession() {
        return is_concession;
    }

    public void setIs_concession(int is_concession) {
        this.is_concession = is_concession;
    }

    public String getInout_datetime() {
        return inout_datetime;
    }

    public void setInout_datetime(String inout_datetime) {
        this.inout_datetime = inout_datetime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put(TRANS_ID, trans_id);
        obj.put(IS_CONCESSION, is_concession);
        obj.put(INOUT_DATETIME, inout_datetime);
        obj.put(TYPE, type);

        return obj;
    }
}
