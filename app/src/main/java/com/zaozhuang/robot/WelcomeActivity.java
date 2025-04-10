package com.zaozhuang.robot;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.helang.lib.IMyAidlCallBackInterface;
import com.helang.lib.IMyAidlInterface;
import com.zaozhuang.robot.view.OffsetBackgroundSpan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class WelcomeActivity extends AppCompatActivity {
    private IMyAidlInterface iMyAidlInterface;
    private ServiceCallBack serviceCallBack;
    private MyServiceConnection myServiceConnection;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView tvTime, tvDate;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private int mTalkingState = 0;

    private static final int IDLE = 0;//闲置中，人和机器人都没说话
    private static final int MEN_TALKING = 1;//人在说话中
    private static final int ROBOT_THINKING = 2;//机器人请求大模型中
    private static final int ROBOT_ANSWERING = 3;//机器人回答中

    private TextView qaText, guideText, introText, remoteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_welcome);
        bindService();

        tvTime = findViewById(R.id.tv_time);
        tvDate = findViewById(R.id.tv_date);
        qaText = findViewById(R.id.qa_tv);
        guideText = findViewById(R.id.guide_tv);
        introText = findViewById(R.id.intro_tv);
        remoteText = findViewById(R.id.remote_tv);

        addOffSetBgForText();
        updateTime();
        startRealtimeUpdates();
    }

    private void addOffSetBgForText() {
        // 计算偏移量（示例：偏移半个字符宽度/高度）
        float textSize = qaText.getTextSize(); // 获取当前文本大小
        float offsetX = 0;  // 向右偏移
        float offsetY = (float) (textSize * 0.7);  // 向下偏移
        OffsetBackgroundSpan span = new OffsetBackgroundSpan(
                Color.parseColor("#006AFF"),
                offsetX,
                offsetY,
                textSize
        );
        //得加点空格，要不最后一行下面没有行间距，导致偏移的部分被crop了
        SpannableStringBuilder spannableText = new SpannableStringBuilder("完备的政策文件库，强大的搜索匹配能力\n语音对话，问你所想，打造交互新模式\n大模型智能问答，精准政策解析回复\n                  ");
        String target = "完备的政策文件库，强大的搜索匹配能力\n语音对话，问你所想，打造交互新模式\n大模型智能问答，精准政策解析回复";
        spannableText.setSpan(
                span,
                0,   // 起始位置
                target.length(),   // 结束位置（不包含）
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        qaText.setText(spannableText);
        span = new OffsetBackgroundSpan(
                Color.parseColor("#7B59FF"),
                offsetX,
                offsetY,
                textSize
        );
        spannableText = new SpannableStringBuilder("“我可以带你去参展公司展位”\n                  ");
        target = "我可以带你去参展公司展位\n";
        spannableText.setSpan(
                span,
                0,   // 起始位置
                target.length(),   // 结束位置（不包含）
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        span = new OffsetBackgroundSpan(
                Color.parseColor("#00B170"),
                offsetX,
                offsetY,
                textSize
        );
        guideText.setText(spannableText);
        spannableText = new SpannableStringBuilder("社保信息定制岗位推荐\n自定义岗位推荐\n                  ");
        target = "社保信息定制岗位推荐\n自定义岗位推荐\n";
        spannableText.setSpan(
                span,
                0,   // 起始位置
                target.length(),   // 结束位置（不包含）
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        span = new OffsetBackgroundSpan(
                Color.parseColor("#FF7813"),
                offsetX,
                offsetY,
                textSize
        );
        introText.setText(spannableText);
        spannableText = new SpannableStringBuilder("在线面试\n智能、高效、快捷、直聘\n                  ");
        target = "在线面试\n";
        spannableText.setSpan(
                span,
                0,   // 起始位置
                target.length(),   // 结束位置（不包含）
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        remoteText.setText(spannableText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
        handler.removeCallbacksAndMessages(null); // 防止内存泄漏
    }

    // 更新时间显示
    private void updateTime() {
        Date now = new Date();
        tvTime.setText(timeFormat.format(now));
        tvDate.setText(dateFormat.format(now));
    }


    // 启动定时更新
    private void startRealtimeUpdates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000); // 每1秒更新一次
            }
        }, 1000);
    }

    private void bindService() {
        myServiceConnection = new MyServiceConnection();
        serviceCallBack = new ServiceCallBack();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.aobo.robot.ai3",
                "com.aobo.aibot.aidl.MyService"));
        startService(intent);
        bindService(intent, myServiceConnection, BIND_AUTO_CREATE);
    }

    private void unbindService() {
        if (myServiceConnection != null) {
            try {
                iMyAidlInterface.unregisterListener(serviceCallBack);
            } catch (Exception e) {
                e.printStackTrace();
            }
            unbindService(myServiceConnection);
        }
    }

    class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(iBinder);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    //注册回调
                    if (iMyAidlInterface != null) {
                        try {
                            iMyAidlInterface.registerListener(serviceCallBack);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    /**
     * service回调client的类
     * tag
     * 为1是返回识别音量
     * 为2是返回识别的内容
     * 为3是合成消息
     * 为4是唤醒消息
     * 为front_ultrasound 是前超声波消息
     * 为back_ultrasound 为后超声波消息
     * 为ultrasound_distance 为超声波距离
     */
    class ServiceCallBack extends IMyAidlCallBackInterface.Stub {

        @Override
        public void callback(final String tag, final String message) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!tag.equals("1")) {
//                        text.append("tag = "+ tag+"  message="+message+"\n");
                        Log.d("TAG", "callback: " + "tag=" + tag + "  message=" + message);
                        if (tag.equals("4")) {
//                            sendMessageToRobot("starttts","唤醒了啊少时诵诗书所");
                            if (message.startsWith("wake up")) {
                                final Random random = new Random();
                                final String s = "ssss";
                                if (iMyAidlInterface != null) {
                                    try {
                                        Log.i("TAG", "run: 发送消息 wake up");
                                        iMyAidlInterface.sendMessage("starttts", s);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d("TAG", "run: " + "tag=" + tag + "  message=" + message + "\n");
                        //语音识别的音量反馈
                    }
                }
            });
        }
    }
}