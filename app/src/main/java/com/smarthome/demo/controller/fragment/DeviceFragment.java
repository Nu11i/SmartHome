package com.smarthome.demo.controller.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.smarthome.demo.R;

import com.smarthome.demo.controller.activity.MainActivity;
import com.smarthome.demo.util.MusicUtil;


public class DeviceFragment extends Fragment {
    private ImageButton bulb;
    private ImageButton temperature;
    private ImageButton fan;
    private ImageButton smog;
    private ImageButton curtain;
    private MusicUtil musicUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        musicUtil = new MusicUtil(getActivity());
        musicUtil.initSound();
        bulb = view.findViewById(R.id.bulb);
        temperature = view.findViewById(R.id.temperature);
        smog = view.findViewById(R.id.smog);
        fan = view.findViewById(R.id.fan);
        curtain = view.findViewById(R.id.curtain);
        //灯光控制
        bulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                ((MainActivity) getActivity()).openBulbActivity();
            }
        });

        //温度管理
        temperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                ((MainActivity) getActivity()).openTemperatureActivity();
            }
        });

        //风扇控制
        fan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                ((MainActivity) getActivity()).openFanActivity();
            }
        });

        //烟雾检测
        smog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                ((MainActivity) getActivity()).openSmogActivity();
            }
        });
        //窗帘
        curtain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                ((MainActivity) getActivity()).openCurtainActivity();
            }
        });
    }

}
