package hk.com.uatech.eticket.eticket;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class HashMapAdapter extends BaseAdapter {
    private Map<String, String> mData = new HashMap<String, String>();
    private String[] mKeys;

    public HashMapAdapter(Map<String, String> data) {
        mData = data;
        mKeys = mData.keySet().toArray(new String[data.size()]);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(mKeys[position]);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        String key = mKeys[pos];
        String value = getItem(pos).toString();
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
        TextView text = ((TextView) view.findViewById(android.R.id.text1));
        text.setText(value);
        text.setTag(key);
        //do your view stuff here

        return view;
    }
}
