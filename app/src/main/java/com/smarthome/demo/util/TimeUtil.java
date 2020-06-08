package com.smarthome.demo.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimeUtil {
    private String time;

    public TimeUtil(String time) {
        this.time = time;
    }

    //获取网络时间，并更新view层
    public String GetTime() {

        DateFormat formatter = new SimpleDateFormat(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        String format = formatter.format(calendar.getTime());

        return "当前网络时间:\t\t\t\t\t\t" + format;

    }

}
