//package com.example.demobaidumap.alarmlist;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Switch;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.demobaidumap.R;
//import com.example.demobaidumap.alarmlist.AlarmData;
//
//import java.util.List;
//
//public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
//    private List<AlarmData> mDataList;
//
//    public MyAdapter(List<AlarmData> dataList) {
//        mDataList = dataList;
//    }
//
//    // 创建ViewHolder
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.content_scrolling, parent, false);
//        return new ViewHolder(itemView);
//    }
//
//    // 绑定数据到ViewHolder
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        AlarmData data = mDataList.get(position);
//        holder.timeTextView.setText(data.getTime());
//        holder.titleTextView.setText(data.getTitle());
//        holder.switchView.setChecked(data.isEnabled());
//    }
//
//    // 返回列表项数量
//    @Override
//    public int getItemCount() {
//        return mDataList.size();
//    }
//
//    // ViewHolder类
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        public TextView timeTextView;
//        public TextView titleTextView;
//        public Switch switchView;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            timeTextView = itemView.findViewById(R.id.time_text);
//            titleTextView = itemView.findViewById(R.id.description_text);
//            switchView = itemView.findViewById(R.id.switch_button);
//        }
//    }
//}
