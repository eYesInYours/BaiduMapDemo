package com.example.demobaidumap.search;

import static com.example.demobaidumap.search.SearchActivity.*;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.example.demobaidumap.MyNavigation;
import com.example.demobaidumap.R;
import com.example.demobaidumap.SharedViewModel;
import com.example.demobaidumap.ui.gallery.GalleryFragment;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements OnGetPoiSearchResultListener {

    private EditText mSearchBox;
    private ListView mSearchResultList;
    private PoiSearch mPoiSearch;
    private ArrayAdapter<Poi> mAdapter;
    private List<Poi> MyMapPoiList = new ArrayList<>();;
    ImageView mImageView;

    private String keyword;
    private SharedViewModel sharedViewModel;
    private MutableLiveData<Double> latitude = new MutableLiveData<>();
    private MutableLiveData<Double> longitude = new MutableLiveData<>();

    private OnDataPassListener mOnDataPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mImageView = findViewById(R.id.search_button);
        mSearchBox = findViewById(R.id.edit_text_search);
        mSearchResultList = findViewById(R.id.list_view_poi);

//        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
//        Log.e("sharedViewModel",""+sharedViewModel);


        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        // 返回
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // 输入框输入
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                keyword = charSequence.toString();
                Log.i("input keyword:", keyword);
                MyMapPoiList.clear();
                if(keyword.length()==0){
                    return;
                }
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city("长沙")
                        .keyword(keyword)
                        .pageNum(0));
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.e("keyword length",""+keyword.length());
                if(keyword.length()==0){
                    MyMapPoiList.clear();
                    mAdapter.clear();
                    Log.e("clear",""+MyMapPoiList.size());
                }
            }
        });

        // 最终搜索点击
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("search","action search");
                // 首先清空上次的搜索
                MyMapPoiList.clear();
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city("长沙")
                        .keyword(keyword)
                        .pageNum(0));
            }
        });

    }



    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


    public interface OnDataPassListener {
        void onDataPass(Poi poi);
    }

    public void setListener(OnDataPassListener mOnDataPass){
        this.mOnDataPass = mOnDataPass;
    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if (poiResult != null && poiResult.getAllPoi() != null) {

            List<PoiInfo> poiList = poiResult.getAllPoi();
            for(PoiInfo poi: poiList){
                String name = poi.getName();
                String address = poi.getAddress();
                LatLng location = poi.getLocation();

                Poi poiObj = new Poi(name, address, location.latitude, location.longitude);
                MyMapPoiList.add(poiObj);
            }

            // 创建自定义 Adapter
            if(mAdapter == null){
                mAdapter = new PoiAdapter(this, MyMapPoiList, new PoiAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Poi poi = MyMapPoiList.get(position);

//                        GalleryFragment fragment = new GalleryFragment();
//                        fragment.setEnd(poi.getPoiLatitude(), poi.getPoiLongitude());

//                        Log.e("fragment ac", ""+ GalleryFragment.getInstance().getActivity() );
//                        GalleryFragment.getInstance().setEnd(poi.getPoiLatitude(), poi.getPoiLongitude());

                        Intent intent = new Intent();
                        intent.putExtra("latitude", poi.getPoiLatitude());  // 将纬度数据放入Intent中
                        intent.putExtra("longitude", poi.getPoiLongitude());  // 将经度数据放入Intent中
                        setResult(RESULT_OK, intent);  // 设置返回结果为OK
                        finish();  // 关闭B页面，返回到A页面


                        System.out.println(poi);
                        finish();
                    }
                });
            }
            // 设置 Adapter
            mSearchResultList.setAdapter(mAdapter);

            // 更新ListView
            mAdapter.addAll(MyMapPoiList);
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // 返回键
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPoiSearch.destroy();
    }

}
