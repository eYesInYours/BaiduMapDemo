package com.example.demobaidumap.fall;


import android.util.Log;


public class Fall{

    private float highThresholdValue;
    private float lowThresholdValue;
//    private int SENSOR_RATE;
    private boolean isFell;
    public static float[] svmData;
    public static float[] svmFilteringData;
    public static int svmCount = 0;
    public static final String TAG = "liuweixiang";



    public Fall(){
        svmData = new float[150];
        svmFilteringData = new float[150];
        isFell = false;
    }

    /*
    设置阈值
     */
    public void setThresholdValue(float highThreshold, float lowThreshold){
        this.highThresholdValue = highThreshold;
        this.lowThresholdValue = lowThreshold;
        Log.d(TAG, highThreshold + "   " + lowThreshold);
    }


    /*
    跌倒检测
    启动了一个新线程，在这个线程中，不断地检查滤波后的数据，使用阈值法进行跌倒检测。
    如果检测到跌倒事件，就会将 isFell 设置为 true
     */
    public  void fallDetection(){
        Log.d(TAG, "Fall.fallDetection()");
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean running = true;
                while(running){
                    //阈值法
                    for(int i = 0; i < svmFilteringData.length; i++){
                        if(svmFilteringData[i] <= lowThresholdValue){

                            // 末尾十个数据之前
                            if(i < svmFilteringData.length-10){
                                for (int j = i; j < i + 10; j++) {
                                    if (svmFilteringData[j] >= highThresholdValue) {
                                        running = false;
                                        setFell(true);
                                    }
                                }
                            }
                            // 末尾十个数据  i >= svmFilteringData.length-10
                            else {

                                for (int j = i; j < svmFilteringData.length; j++) {
                                        if (svmFilteringData[j] >= highThresholdValue) {
                                            running = false;
                                            setFell(true);
                                        }
                                }
                                // 数组前面几个数据：处理数组下表越界
                                for (int k = 0; k < (10-(svmFilteringData.length - i)); k++){
                                    if (svmFilteringData[k] >= highThresholdValue) {
                                        running = false;
                                        setFell(true);
                                    }
                                }
                            }

                        }
                    }

                }
            }
        }).start();
    }

    /*
    3s内svm原始数据收集
     */
    public static void svmCollector(float svm){

        if(svmCount < svmData.length){
            svmData[svmCount] = svm;
        }else{
            svmCount = 0;
            svmData[svmCount] = svm;
        }
        svmCount++;
//        Log.d(TAG, "Fall.svmCollector" + svmData[svmCount]);
    }

    /*
    svm中值滤波
     */
    public static void setSvmFilteringData(){
        //中值滤波取的三个值
        float s1, s2, s3, temp;
        //冒泡排序
        for (int i = 0; i < svmFilteringData.length-1; i++){
            if(i == 0){
                s1 = svmData[i];
                s2 = svmData[i + 1];
                s3 = svmData[i + 2];
            }else if(i < svmFilteringData.length-2){
                s1 = svmData[i - 1];
                s2 = svmData[i];
                s3 = svmData[i + 1];
            }else{
                s1 = svmData[i - 1];
                s2 = svmData[i];
                s3 = svmData[0];
            }
            if(s1 > s2){
                temp = s1;
                s1 = s2;
                s2 = temp;
            }
            if(s2 > s3){
                temp = s2;
                s2 = s3;
                s3 = temp;
            }
            svmFilteringData[i] = s2;
            Log.d(TAG, s1 + " " + s2 + " " + s3);
//            Log.d(TAG, "Fall.setSvmFilteringData" + svmFilteringData[i]);
        }

    }

    public boolean isFell() {
//        Log.e(TAG, "isFELL" + isFell);
        return isFell;

    }

    public void setFell(boolean fell) {
        isFell = fell;
//        Log.e(TAG, "setFELL" + isFell);
    }

    public void cleanData(){
        Log.d(TAG , "Fall.clean()");
        for (int i = 0; i < svmData.length; i++){
            svmData[i] = 0;
        }
        //中值滤波
        for (int i = 0; i < svmFilteringData.length; i++){
            svmFilteringData[i] = 0;
        }
    }
}

