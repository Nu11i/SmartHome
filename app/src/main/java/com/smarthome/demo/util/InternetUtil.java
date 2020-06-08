package com.smarthome.demo.util;


import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetUtil {
    private boolean key = true;//联网标志

    public boolean NetWorking(Activity activity) {

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isAvailable()) {
            key = false;
            new ShowDialogUtil().show(activity, "未连接网络，请重试！", new ShowDialogUtil.OnBottomClickListener() {
                @Override
                public void positive() {

                    activity.finish();
                    activity.startActivity(activity.getIntent());
                }

                @Override
                public void negative() {

                    activity.finish();
                }
            });
        }
        return key;
    }
}
