package hk.com.uatech.eticket.eticket;

/**
 * Created by alex_ on 22/08/2017.
 */


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.List;

public class ListAdapter extends BaseAdapter
{
    private Activity activity;
    private List<String> mList;
    private List<Boolean> mShow;

    private static LayoutInflater inflater = null;

    public ListAdapter(Activity a, List<String> list, List<Boolean> listShow)
    {
        activity = a;
        mList = list;
        mShow = listShow;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount()
    {
        return mList.size();
    }

    public Object getItem(int position)
    {
        return position;
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        View vi = convertView;
        if(convertView==null)
        {
            vi = inflater.inflate(R.layout.house_item, null);
        }

        CheckedTextView chkBshow = (CheckedTextView) vi.findViewById(R.id.check1);
//chkBshow.setChecked(false);

        chkBshow.setChecked(mShow.get(position));

        chkBshow.setText(mList.get(position).toString());

        return vi;
    }
}
