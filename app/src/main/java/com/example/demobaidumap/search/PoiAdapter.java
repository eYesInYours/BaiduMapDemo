package com.example.demobaidumap.search;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.demobaidumap.R;
import com.example.demobaidumap.search.Poi;
import com.example.demobaidumap.ui.gallery.GalleryFragment;

import java.util.List;
import java.util.Locale;

public class PoiAdapter extends ArrayAdapter<Poi> {
    private Context mContext;
    private List<Poi> mPoiList;
    private OnItemClickListener mListener;

    public PoiAdapter(Context context, List<Poi> poiList, OnItemClickListener listener) {
        super(context, 0, poiList);
        mContext = context;
        mPoiList = poiList;
        mListener = listener;
        Log.d("Adapter", "Adapter init");
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    @Override
    public int getCount() {
        Log.i("getcount",""+super.getCount());
        return super.getCount();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.e("getView", "position: " + position);


        // 检查视图是否存在，如果不存在，则创建一个新视图
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_poi, parent, false);
        }

        // 获取当前位置的 Poi 对象
        Poi poi = getItem(position);

        // 显示名称
        TextView nameTextView = convertView.findViewById(R.id.tv_poi_name);
        nameTextView.setText(poi.getPoiName());

        // 显示地址
        TextView addressTextView = convertView.findViewById(R.id.tv_poi_address);
        addressTextView.setText(poi.getPoiAddress());
        Log.e("cur address",""+poi.getPoiAddress());

        // 给列表项添加点击，携带终点参数
        View listItemView = convertView.findViewById(R.id.list_item_poi_root);
        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(position);
                Log.i("onItemClick","触发适配器接口");

            }
        });

        // 返回列表项视图
        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Log.d("Adapter", "Adapter data set changed");
    }
}
