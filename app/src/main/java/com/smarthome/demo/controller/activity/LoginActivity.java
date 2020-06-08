package com.smarthome.demo.controller.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.smarthome.demo.model.UserModel;
import com.smarthome.demo.util.MusicUtil;
import com.smarthome.demo.dao.SqliteDao;
import com.smarthome.demo.R;
import com.smarthome.demo.util.ActivityListUtil;
import com.smarthome.demo.util.ShowDialogUtil;

public class LoginActivity extends AppCompatActivity {
    private Button mlogin;
    private Button mregister;
    private EditText mphone;
    private EditText mpassword;
    private SqliteDao dbhelper;
    private CheckBox checkBox;
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
        setContentView(R.layout.activity_login);
        //隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        //隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mlogin = findViewById(R.id.login);
        mregister = findViewById(R.id.register);
        mphone = findViewById(R.id.phone);
        mpassword = findViewById(R.id.password);
        checkBox = findViewById(R.id.checkBox);

        SharedPreferences sp2 = getSharedPreferences("Logindb", MODE_PRIVATE);
        if (sp2.getBoolean("save", false) == true) {    //判断是否写入了数值
            getDB();
        }


//注册
        mregister.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                musicUtil.playSound();
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }

        });

//登录
        mlogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                userModel.setPhone(mphone.getText().toString());
                userModel.setPassword(mpassword.getText().toString());
                musicUtil.playSound();
                if (TextUtils.isEmpty(userModel.getPhone()) || TextUtils.isEmpty(userModel.getPassword()))
                    Toast.makeText(getApplicationContext(), "请输入完整信息！", Toast.LENGTH_SHORT).show();
                else if (dbhelper.show(userModel)) {

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("phone", userModel.getPhone());
                    intent.putExtras(bundle);
                    startActivity(intent);
                    if (checkBox.isChecked()) {                    //当多选按钮按下时执行报损数据
                        saveDB();
                    } else {
                        clearDB();
                    }

                    Toast.makeText(getApplicationContext(), "用户：" + userModel.getPhone() + "登录成功！", Toast.LENGTH_SHORT).show();
                    finish();
                } else
                    Toast.makeText(getApplicationContext(), "登录失败！", Toast.LENGTH_SHORT).show();
            }

        });
    }


    public void onBackPressed() {
        new ShowDialogUtil().show(this, "退出系统？", new ShowDialogUtil.OnBottomClickListener() {
            @Override
            public void positive() {
                musicUtil.playSound();
                finish();

            }

            @Override
            public void negative() {
                musicUtil.playSound();
            }
        });
    }

    //清除数据
    private void clearDB() {
        SharedPreferences sp = getSharedPreferences("Logindb", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.commit();
    }

    //保存数据
    private void saveDB() {
        SharedPreferences sp = getSharedPreferences("Logindb", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("loginEdt", mphone.getText().toString());
        editor.putString("passwordEdt", mpassword.getText().toString());
        editor.putBoolean("is", checkBox.isChecked());
        editor.putBoolean("save", true);
        editor.commit();            //写入数据
        Toast.makeText(LoginActivity.this, "sd", Toast.LENGTH_LONG).show();
    }

    //读取数据
    private void getDB() {
        SharedPreferences sp = getSharedPreferences("Logindb", MODE_PRIVATE);
        String name = sp.getString("loginEdt", "");
        String password = sp.getString("passwordEdt", "");
        boolean is = sp.getBoolean("is", false);
        mphone.setText(name);
        mpassword.setText(password);
        checkBox.setChecked(is);
    }


}
