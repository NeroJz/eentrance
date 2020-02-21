package hk.com.uatech.eticket.eticket;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List list;

    public ImageAdapter(Context c, List list) {
        mContext = c;
        this.list = list;
    }

    public int getCount() {
        // return mThumbIds.length;
        Log.d("getCount", String.valueOf(list.size()));
        return list.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d("debug", "getView");
        Log.d("dbug2", String.valueOf(position));
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            //imageView.setLayoutParams(new GridView.LayoutParams(120, 120));
            imageView.setLayoutParams(new GridView.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            //imageView.setLayoutParams(new LinearLayout.LayoutParams(parent.getWidth() / 10, LayoutParams.WRAP_CONTENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(8, 8, 8, 8);


        } else {
            imageView = (ImageView) convertView;
        }


        int item = (int) list.get(position);
        int itemValue = 0;

        switch (item) {
            case 0:
                itemValue = R.mipmap.notavailable;
                break;
            case 1:
                itemValue = R.mipmap.free;
                break;
            case 2:
                itemValue = R.mipmap.available;
                break;
        }

        //imageView.setImageResource(mThumbIds[position]);
        imageView.setImageResource(itemValue);
        return imageView;
    }
}