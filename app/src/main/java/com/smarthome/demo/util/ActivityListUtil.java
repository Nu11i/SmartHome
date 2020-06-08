package com.smarthome.demo.util;


import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

public class ActivityListUtil extends Application {

    private List<Activity> mList = new LinkedList<Activity>();
    private static ActivityListUtil instance;

    private ActivityListUtil() {
    }
    //实例化一次
    public synchronized static ActivityListUtil getInstance() {
        if (null == instance) {
            instance = new ActivityListUtil();
        }
        return instance;
    }
    // 加入Activity
    public void addActivity(Activity activity) {
        mList.add(activity);
    }
    //关闭所有activity
    public void exit() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //关闭进程
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

}
