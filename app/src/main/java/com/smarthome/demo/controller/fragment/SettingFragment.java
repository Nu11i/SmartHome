package com.smarthome.demo.controller.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.smarthome.demo.R;
import com.smarthome.demo.controller.activity.MainActivity;
import com.smarthome.demo.util.MusicUtil;

public class SettingFragment extends Fragment {
    private ImageButton user, about;
    private CalendarView calendarView;
    final int FLAG = 0x001;
    private Message message;
    private ViewFlipper viewFlipper;
    private MusicUtil musicUtil;
    private int[] images = {R.mipmap.a2, R.mipmap.a3};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        user = view.findViewById(R.id.user);
        about = view.findViewById(R.id.about);
        viewFlipper = view.findViewById(R.id.view);
        calendarView=view.findViewById(R.id.calendarView);
        calendarView.setEnabled(false);
        musicUtil = new MusicUtil(getActivity());
        musicUtil.initSound();
        for (int i = 0; i < images.length; i++) {

            ImageView imageView = new ImageView(getActivity());
            imageView.setImageResource(images[i]);
            viewFlipper.addView(imageView);
        }
        message = Message.obtain();
        message.what = FLAG;
        handler.sendMessage(message);

        //密码修改
        user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                ((MainActivity) getActivity()).openUserActivity();
            }
        });

        //关于
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                ((MainActivity) getActivity()).openAbout();
            }
        });
    }

    @SuppressLint("HandlerLeak")
    private
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == FLAG) {
                viewFlipper.showPrevious();
                message = handler.obtainMessage(FLAG);
                handler.sendMessageDelayed(message, 9000);
            }
        }
    };
}
