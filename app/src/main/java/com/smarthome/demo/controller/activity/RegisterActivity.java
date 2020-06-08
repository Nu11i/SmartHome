package com.smarthome.demo.controller.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.smarthome.demo.model.UserModel;
import com.smarthome.demo.util.MusicUtil;
import com.smarthome.demo.dao.SqliteDao;
import com.smarthome.demo.R;
import com.smarthome.demo.util.ActivityListUtil;

public class RegisterActivity extends AppCompatActivity {
    private Button msecurity;
    private Button mregister;
    private EditText mphone;
    private EditText mpassword;
    private EditText msecurityCode;
    private SqliteDao dbhelper;
    private String randomNumber;//随机数
    private MusicUtil musicUtil;
    private UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userModel = new UserModel();
        ActivityListUtil.getInstance().addActivity(this);
        dbhelper = new SqliteDao(this, "user", null, 1);
        musicUtil = new MusicUtil(this);
        musicUtil.initSound();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        //隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        msecurity = findViewById(R.id.security);
        mregister = findViewById(R.id.register);
        mphone = findViewById(R.id.phone);
        mpassword = findViewById(R.id.password);
        msecurityCode = findViewById(R.id.securityCode);


//获取验证码

        msecurity.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("Range")
            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                randomNumber = String.valueOf((int) ((Math.random() * 9 + 1) * 1000));
                msecurity.setText(randomNumber);
                msecurity.setBackgroundColor(Color.parseColor("#ffffff"));
                msecurity.setTextColor(Color.parseColor("#0A0A0A"));
                msecurity.setTextSize(30);
            }
        });
//注册
        mregister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                userModel.setPhone(mphone.getText().toString());
                userModel.setPassword(mpassword.getText().toString());
                musicUtil.playSound();
                if (TextUtils.isEmpty(mphone.getText()) || TextUtils.isEmpty(mpassword.getText()) || TextUtils.isEmpty(msecurityCode.getText()))
                    Toast.makeText(getApplicationContext(), "请输入完整信息！", Toast.LENGTH_SHORT).show();
                else if (userModel.getPhone().length() != 11)
                    Toast.makeText(RegisterActivity.this, "请输入11位有效的手机号码！", Toast.LENGTH_LONG).show();
                else if (!msecurityCode.getText().toString().equals(randomNumber)) {
                    Toast.makeText(RegisterActivity.this, "验证码错误，请重试！", Toast.LENGTH_LONG).show();
                } else if (dbhelper.insert(userModel)) {
                    Toast.makeText(RegisterActivity.this, "注册成功！", Toast.LENGTH_LONG).show();
                    finish();
                } else
                    Toast.makeText(RegisterActivity.this, "注册失败,请重试！", Toast.LENGTH_LONG).show();

            }

        });
    }


}
