package com.zaozhuang.robot;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    Button meetingBtn;
    Button talkBtn;
    Button faceDetectionBtn;
    Button robotFuc;
    TextView dmDensityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        meetingBtn = (Button) findViewById(R.id.meeting);
        talkBtn = (Button) findViewById(R.id.talk);
        faceDetectionBtn = (Button) findViewById(R.id.face_detection);
        robotFuc = (Button) findViewById(R.id.robot_func);
        dmDensityText = (TextView) findViewById(R.id.dm_density);
        meetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, HttpTest.class);
                startActivity(intent);
            }
        });
        talkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        faceDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, FaceDetectionActivity.class);
                startActivity(intent);
            }
        });
        robotFuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
                startActivity(intent);
            }
        });
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();

        // 获取屏幕分辨率（物理像素）
        int widthPx = displayMetrics.widthPixels;
        int heightPx = displayMetrics.heightPixels;

        // 获取屏幕密度（dpi）
        int densityDpi = displayMetrics.densityDpi;

        // 适配 API 30+（更精确的窗口尺寸）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager windowManager = (WindowManager) this.getSystemService(this.WINDOW_SERVICE);
            WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();
            widthPx = windowMetrics.getBounds().width();
            heightPx = windowMetrics.getBounds().height();
        }

        // 将 dpi 转换为可读的密度类型（如 hdpi、xhdpi）
        String densityType = getDensityType(densityDpi);
        // 获取系统版本信息
        int sdkVersion = Build.VERSION.SDK_INT;
        String versionName = Build.VERSION.RELEASE;
        // 输出结果
        String info = "Device Resolution: " + widthPx + "x" + heightPx + "px\n"
                + "DPI: " + densityDpi + " (" + densityType + ")\n"
                + "Density: " + displayMetrics.density + "\n"
                + "API Level: " + sdkVersion + "\nVersion: " + versionName;
        dmDensityText.setText(info);

    }

    // 根据 dpi 返回密度类型
    private static String getDensityType(int densityDpi) {
        if (densityDpi <= DisplayMetrics.DENSITY_LOW) {
            return "ldpi";
        } else if (densityDpi <= DisplayMetrics.DENSITY_MEDIUM) {
            return "mdpi";
        } else if (densityDpi <= DisplayMetrics.DENSITY_HIGH) {
            return "hdpi";
        } else if (densityDpi <= DisplayMetrics.DENSITY_XHIGH) {
            return "xhdpi";
        } else if (densityDpi <= DisplayMetrics.DENSITY_XXHIGH) {
            return "xxhdpi";
        } else if (densityDpi <= DisplayMetrics.DENSITY_XXXHIGH) {
            return "xxxhdpi";
        } else {
            return "unknown";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
