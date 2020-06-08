package com.smarthome.demo.controller.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.smarthome.demo.model.UserModel;
import com.smarthome.demo.util.MusicUtil;
import com.smarthome.demo.dao.SqliteDao;
import com.smarthome.demo.R;
import com.smarthome.demo.util.ActivityListUtil;

public class UserActivity extends AppCompatActivity {
    private EditText mpassword, agin;
    private TextView textView;
    private Button sure;
    private SqliteDao dbhelper;
    private MusicUtil musicUtil;
    private UserModel user, anotherUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        user = new UserModel();
        anotherUser = new UserModel();
        ActivityListUtil.getInstance().addActivity(this);
        dbhelper = new SqliteDao(this, "user", null, 1);
        musicUtil = new MusicUtil(this);
        musicUtil.initSound();
        //隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        //隐藏状态栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Bundle bundle = getIntent().getExtras();
        user.setPhone(bundle.getString("phone"));
        anotherUser.setPhone(bundle.getString("phone"));
        textView = findViewById(R.id.phone);
        mpassword = findViewById(R.id.password);

        agin = findViewById(R.id.agin);
        sure = findViewById(R.id.sure);
        textView.setText("用户：" + user.getPhone());
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setPassword(mpassword.getText().toString());
                anotherUser.setPassword(agin.getText().toString());
                musicUtil.playSound();
                if (TextUtils.isEmpty(mpassword.getText())) {
                    Toast.makeText(getApplicationContext(), "请输入当前密码！", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(agin.getText())) {

                    Toast.makeText(getApplicationContext(), "请输入修改密码！", Toast.LENGTH_SHORT).show();
                } else if (!dbhelper.show(user)) {
                    Toast.makeText(getApplicationContext(), "当前密码不正确，请检查！", Toast.LENGTH_SHORT).show();

                } else if (dbhelper.modify(anotherUser)) {
                    Toast.makeText(getApplicationContext(), "修改密码成功，请重新登陆！", Toast.LENGTH_SHORT).show();
                    ActivityListUtil.getInstance().exit();  //结束所有界面
                    clearDB();//清空登录信息
                    Intent intent = new Intent(UserActivity.this, LoginActivity.class);
                    startActivity(intent);//返回登录

                } else
                    Toast.makeText(getApplicationContext(), "修改失败，请重试！", Toast.LENGTH_SHORT).show();
            }
        });


    }

    //清除密码
    private void clearDB() {
        SharedPreferences sp = getSharedPreferences("Logindb", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("passwordEdt", "");
        editor.commit();
    }

}
