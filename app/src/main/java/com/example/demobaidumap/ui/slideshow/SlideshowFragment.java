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

import com.example.demobaidumap.databinding.FragmentSlideshowBinding;
import com.example.demobaidumap.ui.track.MyTrackActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    String track_nowadays = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

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
        step.setText(nowadays_step.split("@")[1] + "步");

        calendarview.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
//                Toast.makeText(getContext(), "您选择的时间是："+ year + "年" + month + "月" + dayOfMonth + "日",Toast.LENGTH_SHORT).show();
                String selectedDate = year + "-0" + (month+1) + "-" + dayOfMonth;
                track_nowadays = selectedDate;
                String selectedDate_step = prefs.getString("stepCount"+selectedDate, "");

                if(selectedDate_step == ""){
                    Toast.makeText(getContext(), "您选择的日期暂未记录步数",Toast.LENGTH_SHORT).show();
                }else{
                    step.setText(selectedDate_step.split("@")[1] + "步");
                }

            }
        });

        RelativeLayout track = binding.trackContainer;
        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyTrackActivity.class);
                intent.putExtra("date", track_nowadays);
                startActivity(intent);
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