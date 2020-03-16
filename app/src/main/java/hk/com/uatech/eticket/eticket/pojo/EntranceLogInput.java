package hk.com.uatech.eticket.eticket.pojo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EntranceLogInput {
    private Entrance[] entrances;

    public Entrance[] getEntrances() {
        return entrances;
    }

    public void setEntrances(Entrance[] entrances) {
        this.entrances = entrances;
    }

    public void addEntrance(Entrance[] seconds) {
        List<Entrance> append = new ArrayList<Entrance>(this.entrances.length + seconds.length);
        Collections.addAll(append, this.entrances);
        Collections.addAll(append, seconds);

        this.entrances = append.toArray(new Entrance[append.size()]);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        if(entrances.length > 0) {
            for(Entrance item : entrances) {
                jsonArray.put(item.toJSON());
            }

            obj.put("entrances", jsonArray);
        }

        return obj;
    }
}
