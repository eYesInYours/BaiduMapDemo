package com.example.demobaidumap.ui.slideshow;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.baidu.mapapi.model.LatLng;
import com.example.demobaidumap.databinding.FragmentSlideshowBinding;
import com.example.demobaidumap.ui.track.MyTrackActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    String track_nowadays = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    String selectedDate_step;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("aside in","ok");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SlideshowViewModel slideshowViewModel =
                new ViewModelProvider(this).get(SlideshowViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        CalendarView calendarview = binding.calendarView;
        TextView step = binding.step;

        String nowadays = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SharedPreferences prefs = getActivity().getSharedPreferences("StepCounterPrefs", MODE_PRIVATE);
        String nowadays_step = prefs.getString("stepCount"+nowadays, "");

        if(nowadays_step != ""){
            selectedDate_step = nowadays_step;
            step.setText(nowadays_step.split("@")[1] + "步");
        }else{
            selectedDate_step = "0";
            step.setText(0 + "步");
        }

        calendarview.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
//                Toast.makeText(getContext(), "您选择的时间是："+ year + "年" + month + "月" + dayOfMonth + "日",Toast.LENGTH_SHORT).show();
                String selectedDate = year + "-0" + (month+1) + "-" + dayOfMonth;
                track_nowadays = selectedDate;
                selectedDate_step = prefs.getString("stepCount"+selectedDate, "");

                if(selectedDate_step == ""){
                    Toast.makeText(getContext(), "您选择的日期暂未记录步数",Toast.LENGTH_SHORT).show();
                    step.setText(0 + "步");
                }else{
                    step.setText(selectedDate_step.split("@")[1] + "步");
                }

            }
        });

        RelativeLayout track = binding.trackContainer;
        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("step",""+selectedDate_step);
                if(selectedDate_step == "" || selectedDate_step == null){
                    return;
                }

                // 1. 从本地从获取数据
                SharedPreferences prefs = getActivity().getSharedPreferences("Track", MODE_PRIVATE);
                String track = prefs.getString("track"+track_nowadays, "");

                // JSON数组转换成ArrayList
                // 2. 将 JSON 字符串转换成 JsonArray 对象
                JsonArray jsonArray = new JsonParser().parse(track).getAsJsonArray();
                Log.e("json list",""+jsonArray);

                // 3. 遍历 JsonArray 并将其转换回对象
                List<LatLng> newList = new ArrayList<>();
                for (JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    Double latitude = jsonObject.get("latitude").getAsDouble();
                    Double longitude = jsonObject.get("longitude").getAsDouble();
                    newList.add(new LatLng(latitude, longitude));
                }

                if(newList.size() >= 2){
                    Intent intent = new Intent(getActivity(), MyTrackActivity.class);
                    intent.putExtra("date", track_nowadays);
                    startActivity(intent);
                }else{
                    Toast.makeText(getContext(), "轨迹太短，不支持回看",Toast.LENGTH_SHORT).show();
                }




            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}